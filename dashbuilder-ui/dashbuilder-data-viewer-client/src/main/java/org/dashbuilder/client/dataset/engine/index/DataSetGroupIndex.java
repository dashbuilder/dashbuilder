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
package org.dashbuilder.client.dataset.engine.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dashbuilder.client.dataset.engine.group.Interval;
import org.dashbuilder.client.dataset.engine.group.IntervalList;
import org.dashbuilder.client.dataset.engine.index.visitor.DataSetIndexVisitor;
import org.dashbuilder.model.dataset.group.ColumnGroup;

/**
 * A DataSet group operation index
 */
public class DataSetGroupIndex extends DataSetIndexNode implements DataSetIntervalIndexHolder {

    // The group index is composed by a set of interval indexes.
    ColumnGroup columnGroup = null;
    List<DataSetIntervalIndex> intervalIndexList = null;

    // And can (optionally) contains a subset of interval selections.
    List<DataSetGroupIndex> selectIndexList = null;

    // When the group represents a selection it has a selection key.
    String selectKey = null;

    public DataSetGroupIndex(ColumnGroup columnGroup) {
        super();
        this.columnGroup = columnGroup;
        this.intervalIndexList = new ArrayList<DataSetIntervalIndex>();
    }

    public DataSetGroupIndex(ColumnGroup columnGroup, IntervalList intervalList) {
        this(columnGroup);
        for (Interval interval : intervalList) {
            intervalIndexList.add(new DataSetIntervalIndex(this, interval));
        }
    }

    public DataSetGroupIndex(String selectKey, List<DataSetIntervalIndex> intervalIndexes) {
        this(null);
        this.selectKey = selectKey;
        for (DataSetIntervalIndex index : intervalIndexes) {
            intervalIndexList.add(index);
        }
    }

    public List<DataSetIntervalIndex> getIntervalIndexes() {
        return intervalIndexList;
    }

    public List<DataSetIntervalIndex> getIntervalIndexes(List<String> names) {
        List<DataSetIntervalIndex> result = new ArrayList<DataSetIntervalIndex>();
        for (String name : names) {
            DataSetIntervalIndex idx = getIntervalIndex(name);
            if (idx != null) result.add(idx);
        }
        return result;
    }

    public DataSetIntervalIndex getIntervalIndex(String name) {
        for (DataSetIntervalIndex idx : intervalIndexList) {
            if (idx.getName().equals(name)) {
                return idx;
            }
        }
        return null;
    }

    public int indexOfIntervalIndex(DataSetIntervalIndex target) {
        for (int i = 0; i < intervalIndexList.size(); i++) {
            DataSetIntervalIndex idx = intervalIndexList.get(i);
            if (idx.getName().equals(target.getName())) {
                return i;
            }
        }
        return -1;
    }

    public DataSetGroupIndex getSelectionIndex(List<String> names) {
        if (selectIndexList == null) return null;

        String targetKey = buildSelectKey(names);
        for (DataSetGroupIndex idx : selectIndexList) {
            if (idx.selectKey.equals(targetKey)) {
                idx.reuseHit();
                return idx;
            }
        }
        return null;
    }

    public DataSetGroupIndex indexSelection(List<String> intervalNames, List<DataSetIntervalIndex> intervalIndexes) {
        if (selectIndexList == null) selectIndexList = new ArrayList<DataSetGroupIndex>();

        String key = buildSelectKey(intervalNames);
        DataSetGroupIndex index = new DataSetGroupIndex(key, intervalIndexes);
        index.setParent(this);
        index.setBuildTime(buildTime);
        selectIndexList.add(index);
        return index;
    }

    protected String buildSelectKey(List<String> intervalNames) {
        StringBuilder out = new StringBuilder();
        for (int i=0; i<intervalNames.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(intervalNames.get(i));
        }
        return out.toString();
    }

    public List<Integer> getRows() {
        if (intervalIndexList == null || intervalIndexList.isEmpty()) {
            return null;
        }
        List<Integer> results = new ArrayList<Integer>();
        for (DataSetIntervalIndex intervalIndex : intervalIndexList) {
            results.addAll(intervalIndex.getRows());
        }
        return results;
    }

    public void indexIntervals(Collection<DataSetIntervalIndex> intervalsIdxs) {
        for (DataSetIntervalIndex idx : intervalsIdxs) {
            indexInterval(idx);
        }
    }

    public void indexInterval(DataSetIntervalIndex intervalIdx) {
        String intervalName = intervalIdx.getName();
        DataSetIntervalIndex existing = getIntervalIndex(intervalName);
        if (existing == null) {
            intervalIndexList.add(intervalIdx);
        } else {
            if (existing instanceof DataSetIntervalSetIndex) {
                existing.getIntervalIndexes().add(intervalIdx);
            }
            else if (existing != intervalIdx){
                int i = indexOfIntervalIndex(existing);
                DataSetIntervalSetIndex indexSet = new DataSetIntervalSetIndex(this, intervalName);
                indexSet.getIntervalIndexes().add(existing);
                indexSet.getIntervalIndexes().add(intervalIdx);
                intervalIndexList.set(i, indexSet);
            }
        }
    }

    public void acceptVisitor(DataSetIndexVisitor visitor) {
        super.acceptVisitor(visitor);

        for (DataSetIntervalIndex index : intervalIndexList) {
            index.acceptVisitor(visitor);
        }
    }
}

