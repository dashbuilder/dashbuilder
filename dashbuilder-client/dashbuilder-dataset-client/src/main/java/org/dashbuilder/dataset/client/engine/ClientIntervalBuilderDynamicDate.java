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
package org.dashbuilder.dataset.client.engine;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.dashbuilder.dataset.engine.DataSetHandler;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.engine.group.Interval;
import org.dashbuilder.dataset.engine.group.IntervalBuilder;
import org.dashbuilder.dataset.engine.group.IntervalList;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.dataset.sort.SortedList;
import org.dashbuilder.dataset.date.Quarter;

import static org.dashbuilder.dataset.group.DateIntervalType.*;

/**
 * Interval builder for date columns which generates intervals depending on the underlying data available.
 */
@ApplicationScoped
public class ClientIntervalBuilderDynamicDate implements IntervalBuilder {

    public IntervalList build(DataSetHandler handler, ColumnGroup columnGroup) {
        IntervalDateRangeList results = new IntervalDateRangeList(columnGroup);
        DataSet dataSet = handler.getDataSet();
        List values = dataSet.getColumnById(columnGroup.getSourceId()).getValues();
        if (values.isEmpty()) {
            return results;
        }

        // Sort the column dates.
        DataSetSort sortOp = new DataSetSort();
        sortOp.addSortColumn(new ColumnSort(columnGroup.getSourceId(), SortOrder.ASCENDING));
        DataSetHandler sortResults = handler.sort(sortOp);
        List<Integer> sortedRows = sortResults.getRows();
        if (sortedRows == null || sortedRows.isEmpty()) {
            return results;
        }

        // Get the lower & upper limits.
        SortedList sortedValues = new SortedList(values, sortedRows);
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
        String preferredSize = columnGroup.getIntervalSize();
        if (preferredSize != null && preferredSize.trim().length() > 0) {
            intervalSize = getByName(columnGroup.getIntervalSize());
        }
        if (intervalSize != null && compare(intervalType, intervalSize) == -1) {
            intervalType = intervalSize;
        }

        // Adjust the minDate according to the interval type.
        Date intervalMinDate = new Date(minDate.getTime());
        if (YEAR.equals(intervalType)) {
            intervalMinDate.setMonth(0);
            intervalMinDate.setDate(1);
            intervalMinDate.setHours(0);
            intervalMinDate.setMinutes(0);
            intervalMinDate.setSeconds(0);
        }
        if (QUARTER.equals(intervalType)) {
            int currentMonth = intervalMinDate.getMonth();
            int firstMonthYear = columnGroup.getFirstMonthOfYear().getIndex();
            int rest = Quarter.getPositionInQuarter(firstMonthYear, currentMonth);
            intervalMinDate.setMonth(currentMonth - rest);
            intervalMinDate.setDate(1);
            intervalMinDate.setHours(0);
            intervalMinDate.setMinutes(0);
            intervalMinDate.setSeconds(0);
        }
        if (MONTH.equals(intervalType)) {
            intervalMinDate.setDate(1);
            intervalMinDate.setHours(0);
            intervalMinDate.setMinutes(0);
            intervalMinDate.setSeconds(0);
        }
        if (DAY.equals(intervalType) || DAY_OF_WEEK.equals(intervalType)) {
            intervalMinDate.setHours(0);
            intervalMinDate.setMinutes(0);
            intervalMinDate.setSeconds(0);
        }
        if (HOUR.equals(intervalType)) {
            intervalMinDate.setMinutes(0);
            intervalMinDate.setSeconds(0);
        }
        if (MINUTE.equals(intervalType)) {
            intervalMinDate.setSeconds(0);
        }

        // Create the intervals according to the min/max dates.
        int index = 0;
        while (intervalMinDate.compareTo(maxDate) <= 0) {
            Date intervalMaxDate = new Date(intervalMinDate.getTime());

            // Go to the next interval
            if (MILLENIUM.equals(intervalType)) {
                intervalMaxDate.setYear(intervalMinDate.getYear()+1000);
            }
            if (CENTURY.equals(intervalType)) {
                intervalMaxDate.setYear(intervalMinDate.getYear()+100);
            }
            if (DECADE.equals(intervalType)) {
                intervalMaxDate.setYear(intervalMinDate.getYear()+10);
            }
            if (YEAR.equals(intervalType)) {
                intervalMaxDate.setYear(intervalMinDate.getYear()+1);
            }
            if (QUARTER.equals(intervalType)) {
                intervalMaxDate.setMonth(intervalMinDate.getMonth()+3);
            }
            if (MONTH.equals(intervalType)) {
                intervalMaxDate.setMonth(intervalMinDate.getMonth()+1);
            }
            if (WEEK.equals(intervalType)) {
                intervalMaxDate.setDate(intervalMinDate.getDate()+7);
            }
            if (DAY.equals(intervalType) || DAY_OF_WEEK.equals(intervalType)) {
                intervalMaxDate.setDate(intervalMinDate.getDate()+1);
            }
            if (HOUR.equals(intervalType)) {
                intervalMaxDate.setHours(intervalMinDate.getHours()+1);
            }
            if (MINUTE.equals(intervalType)) {
                intervalMaxDate.setMinutes(intervalMinDate.getMinutes()+1);
            }
            if (SECOND.equals(intervalType)) {
                intervalMaxDate.setSeconds(intervalMinDate.getSeconds()+1);
            }

            // Create the interval.
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
            // Move to the next interval.
            intervalMinDate = intervalMaxDate;
        }

        // Reverse intervals if requested
        boolean asc = columnGroup.isAscendingOrder();
        if (!asc) Collections.reverse( results );

        return results;
    }

    /**
     * A list containing date range intervals.
     */
    public class IntervalDateRangeList extends IntervalList {

        public IntervalDateRangeList(ColumnGroup columnGroup) {
            super(columnGroup);
        }

        public Interval locateInterval(Object value) {
            Date d = (Date) value;
            for (Interval interval : this) {
                IntervalDateRange dateRange = (IntervalDateRange) interval;
                if (d.equals(dateRange.minDate) || (d.after(dateRange.minDate) && d.before(dateRange.maxDate))) {
                    return interval;
                }
            }
            return null;
        }
    }

    /**
     * A date interval holding dates belonging to a given range.
     */
    public class IntervalDateRange extends Interval {

        protected DateIntervalType intervalType;
        protected Date minDate;
        protected Date maxDate;

        public IntervalDateRange(DateIntervalType intervalType, Date minDate, Date maxDate) {
            super();
            this.name = calculateName(intervalType, minDate);
            this.intervalType = intervalType;
            this.minDate = minDate;
            this.maxDate = maxDate;

        }

        public String calculateName(DateIntervalType intervalType, Date d) {
            if (MILLENIUM.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("yyyy");
                return format.format(d);
            }
            if (CENTURY.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("yyyy");
                return format.format(d);
            }
            if (DECADE.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("yyyy");
                return format.format(d);
            }
            if (YEAR.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("yyyy");
                return format.format(d);
            }
            if (QUARTER.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("yyyy");
                if (d.getMonth() < 3) return "Q1 " + format.format(d);
                if (d.getMonth() < 6) return "Q2 " + format.format(d);
                if (d.getMonth() < 9) return "Q3 " + format.format(d);
                return "Q4 " + format.format(d);
            }
            if (MONTH.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("MMMM yyyy");
                return format.format(d);
            }
            if (WEEK.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("MMM dd");
                return format.format(d);
            }
            if (DAY.equals(intervalType) || DAY_OF_WEEK.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("EEE dd ");
                return format.format(d);
            }
            if (HOUR.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("HH");
                return format.format(d) + "h";
            }
            if (MINUTE.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("mm");
                return format.format(d);
            }
            if (SECOND.equals(intervalType)) {
                DateTimeFormat format  = DateTimeFormat.getFormat("ss");
                return format.format(d);
            }
            return null;
        }
    }
}
