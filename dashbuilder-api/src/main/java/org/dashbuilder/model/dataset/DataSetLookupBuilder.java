/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.model.dataset;

import java.util.List;

import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.GroupColumn;
import org.dashbuilder.model.dataset.group.GroupStrategy;
import org.dashbuilder.model.dataset.group.ScalarFunctionType;
import org.dashbuilder.model.dataset.group.FunctionColumn;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.model.date.DayOfWeek;
import org.dashbuilder.model.date.Month;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * It allows for the building of DataSetLookup instances in a friendly manner.
 *
 * <pre>
     DataSetLookup lookup = new DataSetLookupBuilder()
     .uuid("target-dataset-uuid")
     .group("department", "Department")
     .function("id", "occurrences", "Number of expenses", "count")
     .function("amount", "totalAmount", "Total amount", "sum")
     .sort("total", "asc")
     .build();
 </pre>

 */
@Portable
public class DataSetLookupBuilder {

    private DataSetLookup dataSetLookup = new DataSetLookup();

    public DataSetLookupBuilder() {
    }

    private DataSetOp getCurrentOp() {
        List<DataSetOp> dataSetOps = dataSetLookup.getOperationList();
        if (dataSetOps.isEmpty()) return null;
        return dataSetOps.get(dataSetOps.size()-1);
    }

    public DataSetLookupBuilder uuid(String uuid) {
        dataSetLookup.dataSetUUID = uuid;
        return this;
    }

    public DataSetLookupBuilder rowOffset(int offset) {
        if (offset < 0) throw new IllegalArgumentException("Offset can't be negative: " + offset);
        dataSetLookup.rowOffset = offset;
        return this;
    }

    public DataSetLookupBuilder rowNumber(int rows) {
        dataSetLookup.numberOfRows = rows;
        return this;
    }

    public DataSetLookupBuilder group(String columnId) {
        return group(columnId, columnId, GroupStrategy.DYNAMIC);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId) {
        return group(columnId, newColumnId, GroupStrategy.DYNAMIC);
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy) {
        return group(columnId, columnId, strategy, -1, null);
    }

    public DataSetLookupBuilder group(String columnId, DateIntervalType type) {
        return group(columnId, -1, type.toString());
    }

    public DataSetLookupBuilder group(String columnId, int maxIntervals, DateIntervalType type) {
        return group(columnId, maxIntervals, type.toString());
    }

    public DataSetLookupBuilder group(String columnId, int maxIntervals, String intervalSize) {
        return group(columnId, columnId, GroupStrategy.DYNAMIC, maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, String strategy, int maxIntervals, String intervalSize) {
        return group(columnId, columnId, GroupStrategy.getByName(strategy), maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy, String intervalSize) {
        return group(columnId, columnId, strategy, 0, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy, DateIntervalType type) {
        return group(columnId, columnId, strategy, 0, type.toString());
    }

    public DataSetLookupBuilder group(String columnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        return group(columnId, columnId, strategy, maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, String strategy) {
        return group(columnId, newColumnId, GroupStrategy.getByName(strategy));
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, GroupStrategy strategy) {
        return group(columnId, newColumnId, strategy, 15, null);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, String strategy, int maxIntervals, String intervalSize) {
        return group(columnId, newColumnId, GroupStrategy.getByName(strategy), maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetGroup)) {
            dataSetLookup.addOperation(new DataSetGroup());
        }
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        gOp.addGroupColumn(new GroupColumn(columnId, newColumnId, strategy, maxIntervals, intervalSize));
        return this;
    }

    public DataSetLookupBuilder fixed(DateIntervalType type) {
        return fixed(type, true);
    }

    public DataSetLookupBuilder fixed(DateIntervalType type, boolean ascending) {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        List<GroupColumn> groupColumnList = gOp.getGroupColumns();
        if (gOp == null || groupColumnList.isEmpty()) {
            throw new RuntimeException("A domain must be configured first.");
        }

        GroupColumn groupColumn = groupColumnList.get(groupColumnList.size()-1);
        groupColumn.setStrategy(GroupStrategy.FIXED);
        groupColumn.setIntervalSize(type.toString());
        groupColumn.setAscendingOrder(ascending);
        return this;
    }

    public DataSetLookupBuilder firstDay(DayOfWeek dayOfWeek) {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        List<GroupColumn> groupColumnList = gOp.getGroupColumns();
        if (gOp == null || groupColumnList.isEmpty()) {
            throw new RuntimeException("A domain must is required.");
        }
        GroupColumn groupColumn = groupColumnList.get(groupColumnList.size() - 1);
        if (!GroupStrategy.FIXED.equals(groupColumn.getStrategy())) {
            throw new RuntimeException("A fixed domain is required.");
        }
        if (!DateIntervalType.DAY_OF_WEEK.equals(DateIntervalType.getByName(groupColumn.getIntervalSize()))) {
            throw new RuntimeException("A DAY_OF_WEEK fixed date domain is required.");
        }
        groupColumn.setFirstDayOfWeek(dayOfWeek);
        return this;
    }

    public DataSetLookupBuilder firstMonth(Month month) {
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        List<GroupColumn> groupColumnList = gOp.getGroupColumns();
        if (gOp == null || groupColumnList.isEmpty()) {
            throw new RuntimeException("A domain must is required.");
        }
        GroupColumn groupColumn = groupColumnList.get(groupColumnList.size() - 1);
        if (!GroupStrategy.FIXED.equals(groupColumn.getStrategy())) {
            throw new RuntimeException("A fixed domain is required.");
        }
        if (!DateIntervalType.MONTH.equals(DateIntervalType.getByName(groupColumn.getIntervalSize()))) {
            throw new RuntimeException("A MONTH fixed date domain is required.");
        }
        groupColumn.setFirstMonthOfYear(month);
        return this;
    }

    public DataSetLookupBuilder distinct(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.DISTICNT);
    }

    public DataSetLookupBuilder distinct(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.DISTICNT);
    }

    public DataSetLookupBuilder count(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder count(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder min(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder min(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder max(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder max(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder avg(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder avg(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder sum(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder sum(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.SUM);
    }

    public DataSetLookupBuilder function(String columnId, ScalarFunctionType function) {
        return function(columnId, columnId, function);
    }

    public DataSetLookupBuilder function(String columnId, String newColumnId, ScalarFunctionType function) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetGroup)) {
            dataSetLookup.addOperation(new DataSetGroup());
        }
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        gOp.addFunctionColumn(new FunctionColumn(columnId, newColumnId, function));
        return this;
    }

    public DataSetLookupBuilder sort(String columnId, String order) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetSort)) {
            dataSetLookup.addOperation(new DataSetSort());
        }
        DataSetSort sOp = (DataSetSort) getCurrentOp();
        return this;
    }

    public DataSetLookup build() {
        return dataSetLookup;
    }
}
