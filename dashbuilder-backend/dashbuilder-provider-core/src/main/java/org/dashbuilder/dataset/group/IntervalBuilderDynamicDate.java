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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.engine.DataSetHandler;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.ColumnGroup;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.model.dataset.sort.ColumnSort;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.dataset.sort.SortedList;

import static org.dashbuilder.model.dataset.group.DateIntervalType.*;

/**
 * Interval builder for date columns which generates intervals depending on the underlying data available.
 */
@ApplicationScoped
public class IntervalBuilderDynamicDate implements IntervalBuilder {

    public IntervalList build(DataSetHandler handler, ColumnGroup columnGroup) {
        IntervalDateRangeList results = new IntervalDateRangeList(columnGroup);
        DataSet dataSet = handler.getDataSet();
        List values = dataSet.getColumnById(columnGroup.getSourceId()).getValues();
        if (values.isEmpty()) return results;

        // Sort the column dates.
        DataSetSort sortOp = new DataSetSort();
        sortOp.addSortColumn(new ColumnSort(columnGroup.getSourceId(), SortOrder.ASCENDING));
        DataSetHandler sortResults = handler.sort(sortOp);
        List<Integer> sortedRows = sortResults.getRows();
        SortedList sortedValues = new SortedList(values, sortedRows);

        // Get the lower & upper limits.
        Date minDate = (Date) sortedValues.get(0);
        Date maxDate = (Date) sortedValues.get(sortedValues.size()-1);

        // If min/max are equals then create a single interval.
        if (minDate.compareTo(maxDate) == 0) {
            IntervalDateRange interval = new IntervalDateRange(DAY, minDate, maxDate);
            for (int row = 0; row < sortedValues.size(); row++) interval.rows.add(row);
            results.add(interval);
            return results;
        }

        // Calculate the interval type used according to the constraints set.
        int maxIntervals = columnGroup.getMaxIntervals();
        if (maxIntervals < 1) maxIntervals = 15;
        DateIntervalType intervalType = YEAR;
        long millis = (maxDate.getTime() - minDate.getTime());
        for (DateIntervalType type : values()) {
            long nintervals = millis / getDurationInMillis(type);
            if (nintervals < maxIntervals) {
                intervalType = type;
                break;
            }
        }

        // Ensure the interval mode obtained is always greater or equals than the preferred interval size.
        DateIntervalType intervalSize = null;
        if (!StringUtils.isBlank(columnGroup.getIntervalSize())) {
            intervalSize = getByName(columnGroup.getIntervalSize());
        }
        if (intervalSize != null && compare(intervalType, intervalSize) == -1) {
            intervalType = intervalSize;
        }

        // Adjust the minDate according to the interval type.
        Calendar gc = GregorianCalendar.getInstance();
        gc.setLenient(false);
        gc.setTime(minDate);
        if (YEAR.equals(intervalType)) {
            gc.add(Calendar.MONTH, gc.get(Calendar.MONTH) * -1);
        }
        if (MONTH.equals(intervalType)) {
            gc.add(Calendar.DAY_OF_MONTH, (gc.get(Calendar.DAY_OF_MONTH) - 1) * -1);
        }
        if (DAY.equals(intervalType) || DAY_OF_WEEK.equals(intervalType)) {
            gc.add(Calendar.HOUR, gc.get(Calendar.HOUR_OF_DAY) * -1);
        }
        if (HOUR.equals(intervalType)) {
            gc.add(Calendar.MINUTE, gc.get(Calendar.MINUTE) * -1);
        }
        if (MINUTE.equals(intervalType)) {
            gc.add(Calendar.SECOND, gc.get(Calendar.SECOND) * -1);
        }
        if (SECOND.equals(intervalType)) {
            gc.add(Calendar.MILLISECOND, gc.get(Calendar.MILLISECOND) * -1);
        }

        // Create the intervals according to the min/max dates.
        int index = 0;
        while (gc.getTime().compareTo(maxDate) <= 0) {
            Date intervalMinDate = gc.getTime();

            // Go to the next interval
            if (MILLENIUM.equals(intervalType)) {
                gc.add(Calendar.YEAR, 1000);
            }
            if (CENTURY.equals(intervalType)) {
                gc.add(Calendar.YEAR, 100);
            }
            if (DECADE.equals(intervalType)) {
                gc.add(Calendar.YEAR, 10);
            }
            if (YEAR.equals(intervalType)) {
                gc.add(Calendar.YEAR, 1);
            }
            if (QUARTER.equals(intervalType)) {
                gc.add(Calendar.MONTH, 3);
            }
            if (MONTH.equals(intervalType)) {
                gc.add(Calendar.MONTH, 1);
            }
            if (WEEK.equals(intervalType)) {
                gc.add(Calendar.DAY_OF_MONTH, 7);
            }
            if (DAY.equals(intervalType) || DAY_OF_WEEK.equals(intervalType)) {
                gc.add(Calendar.DAY_OF_MONTH, 1);
            }
            if (HOUR.equals(intervalType)) {
                gc.add(Calendar.HOUR_OF_DAY, 1);
            }
            if (MINUTE.equals(intervalType)) {
                gc.add(Calendar.MINUTE, 1);
            }
            if (SECOND.equals(intervalType)) {
                gc.add(Calendar.SECOND, 1);
            }

            // Create the interval.
            Date intervalMaxDate = gc.getTime();
            IntervalDateRange interval = new IntervalDateRange(intervalType, intervalMinDate, intervalMaxDate);
            results.add(interval);

            // Add the target rows to the interval.
            boolean stop = false;
            while (!stop) {
                if (index >= sortedValues.size()) {
                    stop = true;
                } else {
                    Date dateValue = (Date) sortedValues.get(index);
                    Integer row = sortedRows.get(index);
                    if (dateValue.before(intervalMaxDate)){
                        interval.rows.add(row);
                        index++;
                    } else {
                        stop = true;
                    }
                }
            }
        }
        return results;
    }
}
