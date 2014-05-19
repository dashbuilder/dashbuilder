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
package org.dashbuilder.dataset.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dashbuilder.dataset.group.IntervalList;
import org.dashbuilder.dataset.index.visitor.DataSetIndexVisitor;
import org.dashbuilder.model.dataset.group.GroupColumn;
import org.dashbuilder.model.dataset.group.ScalarFunctionType;
import org.dashbuilder.model.dataset.sort.DataSetSort;

/**
 * A DataSet index node
 */
public abstract class DataSetIndexNode extends DataSetIndexElement {

    DataSetIndexNode parent = null;
    List<DataSetGroupIndex> groupIndexes = null;
    List<DataSetSortIndex> sortIndexes = null;
    Map<String, Map<ScalarFunctionType, DataSetScalarIndex>> scalarIndexes = null;

    public DataSetIndexNode() {
        this(null, 0);
    }

    public DataSetIndexNode(DataSetIndexNode parent, long buildTime) {
        super(buildTime);
        this.parent = parent;
    }

    public DataSetIndexNode getParent() {
        return parent;
    }

    public void setParent(DataSetIndexNode parent) {
        this.parent = parent;
    }

    public void acceptVisitor(DataSetIndexVisitor visitor) {
        super.acceptVisitor(visitor);

        if (groupIndexes != null) {
            for (DataSetGroupIndex index : groupIndexes) {
                index.acceptVisitor(visitor);
            }
        }
        if (sortIndexes != null) {
            for (DataSetSortIndex index : sortIndexes) {
                index.acceptVisitor(visitor);
            }
        }
        if (scalarIndexes != null) {
            for (Map<ScalarFunctionType, DataSetScalarIndex> indexMap : scalarIndexes.values()) {
                for (DataSetScalarIndex index : indexMap.values()) {
                    index.acceptVisitor(visitor);
                }
            }
        }
    }

    // Scalar function indexes

    public DataSetScalarIndex indexScalar(String columnId, ScalarFunctionType type, Double value, long buildTime) {
        if (scalarIndexes == null) scalarIndexes = new HashMap<String, Map<ScalarFunctionType, DataSetScalarIndex>>();

        Map<ScalarFunctionType,DataSetScalarIndex> columnScalars = scalarIndexes.get(columnId);
        if (columnScalars == null) scalarIndexes.put(columnId, columnScalars = new HashMap<ScalarFunctionType,DataSetScalarIndex>());

        DataSetScalarIndex index = new DataSetScalarIndex(value, buildTime);
        columnScalars.put(type, index);
        return index;
    }

    public Double getScalar(String columnId, ScalarFunctionType type) {
        if (scalarIndexes == null) return null;

        Map<ScalarFunctionType,DataSetScalarIndex> columnScalars = scalarIndexes.get(columnId);
        if (columnScalars == null) return null;

        DataSetScalarIndex indexScalar = columnScalars.get(type);
        if (indexScalar == null) return null;

        indexScalar.reuseHit();
        return indexScalar.getValue();
    }

    // TODO: coordinate concurrent index modifications

    // Group indexes

    public DataSetGroupIndex indexGroup(GroupColumn groupColumn, IntervalList intervalList, long buildTime) {
        if (groupIndexes == null) groupIndexes = new ArrayList<DataSetGroupIndex>();
        DataSetGroupIndex index = new DataSetGroupIndex(groupColumn, intervalList);
        index.setParent(this);
        index.setBuildTime(buildTime);
        groupIndexes.add(index);
        return index;
    }

    public DataSetGroupIndex getGroupIndex(GroupColumn gc) {
        if (groupIndexes == null) return null;

        String key = getGroupKey(gc);
        for (DataSetGroupIndex groupIndex : groupIndexes) {
            GroupColumn c = groupIndex.groupColumn;
            if (key.equals(getGroupKey(c))) {
                groupIndex.reuseHit();
                return groupIndex;
            }
        }
        return null;
    }

    public String getGroupKey(GroupColumn groupColumn) {
        return groupColumn.getSourceId() + "_" +
                groupColumn.getStrategy().toString() + "_" +
                groupColumn.getIntervalSize() + "_" +
                groupColumn.getMaxIntervals();
    }


    // Sort indexes

    public DataSetSortIndex indexSort(DataSetSort sortOp, List<Integer> sortedRows, long buildTime) {
        if (sortIndexes == null) sortIndexes = new ArrayList<DataSetSortIndex>();
        DataSetSortIndex index = new DataSetSortIndex(sortOp, sortedRows);
        index.setParent(this);
        index.setBuildTime(buildTime);
        sortIndexes.add(index);
        return index;
    }

    public DataSetSortIndex getSortIndex(DataSetSort sortOp) {
        if (sortIndexes == null) return null;

        for (DataSetSortIndex sortIndex : sortIndexes) {
            if (sortOp.equals(sortIndex.getSortOp())) {
                sortIndex.reuseHit();
                return sortIndex;
            }
        }
        return null;
    }
}

