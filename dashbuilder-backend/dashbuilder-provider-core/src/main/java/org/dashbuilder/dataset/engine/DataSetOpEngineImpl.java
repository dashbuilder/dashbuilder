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
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.DataSetServices;
import org.dashbuilder.dataset.function.AggregateFunction;
import org.dashbuilder.dataset.function.AggregateFunctionManager;
import org.dashbuilder.dataset.group.IntervalBuilder;
import org.dashbuilder.dataset.group.IntervalBuilderLocator;
import org.dashbuilder.dataset.group.IntervalList;
import org.dashbuilder.dataset.index.DataSetGroupIndex;
import org.dashbuilder.dataset.index.DataSetIndex;
import org.dashbuilder.dataset.index.DataSetIndexNode;
import org.dashbuilder.dataset.index.DataSetIntervalIndex;
import org.dashbuilder.dataset.index.DataSetSortIndex;
import org.dashbuilder.dataset.index.spi.DataSetIndexRegistry;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.filter.DataSetFilter;
import org.dashbuilder.model.dataset.group.AggregateFunctionType;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.group.GroupColumn;
import org.dashbuilder.model.dataset.group.GroupFunction;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.model.dataset.sort.SortedList;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.model.dataset.sort.DataSetSortAlgorithm;
import org.slf4j.Logger;

@ApplicationScoped
public class DataSetOpEngineImpl implements DataSetOpEngine {

    @Inject Logger log;
    @Inject AggregateFunctionManager aggregateFunctionManager;
    @Inject IntervalBuilderLocator intervalBuilderLocator;
    @Inject DataSetServices dataSetServices;

    public DataSetOpResults execute(DataSet dataSet, DataSetOp... ops) {
        List<DataSetOp> opList = new ArrayList<DataSetOp>();
        Collections.addAll(opList, ops);
        return execute(dataSet, opList);
    }

    public DataSetOpResults execute(DataSet dataSet, List<DataSetOp> opList) {
        DataSetIndexRegistry indexRegistry = dataSetServices.getDataSetIndexRegistry();

        DataSetOpRunner runner = new DataSetOpRunner();
        runner.setDataSetIndex(indexRegistry.get(dataSet.getUUID()));
        runner.setOperationList(opList);
        runner.run();
        return runner;
    }

    private class DataSetOpRunner implements Runnable, DataSetOpResults {

        DataSet currentDataSet = null;
        DataSetIndexNode currentIndex = null;

        List<DataSetOp> operationList;

        DataSetGroup lastGroupOp = null;
        DataSetGroupIndex lastGroupIndex = null;

        DataSetSort lastSortOp = null;
        DataSetSortIndex lastSortIndex = null;

        public void setDataSetIndex(DataSetIndex index) {
            currentIndex = index;
            currentDataSet = index.getDataSet();
        }

        public void setOperationList(List<DataSetOp> opList) {
            operationList = new ArrayList<DataSetOp>(opList);
            lastGroupOp = null;
            lastGroupIndex = null;
        }

        public DataSet getDataSet() {
            return currentDataSet;
        }

        public DataSetIndexNode getIndex() {
            return currentIndex;
        }

        public DataSetGroup getLastGroupOp() {
            return lastGroupOp;
        }

        public DataSetGroupIndex getLastGroupIndex() {
            return lastGroupIndex;
        }

        public DataSetSort getLastSortOp() {
            return lastSortOp;
        }

        public DataSetSortIndex getLastSortIndex() {
            return lastSortIndex;
        }

        public void run() {
            Iterator<DataSetOp> opIt = operationList.iterator();
            while (opIt.hasNext()) {
                DataSetOp op = opIt.next();
                opIt.remove();

                if (DataSetOpType.GROUP.equals(op.getType())) {
                    currentDataSet = group((DataSetGroup) op);
                }
                else if (DataSetOpType.FILTER.equals(op.getType())) {
                    currentDataSet = filter((DataSetFilter) op);
                }
                else if (DataSetOpType.SORT.equals(op.getType())) {
                    currentDataSet = sort((DataSetSort) op);
                }
                else {
                    throw new IllegalArgumentException("Unsupported operation: " + op.getClass().getName());
                }
            }
        }

        // GROUP OPERATION

        public DataSet group(DataSetGroup op) {
            if (lastGroupOp != null) {
                throw new RuntimeException("Nested groups not supported yet.");
            } else {
                lastGroupOp = op;
            }

            // Group by the specified column (if any).
            GroupColumn groupColumn = op.getGroupColumn();
            if (groupColumn != null) {
                currentIndex = lastGroupIndex = _getGroupIndex(groupColumn);
                return _buildDataSet(op);
            }

            // No real group requested. Only function calculations on the data set.
            return _buildDataSet(op.getGroupFunctions());
        }

        private DataSetGroupIndex _getGroupIndex(GroupColumn groupColumn) {
            DataColumn sourceColumn = currentDataSet.getColumnById(groupColumn.getSourceId());
            IntervalBuilder intervalBuilder = intervalBuilderLocator.lookup(sourceColumn, groupColumn.getStrategy());
            if (intervalBuilder == null) throw new RuntimeException("Interval generator not supported.");

            // No index => Build required
            if (currentIndex == null) {
                IntervalList intervalList = intervalBuilder.build(sourceColumn, groupColumn);
                return new DataSetGroupIndex(groupColumn, intervalList);
            }
            // Index match => Reuse it
            DataSetGroupIndex groupIndex = currentIndex.getGroupIndex(groupColumn);
            if (groupIndex != null) {
                return groupIndex;
            }
            // No index match => Build required
            long _beginTime = System.nanoTime();
            IntervalList intervalList = intervalBuilder.build(sourceColumn, groupColumn);
            long _buildTime = System.nanoTime() - _beginTime;

            // Index before return.
            return currentIndex.indexGroup(groupColumn, intervalList, _buildTime);
        }

        private DataSet _buildDataSet(DataSetGroup op) {
            GroupColumn groupColumn = op.getGroupColumn();
            List<GroupFunction> groupFunctions = op.getGroupFunctions();

            // Data set header.
            DataSet result = DataSetFactory.newDataSet();
            result.addColumn(groupColumn.getColumnId(), ColumnType.LABEL);
            for (GroupFunction groupFunction : op.getGroupFunctions()) {
                result.addColumn(groupFunction.getColumnId(), ColumnType.NUMBER);
            }
            // Add the aggregate calculations to the result.
            List<DataSetIntervalIndex> intervalIdxs = lastGroupIndex.getIntervalIndexes();
            for (int i=0; i<intervalIdxs.size(); i++) {
                DataSetIntervalIndex intervalIdx = intervalIdxs.get(i);
                result.setValueAt(i, 0, intervalIdx.getName());

                // Add the aggregate calculations.
                for (int j=0; j< groupFunctions.size(); j++) {
                    GroupFunction groupFunction = groupFunctions.get(j);
                    DataColumn dataColumn = currentDataSet.getColumnById(groupFunction.getSourceId());
                    if (dataColumn == null) dataColumn = currentDataSet.getColumnByIndex(0);

                    Double aggValue = _calculateFunction(dataColumn, groupFunction.getFunction(), intervalIdx.getRows(), intervalIdx);
                    result.setValueAt(i, j + 1, aggValue);
                }
            }
            return result;
        }

        private DataSet _buildDataSet(List<GroupFunction> groupFunctions) {
            DataSet result= DataSetFactory.newDataSet();

            for (int i=0; i< groupFunctions.size(); i++) {
                GroupFunction groupFunction = groupFunctions.get(i);
                result.addColumn(groupFunction.getColumnId(), ColumnType.NUMBER);

                DataColumn dataColumn = currentDataSet.getColumnById(groupFunction.getSourceId());
                if (dataColumn == null) dataColumn = currentDataSet.getColumnByIndex(0);

                Double aggValue = _calculateFunction(dataColumn, groupFunction.getFunction(), null, currentIndex);
                result.setValueAt(0, i, aggValue);
            }
            return result;
        }

        private Double _calculateFunction(DataColumn column, AggregateFunctionType type, List<Integer> rows, DataSetIndexNode index) {
            // Look into the index first
            if (index != null) {
                Double sv = index.getAggValue(column.getId(), type);
                if (sv != null) return sv;
            }
            // Do the aggregate calculations.
            long _beginTime = System.nanoTime();
            AggregateFunction function = aggregateFunctionManager.getFunctionByCode(type.toString().toLowerCase());
            double aggValue = function.aggregate(column.getValues(), rows);
            long _buildTime = System.nanoTime() - _beginTime;

            // Index the result
            if (index != null) {
                index.indexAggValue(column.getId(), type, aggValue, _buildTime);
            }
            return aggValue;
        }

        // FILTER OPERATION

        public DataSet filter(DataSetFilter op) {
            if (currentDataSet.getRowCount() == 0) {
                return currentDataSet;
            }
            return null;
        }

        // SORT OPERATION

        public DataSet sort(DataSetSort op) {
            lastSortOp = op;
            if (currentDataSet.getRowCount() < 2) {
                return currentDataSet;
            }

            // Get the sort index.
            DataSetSortIndex sortIndex = _getSortIndex(op);
            currentIndex = lastSortIndex = sortIndex;

            // Create a sorted data set
            DataSetImpl sortedDataSet = new DataSetImpl();
            for (DataColumn column : currentDataSet.getColumns()) {
                SortedList sortedValues = new SortedList(column.getValues(), sortIndex.getRows());
                sortedDataSet.addColumn(column.getId(), column.getColumnType(), sortedValues);
            }
            return currentDataSet = sortedDataSet;
        }

        protected DataSetSortIndex _getSortIndex(DataSetSort op) {

            // No index => Sort required
            if (currentIndex == null) {
                DataSetSortAlgorithm sortAlgorithm = dataSetServices.getDataSetSortAlgorithm();
                List<Integer> orderedRows = sortAlgorithm.sort(currentDataSet, op.getSortColumnList());
                return new DataSetSortIndex(op, orderedRows);
            }
            // Index match => Reuse it
            DataSetSortIndex sortIndex = currentIndex.getSortIndex(op);
            if (sortIndex != null) {
                return sortIndex;
            }
            // No index match => Build required
            long _beginTime = System.nanoTime();
            DataSetSortAlgorithm sortAlgorithm = dataSetServices.getDataSetSortAlgorithm();
            List<Integer> orderedRows = sortAlgorithm.sort(currentDataSet, op.getSortColumnList());
            long _buildTime = System.nanoTime() - _beginTime;

            // Index before return.
            return currentIndex.indexSort(op, orderedRows, _buildTime);
        }
    }
}