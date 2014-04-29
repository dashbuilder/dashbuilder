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
package org.dashbuilder.storage.memory;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.comparator.ComparatorUtils;
import org.dashbuilder.config.Config;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.DataSetStats;
import org.dashbuilder.model.dataset.group.GroupColumn;
import org.dashbuilder.model.dataset.group.FunctionColumn;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.storage.memory.group.Interval;
import org.dashbuilder.storage.memory.group.IntervalBuilder;
import org.dashbuilder.storage.memory.group.IntervalBuilderLocator;
import org.dashbuilder.storage.memory.group.IntervalList;
import org.dashbuilder.storage.spi.DataSetStorage;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.filter.DataSetFilter;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.uuid.UUIDGenerator;
import org.uberfire.commons.services.cdi.Startup;

/**
 * An in-memory, no persistence support (transient) data set storage.
 */
@ApplicationScoped
@Startup
public class TransientDataSetStorage implements DataSetStorage {

    /**
     * Flag indicating whether to save the data sets obtained on apply operations.
     */
    @Inject @Config("true")
    protected boolean putOnApply;

    @Inject
    protected UUIDGenerator uuidGenerator;

    @Inject
    protected IntervalBuilderLocator intervalBuilderLocator;

    @Inject
    protected SizeEstimator sizeEstimator;

    /**
     * The in-memory data set cache.
     */
    protected Map<String,DataSetHolder> dataSetCache = new HashMap<String, DataSetHolder>();

    public void put(DataSet source) throws Exception {
        DataSetHolder entry = new DataSetHolder(source);
        dataSetCache.put(source.getUUID(), entry);
    }

    public DataSet get(String uuid) throws Exception {
        DataSetHolder holder = dataSetCache.get(uuid);
        if (holder == null) return null;
        holder.reuseHits++;
        return holder.dataSet;
    }

    public DataSetStats stats(String uuid) throws Exception {
        DataSetHolder holder = dataSetCache.get(uuid);
        if (holder == null) throw new Exception("Data set not found: " + uuid);
        return new TransientDataSetStats(holder, sizeEstimator);
    }

    public void remove(String uuid) throws Exception {
    }

    public DataSet apply(String uuid, DataSetOp op) throws Exception {
        DataSetHolder holder = dataSetCache.get(uuid);
        if (holder == null) throw new Exception("Data set not found: " + uuid);
        holder.reuseHits++;

        // Check the cache first
        DataSetHolder opHolder = holder.getChildByOp(op);
        if (opHolder != null) {
            return opHolder.dataSet;
        }

        // Apply the operation
        DataSetHolder result;
        if (DataSetOpType.FILTER.equals(op.getType())) result = filter(holder, (DataSetFilter) op);
        else if (DataSetOpType.GROUP.equals(op.getType())) result = group(holder, (DataSetGroup) op);
        else if (DataSetOpType.SORT.equals(op.getType())) result = sort(holder, (DataSetSort) op);
        else throw new IllegalArgumentException("Unsupported operation: " + op.getClass().getName());

        // Link the result with the source data set.
        result.dataSet.setUUID(uuidGenerator.newUuidBase64());
        result.dataSet.setParent(holder.dataSet.getUUID());

        // Cache the resulting data set
        if (putOnApply) {
            dataSetCache.put(result.dataSet.getUUID(), result);
        }
        return result.dataSet;
    }

    public DataSetHolder filter(DataSetHolder dataSet, DataSetFilter op) throws Exception {
        return null;
    }

    /**
         Group strategy:

         The intervals generated depends directly on the domain strategy:

         - DYNAMIC - distinct values for labels, based on overall max/min + maxIntervals for date&number
         - FIXED & MULTI (ONLY date&number) - fixed number of intervals selecting an interval size.
         - TODO: CUSTOM - fixed intervals with a custom group function

         Group op result:
         - set of intervals, each interval a set of row numbers
         - each interval contains: a name, a map of column + scalar function result
         - a data set with the same rows as the number of intervals

         => Interval class is a cache of rows ref + scalar calculations
     */
    public DataSetHolder group(DataSetHolder source, DataSetGroup op) throws Exception {
        long _beginGroup = System.currentTimeMillis();
        for (GroupColumn groupColumn : op.getGroupColumns()) {

            // Get the domain intervals. Look into the cache first.
            IntervalList intervals = source.getIntervalList(groupColumn);
            if (intervals == null) {
                // Build the group intervals by applying the domain strategy specified
                long _beginIntervals = System.currentTimeMillis();
                DataColumn domainColumn = source.dataSet.getColumnById(groupColumn.getSourceId());
                IntervalBuilder intervalBuilder = intervalBuilderLocator.lookup(domainColumn, groupColumn);
                if (intervalBuilder == null) throw new Exception("Interval generator not supported.");

                intervals = intervalBuilder.build(domainColumn, groupColumn);
                long _timeIntervals = System.currentTimeMillis() - _beginIntervals;

                // Keep the group intervals for reusing purposes
                source.setIntervalList(groupColumn, intervals, _timeIntervals);
            }

            // Build the grouped data set header.
            DataSetImpl dataSet = new DataSetImpl();
            dataSet.addColumn(groupColumn.getColumnId(), ColumnType.LABEL);
            for (FunctionColumn functionColumn : op.getFunctionColumns()) {
                dataSet.addColumn(functionColumn.getColumnId(), ColumnType.NUMBER);
            }
            // Add the scalar calculations to the result.
            for (int i=0; i<intervals.size(); i++) {
                Interval interval = intervals.get(i);
                dataSet.setValueAt(i, 0, interval.name);
                List<FunctionColumn> functionColumns = op.getFunctionColumns();
                for (int j=0; j< functionColumns.size(); j++) {
                    FunctionColumn functionColumn = functionColumns.get(j);
                    DataColumn rangeColumn = source.dataSet.getColumnById(functionColumn.getSourceId());
                    Double scalar = interval.calculateScalar(rangeColumn, functionColumn.getFunction());
                    dataSet.setValueAt(i, j + 1, scalar);
                }
            }

            // Return the results.
            long _timeGroup = System.currentTimeMillis() - _beginGroup;
            return new DataSetHolder(source, dataSet, op, _timeGroup);
        }
        return null;
    }

    public DataSetHolder sort(DataSetHolder source, DataSetSort op) throws Exception {
        return null;
    }

    /**
     * Sort the specified column values and return an ordered list of values.
     * (Null values are removed from the result)
     */
    public SortedList<ComparableValue> sortColumn(DataColumn column) {
        // Look into the cache first
        String dsUuid = column.getDataSet().getUUID();
        DataSetHolder dataSetHolder = dataSetCache.get(dsUuid);
        if (dataSetHolder != null) {
            SortedList<ComparableValue> result = dataSetHolder.getSortedList(column);
            if (result != null) return result;
        }

        long _beginSort = System.currentTimeMillis();
        int row = 0;
        int nulls = 0;
        SortedList<ComparableValue> result = new SortedList<ComparableValue>();

        // Get all the comparable values (ignore nulls)
        Iterator it = column.getValues().iterator();
        while (it.hasNext()) {
            Object value = it.next();
            if (value == null) {
                // Ignore the null values to avoid errors while sorting
                nulls++;
            } else {
                // Add the value to the comparable list.
                result.add(new ComparableValue((Comparable) value, row));
            }
            row++;
        }

        // Sort the not-null values
        Collections.sort(result, new Comparator<ComparableValue>() {
            public int compare(ComparableValue o1, ComparableValue o2) {
                return ComparatorUtils.compare(o1.value, o2.value, 1);
            }
        });

        // Keep the max/min
        result.setMin(result.get(0));
        result.setMax(result.get(result.size() - 1));

        // Append the null values to the end
        result.setNulls(nulls);
        for (row = result.size(); row < nulls; row++) {
            result.add(new ComparableValue(null, row));
        }
        // Keep the sort results for reusing purposes
        if (dataSetHolder != null) {
            long _timeSort = System.currentTimeMillis() - _beginSort;
            dataSetHolder.setSortedList(column, result, _timeSort);
        }
        return result;
    }
}
