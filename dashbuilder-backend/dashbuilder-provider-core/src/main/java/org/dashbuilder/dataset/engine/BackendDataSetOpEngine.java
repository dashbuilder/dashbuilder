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
package org.dashbuilder.dataset.engine;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.engine.DataSetHandler;
import org.dashbuilder.dataset.engine.DataSetRowSet;
import org.dashbuilder.dataset.engine.DataSetOpEngine;
import org.dashbuilder.dataset.engine.function.AggregateFunction;
import org.dashbuilder.dataset.engine.function.AggregateFunctionManager;
import org.dashbuilder.dataset.engine.group.IntervalBuilder;
import org.dashbuilder.dataset.engine.group.IntervalBuilderLocator;
import org.dashbuilder.dataset.engine.group.IntervalList;
import org.dashbuilder.dataset.engine.index.DataSetFilterIndex;
import org.dashbuilder.dataset.engine.index.DataSetGroupIndex;
import org.dashbuilder.dataset.engine.index.DataSetIndex;
import org.dashbuilder.dataset.engine.index.DataSetIndexNode;
import org.dashbuilder.dataset.engine.index.DataSetIntervalIndex;
import org.dashbuilder.dataset.engine.index.DataSetIntervalSetIndex;
import org.dashbuilder.dataset.engine.index.DataSetSortIndex;
import org.dashbuilder.dataset.engine.index.spi.DataSetIndexRegistry;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.engine.filter.DataSetFilterAlgorithm;
import org.dashbuilder.model.dataset.filter.ColumnFilter;
import org.dashbuilder.model.dataset.group.AggregateFunctionType;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.group.ColumnGroup;
import org.dashbuilder.model.dataset.group.GroupFunction;
import org.dashbuilder.model.dataset.group.GroupStrategy;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.model.dataset.sort.ColumnSort;
import org.dashbuilder.model.dataset.sort.SortedList;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.engine.sort.DataSetSortAlgorithm;
import org.slf4j.Logger;

@ApplicationScoped
public class BackendDataSetOpEngine implements DataSetOpEngine {

    @Inject protected Logger log;
    @Inject protected AggregateFunctionManager aggregateFunctionManager;
    @Inject protected IntervalBuilderLocator intervalBuilderLocator;
    @Inject protected DataSetIndexRegistry indexRegistry;
    @Inject protected DataSetSortAlgorithm sortAlgorithm;
    @Inject protected DataSetFilterAlgorithm filterAlgorithm;

    public AggregateFunctionManager getAggregateFunctionManager() {
        return aggregateFunctionManager;
    }

    public IntervalBuilderLocator getIntervalBuilderLocator() {
        return intervalBuilderLocator;
    }

    public DataSetIndexRegistry getIndexRegistry() {
        return indexRegistry;
    }

    public DataSetSortAlgorithm getSortAlgorithm() {
        return sortAlgorithm;
    }

    public DataSetFilterAlgorithm getFilterAlgorithm() {
        return filterAlgorithm;
    }

    public DataSet execute(DataSet dataSet, DataSetOp... ops) {
        List<DataSetOp> opList = new ArrayList<DataSetOp>();
        Collections.addAll(opList, ops);
        return execute(dataSet, opList);
    }

    public DataSet execute(DataSet dataSet, List<DataSetOp> opList) {
        DataSetOpListProcessor processor = new DataSetOpListProcessor();
        processor.setDataSetIndex(indexRegistry.get(dataSet.getUUID()));
        processor.setOperationList(opList);
        processor.run();
        return processor.getDataSet();
    }

    private class DataSetOpListProcessor implements Runnable {

        List<DataSetOp> operationList;
        InternalContext context;

        public void setDataSetIndex(DataSetIndex index) {
            context = new InternalContext(index);
        }

        public void setOperationList(List<DataSetOp> opList) {
            operationList = new ArrayList<DataSetOp>(opList);
        }

        /**
         * Ensure the sequence of operations to apply match the following pattern:
         * <ul>
         * <li>(0..N) Filter</li>
         * <li>(0..N) Group</li>
         * <li>(0..1) Sort</li>
         * </ul>
         * @throws IllegalArgumentException If the operation sequence is invalid.
         */
        protected void checkOpList(List<DataSetOp> opList) {
            StringBuilder out = new StringBuilder();
            for (DataSetOp op : opList) {
                if (DataSetOpType.FILTER.equals(op.getType())) out.append("F");
                if (DataSetOpType.GROUP.equals(op.getType())) out.append("G");
                if (DataSetOpType.SORT.equals(op.getType())) out.append("S");
            }
            String pattern = out.toString();
            if (!pattern.matches("F*G*S?")) {
                throw new IllegalArgumentException("Invalid operation sequence order. Valid = (0..N) FILTER > (0..N) GROUP > (0..1) SORT");
            }
        }

        public DataSet getDataSet() {
            return context.dataSet;
        }

        public void run() {
            if (context == null) {
                throw new IllegalStateException("Data set to process missing.");
            }

            checkOpList(operationList);

            boolean group = false;
            boolean calculations = false;
            boolean sort = false;
            boolean build = false;

            for (int i=0; i<operationList.size(); i++) {
                DataSetOp op = operationList.get(i);

                if (DataSetOpType.GROUP.equals(op.getType())) {
                    if (calculations) throw new IllegalStateException("Group not permitted after a simple function calculation operation.");
                    if (sort) throw new IllegalStateException("Sort operations must be applied ALWAYS AFTER GROUP.");

                    DataSetGroup gOp = (DataSetGroup) op;
                    ColumnGroup columnGroup = gOp.getColumnGroup();
                    if (columnGroup == null) {
                        // No real group requested. Only function calculations on the data set.
                        calculations = true;
                    } else {
                        group(gOp, context);
                        if (!gOp.getGroupFunctions().isEmpty()) {
                            // If the op is not an interval selection then group.
                            group = true;
                            build = false;
                        }
                    }
                }
                else if (DataSetOpType.FILTER.equals(op.getType())) {
                    if (calculations) throw new IllegalStateException("Filter not permitted after a simple function calculation operation.");
                    if (group) throw new IllegalStateException("Filter operations must be applied ALWAYS BEFORE GROUP.");
                    if (sort) throw new IllegalStateException("Sort operations must be applied ALWAYS AFTER FILTER.");

                    filter((DataSetFilter) op, context);
                    build = false;
                }
                else if (DataSetOpType.SORT.equals(op.getType())) {
                    if (calculations) throw new IllegalStateException("Sort not permitted after a function calculation operation.");
                    if (sort) throw new IllegalStateException("Sort can only be executed once.");

                    if (group) {
                        buildDataSet(context);
                    }

                    sort = true;
                    sort((DataSetSort) op, context);
                    build = false;
                }
                else {
                    throw new IllegalArgumentException("Unsupported operation: " + op.getClass().getName());
                }
                context.lastOperation = op;
            }
            if (!build) {
                buildDataSet(context);
            }
        }

        // GROUP OPERATION

        protected void checkGroupOp(DataSet dataSet, DataSetGroup op) {
            ColumnGroup cg = op.getColumnGroup();
            if (cg != null) {
                String id = cg.getSourceId();
                if (dataSet.getColumnById(id) == null) {
                    throw new IllegalArgumentException("Group column specified not found in the data set: " + id);
                }
            }
        }

        protected void group(DataSetGroup op, InternalContext context) {
            checkGroupOp(context.dataSet, op);

            // Group by the specified column (if any).
            ColumnGroup columnGroup = op.getColumnGroup();

            // No real group requested. Only function calculations on the data set.
            if (columnGroup == null) return;

            // Create a root or nested group.
            DataSetGroupIndex groupIndex = null;
            if (context.lastGroupIndex == null) groupIndex = singleGroup(op, context);
            else groupIndex = nestedGroup(op, context.lastGroupIndex, context);

            // Select the group intervals (if any)
            groupIndex = selectIntervals(op, groupIndex);

            // Index the group
            context.index(op, groupIndex);
        }

        protected DataSetGroupIndex singleGroup(DataSetGroup op, InternalContext context) {

            ColumnGroup columnGroup = op.getColumnGroup();
            DataColumn sourceColumn = context.dataSet.getColumnById(columnGroup.getSourceId());
            ColumnType columnType = sourceColumn.getColumnType();
            GroupStrategy groupStrategy = columnGroup.getStrategy();
            IntervalBuilder intervalBuilder = intervalBuilderLocator.lookup(columnType, groupStrategy);
            if (intervalBuilder == null) throw new RuntimeException("Interval generator not supported.");

            // No index => Build required
            if (context.index == null) {
                IntervalList intervalList = intervalBuilder.build(new InternalHandler(context), columnGroup);
                return new DataSetGroupIndex(columnGroup, intervalList);
            }
            // Index match => Reuse it
            DataSetGroupIndex groupIndex = context.index.getGroupIndex(columnGroup);
            if (groupIndex != null) {
                return groupIndex;
            }
            // No index match => Build required
            long _beginTime = System.nanoTime();
            IntervalList intervalList = intervalBuilder.build(new InternalHandler(context), columnGroup);
            long _buildTime = System.nanoTime() - _beginTime;

            // Index before return.
            DataSetGroupIndex index = new DataSetGroupIndex(columnGroup, intervalList);
            index.setBuildTime(_buildTime);
            return context.index.indexGroup(index);
        }

        protected DataSetGroupIndex nestedGroup(DataSetGroup op, DataSetGroupIndex lastGroupIndex, InternalContext context) {

            // Index match => Reuse it
            DataSetGroupIndex nestedGroupIndex = lastGroupIndex.getGroupIndex(op.getColumnGroup());
            if (nestedGroupIndex != null) return nestedGroupIndex;

            // No index match => Create a brand new group index
            nestedGroupIndex = new DataSetGroupIndex(op.getColumnGroup());

            // Apply the nested group operation on each parent group interval.
            InternalContext nestedContext = new InternalContext(context.dataSet, null);
            List<DataSetIntervalIndex> intervalsIdxs = lastGroupIndex.getIntervalIndexes();
            for (DataSetIntervalIndex intervalIndex : intervalsIdxs) {

                // In a nested group the intervals can aggregate other intervals.
                if (intervalIndex instanceof DataSetIntervalSetIndex) {
                    DataSetIntervalSetIndex indexSet = (DataSetIntervalSetIndex) intervalIndex;
                    for (DataSetIntervalIndex subIndex : indexSet.getIntervalIndexes()) {
                        nestedContext.index = subIndex;
                        DataSetGroupIndex sg = singleGroup(op, nestedContext);
                        nestedGroupIndex.indexIntervals(sg.getIntervalIndexes());
                    }
                }
                // Or can just be single intervals.
                else {
                    nestedContext.index = intervalIndex;
                    DataSetGroupIndex sg = singleGroup(op, nestedContext);
                    nestedGroupIndex.indexIntervals(sg.getIntervalIndexes());
                }
            }
            context.index.indexGroup(nestedGroupIndex);
            return nestedGroupIndex;
        }


        protected DataSetGroupIndex selectIntervals(DataSetGroup groupOp, DataSetGroupIndex groupIndex) {
            List<String> intervalNames = groupOp.getSelectedIntervalNames();
            if (intervalNames != null && !intervalNames.isEmpty()) {

                // Look for an existing selection index.
                DataSetGroupIndex selectionIndex = groupIndex.getSelectionIndex(intervalNames);
                if (selectionIndex != null) return selectionIndex;

                // Create a brand new selection index.
                List<DataSetIntervalIndex> intervalIdxs = groupIndex.getIntervalIndexes(intervalNames);
                if (intervalIdxs.isEmpty()) throw new IllegalStateException("Intervals not found: " + StringUtils.join(intervalNames, ","));

                //if (intervalIdxs.size() == 1) return intervalIdxs.get(0);
                return groupIndex.indexSelection(intervalNames, intervalIdxs);
            }
            return groupIndex;
        }

        // FILTER OPERATION

        protected void checkFilterOp(DataSet dataSet, DataSetFilter op) {
            for (ColumnFilter columnFilter : op.getColumnFilterList()) {
                String id = columnFilter.getColumnId();
                if (dataSet.getColumnById(id) == null) {
                    throw new IllegalArgumentException("Filter column specified not found in the data set: " + id);
                }
            }
        }

        protected void filter(DataSetFilter op, InternalContext context) {
            checkFilterOp(context.dataSet, op);

            if (context.dataSet.getRowCount() == 0) {
                return;
            }

            // Process the filter requests.
            for (ColumnFilter filter : op.getColumnFilterList()) {

                // No index => Filter required
                if (context.index == null) {
                    List<Integer> rows = filterAlgorithm.filter(new InternalHandler(context), filter);
                    context.index(op, new DataSetFilterIndex(filter, rows));
                    continue;
                }
                // Index match => Reuse it
                DataSetFilterIndex index = context.index.getFilterIndex(filter);
                if (index != null) {
                    context.index(op, index);
                    continue;
                }
                // No index match => Filter required
                long _beginTime = System.nanoTime();
                List<Integer> rows = filterAlgorithm.filter(new InternalHandler(context), filter);
                long _buildTime = System.nanoTime() - _beginTime;

                // Index before continue.
                context.index(op, context.index.indexFilter(filter, rows, _buildTime));
            }
        }

        // SORT OPERATION

        protected void checkSortOp(DataSet dataSet, DataSetSort op) {
            for (ColumnSort columnSort : op.getColumnSortList()) {
                String id = columnSort.getColumnId();
                if (dataSet.getColumnById(id) == null) {
                    throw new IllegalArgumentException("Sort column specified not found in the data set: " + id);
                }
            }
        }

        protected void sort(DataSetSort op, InternalContext context) {
            if (context.dataSet.getRowCount() < 2) {
                return;
            }

            // No index => Sort required
            if (context.index == null) {
                List<Integer> orderedRows = sortAlgorithm.sort(new InternalHandler(context), op.getColumnSortList());
                context.index(op, new DataSetSortIndex(op, orderedRows));
                return;

            }
            // Index match => Reuse it
            DataSetSortIndex sortIndex = context.index.getSortIndex(op);
            if (sortIndex != null) {
                context.index(op, sortIndex);
                return;
            }
            // No index match => Sort required
            long _beginTime = System.nanoTime();
            List<Integer> orderedRows = sortAlgorithm.sort(new InternalHandler(context), op.getColumnSortList());
            long _buildTime = System.nanoTime() - _beginTime;

            // Index before return.
            context.index(op, context.index.indexSort(op, orderedRows, _buildTime));
        }

        // DATASET BUILD

        public DataSet buildDataSet(InternalContext context) {
            if (context.index == null) {
                // If no index exists then just return the data set from context
                return context.dataSet;
            }
            DataSet result = _buildDataSet(context);
            context.dataSet = result;
            context.index = null;
            return result;
        }

        private DataSet _buildDataSet(InternalContext context) {
            DataSetOp lastOp = context.lastOperation;
            DataSetIndexNode index = context.index;
            DataSet dataSet = context.dataSet;

            if (lastOp instanceof DataSetGroup) {
                DataSetGroup gOp = (DataSetGroup) lastOp;
                ColumnGroup columnGroup = gOp.getColumnGroup();
                if (columnGroup == null) {
                    return _buildDataSet(context, gOp.getGroupFunctions());
                } else {
                    if (!gOp.getSelectedIntervalNames().isEmpty() && gOp.getGroupFunctions().isEmpty()) {
                        return dataSet.trim(index.getRows());
                    } else {
                        return _buildDataSet(context, gOp);
                    }
                }
            }
            if (lastOp instanceof DataSetFilter) {
                return dataSet.trim(index.getRows());
            }
            if (lastOp instanceof DataSetSort) {
                DataSetImpl sortedDataSet = new DataSetImpl();
                for (DataColumn column : dataSet.getColumns()) {
                    SortedList sortedValues = new SortedList(column.getValues(), index.getRows());
                    sortedDataSet.addColumn(column.getId(), column.getColumnType(), sortedValues);
                }
                return sortedDataSet;
            }
            return dataSet;
        }

        private DataSet _buildDataSet(InternalContext context, DataSetGroup op) {
            DataSetGroupIndex index = context.lastGroupIndex;
            DataSet dataSet = context.dataSet;

            ColumnGroup columnGroup = op.getColumnGroup();
            List<GroupFunction> groupFunctions = op.getGroupFunctions();

            // Data set header.
            DataSet result = DataSetFactory.newDataSet();
            result.addColumn(columnGroup.getColumnId(), ColumnType.LABEL);
            for (GroupFunction groupFunction : op.getGroupFunctions()) {
                result.addColumn(groupFunction.getColumnId(), ColumnType.NUMBER);
            }
            // Add the aggregate calculations to the result.
            List<DataSetIntervalIndex> intervalIdxs = index.getIntervalIndexes();
            for (int i=0; i<intervalIdxs.size(); i++) {
                DataSetIntervalIndex intervalIdx = intervalIdxs.get(i);
                result.setValueAt(i, 0, intervalIdx.getName());

                // Add the aggregate calculations.
                for (int j=0; j< groupFunctions.size(); j++) {
                    GroupFunction groupFunction = groupFunctions.get(j);
                    DataColumn dataColumn = dataSet.getColumnById(groupFunction.getSourceId());
                    if (dataColumn == null) dataColumn = dataSet.getColumnByIndex(0);

                    Double aggValue = _calculateFunction(dataColumn, groupFunction.getFunction(), intervalIdx);
                    result.setValueAt(i, j + 1, aggValue);
                }
            }
            return result;
        }

        private DataSet _buildDataSet(InternalContext context, List<GroupFunction> groupFunctions) {
            DataSetIndexNode index = context.index;
            DataSet dataSet = context.dataSet;

            DataSet result= DataSetFactory.newDataSet();

            for (int i=0; i< groupFunctions.size(); i++) {
                GroupFunction groupFunction = groupFunctions.get(i);
                result.addColumn(groupFunction.getColumnId(), ColumnType.NUMBER);

                DataColumn dataColumn = dataSet.getColumnById(groupFunction.getSourceId());
                if (dataColumn == null) dataColumn = dataSet.getColumnByIndex(0);

                Double aggValue = _calculateFunction(dataColumn, groupFunction.getFunction(), index);
                result.setValueAt(0, i, aggValue);
            }
            return result;
        }

        private Double _calculateFunction(DataColumn column, AggregateFunctionType type, DataSetIndexNode index) {
            // Look into the index first
            if (index != null) {
                Double sv = index.getAggValue(column.getId(), type);
                if (sv != null) return sv;
            }
            // Do the aggregate calculations.
            long _beginTime = System.nanoTime();
            AggregateFunction function = aggregateFunctionManager.getFunctionByCode(type.toString().toLowerCase());
            double aggValue = function.aggregate(column.getValues(), index.getRows());
            long _buildTime = System.nanoTime() - _beginTime;

            // Index the result
            if (index != null) {
                index.indexAggValue(column.getId(), type, aggValue, _buildTime);
            }
            return aggValue;
        }

        class InternalContext implements DataSetRowSet {

            DataSet dataSet = null;
            DataSetIndexNode index = null;
            DataSetOp lastOperation = null;

            DataSetGroup lastGroupOp = null;
            DataSetGroupIndex lastGroupIndex = null;
            DataSetFilter lastFilterOp = null;
            DataSetFilterIndex lastFilterIndex = null;
            DataSetSort lastSortOp = null;
            DataSetSortIndex lastSortIndex = null;

            InternalContext(DataSetIndex index) {
                this(index.getDataSet(), index);
            }
            InternalContext(DataSet dataSet, DataSetIndexNode index) {
                this.dataSet = dataSet;
                this.index = index;
            }
            public DataSet getDataSet() {
                return dataSet;
            }
            public List<Integer> getRows() {
                if (index == null) return null;
                return index.getRows();
            }
            public void index(DataSetGroup op, DataSetGroupIndex gi) {
                index = lastGroupIndex = gi;
                lastOperation = lastGroupOp = op;
            }
            public void index(DataSetFilter op, DataSetFilterIndex i) {
                index = lastFilterIndex = i;
                lastOperation = lastFilterOp = op;
            }
            public void index(DataSetSort op, DataSetSortIndex i) {
                index = lastSortIndex = i;
                lastOperation = lastSortOp = op;
            }
        }

        class InternalHandler extends InternalContext implements DataSetHandler {

            InternalHandler(InternalContext context) {
                super(context.dataSet, context.index);
            }
            public DataSetHandler group(DataSetGroup op) {
                DataSetOpListProcessor.this.group(op, this);
                return this;
            }
            public DataSetHandler filter(DataSetFilter op) {
                DataSetOpListProcessor.this.filter(op, this);
                return this;
            }
            public DataSetHandler sort(DataSetSort op) {
                DataSetOpListProcessor.this.sort(op, this);
                return this;
            }
        }
    }
}