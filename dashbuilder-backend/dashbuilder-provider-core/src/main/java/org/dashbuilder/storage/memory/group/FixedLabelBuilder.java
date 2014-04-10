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
package org.dashbuilder.storage.memory.group;

import java.util.ArrayList;
import java.util.List;

import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.group.DomainStrategy;

/**
 * Interval builder for label type columns which generates one interval per label.
 */
public class FixedLabelBuilder implements IntervalBuilder {

    public List<Interval> build(DataColumn column, DomainStrategy strategy) {
        List<Interval> result = new ArrayList<Interval>();
        indexValues(result, column.getValues());
        return result;
    }

    public synchronized void indexValues(List<Interval> result, List values) {
        for (int row = 0; row < values.size(); row++) {
            Object value = values.get(row);
            Interval interval = getInterval(result, value);
            if (interval == null) result.add(interval = new Interval(value == null ? null : value.toString()));
            interval.rows.add(row);
        }
    }

    public synchronized Interval getInterval(List<Interval> result, Object value) {
        for (Interval interval : result) {
            if (interval.name == value || (interval.name != null && interval.name.equals(value))) {
                return interval;
            }
        }
        return null;
    }
}
