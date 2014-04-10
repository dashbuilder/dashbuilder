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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.config.Config;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.group.Domain;
import org.dashbuilder.model.dataset.group.DomainStrategy;
import org.dashbuilder.model.dataset.group.Range;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.storage.memory.group.Interval;
import org.dashbuilder.storage.memory.group.IntervalBuilder;
import org.dashbuilder.storage.memory.group.IntervalBuilderLocator;
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

    @PostConstruct
    private void init() {
        DataSetImpl dataSet = new DataSetImpl();
        dataSet.setUUID("test-sample");
        dataSet.addColumn("department", ColumnType.LABEL);
        dataSet.addColumn("amount", ColumnType.NUMBER);
        dataSet.setValueAt(0, 0, "Engineering");
        dataSet.setValueAt(1, 0, "Services");
        dataSet.setValueAt(2, 0, "HR");
        dataSet.setValueAt(3, 0, "Administration");
        dataSet.setValueAt(0, 1, 13423);
        dataSet.setValueAt(1, 1, 8984.45);
        dataSet.setValueAt(2, 1, 3434.44);
        dataSet.setValueAt(3, 1, 10034.4);
        try {
            put(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Flag indicating whether to save the data sets obtained on apply operations.
     */
    @Inject @Config("true")
    protected boolean putOnApply;

    @Inject
    protected UUIDGenerator uuidGenerator;

    @Inject
    protected IntervalBuilderLocator intervalBuilderLocator;

    /**
     * The in-memory data set cache.
     */
    protected Map<String,DataSetCacheEntry> dataSetCache = new HashMap<String, DataSetCacheEntry>();

    public void put(DataSet source) throws Exception {
        DataSetCacheEntry entry = new DataSetCacheEntry(source);
        dataSetCache.put(source.getUUID(), entry);
    }

    public void remove(String uuid) throws Exception {
    }

    public DataSet get(String uuid) throws Exception {
        DataSetCacheEntry entry = dataSetCache.get(uuid);
        if (entry == null) return null;
        return entry.dataSet;
    }

    public DataSet apply(String uuid, DataSetOp op) throws Exception {
        DataSetCacheEntry holder = dataSetCache.get(uuid);
        if (holder == null) throw new Exception("Data set not found: " + uuid);

        // Check the cache first
        DataSetCacheEntry opHolder = holder.getChildByOp(op);
        if (opHolder != null) {
            return opHolder.dataSet;
        }

        // Apply the operation
        DataSetCacheEntry result;
        if (op instanceof DataSetFilter) result = filter(holder, (DataSetFilter) op);
        else if (op instanceof DataSetGroup) result = group(holder, (DataSetGroup) op);
        else if (op instanceof DataSetSort) result = sort(holder, (DataSetSort) op);
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

    public DataSetCacheEntry filter(DataSetCacheEntry dataSet, DataSetFilter op) throws Exception {
        return null;
    }

    /**
         Group strategy:

         The intervals generated depends directly on the domain strategy:

         - ADAPTATIVE - distinct values for labels, based on overall max/min + maxIntervals for date&number
         - FIXED & MULTI (ONLY date&number) - fixed number of intervals selecting an interval size.
         - TODO: CUSTOM - fixed intervals with a custom group function

         Group op result:
         - set of intervals, each interval a set of row numbers
         - each interval contains: a name, a map of column + scalar function result
         - a data set with the same rows as the number of intervals

         => Interval class is a cache of rows ref + scalar calculations
     */
    public DataSetCacheEntry group(DataSetCacheEntry source, DataSetGroup op) throws Exception {
        for (Domain domain : op.getDomainList()) {
            String domainId = domain.getColumnId();
            DataColumn domainColumn = source.dataSet.getColumnById(domainId);
            DomainStrategy domainStrategy = domain.getDomainStrategy();

            // Build the group intervals by applying the domain strategy specified
            IntervalBuilder intervalBuilder = intervalBuilderLocator.lookup(domainColumn, domainStrategy);
            if (intervalBuilder == null) throw new Exception("Interval generator not supported yet.");
            List<Interval> intervals = intervalBuilder.build(domainColumn, domainStrategy);

            // Build the grouped data set header.
            DataSetImpl dataSet = new DataSetImpl();
            dataSet.addColumn(domainId, domainColumn.getName(), ColumnType.LABEL);
            for (Range range : op.getRangeList()) {
                dataSet.addColumn(range.getColumnId(), range.getColumnName(), ColumnType.NUMBER);
            }
            // Add the scalar calculations to the result.
            for (int i=0; i<intervals.size(); i++) {
                Interval interval = intervals.get(i);
                dataSet.setValueAt(i, 0, interval.name);
                List<Range> ranges = op.getRangeList();
                for (int j=0; j<ranges.size(); j++) {
                    Range range = ranges.get(j);
                    DataColumn rangeColumn = source.dataSet.getColumnById(range.getSourceId());
                    Double scalar = interval.calculateScalar(rangeColumn, range.getFunctionCode());
                    dataSet.setValueAt(i, j + 1, scalar);
                }
            }
            return new DataSetCacheEntry(source, dataSet, op, intervals);
        }
        return null;
    }

    public DataSetCacheEntry sort(DataSetCacheEntry source, DataSetSort op) throws Exception {
        return null;
    }

    // The internal data set holder

    private class DataSetCacheEntry {

        DataSet dataSet;
        DataSetOp op;
        List<DataSetCacheEntry> children = new ArrayList<DataSetCacheEntry>();
        List<Interval> intervals = null;

        DataSetCacheEntry(DataSet dataSet) {
            this(null, dataSet, null);
        }

        DataSetCacheEntry(DataSetCacheEntry parent, DataSet dataSet, DataSetOp op) {
            this(null, dataSet, null, null);
        }

        DataSetCacheEntry(DataSetCacheEntry parent, DataSet dataSet, DataSetOp op, List<Interval> intervals) {
            this.dataSet = dataSet;
            this.op = op;
            this.intervals = intervals;
            if (parent != null) {
                parent.children.add(this);
            }
        }

        public DataSetCacheEntry getChildByOp(DataSetOp op) {
            for (DataSetCacheEntry child : children) {
                if (op.equals(child.op)) {
                    return child;
                }
            }
            return null;
        }
    }
}
