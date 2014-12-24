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
package org.dashbuilder.dataset.engine.group;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.engine.DataSetHandler;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.Interval;

/**
 * Group operations requires to split the values of a column into intervals. This interface provides an abstraction
 * for the different group strategy implementations.
 */
public interface IntervalBuilder {

    /**
     * Build a list of intervals according to the column group settings.
     * @param ctx The current operation engine data set handler context.
     * @param columnGroup The column group operation to apply.
     *
     * @return A list of intervals containing a split of all the values for the given column.
     */
    IntervalList build(DataSetHandler ctx, ColumnGroup columnGroup);

    /**
     * Given a grouped column, locate the interval at the position specified.
     *
     * @param column The grouped column.
     * @param intervalIndex The target interval index we want to select.
     *
     * @return The target interval
     */
    Interval locate(DataColumn column, Integer intervalIndex);
}
