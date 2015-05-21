/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataprovider.backend.sql;

import java.sql.Connection;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.DataSetOp;
import org.dashbuilder.dataset.DataSetOpEngine;
import org.dashbuilder.dataset.backend.BackendIntervalBuilderDynamicDate;
import org.dashbuilder.dataset.backend.date.DateUtils;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.dataset.engine.group.IntervalBuilder;
import org.dashbuilder.dataset.engine.group.IntervalBuilderLocator;
import org.dashbuilder.dataset.engine.group.IntervalList;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.events.DataSetStaleEvent;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.filter.LogicalExprFilter;
import org.dashbuilder.dataset.filter.LogicalExprType;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.date.TimeFrame;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.impl.DataSetMetadataImpl;
import org.dashbuilder.dataset.impl.MemSizeEstimator;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jooq.Condition;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.SelectSelectStep;
import org.jooq.SelectWhereStep;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;

import static org.jooq.impl.DSL.*;

/**
 *  DataSetProvider implementation for JDBC-compliant data sources.
 *
 *  <p>It totally relies on the jOOQ library in order to abstract the query logic from the database implementation.
 *  jOOQ takes are of providing the right SQL syntax for the target database. The SQL provider resolves
 *  every data set lookup request by transforming such request into the proper jOOQ query. In some cases, an extra
 *  processing of the resulting data is required since some lookup requests do not map directly into the SQL world.
 *  In such cases, specially the grouping of date based data, the core data set operation engine is used.</p>
 *
 *  <p>
 *      Pending stuff:
 *      - Filter on foreign data sets
 *      - Group (fixed) by date of week
 *  </p>
 */
@ApplicationScoped 
@Named("sql")
public class SQLDataSetProvider implements DataSetProvider {

    @Inject
    protected Logger log;

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    @Inject
    protected SQLDataSourceLocator dataSourceLocator;

    @Inject
    protected IntervalBuilderLocator intervalBuilderLocator;

    @Inject
    protected BackendIntervalBuilderDynamicDate intervalBuilderDynamicDate;

    @Inject
    protected DataSetOpEngine opEngine;

    public DataSetProviderType getType() {
        return DataSetProviderType.SQL;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        SQLDataSetDef sqlDef = (SQLDataSetDef) def;
        if (StringUtils.isBlank(sqlDef.getDataSource())) {
            throw new IllegalArgumentException("Missing data source in SQL data set definition: " + sqlDef);
        }
        if (StringUtils.isBlank(sqlDef.getDbSQL()) && StringUtils.isBlank(sqlDef.getDbTable())) {
            throw new IllegalArgumentException("Missing DB table or SQL in the data set definition: " + sqlDef);
        }

        // Look first into the static data set provider cache.
        if (sqlDef.isCacheEnabled()) {
            DataSet dataSet = staticDataSetProvider.lookupDataSet(def.getUUID(), null);
            if (dataSet != null) {

                // Lookup from cache.
                return staticDataSetProvider.lookupDataSet(def.getUUID(), lookup);
            } else  {

                // Fetch always from database if existing rows are greater than the cache max. rows
                int rows = getRowCount(sqlDef);
                if (rows > sqlDef.getCacheMaxRows()) {
                    return _lookupDataSet(sqlDef, lookup);
                }
                // Fetch from database and register into the static cache. Further requests will lookup from cache.
                dataSet = _lookupDataSet(sqlDef, null);
                dataSet.setUUID(def.getUUID());
                dataSet.setDefinition(def);
                staticDataSetProvider.registerDataSet(dataSet);
                return staticDataSetProvider.lookupDataSet(def.getUUID(), lookup);
            }
        }

        // If cache is disabled then always fetch from database.
        return _lookupDataSet(sqlDef, lookup);
    }

    public boolean isDataSetOutdated(DataSetDef def) {

        // Non fetched data sets can't get outdated.
        MetadataHolder last = _metadataMap.remove(def.getUUID());
        if (last == null) return false;

        // Check if the metadata has changed since the last time it was fetched.
        try {
            DataSetMetadata current = getDataSetMetadata(def);
            return !current.equals(last);
        }
        catch (Exception e) {
            log.error("Error fetching metadata: " + def, e);
            return false;
        }
    }

    public DataSetMetadata getDataSetMetadata(DataSetDef def) throws Exception {
        SQLDataSetDef sqlDef = (SQLDataSetDef) def;
        DataSource ds = dataSourceLocator.lookup(sqlDef);
        Connection conn = ds.getConnection();
        try {
            return _getDataSetMetadata(sqlDef, conn);
        } finally {
            conn.close();
        }
    }

    public int getRowCount(SQLDataSetDef def) throws Exception {
        DataSource ds = dataSourceLocator.lookup(def);
        Connection conn = ds.getConnection();
        try {
            return _getRowCount(def, conn);
        } finally {
            conn.close();
        }
    }

    // Listen to changes on the data set definition registry

    protected void onDataSetStaleEvent(@Observes DataSetStaleEvent event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.SQL.equals(def.getProvider())) {
            String uuid = def.getUUID();
            staticDataSetProvider.removeDataSet(uuid);
        }
    }

    protected void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent  event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.SQL.equals(def.getProvider())) {
            String uuid = def.getUUID();
            _metadataMap.remove(uuid);
            staticDataSetProvider.removeDataSet(uuid);
        }
    }
    
    // Internal implementation logic

    protected class MetadataHolder {

        DataSetMetadata metadata;
        Field[] jooqFields;

        public Field getField(String name) {
            for (Field jooqField : jooqFields) {
                if (jooqField.getName().equals(name)) {
                    return jooqField;
                }
            }
            return null;
        }
    }

    protected transient Map<String,MetadataHolder> _metadataMap = new HashMap<String,MetadataHolder>();

    protected DataSetMetadata _getDataSetMetadata(SQLDataSetDef def, Connection conn) throws Exception {
        MetadataHolder result = _metadataMap.get(def.getUUID());
        if (result != null) return result.metadata;

        int estimatedSize = 0;
        int rowCount = _getRowCount(def, conn);

        List<String> columnIds = new ArrayList<String>();
        List<ColumnType> columnTypes = new ArrayList<ColumnType>();
        if (def.getColumns() != null) {
            for (DataColumnDef column : def.getColumns()) {
                columnIds.add(column.getId());
                columnTypes.add(column.getColumnType());
            }
        }

        Field[] _jooqFields = _getFields(def, conn);
        for (Field _jooqField : _jooqFields) {

            String columnId = _jooqField.getName();
            int columnIdx = columnIds.indexOf(columnId);
            boolean columnExists  = columnIdx != -1;

            // Add or skip non-existing columns depending on the data set definition.
            if (!columnExists) {

                // Add any table column
                if (def.isAllColumnsEnabled()) {
                    columnIds.add(columnId);
                    columnIdx = columnIds.size()-1;

                    if (Number.class.isAssignableFrom(_jooqField.getType())) {
                        columnTypes.add(ColumnType.NUMBER);
                    }
                    else if (Date.class.isAssignableFrom(_jooqField.getType())) {
                        columnTypes.add(ColumnType.DATE);
                    }
                    else {
                        columnTypes.add(ColumnType.LABEL);
                    }
                }
                // Skip non existing columns
                else {
                    continue;
                }
            }

            // Calculate the estimated size
            ColumnType cType = columnTypes.get(columnIdx);
            if (ColumnType.DATE.equals(cType)) {
                estimatedSize += MemSizeEstimator.sizeOf(Date.class) * rowCount;
            }
            else if (ColumnType.NUMBER.equals(cType)) {
                estimatedSize += MemSizeEstimator.sizeOf(Double.class) * rowCount;
            }
            else {
                int length = _jooqField.getDataType().length();
                estimatedSize += length/2 * rowCount;
            }
        }
        if (columnIds.isEmpty()) {
            throw new IllegalArgumentException("No data set columns defined for the table '" + def.getDbTable() +
                    " in database '" + def.getDataSource()+ "'");
        }
        result = new MetadataHolder();
        result.jooqFields = _jooqFields;
        result.metadata = new DataSetMetadataImpl(def, def.getUUID(), rowCount, columnIds.size(), columnIds, columnTypes, estimatedSize);
        _metadataMap.put(def.getUUID(), result);
        return result.metadata;
    }

    protected Field[] _getFields(SQLDataSetDef def, Connection conn) throws Exception {
        if (!StringUtils.isBlank(def.getDbSQL())) {
            return using(conn).select()
                    .from("(" + def.getDbSQL() + ") as dbSQL")
                    .limit(1).fetch().fields();
        }
        else {
            List<Table<?>> _jooqTables = using(conn).meta().getTables();
            for (Table<?> _jooqTable : _jooqTables) {
                if (_jooqTable.getName().toLowerCase().equals(def.getDbTable().toLowerCase())) {
                    return _jooqTable.fields();
                }
            }
            throw new IllegalArgumentException("Table '" + def.getDbTable() + "'not found " +
                    "in database '" + def.getDataSource()+ "'");
        }
    }

    protected int _getRowCount(SQLDataSetDef def, Connection conn) throws Exception {

        // Count rows, either on an SQL or a DB table
        SelectSelectStep _jooqQuery = using(conn).selectCount();
        _appendJooqFrom(def, _jooqQuery);

        // Filters set must be taken into account
        DataSetFilter filterOp = def.getDataSetFilter();
        if (filterOp != null) {
            List<ColumnFilter> filterList = filterOp.getColumnFilterList();
            for (ColumnFilter filter : filterList) {
                _appendJooqFilterBy(def, filter, _jooqQuery);
            }
        }
        return ((Number) _jooqQuery.fetch().getValue(0 ,0)).intValue();
    }

    protected DataSet _lookupDataSet(SQLDataSetDef def, DataSetLookup lookup) throws Exception {
        LookupProcessor processor = new LookupProcessor(def, lookup);
        return processor.run();
    }

    protected Table _getJooqTable(SQLDataSetDef def) {
        if (StringUtils.isBlank(def.getDbSchema())) return table(def.getDbTable());
        else return tableByName(def.getDbSchema(), def.getDbTable());
    }

    protected Field _getJooqField(SQLDataSetDef def, String name) {
        MetadataHolder metadataHolder = _metadataMap.get(def.getUUID());
        Field jooqField = metadataHolder != null ? metadataHolder.getField(name) : null;
        if (jooqField == null) {
            if (def.getDbSchema() == null) return field(name);
            else return fieldByName(def.getDbTable(), name);
        } else {
            if (def.getDbSchema() == null) return field(name, jooqField.getDataType());
            else return fieldByName(jooqField.getDataType(), def.getDbTable(), name);
        }
    }

    protected Field _getJooqField(SQLDataSetDef def, String name, DataType type) {
        if (def.getDbSchema() == null) return field(name, type);
        else return fieldByName(type, def.getDbTable(), name);
    }

    protected Field _getJooqField(SQLDataSetDef def, String name, Class clazz) {
        if (def.getDbSchema() == null) return field(name, clazz);
        else return fieldByName(clazz, def.getDbTable(), name);
    }

    protected void _appendJooqFrom(SQLDataSetDef def, SelectSelectStep _jooqQuery) {
        if (!StringUtils.isBlank(def.getDbSQL())) _jooqQuery.from("(" + def.getDbSQL() + ") as dbSQL");
        else _jooqQuery.from(_getJooqTable(def));
    }

    protected void _appendJooqFilterBy(SQLDataSetDef def, DataSetFilter filterOp, SelectWhereStep _jooqQuery) {
        List<ColumnFilter> filterList = filterOp.getColumnFilterList();
        for (ColumnFilter filter : filterList) {
            _appendJooqFilterBy(def, filter, _jooqQuery);
        }
    }

    protected void _appendJooqFilterBy(SQLDataSetDef def, ColumnFilter filter, SelectWhereStep _jooqQuery) {
        _jooqQuery.where(_jooqCondition(def, filter));
    }

    protected Condition _jooqCondition(SQLDataSetDef def, ColumnFilter filter) {
        String filterId = filter.getColumnId();
        Field _jooqField = _getJooqField(def, filterId);

        if (filter instanceof CoreFunctionFilter) {
            CoreFunctionFilter f = (CoreFunctionFilter) filter;
            CoreFunctionType type = f.getType();
            List params = f.getParameters();

            if (CoreFunctionType.IS_NULL.equals(type)) {
                return _jooqField.isNull();
            }
            if (CoreFunctionType.NOT_NULL.equals(type)) {
                return _jooqField.isNotNull();
            }
            if (CoreFunctionType.EQUALS_TO.equals(type)) {
                if (params.size() == 1) return _jooqField.equal(params.get(0));
                return _jooqField.in(params);
            }
            if (CoreFunctionType.NOT_EQUALS_TO.equals(type)) {
                return _jooqField.notEqual(params.get(0));
            }
            if (CoreFunctionType.LOWER_THAN.equals(type)) {
                return _jooqField.lessThan(params.get(0));
            }
            if (CoreFunctionType.LOWER_OR_EQUALS_TO.equals(type)) {
                return _jooqField.lessOrEqual(params.get(0));
            }
            if (CoreFunctionType.GREATER_THAN.equals(type)) {
                return _jooqField.greaterThan(params.get(0));
            }
            if (CoreFunctionType.GREATER_OR_EQUALS_TO.equals(type)) {
                return _jooqField.greaterOrEqual(params.get(0));
            }
            if (CoreFunctionType.BETWEEN.equals(type)) {
                return _jooqField.between(params.get(0), params.get(1));
            }
            if (CoreFunctionType.TIME_FRAME.equals(type)) {
                TimeFrame timeFrame = TimeFrame.parse(params.get(0).toString());
                if (timeFrame != null) {
                    java.sql.Date past = new java.sql.Date(timeFrame.getFrom().getTimeInstant().getTime());
                    java.sql.Date future = new java.sql.Date(timeFrame.getTo().getTimeInstant().getTime());
                    return _jooqField.between(past, future);
                }
            }
        }
        if (filter instanceof LogicalExprFilter) {
            LogicalExprFilter f = (LogicalExprFilter) filter;
            LogicalExprType type = f.getLogicalOperator();

            Condition condition = null;
            List<ColumnFilter> logicalTerms = f.getLogicalTerms();
            for (int i=0; i<logicalTerms.size(); i++) {
                Condition next = _jooqCondition(def, logicalTerms.get(i));

                if (LogicalExprType.AND.equals(type)) {
                    if (condition == null) condition = next;
                    else condition = condition.and(next);
                }
                if (LogicalExprType.OR.equals(type)) {
                    if (condition == null) condition = next;
                    else condition = condition.or(next);
                }
                if (LogicalExprType.NOT.equals(type)) {
                    if (condition == null) condition = next.not();
                    else condition = condition.andNot(next);
                }
            }
            return condition;
        }
        throw new IllegalArgumentException("Filter not supported: " + filter);
    }

    /**
     * Class that provides an isolated context for the processing of a single lookup request.
     */
    private class LookupProcessor {

        SQLDataSetDef def;
        DataSetLookup lookup;
        DataSetMetadata metadata;
        SelectSelectStep _jooqQuery;
        Connection conn;
        Date[] dateLimits;
        DateIntervalType dateIntervalType;
        List<DataSetOp> postProcessingOps = new ArrayList<DataSetOp>();

        public LookupProcessor(SQLDataSetDef def, DataSetLookup lookup) {
            this.def = def;
            this.lookup = lookup;
            DataSetFilter dataSetFilter = def.getDataSetFilter();
            if (dataSetFilter != null) {
                if (lookup == null) this.lookup = new DataSetLookup(def.getUUID(), dataSetFilter);
                else this.lookup.addOperation(0, dataSetFilter);
            }
        }

        public DataSet run() throws Exception {
            DataSource ds = dataSourceLocator.lookup(def);
            conn = ds.getConnection();
            try {
                metadata = _getDataSetMetadata(def, conn);
                int totalRows = metadata.getNumberOfRows();
                boolean trim = (lookup != null && lookup.getNumberOfRows() > 0);

                // The whole data set
                if (lookup == null || lookup.getOperationList().isEmpty()) {

                    // Prepare the jOOQ query
                    _jooqQuery = using(conn).select(_createAllJooqFields());
                    _appendJooqFrom(def, _jooqQuery);

                    // Row limits
                    if (trim) {
                        totalRows = using(conn).fetchCount(_jooqQuery);
                        _jooqQuery.limit(lookup.getNumberOfRows()).offset(lookup.getRowOffset());
                    }

                    // Fetch the results and build the data set
                    Result _jooqResults = _jooqQuery.fetch();
                    List<DataColumn> columns = calculateColumns(null);
                    DataSet dataSet = _buildDataSet(columns, _jooqResults);
                    if (trim) dataSet.setRowCountNonTrimmed(totalRows);
                    return dataSet;
                }
                // ... or a list of operations.
                else {
                    DataSetGroup groupOp = null;
                    int groupIdx = lookup.getFirstGroupOpIndex(0, null, false);
                    if (groupIdx != -1) groupOp = lookup.getOperation(groupIdx);

                    // Prepare the jOOQ query
                    _jooqQuery = using(conn).select(_createJooqFields(groupOp));
                    _appendJooqFrom(def, _jooqQuery);

                    // Append the filter clauses
                    DataSetFilter filterOp = lookup.getFirstFilterOp();
                    if (filterOp != null) {
                        _appendJooqFilterBy(def, filterOp, _jooqQuery);
                    }

                    // Append the interval selections
                    List<DataSetGroup> intervalSelects = lookup.getFirstGroupOpSelections();
                    for (DataSetGroup intervalSelect : intervalSelects) {
                        _appendJooqIntervalSelection(intervalSelect, _jooqQuery);
                    }

                    // ... the group by clauses
                    ColumnGroup cg = null;
                    if (groupOp != null) {
                        cg = groupOp.getColumnGroup();
                        if (cg != null) {
                            _appendJooqGroupBy(groupOp);
                        }
                    }

                    // ... the sort clauses
                    DataSetSort sortOp = lookup.getFirstSortOp();
                    if (sortOp != null) {
                        if (cg != null) _appendJooqOrderGroupBy(groupOp, sortOp);
                        else _appendJooqOrderBy(sortOp);
                    }
                    else if (cg != null) {
                        _appendJooqOrderGroupBy(groupOp);
                    }

                    // ... and the row limits
                    if (trim) {
                        totalRows = using(conn).fetchCount(_jooqQuery);
                        _jooqQuery.limit(lookup.getNumberOfRows()).offset(lookup.getRowOffset());
                    }

                    // Fetch the results and build the data set
                    Result _jooqResults = _jooqQuery.fetch();
                    List<DataColumn> columns = calculateColumns(groupOp);
                    DataSet dataSet = _buildDataSet(columns, _jooqResults);
                    if (trim) dataSet.setRowCountNonTrimmed(totalRows);
                    return dataSet;
                }
            } finally {
                conn.close();
            }
        }

        protected DateIntervalType calculateDateInterval(ColumnGroup cg) {
            if (dateIntervalType != null) return dateIntervalType;

            if (GroupStrategy.DYNAMIC.equals(cg.getStrategy())) {
                Date[] limits = calculateDateLimits(cg.getSourceId());
                if (limits != null) {
                    dateIntervalType = intervalBuilderDynamicDate.calculateIntervalSize(limits[0], limits[1], cg);
                    return dateIntervalType;
                }
            }
            dateIntervalType = DateIntervalType.getByName(cg.getIntervalSize());
            return dateIntervalType;
        }

        protected Date[] calculateDateLimits(String dateColumnId) {
            if (dateLimits != null) return dateLimits;

            Date minDate = calculateDateLimit(dateColumnId, true);
            Date maxDate = calculateDateLimit(dateColumnId, false);
            return dateLimits = new Date[] {minDate, maxDate};
        }

        protected Date calculateDateLimit(String dateColumnId, boolean min) {

            Field _jooqDate = _createJooqField(dateColumnId);
            SelectSelectStep _jooqLimits = using(conn).select(_jooqDate);
            _appendJooqFrom(def, _jooqLimits);

            // Append the filter clauses
            DataSetFilter filterOp = lookup.getFirstFilterOp();
            if (filterOp != null) {
                _appendJooqFilterBy(def, filterOp, _jooqLimits);
            }

            // Append group interval selection filters
            List<DataSetGroup> intervalSelects = lookup.getFirstGroupOpSelections();
            for (DataSetGroup intervalSelect : intervalSelects) {
                _appendJooqIntervalSelection(intervalSelect, _jooqLimits);
            }

            // Fetch the date
            Result rs = _jooqLimits
                    .where(_jooqDate.isNotNull())
                    .orderBy(min ? _jooqDate.asc() : _jooqDate.desc())
                    .limit(1)
                    .fetch();

            if (rs.isEmpty()) return null;
            return (Date) rs.getValue(0, _jooqDate);
        }

        protected List<DataColumn> calculateColumns(DataSetGroup gOp) {
            List<DataColumn> result = new ArrayList<DataColumn>();

            if (gOp == null) {
                for (int i = 0; i < metadata.getNumberOfColumns(); i++) {
                    String columnId = metadata.getColumnId(i);
                    ColumnType columnType = metadata.getColumnType(i);
                    DataColumn column = new DataColumnImpl(columnId, columnType);
                    result.add(column);
                }
            }
            else {
                ColumnGroup cg = gOp.getColumnGroup();
                for (GroupFunction gf : gOp.getGroupFunctions()) {

                    String sourceId = gf.getSourceId();
                    String columnId = _getTargetColumnId(gf);

                    DataColumnImpl column = new DataColumnImpl();
                    column.setId(columnId);
                    column.setGroupFunction(gf);
                    result.add(column);

                    // Group column
                    if (cg != null && cg.getSourceId().equals(sourceId)) {
                        column.setColumnType(ColumnType.LABEL);
                        column.setColumnGroup(cg);
                        if (ColumnType.DATE.equals(metadata.getColumnType(sourceId))) {
                            column.setIntervalType(dateIntervalType != null ? dateIntervalType.toString() : null);
                            column.setMinValue(dateLimits != null ? dateLimits[0] : null);
                            column.setMaxValue(dateLimits != null ? dateLimits[1] : null);
                        }
                    }
                    // Aggregated column
                    else if (gf.getFunction() != null) {
                        column.setColumnType(ColumnType.NUMBER);
                    }
                    // Existing Column
                    else {
                        column.setColumnType(metadata.getColumnType(sourceId));
                    }
                }
            }
            return result;
        }

        protected void _appendJooqOrderBy(DataSetSort sortOp) {
            List<SortField> _jooqFields = new ArrayList<SortField>();
            List<ColumnSort> sortList = sortOp.getColumnSortList();
            for (ColumnSort columnSort : sortList) {
                String columnId = columnSort.getColumnId();
                _assertColumnExists(columnId);

                if (SortOrder.DESCENDING.equals(columnSort.getOrder())) {
                    _jooqFields.add(_createJooqField(columnId).desc());
                } else {
                    _jooqFields.add(_createJooqField(columnId).asc());
                }
            }
            _jooqQuery.orderBy(_jooqFields);
        }

        protected boolean isDynamicDateGroup(DataSetGroup groupOp) {
            ColumnGroup cg = groupOp.getColumnGroup();
            if (!ColumnType.DATE.equals(metadata.getColumnType(cg.getSourceId()))) return false;
            if (!GroupStrategy.DYNAMIC.equals(cg.getStrategy())) return false;
            return true;
        }

        protected void _appendJooqOrderGroupBy(DataSetGroup groupOp) {
            if (isDynamicDateGroup(groupOp)) {
                ColumnGroup cg = groupOp.getColumnGroup();
                _jooqQuery.orderBy(_createJooqField(cg).asc());

                // If the group column is in the resulting data set then ensure the data set order
                GroupFunction gf = groupOp.getGroupFunction(cg.getSourceId());
                if (gf != null) {
                    DataSetSort sortOp = new DataSetSort();
                    String targetId = _getTargetColumnId(gf);
                    sortOp.addSortColumn(new ColumnSort(targetId, SortOrder.ASCENDING));
                    postProcessingOps.add(sortOp);
                }
            }
        }

        protected void _appendJooqOrderGroupBy(DataSetGroup groupOp, DataSetSort sortOp) {
            List<SortField> _jooqFields = new ArrayList<SortField>();
            List<ColumnSort> sortList = sortOp.getColumnSortList();
            ColumnGroup cg = groupOp.getColumnGroup();

            for (ColumnSort cs : sortList) {
                GroupFunction gf = groupOp.getGroupFunction(cs.getColumnId());

                // Sort by the group column
                if (cg.getSourceId().equals(cs.getColumnId()) || cg.getColumnId().equals(cs.getColumnId())) {
                    if (SortOrder.DESCENDING.equals(cs.getOrder())) {
                        _jooqFields.add(_createJooqField(cg).desc());
                        if (isDynamicDateGroup(groupOp)) {
                            postProcessingOps.add(sortOp);
                        }
                    } else {
                        _jooqFields.add(_createJooqField(cg).asc());
                        if (isDynamicDateGroup(groupOp)) {
                            postProcessingOps.add(sortOp);
                        }
                    }
                }
                // Sort by an aggregation
                else if (gf != null) {
                    // In SQL, sort is only permitted for columns belonging to the result set.
                    if (SortOrder.DESCENDING.equals(cs.getOrder())) {
                        _jooqFields.add(_createJooqField(gf).desc());
                    } else {
                        _jooqFields.add(_createJooqField(gf).asc());
                    }
                }
            }
            _jooqQuery.orderBy(_jooqFields);
        }

        protected void _appendJooqIntervalSelection(DataSetGroup intervalSel, SelectWhereStep _jooqQuery) {
            if (intervalSel != null && intervalSel.isSelect()) {
                ColumnGroup cg = intervalSel.getColumnGroup();
                List<Interval> intervalList = intervalSel.getSelectedIntervalList();

                // Get the filter values
                List<Comparable> names = new ArrayList<Comparable>();
                Comparable min = null;
                Comparable max = null;
                for (Interval interval : intervalList) {
                    names.add(interval.getName());
                    Comparable intervalMin = (Comparable) interval.getMinValue();
                    Comparable intervalMax = (Comparable) interval.getMaxValue();

                    if (intervalMin != null) {
                        if (min == null) min = intervalMin;
                        else if (min.compareTo(intervalMin) > 0) min = intervalMin;
                    }
                    if (intervalMax != null) {
                        if (max == null) max = intervalMax;
                        else if (max.compareTo(intervalMax) > 0) max = intervalMax;
                    }
                }
                // Min can't be greater than max.
                if (min != null && max != null && min.compareTo(max) > 0) {
                    min = max;
                }

                // Apply the filter
                ColumnFilter filter = null;
                if (min != null && max != null) {
                    filter = FilterFactory.between(cg.getSourceId(), min, max);
                }
                else if (min != null) {
                    filter = FilterFactory.greaterOrEqualsTo(cg.getSourceId(), min);
                }
                else if (max != null) {
                    filter = FilterFactory.lowerOrEqualsTo(cg.getSourceId(), max);
                }
                else {
                    filter = FilterFactory.equalsTo(cg.getSourceId(), names);
                }
                _appendJooqFilterBy(def, filter, _jooqQuery);
            }
        }

        protected void _appendJooqGroupBy(DataSetGroup groupOp) {
            ColumnGroup cg = groupOp.getColumnGroup();
            String sourceId = cg.getSourceId();
            ColumnType columnType = metadata.getColumnType(_assertColumnExists(sourceId));
            boolean postProcessing = false;

            // Group by Number => not supported
            if (ColumnType.NUMBER.equals(columnType)) {
                throw new IllegalArgumentException("Group by number '" + sourceId + "' not supported");
            }
            // Group by Text => not supported
            if (ColumnType.TEXT.equals(columnType)) {
                throw new IllegalArgumentException("Group by text '" + sourceId + "' not supported");
            }
            // Group by Date
            else if (ColumnType.DATE.equals(columnType)) {
                DateIntervalType type = calculateDateInterval(cg);
                _jooqQuery.groupBy(_createJooqGroupField(cg, type));
                postProcessing = true;
            }
            // Group by Label
            else {
                _jooqQuery.groupBy(_createJooqField(sourceId));
            }

            // Also add any non-aggregated column (columns pick up) to the group statement
            for (GroupFunction gf : groupOp.getGroupFunctions()) {
                if (gf.getFunction() == null && !gf.getSourceId().equals(cg.getSourceId())) {
                    _jooqQuery.groupBy(_createJooqField(gf.getSourceId()));
                }
            }
            // The group operation might require post processing
            if (postProcessing) {
                DataSetGroup postGroup = groupOp.cloneInstance();
                GroupFunction gf = postGroup.getGroupFunction(sourceId);
                if (gf != null) {
                    String targetId = _getTargetColumnId(gf);
                    postGroup.getColumnGroup().setSourceId(targetId);
                    postGroup.getColumnGroup().setColumnId(targetId);
                }
                for (GroupFunction pgf : postGroup.getGroupFunctions()) {
                    AggregateFunctionType pft = pgf.getFunction();
                    pgf.setSourceId(_getTargetColumnId(pgf));
                    if (pft != null && (AggregateFunctionType.DISTINCT.equals(pft) || AggregateFunctionType.COUNT.equals(pft))) {
                        pgf.setFunction(AggregateFunctionType.SUM);
                    }
                }
                postProcessingOps.add(postGroup);
            }
        }

        protected DataSet _buildDataSet(List<DataColumn> columns, Result _jooqRs) {
            DataSet dataSet = DataSetFactory.newEmptyDataSet();
            int dateGroupColumnIdx = -1;
            boolean dateIncludeEmptyIntervals = false;
            Field[] rsFields = _jooqRs.fields();
            for (int i = 0; i < rsFields.length; i++) {
                Field f = rsFields[i];
                DataColumn column = columns.get(i).cloneEmpty();
                ColumnType columnType = column.getColumnType();
                List values = _jooqRs.getValues(f);

                if (ColumnType.LABEL.equals(columnType)) {
                    ColumnGroup cg = column.getColumnGroup();
                    if (cg != null && ColumnType.DATE.equals(metadata.getColumnType(cg.getSourceId()))) {
                        dateGroupColumnIdx = i;
                        dateIncludeEmptyIntervals = cg.areEmptyIntervalsAllowed();

                        // If grouped by date then convert back to absolute dates
                        // in order to allow the post processing of the data set.
                        column.setColumnType(ColumnType.DATE);
                        for (int j=0; j<values.size(); j++) {
                            Object val = values.remove(j);
                            try {
                                Date dateObj = DateUtils.parseDate(column, val);
                                values.add(j, dateObj);
                            } catch (Exception e) {
                                log.error("Error parsing date: " + val);
                                values.add(j, null);
                            }
                        }
                    }
                }
                if (ColumnType.NUMBER.equals(columnType)) {
                    for (int j=0; j<values.size(); j++) {
                        Number num = (Number) values.remove(j);
                        values.add(j, num != null ? num.doubleValue() : null);
                    }
                }

                column.setValues(values);
                dataSet.addColumn(column);
            }
            // Some operations requires some in-memory post-processing
            if (!postProcessingOps.isEmpty()) {
                DataSet tempSet = opEngine.execute(dataSet, postProcessingOps);
                dataSet = dataSet.cloneEmpty();
                for (int i=0; i<columns.size(); i++) {
                    DataColumn source = tempSet.getColumnByIndex(i);
                    DataColumn target = dataSet.getColumnByIndex(i);
                    target.setColumnType(source.getColumnType());
                    target.setIntervalType(source.getIntervalType());
                    target.setMinValue(target.getMinValue());
                    target.setMaxValue(target.getMaxValue());
                    target.setValues(new ArrayList(source.getValues()));
                }
            }
            // Group by date might require to include empty intervals
            if (dateIncludeEmptyIntervals)  {
                DataColumn dateColumn = dataSet.getColumnByIndex(dateGroupColumnIdx);
                IntervalBuilder intervalBuilder = intervalBuilderLocator.lookup(ColumnType.DATE, dateColumn.getColumnGroup().getStrategy());
                IntervalList intervalList = intervalBuilder.build(dateColumn);
                if (intervalList.size() > dataSet.getRowCount()) {
                    List values = dateColumn.getValues();
                    int valueIdx = 0;

                    for (int intervalIdx = 0; intervalIdx < intervalList.size(); intervalIdx++) {
                        String interval = intervalList.get(intervalIdx).getName();
                        String value = values.isEmpty() ? null : (String) values.get(valueIdx++);
                        if (value == null || !value.equals(interval)) {
                            dataSet.addEmptyRowAt(intervalIdx);
                            dataSet.setValueAt(intervalIdx, dateGroupColumnIdx, interval);
                        }
                    }
                }
            }
            return dataSet;
        }

        protected Field _createJooqField(String name) {
            return _getJooqField(def, name);
        }

        protected Field _createJooqField(String name, DataType type) {
            return _getJooqField(def, name, type);
        }

        protected Field _createJooqField(String name, Class clazz) {
            return _getJooqField(def, name, clazz);
        }

        protected Collection<Field<?>> _createAllJooqFields() {
            Collection<Field<?>> _jooqFields = new ArrayList<Field<?>>();
            for (int i = 0; i < metadata.getNumberOfColumns(); i++) {

                String columnId = metadata.getColumnId(i);
                ColumnType columnType = metadata.getColumnType(i);

                if (ColumnType.DATE.equals(columnType)) {
                    _jooqFields.add(_createJooqField(columnId, java.sql.Timestamp.class));
                }
                else if (ColumnType.NUMBER.equals(columnType)) {
                    _jooqFields.add(_createJooqField(columnId, Double.class));
                }
                else {
                    _jooqFields.add(_createJooqField(columnId, String.class));
                }
            }
            return _jooqFields;
        }

        protected Collection<Field<?>> _createJooqFields(DataSetGroup gOp) {
            if (gOp == null) return _createAllJooqFields();

            ColumnGroup cg = gOp.getColumnGroup();
            Collection<Field<?>> _jooqFields = new ArrayList<Field<?>>();
            for (GroupFunction gf : gOp.getGroupFunctions()) {

                String columnId = gf.getSourceId();
                if (columnId == null) columnId = metadata.getColumnId(0);
                else _assertColumnExists(gf.getSourceId());

                if (cg != null && cg.getSourceId().equals(columnId)) {
                    _jooqFields.add(_createJooqField(cg));
                } else {
                    _jooqFields.add(_createJooqField(gf));
                }
            }
            return _jooqFields;
        }

        protected Field _createJooqField(GroupFunction gf) {
            String sourceId = gf.getSourceId();
            String targetId = gf.getColumnId();
            if (sourceId == null) sourceId = metadata.getColumnId(0);
            if (targetId == null) targetId = sourceId;

            // Raw column
            AggregateFunctionType ft = gf.getFunction();
            Field _jooqField = _createJooqField(sourceId);
            if (ft == null) return _jooqField.as(targetId);

            // Aggregation function
            if (AggregateFunctionType.SUM.equals(ft)) return _jooqField.sum().as(targetId);
            if (AggregateFunctionType.MAX.equals(ft)) return _jooqField.max().as(targetId);
            if (AggregateFunctionType.MIN.equals(ft)) return _jooqField.min().as(targetId);
            if (AggregateFunctionType.AVERAGE.equals(ft)) return _jooqField.avg().as(targetId);
            if (AggregateFunctionType.DISTINCT.equals(ft)) return _jooqField.countDistinct().as(targetId);
            if (AggregateFunctionType.COUNT.equals(ft)) return _jooqField.count().as(targetId);
            return _jooqField.as(targetId);
        }

        protected Field _createJooqField(ColumnGroup cg) {
            String columnId = cg.getSourceId();
            _assertColumnExists(columnId);

            ColumnType type = metadata.getColumnType(columnId);

            if (ColumnType.DATE.equals(type)) {
                DateIntervalType size = calculateDateInterval(cg);
                return _createJooqGroupField(cg, size);
            }
            if (ColumnType.NUMBER.equals(type)) {
                throw new IllegalArgumentException("Group by number '" + columnId + "' not supported");
            }
            if (ColumnType.TEXT.equals(type)) {
                throw new IllegalArgumentException("Group by text '" + columnId + "' not supported");
            }
            return _createJooqField(columnId);
        }

        protected Field _createJooqGroupField(ColumnGroup cg, DateIntervalType type) {

            if (GroupStrategy.FIXED.equals(cg.getStrategy())) {
                return _createJooqGroupFixedField(cg, type);
            }
            else {
                return _createJooqGroupDynamicField(cg, type);
            }
        }

        protected Field _createJooqGroupDynamicField(ColumnGroup cg, DateIntervalType type) {
            String columnId = cg.getSourceId();
            Field _jooqField = _createJooqField(columnId, SQLDataType.DATE);
            Field SEPARATOR = field("'-'");

            if (DateIntervalType.SECOND.equals(type)) {
                return concat(year(_jooqField), SEPARATOR,
                        month(_jooqField), SEPARATOR,
                        day(_jooqField), SEPARATOR,
                        hour(_jooqField), SEPARATOR,
                        minute(_jooqField), SEPARATOR,
                        second(_jooqField));
            }
            if (DateIntervalType.MINUTE.equals(type)) {
                return concat(year(_jooqField), SEPARATOR,
                        month(_jooqField), SEPARATOR,
                        day(_jooqField), SEPARATOR,
                        hour(_jooqField), SEPARATOR,
                        minute(_jooqField));
            }
            if (DateIntervalType.HOUR.equals(type)) {
                return concat(year(_jooqField), SEPARATOR,
                        month(_jooqField), SEPARATOR,
                        day(_jooqField), SEPARATOR,
                        hour(_jooqField));
            }
            if (DateIntervalType.DAY.equals(type) || DateIntervalType.WEEK.equals(type)) {
                return concat(year(_jooqField), SEPARATOR,
                        month(_jooqField), SEPARATOR,
                        day(_jooqField));
            }
            if (DateIntervalType.MONTH.equals(type)
                || DateIntervalType.QUARTER.equals(type)) {

                return concat(year(_jooqField), SEPARATOR,
                        month(_jooqField));
            }
            if (DateIntervalType.YEAR.equals(type)
                || DateIntervalType.DECADE.equals(type)
                || DateIntervalType.CENTURY.equals(type)
                || DateIntervalType.MILLENIUM.equals(type)) {

                return year(_jooqField);
            }
            throw new IllegalArgumentException("Group '" + columnId +
                    "' by the given data interval type is not supported: " + type);
        }

        protected Field _createJooqGroupFixedField(ColumnGroup cg, DateIntervalType type) {
            String columnId = cg.getSourceId();
            Field _jooqField = _createJooqField(columnId, SQLDataType.DATE);

            if (DateIntervalType.SECOND.equals(type)) {
                return second(_jooqField);
            }
            if (DateIntervalType.MINUTE.equals(type)) {
                return minute(_jooqField);
            }
            if (DateIntervalType.HOUR.equals(type)) {
                return hour(_jooqField);
            }
            if (DateIntervalType.DAY_OF_WEEK.equals(type)) {
                return day(_jooqField);
            }
            if (DateIntervalType.MONTH.equals(type)) {
                return month(_jooqField);
            }
            if (DateIntervalType.QUARTER.equals(type)) {
                // Emulated using month and converted to quarter during the data set post-processing
                return month(_jooqField);
            }
            throw new IllegalArgumentException("Interval size '" + type + "' not supported for " +
                    "fixed date intervals. The only supported sizes are: " +
                    StringUtils.join(DateIntervalType.FIXED_INTERVALS_SUPPORTED, ","));
        }

        protected int _assertColumnExists(String columnId) {
            for (int i = 0; i < metadata.getNumberOfColumns(); i++) {
                if (metadata.getColumnId(i).equals(columnId)) {
                    return i;
                }
            }
            throw new RuntimeException("Column '" + columnId +
                    "' not found in data set: " + metadata.getUUID());
        }

        protected String _getTargetColumnId(GroupFunction gf) {
            String sourceId = gf.getSourceId();
            if (sourceId != null) _assertColumnExists(sourceId);
            return gf.getColumnId() == null ?  sourceId : gf.getColumnId();
        }
    }
}
