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
package org.dashbuilder.dataset.backend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.engine.DataSetHandler;
import org.dashbuilder.dataset.engine.group.Interval;
import org.dashbuilder.dataset.engine.group.IntervalBuilder;
import org.dashbuilder.dataset.engine.group.IntervalList;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.dataset.sort.SortedList;
import org.dashbuilder.dataset.date.Quarter;

import static org.dashbuilder.dataset.group.DateIntervalType.*;

/**
 * Interval builder for date columns which generates intervals depending on the underlying data available.
 */
@ApplicationScoped
public class BackendIntervalBuilderDynamicDate implements IntervalBuilder {

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
            gc.set(Calendar.MONTH, 0);
            gc.set(Calendar.DAY_OF_MONTH, 1);
            gc.set(Calendar.HOUR, 0);
            gc.set(Calendar.MINUTE, 0);
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
        }
        if (QUARTER.equals(intervalType)) {
            int currentMonth = gc.get(Calendar.MONTH);
            int firstMonthYear = columnGroup.getFirstMonthOfYear().getIndex();
            int rest = Quarter.getPositionInQuarter(firstMonthYear, currentMonth);
            gc.add(Calendar.MONTH, rest * -1);
            gc.set(Calendar.DAY_OF_MONTH, 1);
            gc.set(Calendar.HOUR, 0);
            gc.set(Calendar.MINUTE, 0);
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
        }
        if (MONTH.equals(intervalType)) {
            gc.set(Calendar.DAY_OF_MONTH, 1);
            gc.set(Calendar.HOUR, 0);
            gc.set(Calendar.MINUTE, 0);
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
        }
        if (DAY.equals(intervalType) || DAY_OF_WEEK.equals(intervalType)) {
            gc.set(Calendar.HOUR, 0);
            gc.set(Calendar.MINUTE, 0);
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
        }
        if (HOUR.equals(intervalType)) {
            gc.set(Calendar.MINUTE, 0);
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
        }
        if (MINUTE.equals(intervalType)) {
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
        }
        if (SECOND.equals(intervalType)) {
            gc.set(Calendar.MILLISECOND, 0);
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

        // Reverse intervals if requested
        boolean asc = columnGroup.isAscendingOrder();
        if (!asc) Collections.reverse( results );

        return results;
    }

    private static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

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
            Locale l = Locale.getDefault();
            if (MILLENIUM.equals(intervalType)) {
                SimpleDateFormat formatYear  = new SimpleDateFormat("yyyy", l);
                return formatYear.format(d);
            }
            if (CENTURY.equals(intervalType)) {
                SimpleDateFormat formatYear  = new SimpleDateFormat("yyyy", l);
                return formatYear.format(d);
            }
            if (DECADE.equals(intervalType)) {
                SimpleDateFormat formatYear  = new SimpleDateFormat("yyyy", l);
                return formatYear.format(d);
            }
            if (YEAR.equals(intervalType)) {
                SimpleDateFormat format  = new SimpleDateFormat("yyyy", l);
                return format.format(d);
            }
            if (QUARTER.equals(intervalType)) {
                SimpleDateFormat format = new SimpleDateFormat("MMM yyyy", l);
                return format.format(d);
            }
            if (MONTH.equals(intervalType)) {
                SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", l);
                return format.format(d);
            }
            if (WEEK.equals(intervalType)) {
                return DateFormat.getDateInstance(DateFormat.SHORT, l).format(d);
            }
            if (DAY.equals(intervalType) || DAY_OF_WEEK.equals(intervalType)) {
                SimpleDateFormat format = new SimpleDateFormat("EEE", l);
                return format.format(d) + " " + DateFormat.getDateInstance(DateFormat.SHORT, l).format(d);
            }
            if (HOUR.equals(intervalType)) {
                SimpleDateFormat format = new SimpleDateFormat("HH", l);
                return format.format(d) + "h";
            }
            if (MINUTE.equals(intervalType)) {
                SimpleDateFormat format = new SimpleDateFormat("mm", l);
                return format.format(d);
            }
            if (SECOND.equals(intervalType)) {
                SimpleDateFormat format = new SimpleDateFormat("ss", l);
                return format.format(d);
            }
            return format.format(d);
        }
    }
}
