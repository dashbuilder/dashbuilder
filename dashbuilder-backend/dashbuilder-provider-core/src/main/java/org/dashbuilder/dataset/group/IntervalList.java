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
package org.dashbuilder.dataset.group;

import java.util.ArrayList;
import java.util.List;

import org.dashbuilder.model.dataset.group.ColumnGroup;

/**
 * An list containing the intervals derived from an specific domain configuration.
 */
public abstract class IntervalList extends ArrayList<Interval> {

    protected ColumnGroup columnGroup;

    public IntervalList(ColumnGroup columnGroup) {
        super();
        this.columnGroup = columnGroup;
    }

    public ColumnGroup getColumnGroup() {
        return columnGroup;
    }

    /**
     * Creates and classify the list of specified values into intervals.
     */

    public IntervalList indexValues(List values) {
        for (int row = 0; row < values.size(); row++) {
            Object value = values.get(row);
            indexValue(value, row);
        }
        return this;
    }

    /**
     * Index the given value into the appropriate interval.
     * @param value The value to index
     * @param row The row index where the value is hold within the data set.
     * @return The interval which the value belongs to.
     */
    public Interval indexValue(Object value, int row) {
        Interval interval = locateInterval(value);
        if (interval == null) throw new RuntimeException("Can't locate the interval for the specified value: " + value);
        interval.rows.add(row);
        return interval;
    }

    /**
     * Get the interval that holds the given value.
     * @param value The value we are asking for.
     * @return The interval which the value belongs to.
     */
    public abstract Interval locateInterval(Object value);
}
