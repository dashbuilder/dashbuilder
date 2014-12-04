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
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.filter.LogicalExprFilter;
import org.dashbuilder.dataset.filter.LogicalExprType;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.impl.DataSetMetadataImpl;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jooq.DataType;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSeekStepN;
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
                int rows = _getRowCount(sqlDef);
                if (rows > sqlDef.getCacheMaxRows()) {
                    return _lookupDataSet(sqlDef, lookup);
                }
                // Fetch from database and register into the static cache. Further requests will lookup from cache.
                dataSet = _lookupDataSet(sqlDef, null);
                staticDataSetProvider.registerDataSet(dataSet);
                return staticDataSetProvider.lookupDataSet(def, lookup);
            }
        }

        // If cache is disabled always fetch from database.
        return _lookupDataSet(sqlDef, lookup);
    }

    public boolean isDataSetOutdated(DataSetDef def) {
        try {
            // If cache is disabled then no way for a data set to get outdated
            SQLDataSetDef sqlDef = (SQLDataSetDef) def;
            if (!sqlDef.isCacheEnabled()) return false;

            // ... for non cached data sets either.
            DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);
            if (dataSet == null) return false;

            // Compare the cached vs database data set sizes.
            int rows = _getRowCount(sqlDef);
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

    // Listen to changes on the data set definition registry

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);

        DataSetDef oldDef = event.getOldDataSetDef();
        if (DataSetProviderType.SQL.equals(oldDef.getProvider())) {
            String uuid = event.getOldDataSetDef().getUUID();
            staticDataSetProvider.removeDataSet(uuid);
        }
    }

    // Internal implementation logic

    Map<String,DataSetMetadata> _metadataMap = new HashMap<String,DataSetMetadata>();

    protected DataSetMetadata _getDataSetMetadata(SQLDataSetDef def, Connection conn) throws Exception {
        DataSetMetadata result = _metadataMap.get(def.getUUID());
        if (result != null) return result;

        int estimatedSize = 0;
        int rowCount = _getRowCount(def);
        List<String> columnIds = new ArrayList<String>();
        List<ColumnType> columnTypes = new ArrayList<ColumnType>();

        boolean tableFound = false;
        List<Table<?>> _jooqTables = using(conn).meta().getTables();
        for (Table<?> _jooqTable : _jooqTables) {

            if (_jooqTable.getName().toLowerCase().equals(def.getDbTable().toLowerCase())) {
                tableFound = true;
                Result oneRow = using(conn).selectFrom(_jooqTable).limit(1).offset(0).fetch();
                for (Field _jooqField : oneRow.fields()) {

                    columnIds.add(_jooqField.getName());
                    columnTypes.add(_calculateType(def, _jooqField));
                    DataType _jooqType = _jooqField.getDataType().getSQLDataType();
                    estimatedSize += 100 * rowCount;
                }
            }
        }
        if (!tableFound) {
            throw new IllegalArgumentException("Table '" + def.getDbTable() + "'not found " +
                    "in database '" + def.getDataSource()+ "'");
        }
        _metadataMap.put(def.getUUID(), result =
                new DataSetMetadataImpl(def, def.getUUID(), rowCount,
                        columnIds.size(), columnIds, columnTypes, estimatedSize));
        return result;
    }

    protected int _getRowCount(SQLDataSetDef def) throws Exception {
        DataSource ds = dataSourceLocator.lookup(def);
        Connection conn = ds.getConnection();
        try {
            return using(conn).fetchCount(table(def.getDbTable()));
        } finally {
            conn.close();
        }
    }

    protected DataSet _lookupDataSet(SQLDataSetDef def, DataSetLookup lookup) throws Exception {
        DataSource ds = dataSourceLocator.lookup(def);
        Connection conn = ds.getConnection();
        try {
            if (lookup == null || lookup.getOperationList().isEmpty()) {

                // Data set with row limit
                if (lookup != null && lookup.getNumberOfRows() > 0) {

                    return _buildDataSet(def, null, using(conn)
                            .selectFrom(table(def.getDbTable()))
                            .limit(lookup.getNumberOfRows())
                            .offset(lookup.getRowOffset())
                            .fetch());
                }
                // The whole data set
                return _buildDataSet(def, null, using(conn)
                        .selectFrom(table(def.getDbTable()))
                        .fetch());
            } else {
                // Group operation
                DataSetMetadata metadata = _getDataSetMetadata(def, conn);
                DataSetGroup groupOp = null;
                int groupIdx = lookup.getFirstGroupOpIndex(0, null, false);
                if (groupIdx != -1) groupOp = lookup.getOperation(groupIdx);

                // Prepare the jOOQ query
                SelectJoinStep _jooqQuery = using(conn)
                        .select(_createJooqFields(metadata, groupOp))
                        .from(table(def.getDbTable()));

                // Append the filter clauses (if any)
                // TODO: append group interval selection filters
                DataSetFilter filterOp = lookup.getFirstFilterOp();
                if (filterOp != null) _appendJooqFilterBy(metadata, filterOp, _jooqQuery);

                // ... the sort clauses (if any)
                DataSetSort sortOp = lookup.getFirstSortOp();
                if (sortOp != null) _appendJooqOrderBy(metadata, sortOp, _jooqQuery);

                // ... the group by clauses (if any)
                if (groupOp != null) {
                    ColumnGroup cg = groupOp.getColumnGroup();
                    if (cg != null) _appendJooqGroupBy(metadata, groupOp, _jooqQuery);
                }

                // Fetch the results and build the data set
                return _buildDataSet(def, groupOp, _jooqQuery.fetch());
            }
        } finally {
            conn.close();
        }
    }

    protected SelectWhereStep _appendJooqOrderBy(DataSetMetadata metadata, DataSetSort sortOp, SelectWhereStep _jooqQuery) {
        List<SortField> _jooqFields = new ArrayList<SortField>();
        List<ColumnSort> sortList = sortOp.getColumnSortList();
        for (ColumnSort columnSort : sortList) {
            String columnId = columnSort.getColumnId();
            _assertColumnExists(metadata, columnId);

            if (SortOrder.DESCENDING.equals(columnSort.getOrder())) {
                _jooqFields.add(field(columnId).desc());
            } else {
                _jooqFields.add(field(columnId).asc());
            }
        }
        _jooqQuery.orderBy(_jooqFields);
        return _jooqQuery;
    }

    protected SelectWhereStep _appendJooqFilterBy(DataSetMetadata metadata, DataSetFilter filterOp, SelectWhereStep _jooqQuery) {
        List<ColumnFilter> filterList = filterOp.getColumnFilterList();
        for (ColumnFilter filter : filterList) {
            String filterId = filter.getColumnId();
            Field _jooqField = field(filterId);

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
                    _jooqQuery.where(_jooqField.equal(params.get(0)));
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
                // TODO: logical expr filters
                LogicalExprFilter f = (LogicalExprFilter) filter;
                LogicalExprType type = f.getLogicalOperator();
                if (LogicalExprType.AND.equals(type)) {
                }
                if (LogicalExprType.OR.equals(type)) {

                }
                if (LogicalExprType.NOT.equals(type)) {

                }
            }
        }
        return _jooqQuery;
    }

    protected SelectJoinStep _appendJooqGroupBy(DataSetMetadata metadata, DataSetGroup groupOp, SelectJoinStep _jooqQuery) {
        ColumnGroup cg = groupOp.getColumnGroup();
        String columnId = cg.getSourceId();
        ColumnType columnType = metadata.getColumnType(_assertColumnExists(metadata, columnId));

        // Group by Number => not supported
        if (ColumnType.NUMBER.equals(columnType)) {
            throw new IllegalArgumentException("Group by number '" + columnId + "' not supported");
        }
        // Group by Date
        if (ColumnType.DATE.equals(columnType)) {
            DateIntervalType type = DateIntervalType.getByName(cg.getIntervalSize());

            // Fixed date intervals
            if (GroupStrategy.FIXED.equals(cg.getStrategy())) {
                if (DateIntervalType.MONTH.equals(type)) {
                    _jooqQuery.groupBy(month(field(columnId, SQLDataType.DATE)));
                }
                return _jooqQuery;
            }
            // Dynamic strategy
            _jooqQuery.groupBy(month(field(columnId, SQLDataType.DATE)));
            return _jooqQuery;
        }
        // Group by Label
        _jooqQuery.groupBy(field(columnId));
        return _jooqQuery;
    }

    protected DataSet _buildDataSet(SQLDataSetDef def, DataSetGroup groupOp, Result _jooqRs) {
        DataSet dataSet = DataSetFactory.newEmptyDataSet();

        Field[] rsFields = _jooqRs.fields();
        for (int i = 0; i < rsFields.length; i++) {
            Field f = rsFields[i];

            String columnId = f.getName();
            String columnName = columnId;
            ColumnType columnType = ColumnType.TEXT;

            DataColumn column = def.getDataSet().getColumnById(columnId);
            if (column != null) {
                columnName = column.getName();
                columnType = column.getColumnType();
            }

            if (groupOp != null) {
                columnId = groupOp.getGroupFunctions().get(i).getSourceId();
                columnName = groupOp.getGroupFunctions().get(i).getColumnId();
                if (columnId == null) columnId = columnName;
            }

            List values = _jooqRs.getValues(f);
            if (!values.isEmpty()) {
                if (values.get(0) instanceof Number) {
                    columnType = ColumnType.NUMBER;

                    values = new ArrayList();
                    for (Object _jooqVal : _jooqRs.getValues(f)) {
                        values.add(((Number) _jooqVal).doubleValue());
                    }
                }
                else if (values.get(0) instanceof Date) {
                    columnType = ColumnType.DATE;
                }
            }
            dataSet.addColumn(columnId, columnName, columnType, values);
        }
        return dataSet;
    }

    protected ColumnType _calculateType(SQLDataSetDef def, Field _jooqField) {
        String columnId = _jooqField.getName();
        DataColumn column = def.getDataSet().getColumnById(columnId);
        if (column != null) return column.getColumnType();

        Class c = _jooqField.getType();
        if (Number.class.isAssignableFrom(c)) {
            return ColumnType.NUMBER;
        }
        if (java.util.Date.class.isAssignableFrom(c)) {
            return ColumnType.DATE;
        }
        return ColumnType.TEXT;
    }

    protected Collection<Field<?>> _createJooqFields(DataSetMetadata metadata) {
        Collection<Field<?>> _jooqFields = new ArrayList<Field<?>>();
        for (int i = 0; i < metadata.getNumberOfColumns(); i++) {
            String columnId = metadata.getColumnId(i);
            ColumnType columnType = metadata.getColumnType(i);
            if (ColumnType.DATE.equals(columnType)) {
                _jooqFields.add(field(columnId, java.sql.Timestamp.class));
            }
            else if (ColumnType.NUMBER.equals(columnType)) {
                _jooqFields.add(field(columnId, Double.class));
            }
            else {
                _jooqFields.add(field(columnId, String.class));
            }
        }
        return _jooqFields;
    }

    protected Collection<Field<?>> _createJooqFields(DataSetMetadata metadata, DataSetGroup gOp) {
        if (gOp == null) return _createJooqFields(metadata);

        Collection<Field<?>> _jooqFields = new ArrayList<Field<?>>();
        for (GroupFunction groupFunction : gOp.getGroupFunctions()) {
            _jooqFields.add(_createJooqField(metadata, groupFunction));
        }
        return _jooqFields;
    }

    protected Field _createJooqField(DataSetMetadata metadata, GroupFunction gf) {
        String columnId = gf.getSourceId();
        if (columnId == null) columnId = metadata.getColumnId(0);
        else _assertColumnExists(metadata, gf.getSourceId());

        AggregateFunctionType ft = gf.getFunction();
        Field _jooqField = field(columnId);
        if (ft == null) return _jooqField;

        if (AggregateFunctionType.SUM.equals(ft)) return _jooqField.sum();
        if (AggregateFunctionType.MAX.equals(ft)) return _jooqField.max();
        if (AggregateFunctionType.MIN.equals(ft)) return _jooqField.min();
        if (AggregateFunctionType.AVERAGE.equals(ft)) return _jooqField.avg();
        if (AggregateFunctionType.DISTINCT.equals(ft)) return _jooqField.countDistinct();
        if (AggregateFunctionType.COUNT.equals(ft)) return _jooqField.count();
        return _jooqField;
    }

    protected int _assertColumnExists(DataSetMetadata metadata, String columnId) {
        for (int i = 0; i < metadata.getNumberOfColumns(); i++) {
            if (metadata.getColumnId(i).equals(columnId)) {
                return i;
            }
        }
        throw new RuntimeException("Column '" + columnId + "' not found in data set: " + metadata.getUUID());
    }
}
