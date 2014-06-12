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

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.engine.DataSetHandler;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.ColumnGroup;

import static org.dashbuilder.model.dataset.group.DateIntervalType.*;

/**
 * Interval builder for date columns which generates a fixed number of intervals for a given interval size.
 * <p>The only intervals sizes supported are: QUARTER, MONTH, DAY_OF_WEEK, HOUR, MINUTE & SECOND.</p>
 */
@ApplicationScoped
public class IntervalBuilderFixedDate implements IntervalBuilder {

    /** List of the only DateIntervalType's supported as fixed date intervals. */
    private DateIntervalType[] FIXED_INTERVALS_SUPPORTED = new DateIntervalType[] {
            QUARTER, MONTH, DAY_OF_WEEK, HOUR, MINUTE, SECOND};


    public IntervalList build(DataSetHandler ctx, ColumnGroup columnGroup) {
        IntervalList intervalList = createIntervalList(columnGroup);

        // Reverse intervals if requested
        boolean asc = columnGroup.isAscendingOrder();
        if (!asc) Collections.reverse(intervalList);

        // Index the values
        String columnId = columnGroup.getSourceId();
        List values = ctx.getDataSet().getColumnById(columnId).getValues();
        List<Integer> rows = ctx.getRows();
        intervalList.indexValues(values, rows);
        return intervalList;
    }

    public IntervalList createIntervalList(ColumnGroup columnGroup) {
        DateIntervalType type = DateIntervalType.getByName(columnGroup.getIntervalSize());
        if (QUARTER.equals(type)) {
            return new IntervalListQuarter(columnGroup);
        }
        if (MONTH.equals(type)) {
            return new IntervalListMonth(columnGroup);
        }
        if (DAY_OF_WEEK.equals(type)) {
            return new IntervalListDayOfWeek(columnGroup);
        }
        if (HOUR.equals(type)) {
            return new IntervalListHour(columnGroup);
        }
        if (MINUTE.equals(type)) {
            return new IntervalListMinute(columnGroup);
        }
        if (SECOND.equals(type)) {
            return new IntervalListSecond(columnGroup);
        }
        throw new IllegalArgumentException("Interval size '" + columnGroup.getIntervalSize() + "' not supported for " +
                "fixed date intervals. The only supported sizes are: " + StringUtils.join(FIXED_INTERVALS_SUPPORTED, ","));
    }
}
