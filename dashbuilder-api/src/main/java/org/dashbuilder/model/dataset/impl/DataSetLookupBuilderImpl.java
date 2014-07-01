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
package org.dashbuilder.model.dataset.impl;

import java.util.List;

import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.filter.DataSetFilter;
import org.dashbuilder.model.dataset.filter.ColumnFilter;
import org.dashbuilder.model.dataset.group.AggregateFunctionType;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.ColumnGroup;
import org.dashbuilder.model.dataset.group.GroupFunction;
import org.dashbuilder.model.dataset.group.GroupStrategy;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.model.dataset.sort.ColumnSort;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.date.DayOfWeek;
import org.dashbuilder.model.date.Month;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataSetLookupBuilderImpl implements DataSetLookupBuilder {

    private DataSetLookup dataSetLookup = new DataSetLookup();

    public DataSetLookupBuilderImpl() {
    }

    private DataSetOp getCurrentOp() {
        List<DataSetOp> dataSetOps = dataSetLookup.getOperationList();
        if (dataSetOps.isEmpty()) return null;
        return dataSetOps.get(dataSetOps.size()-1);
    }

    public DataSetLookupBuilder dataset(String uuid) {
        dataSetLookup.setDataSetUUID(uuid);
        return this;
    }

    public DataSetLookupBuilder rowOffset(int offset) {
        dataSetLookup.setRowOffset(offset);
        return this;
    }

    public DataSetLookupBuilder rowNumber(int rows) {
        dataSetLookup.setNumberOfRows(rows);
        return this;
    }

    public DataSetLookupBuilder group(String columnId) {
        return group(columnId, columnId, GroupStrategy.DYNAMIC);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId) {
        return group(columnId, newColumnId, GroupStrategy.DYNAMIC);
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy) {
        return group(columnId, columnId, strategy, -1, (String) null);
    }

    public DataSetLookupBuilder group(String columnId, DateIntervalType intervalSize) {
        return group(columnId, -1, intervalSize.toString());
    }

    public DataSetLookupBuilder group(String columnId, int maxIntervals, DateIntervalType intervalSize) {
        return group(columnId, maxIntervals, intervalSize.toString());
    }

    public DataSetLookupBuilder group(String columnId, int maxIntervals, String intervalSize) {
        return group(columnId, columnId, GroupStrategy.DYNAMIC, maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, String strategy, int maxIntervals, DateIntervalType intervalSize) {
        return group(columnId, columnId, GroupStrategy.getByName(strategy), maxIntervals, intervalSize.toString());
    }

    public DataSetLookupBuilder group(String columnId, String strategy, int maxIntervals, String intervalSize) {
        return group(columnId, columnId, GroupStrategy.getByName(strategy), maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy, String intervalSize) {
        return group(columnId, columnId, strategy, 0, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy, DateIntervalType intervalSize) {
        return group(columnId, columnId, strategy, 0, intervalSize.toString());
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        return group(columnId, columnId, strategy, maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, DateIntervalType intervalSize) {
        return group(columnId, newColumnId, GroupStrategy.DYNAMIC, 0, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, String strategy) {
        return group(columnId, newColumnId, GroupStrategy.getByName(strategy));
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, GroupStrategy strategy) {
        return group(columnId, newColumnId, strategy, 15, (String) null);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, String strategy, int maxIntervals, String intervalSize) {
        return group(columnId, newColumnId, GroupStrategy.getByName(strategy), maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, DateIntervalType intervalSize) {
        return group(columnId, newColumnId, strategy, maxIntervals, intervalSize.toString());
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        DataSetGroup gOp = new DataSetGroup();
        gOp.setColumnGroup(new ColumnGroup(columnId, newColumnId, strategy, maxIntervals, intervalSize));
        dataSetLookup.addOperation(gOp);
        return this;
    }

    public DataSetLookupBuilder fixed(DateIntervalType type) {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        if (gOp == null || gOp.getColumnGroup() == null) {
            throw new RuntimeException("group() must be called first.");
        }

        ColumnGroup columnGroup = gOp.getColumnGroup();
        columnGroup.setStrategy( GroupStrategy.FIXED );
        columnGroup.setIntervalSize( type.toString() );
        return this;
    }

    public DataSetLookupBuilder asc() {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        if (gOp == null || gOp.getColumnGroup() == null) {
            throw new RuntimeException("group() must be called first.");
        }

        ColumnGroup columnGroup = gOp.getColumnGroup();
        columnGroup.setAscendingOrder( true );
        return this;
    }

    public DataSetLookupBuilder desc() {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        if (gOp == null || gOp.getColumnGroup() == null) {
            throw new RuntimeException("group() must be called first.");
        }

        ColumnGroup columnGroup = gOp.getColumnGroup();
        columnGroup.setAscendingOrder( false );
        return this;
    }

    public DataSetLookupBuilder firstDay(DayOfWeek dayOfWeek) {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        if (gOp == null || gOp.getColumnGroup() == null) {
            throw new RuntimeException("group() must be called first.");
        }

        ColumnGroup columnGroup = gOp.getColumnGroup();
        if (!GroupStrategy.FIXED.equals(columnGroup.getStrategy())) {
            throw new RuntimeException("A fixed group is required.");
        }
        if (!DateIntervalType.DAY_OF_WEEK.equals(DateIntervalType.getByName(columnGroup.getIntervalSize()))) {
            throw new RuntimeException("A fixed DAY_OF_WEEK date group is required.");
        }
        columnGroup.setFirstDayOfWeek(dayOfWeek);
        return this;
    }

    public DataSetLookupBuilder firstMonth(Month month) {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        if (gOp == null || gOp.getColumnGroup() == null) {
            throw new RuntimeException("group() must be called first.");
        }

        ColumnGroup columnGroup = gOp.getColumnGroup();
        if (!GroupStrategy.FIXED.equals(columnGroup.getStrategy())) {
            throw new RuntimeException("A fixed group is required.");
        }
        if (!DateIntervalType.MONTH.equals(DateIntervalType.getByName(columnGroup.getIntervalSize()))) {
            throw new RuntimeException("A fixed MONTH date group is required.");
        }
        columnGroup.setFirstMonthOfYear(month);
        return this;
    }

    public DataSetLookupBuilder distinct(String columnId) {
        return function(columnId, columnId, AggregateFunctionType.DISTICNT);
    }

    public DataSetLookupBuilder distinct(String columnId, String newColumnId) {
        return function(columnId, newColumnId, AggregateFunctionType.DISTICNT);
    }

    public DataSetLookupBuilder count(String newColumnId) {
        return function(null, newColumnId, AggregateFunctionType.COUNT);
    }

    public DataSetLookupBuilder min(String columnId) {
        return function(columnId, columnId, AggregateFunctionType.MIN);
    }

    public DataSetLookupBuilder min(String columnId, String newColumnId) {
        return function(columnId, newColumnId, AggregateFunctionType.MIN);
    }

    public DataSetLookupBuilder max(String columnId) {
        return function(columnId, columnId, AggregateFunctionType.MAX);
    }

    public DataSetLookupBuilder max(String columnId, String newColumnId) {
        return function(columnId, newColumnId, AggregateFunctionType.MAX);
    }

    public DataSetLookupBuilder avg(String columnId) {
        return function(columnId, columnId, AggregateFunctionType.AVERAGE);
    }

    public DataSetLookupBuilder avg(String columnId, String newColumnId) {
        return function(columnId, newColumnId, AggregateFunctionType.AVERAGE);
    }

    public DataSetLookupBuilder sum(String columnId) {
        return function(columnId, columnId, AggregateFunctionType.SUM);
    }

    public DataSetLookupBuilder sum(String columnId, String newColumnId) {
        return function(columnId, newColumnId, AggregateFunctionType.SUM);
    }

    protected DataSetLookupBuilder function(String columnId, String newColumnId, AggregateFunctionType function) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetGroup)) {
            dataSetLookup.addOperation(new DataSetGroup());
        }
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        gOp.addGroupFunction(new GroupFunction(columnId, newColumnId, function));
        return this;
    }

    public DataSetLookupBuilder select(String... intervalNames) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetGroup)) {
            dataSetLookup.addOperation(new DataSetGroup());
        }
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        gOp.addSelectedIntervalNames(intervalNames);
        return this;
    }

    public DataSetLookupBuilder filter(ColumnFilter... filters) {
        return filter(null, filters);
    }

    public DataSetLookupBuilder filter(String columnId, ColumnFilter... filters) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetFilter)) {
            dataSetLookup.addOperation(new DataSetFilter());
        }
        DataSetFilter fOp = (DataSetFilter) getCurrentOp();
        for (ColumnFilter filter : filters) {
            if (columnId != null) filter.setColumnId(columnId);
            fOp.addFilterColumn(filter);
        }
        return this;
    }

    public DataSetLookupBuilder sort(String columnId, String order) {
        return sort(columnId, SortOrder.getByName(order));
    }

    public DataSetLookupBuilder sort(String columnId, SortOrder order) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetSort)) {
            dataSetLookup.addOperation(new DataSetSort());
        }
        DataSetSort sOp = (DataSetSort) getCurrentOp();
        sOp.addSortColumn(new ColumnSort(columnId, order));
        return this;
    }

    public DataSetLookup buildLookup() {
        return dataSetLookup;
    }
}
