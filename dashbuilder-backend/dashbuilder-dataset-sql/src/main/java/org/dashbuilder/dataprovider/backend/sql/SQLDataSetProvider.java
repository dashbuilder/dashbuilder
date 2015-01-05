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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.backend.BackendIntervalBuilderDynamicDate;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
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
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.impl.DataSetMetadataImpl;
import org.dashbuilder.dataset.impl.MemSizeEstimator;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.SelectWhereStep;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;
import static org.jooq.impl.DSL.*;

@Named("sql")
public class SQLDataSetProvider implements DataSetProvider {

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    @Inject
    protected SQLDataSourceLocator dataSourceLocator;

    @Inject
    protected BackendIntervalBuilderDynamicDate intervalBuilder;

    public DataSetProviderType getType() {
        return DataSetProviderType.SQL;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        SQLDataSetDef sqlDef = (SQLDataSetDef) def;
        if (StringUtils.isBlank(sqlDef.getDataSource())) {
            throw new IllegalArgumentException("Missing data source in SQL data set definition: " + sqlDef);
        }
        if (StringUtils.isBlank(sqlDef.getDbTable())) {
            throw new IllegalArgumentException("Missing data table in SQL data set definition: " + sqlDef);
        }

        // Look first into the static data set provider cache.
        if (sqlDef.isCacheEnabled()) {
            DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);
            if (dataSet != null) {

                // Lookup from cache.
                return staticDataSetProvider.lookupDataSet(def, lookup);
            }  else  {

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
                return staticDataSetProvider.lookupDataSet(def, lookup);
            }
        }

        // If cache is disabled then always fetch from database.
        return _lookupDataSet(sqlDef, lookup);
    }

    public boolean isDataSetOutdated(DataSetDef def) {
        try {
            // If cache is disabled then no way for a data set to get outdated
            SQLDataSetDef sqlDef = (SQLDataSetDef) def;
            if (!sqlDef.isCacheEnabled()) return false;

            // A no-synced cache will always contains the same rows since it was created.
            if (!sqlDef.isCacheSynced()) return false;

            // ... for non cached data sets either.
            DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);
            if (dataSet == null) return false;

            // Compare the cached vs database rows.
            int rows = getRowCount(sqlDef);
            return rows != dataSet.getRowCount();
        }
        catch (Exception e) {
            e.printStackTrace();
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

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);

        DataSetDef oldDef = event.getOldDataSetDef();
        if (DataSetProviderType.SQL.equals(oldDef.getProvider())) {
            String uuid = event.getOldDataSetDef().getUUID();
            _metadataMap.remove(uuid);
            staticDataSetProvider.removeDataSet(uuid);
        }
    }

    // Internal implementation logic

    protected transient Map<String,DataSetMetadata> _metadataMap = new HashMap<String,DataSetMetadata>();

    protected DataSetMetadata _getDataSetMetadata(SQLDataSetDef def, Connection conn) throws Exception {
        DataSetMetadata result = _metadataMap.get(def.getUUID());
        if (result != null) return result;

        int estimatedSize = 0;
        int rowCount = _getRowCount(def, conn);

        List<String> columnIds = new ArrayList<String>();
        List<ColumnType> columnTypes = new ArrayList<ColumnType>();
        for (DataColumn column : def.getDataSet().getColumns()) {
            columnIds.add(column.getId());
            columnTypes.add(column.getColumnType());
        }

        boolean tableFound = false;
        List<Table<?>> _jooqTables = using(conn).meta().getTables();
        for (Table<?> _jooqTable : _jooqTables) {

            if (_jooqTable.getName().toLowerCase().equals(def.getDbTable().toLowerCase())) {
                tableFound = true;

                for (Field _jooqField : _jooqTable.fields()) {
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
            }
        }
        if (!tableFound) {
            throw new IllegalArgumentException("Table '" + def.getDbTable() + "'not found " +
                    "in database '" + def.getDataSource()+ "'");
        }
        if (columnIds.isEmpty()) {
            throw new IllegalArgumentException("No data set columns defined for the table '" + def.getDbTable() +
                    " in database '" + def.getDataSource()+ "'");
        }
        _metadataMap.put(def.getUUID(), result =
                new DataSetMetadataImpl(def, def.getUUID(), rowCount,
                        columnIds.size(), columnIds, columnTypes, estimatedSize));
        return result;
    }

    protected int _getRowCount(SQLDataSetDef def, Connection conn) throws Exception {
        /* jOOQ 3.1.0 style => return using(conn).selectFrom(_getJooqTable(def, def.getDbTable())).fetchCount(); */
        return using(conn).fetchCount(_getJooqTable(def, def.getDbTable()));
    }

    protected DataSet _lookupDataSet(SQLDataSetDef def, DataSetLookup lookup) throws Exception {
        LookupProcessor processor = new LookupProcessor(def, lookup);
        return processor.run();
    }

    protected Table _getJooqTable(SQLDataSetDef def, String name) {
        if (def.getDbSchema() == null) return table(name);
        else return tableByName(def.getDbSchema(), name);
    }

    protected Field _getJooqField(SQLDataSetDef def, String name) {
        if (def.getDbSchema() == null) return field(name);
        else return fieldByName(def.getDbTable(), name);
    }

    protected Field _getJooqField(SQLDataSetDef def, String name, DataType type) {
        if (def.getDbSchema() == null) return field(name, type);
        else return fieldByName(type, def.getDbTable(), name);
    }

    protected Field _getJooqField(SQLDataSetDef def, String name, Class clazz) {
        if (def.getDbSchema() == null) return field(name, clazz);
        else return fieldByName(clazz, def.getDbTable(), name);
    }

    /**
     * Class that provides an isolated context for the processing of a single lookup request.
     */
    private class LookupProcessor {

        SQLDataSetDef def;
        DataSetLookup lookup;
        DataSetMetadata metadata;
        Table _jooqTable;
        SelectWhereStep _jooqQuery;
        Connection conn;
        Date[] dateLimits;
        DateIntervalType dateIntervalType;

        public LookupProcessor(SQLDataSetDef def, DataSetLookup lookup) {
            this.def = def;
            this.lookup = lookup;
            _jooqTable = _createJooqTable(def.getDbTable());
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
                    _jooqQuery = using(conn)
                            .select(_createAllJooqFields())
                            .from(_jooqTable);

                    // Row limits
                    if (trim) {
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
                    boolean totalRowsChanged = false;

                    // Prepare the jOOQ query
                    _jooqQuery = using(conn)
                            .select(_createJooqFields(groupOp))
                            .from(_jooqTable);


                    // Append the filter clauses
                    DataSetFilter filterOp = lookup.getFirstFilterOp();
                    if (filterOp != null) {
                        _appendJooqFilterBy(filterOp, _jooqQuery);
                        totalRowsChanged = true;
                    }

                    // Append the interval selections
                    List<DataSetGroup> intervalSelects = lookup.getFirstGroupOpSelections();
                    for (DataSetGroup intervalSelect : intervalSelects) {
                        _appendJooqIntervalSelection(intervalSelect, _jooqQuery);
                        totalRowsChanged = true;
                    }

                    // ... the group by clauses
                    ColumnGroup cg = null;
                    if (groupOp != null) {
                        cg = groupOp.getColumnGroup();
                        if (cg != null) {
                            _appendJooqGroupBy(groupOp);
                            totalRowsChanged = true;
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
                        if (totalRowsChanged) totalRows = using(conn).fetchCount(_jooqQuery);
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
                    dateIntervalType = intervalBuilder.calculateIntervalSize(limits[0], limits[1], cg);
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
            SelectWhereStep _jooqLimits = using(conn)
                    .select(_jooqDate)
                    .from(_jooqTable);

            // Append the filter clauses
            DataSetFilter filterOp = lookup.getFirstFilterOp();
            if (filterOp != null) {
                _appendJooqFilterBy(filterOp, _jooqLimits);
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
                for (GroupFunction gFunc : gOp.getGroupFunctions()) {

                    String columnId = gFunc.getSourceId();
                    String columnName = gFunc.getColumnId();
                    if (columnId != null) _assertColumnExists(columnId);

                    DataColumnImpl column = new DataColumnImpl();
                    column.setId(columnId);
                    column.setName(columnName);
                    result.add(column);

                    // Group column
                    if (cg != null && cg.getSourceId().equals(columnId)) {
                        column.setColumnType(ColumnType.LABEL);
                        column.setColumnGroup(cg);
                        if (ColumnType.DATE.equals(metadata.getColumnType(columnId))) {
                            column.setIntervalType(dateIntervalType != null ? dateIntervalType.toString() : null);
                            column.setMinValue(dateLimits != null ? dateLimits[0] : null);
                            column.setMaxValue(dateLimits != null ? dateLimits[1] : null);
                        }
                    }
                    // Aggregated column
                    else if (gFunc.getFunction() != null) {
                        column.setColumnType(ColumnType.NUMBER);
                    }
                    // Existing Column
                    else {
                        column.setColumnType(metadata.getColumnType(columnId));
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

        protected void _appendJooqOrderGroupBy(DataSetGroup groupOp) {
            ColumnGroup cg = groupOp.getColumnGroup();
            if (ColumnType.DATE.equals(metadata.getColumnType(cg.getSourceId()))) {
                _jooqQuery.orderBy(_createJooqField(cg).asc());
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
                    } else {
                        _jooqFields.add(_createJooqField(cg).asc());
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

        protected void _appendJooqFilterBy(DataSetFilter filterOp, SelectWhereStep _jooqQuery) {
            List<ColumnFilter> filterList = filterOp.getColumnFilterList();
            for (ColumnFilter filter : filterList) {
                _appendJooqFilterBy(filter, _jooqQuery);
            }
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
                    filter = FilterFactory.isBetween(cg.getSourceId(), min, max);
                }
                else if (min != null) {
                    filter = FilterFactory.isGreaterOrEqualsTo(cg.getSourceId(), min);
                }
                else if (max != null) {
                    filter = FilterFactory.isLowerOrEqualsTo(cg.getSourceId(), max);
                }
                else {
                    filter = FilterFactory.isEqualsTo(cg.getSourceId(), names);
                }
                _appendJooqFilterBy(filter, _jooqQuery);
            }
        }

        protected void _appendJooqFilterBy(ColumnFilter filter, SelectWhereStep _jooqQuery) {
            String filterId = filter.getColumnId();
            Field _jooqField = _createJooqField(filterId);

            if (filter instanceof CoreFunctionFilter) {
                CoreFunctionFilter f = (CoreFunctionFilter) filter;
                CoreFunctionType type = f.getType();
                List<Comparable> params = f.getParameters();

                if (CoreFunctionType.IS_NULL.equals(type)) {
                    _jooqQuery.where(_jooqField.isNull());
                }
                else if (CoreFunctionType.IS_NOT_NULL.equals(type)) {
                    _jooqQuery.where(_jooqField.isNotNull());
                }
                else if (CoreFunctionType.IS_EQUALS_TO.equals(type)) {
                    if (params.size() == 1) _jooqQuery.where(_jooqField.equal(params.get(0)));
                    else _jooqQuery.where(_jooqField.in(params));
                }
                else if (CoreFunctionType.IS_NOT_EQUALS_TO.equals(type)) {
                    _jooqQuery.where(_jooqField.notEqual(params.get(0)));
                }
                else if (CoreFunctionType.IS_LOWER_THAN.equals(type)) {
                    _jooqQuery.where(_jooqField.lessThan(params.get(0)));
                }
                else if (CoreFunctionType.IS_LOWER_OR_EQUALS_TO.equals(type)) {
                    _jooqQuery.where(_jooqField.lessOrEqual(params.get(0)));
                }
                else if (CoreFunctionType.IS_GREATER_THAN.equals(type)) {
                    _jooqQuery.where(_jooqField.greaterThan(params.get(0)));
                }
                else if (CoreFunctionType.IS_GREATER_OR_EQUALS_TO.equals(type)) {
                    _jooqQuery.where(_jooqField.greaterOrEqual(params.get(0)));
                }
                else if (CoreFunctionType.IS_BETWEEN.equals(type)) {
                    _jooqQuery.where(_jooqField.between(params.get(0), params.get(1)));
                }
                else {
                    throw new IllegalArgumentException("Core function type not supported: " + type);
                }
            }
            if (filter instanceof LogicalExprFilter) {
                LogicalExprFilter f = (LogicalExprFilter) filter;
                LogicalExprType type = f.getLogicalOperator();

                // TODO: logical expr filters
                if (LogicalExprType.AND.equals(type)) {
                }
                if (LogicalExprType.OR.equals(type)) {
                }
                if (LogicalExprType.NOT.equals(type)) {
                }
            }
        }

        protected void _appendJooqGroupBy(DataSetGroup groupOp) {
            ColumnGroup cg = groupOp.getColumnGroup();
            String columnId = cg.getSourceId();
            ColumnType columnType = metadata.getColumnType(_assertColumnExists(columnId));

            // Group by Number => not supported
            if (ColumnType.NUMBER.equals(columnType)) {
                throw new IllegalArgumentException("Group by number '" + columnId + "' not supported");
            }
            // Group by Text => not supported
            if (ColumnType.TEXT.equals(columnType)) {
                throw new IllegalArgumentException("Group by text '" + columnId + "' not supported");
            }
            // Group by Date
            else if (ColumnType.DATE.equals(columnType)) {
                DateIntervalType type = calculateDateInterval(cg);
                _jooqQuery.groupBy(_createJooqGroupField(cg, type));
            }
            // Group by Label
            else {
                _jooqQuery.groupBy(_createJooqField(columnId));
            }
        }

        protected DataSet _buildDataSet(List<DataColumn> columns, Result _jooqRs) {
            DataSet dataSet = DataSetFactory.newEmptyDataSet();

            Field[] rsFields = _jooqRs.fields();
            for (int i = 0; i < rsFields.length; i++) {
                Field f = rsFields[i];
                DataColumnImpl column = (DataColumnImpl) columns.get(i);
                ColumnType columnType = column.getColumnType();

                List values = _jooqRs.getValues(f);
                if (!values.isEmpty()) {

                    if (ColumnType.LABEL.equals(columnType)) {
                        if (!(values.get(0) instanceof String)) {
                            for (int j=0; j<values.size(); j++) {
                                Object val = values.remove(j);
                                values.add(j, val != null ? val.toString() : null);
                            }
                        }
                    }
                    if (ColumnType.NUMBER.equals(columnType)) {
                        for (int j=0; j<values.size(); j++) {
                            Number num = (Number) values.remove(j);
                            values.add(j, num != null ? num.doubleValue() : null);
                        }
                    }
                }

                column.setValues(values);
                dataSet.getColumns().add(column);
            }
            return dataSet;
        }

        protected Table _createJooqTable(String name) {
            return _getJooqTable(def, name);
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
            String columnId = gf.getSourceId();
            if (columnId == null) columnId = metadata.getColumnId(0);
            else _assertColumnExists(columnId);

            // Raw column
            AggregateFunctionType ft = gf.getFunction();
            Field _jooqField = _createJooqField(columnId);
            if (ft == null) return _jooqField;

            // Aggregation function
            if (AggregateFunctionType.SUM.equals(ft)) return _jooqField.sum();
            if (AggregateFunctionType.MAX.equals(ft)) return _jooqField.max();
            if (AggregateFunctionType.MIN.equals(ft)) return _jooqField.min();
            if (AggregateFunctionType.AVERAGE.equals(ft)) return _jooqField.avg();
            if (AggregateFunctionType.DISTINCT.equals(ft)) return _jooqField.countDistinct();
            if (AggregateFunctionType.COUNT.equals(ft)) return _jooqField.count();
            return _jooqField;
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
            if (DateIntervalType.DAY.equals(type)) {
                return concat(year(_jooqField), SEPARATOR,
                        month(_jooqField), SEPARATOR,
                        day(_jooqField));
            }
            if (DateIntervalType.MONTH.equals(type)) {
                return concat(year(_jooqField), SEPARATOR,
                        month(_jooqField));
            }
            if (DateIntervalType.YEAR.equals(type)) {
                return year(_jooqField);
            }
            throw new IllegalArgumentException("Group '" + columnId + "' by the given data interval type is not supported: " + type);
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
            if (DateIntervalType.DAY.equals(type)) {
                return day(_jooqField);
            }
            if (DateIntervalType.MONTH.equals(type)) {
                return month(_jooqField);
            }
            if (DateIntervalType.YEAR.equals(type)) {
                return year(_jooqField);
            }
            throw new IllegalArgumentException("Group '" + columnId + "' by the given data interval type is not supported: " + type);
        }

        protected int _assertColumnExists(String columnId) {
            for (int i = 0; i < metadata.getNumberOfColumns(); i++) {
                if (metadata.getColumnId(i).equals(columnId)) {
                    return i;
                }
            }
            throw new RuntimeException("Column '" + columnId + "' not found in data set: " + metadata.getUUID());
        }
    }
}
