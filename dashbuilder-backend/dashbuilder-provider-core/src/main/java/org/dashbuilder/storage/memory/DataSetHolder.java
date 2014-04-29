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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.group.GroupColumn;
import org.dashbuilder.storage.memory.group.IntervalList;

/**
 * A DataSet instance holder
 */
public class DataSetHolder {

    DataSet dataSet;
    DataSetOp op;

    long buildTime = 0;
    int reuseHits = 0;

    List<DataSetHolder> children = new ArrayList<DataSetHolder>();
    Map<String, IntervalListHolder> intervalLists = new HashMap<String, IntervalListHolder>();
    Map<String, SortedListHolder> sortedLists = new HashMap<String, SortedListHolder>();
    Map<DataSetOpType, TransientDataSetOpStats> opStats = new HashMap<DataSetOpType, TransientDataSetOpStats>();

    DataSetHolder(DataSet dataSet) {
        this(null, dataSet, null, 0);
    }

    DataSetHolder(DataSetHolder parent, DataSet dataSet, DataSetOp op, long buildTime) {
        this.dataSet = dataSet;
        this.op = op;
        this.opStats.put(DataSetOpType.GROUP, new TransientDataSetOpStats());
        this.opStats.put(DataSetOpType.FILTER, new TransientDataSetOpStats());
        this.opStats.put(DataSetOpType.SORT, new TransientDataSetOpStats());
        this.buildTime = buildTime;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void setIntervalList(GroupColumn groupColumn, IntervalList intervals, long buildTime) {
        String key = getDomainKey(groupColumn);
        IntervalListHolder holder = new IntervalListHolder(intervals, buildTime);
        intervalLists.put(key, holder);
    }

    public IntervalList getIntervalList(GroupColumn groupColumn) {
        String key = getDomainKey(groupColumn);
        IntervalListHolder holder = intervalLists.get(key);
        if (holder == null) return null;
        holder.reuseHits++;
        return holder.intervalList;
    }

    public void setSortedList(DataColumn column, SortedList<ComparableValue> l, long buildTime) {
        SortedListHolder holder = new SortedListHolder(l, buildTime);
        sortedLists.put(column.getId(), holder);
    }

    public SortedList<ComparableValue> getSortedList(DataColumn column) {
        SortedListHolder holder = sortedLists.get(column.getId());
        if (holder == null) return null;
        holder.reuseHits++;
        return holder.sortedList;
    }

    public void addChild(DataSetHolder child) {
        children.add(child);
        opStats.get(child.op.getType()).update(child);
    }

    public DataSetHolder getChildByOp(DataSetOp op) {
        for (DataSetHolder child : children) {
            if (op.equals(child.op)) {
                child.reuseHits++;
                opStats.get(child.op.getType()).reuseHits++;
                return child;
            }
        }
        return null;
    }

    public String getDomainKey(GroupColumn groupColumn) {
        return groupColumn.getSourceId() + "_" +
                groupColumn.getStrategy().toString() + "_" +
                groupColumn.getIntervalSize() + "_" +
                groupColumn.getMaxIntervals();
    }
}

