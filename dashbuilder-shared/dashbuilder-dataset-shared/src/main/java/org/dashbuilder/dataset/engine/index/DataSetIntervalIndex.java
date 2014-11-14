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
package org.dashbuilder.dataset.engine.index;

import java.util.Arrays;
import java.util.List;

import org.dashbuilder.dataset.engine.group.Interval;
import org.dashbuilder.dataset.impl.MemSizeEstimator;

/**
 * An interval index
 */
public class DataSetIntervalIndex extends DataSetIndexNode implements DataSetIntervalIndexHolder {

    String intervalName = null;

    public DataSetIntervalIndex(DataSetGroupIndex parent, String intervalName, List<Integer> rows) {
        super(parent, rows, 0);
        this.intervalName = intervalName;
    }

    DataSetIntervalIndex(DataSetGroupIndex parent, String intervalName) {
        super(parent, null, 0);
        this.intervalName = intervalName;
    }

    DataSetIntervalIndex(DataSetGroupIndex parent, Interval interval) {
        super(parent, interval.getRows(), 0);
        this.intervalName = interval.getName();
    }

    public List<DataSetIntervalIndex> getIntervalIndexes() {
        return Arrays.asList(this);
    }

    public String getName() {
        return intervalName;
    }

    public long getEstimatedSize() {
        long result = super.getEstimatedSize();
        if (intervalName != null) {
            result += MemSizeEstimator.sizeOfString(intervalName);
        }
        return result;
    }

    public String toString() {
        StringBuilder out = new StringBuilder(intervalName);
        out.append(" ").append(super.toString());
        return out.toString();
    }
}

