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
import java.util.List;

import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.group.IntervalList;
import org.dashbuilder.dataset.index.visitor.DataSetIndexVisitor;
import org.dashbuilder.model.dataset.group.GroupColumn;

/**
 * A DataSet index
 */
public class DataSetGroupIndex extends DataSetIndexNode {

    GroupColumn groupColumn;
    List<DataSetIntervalIndex> intervalIndexes = new ArrayList<DataSetIntervalIndex>();

    public DataSetGroupIndex(GroupColumn groupColumn, IntervalList intervalList) {
        super();
        this.groupColumn = groupColumn;
        for (Interval interval : intervalList) {
            intervalIndexes.add(new DataSetIntervalIndex(this, interval));
        }
    }

    public List<DataSetIntervalIndex> getIntervalIndexes() {
        return intervalIndexes;
    }

    public GroupColumn getGroupColumn() {
        return groupColumn;
    }

    public void acceptVisit(DataSetIndexVisitor visitor) {
        super.acceptVisitor(visitor);
        for (DataSetIntervalIndex index : intervalIndexes) {
            index.acceptVisitor(visitor);
        }
    }
}

