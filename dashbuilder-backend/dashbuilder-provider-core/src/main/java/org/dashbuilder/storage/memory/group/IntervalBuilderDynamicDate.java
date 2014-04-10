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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.Domain;
import org.dashbuilder.model.dataset.group.DomainStrategy;
import org.dashbuilder.storage.memory.ComparableValue;
import org.dashbuilder.storage.memory.SortedList;
import org.dashbuilder.storage.memory.TransientDataSetStorage;

/**
 * Interval builder for date columns which generates intervals depending on the underlying data available.
 */
@ApplicationScoped
public class IntervalBuilderDynamicDate implements IntervalBuilder {

    @Inject TransientDataSetStorage dataSetStorage;

    public List<Interval> build(DataColumn column, Domain domain) {
        // Sort the target dates.
        List<Interval> results = new ArrayList<Interval>();
        SortedList<ComparableValue> values = dataSetStorage.sortColumn(column);
        if (values.isEmpty()) return results;

        // Get the low & upper limits.
        Date minDate = (Date) values.getMin().getValue();
        Date maxDate = (Date) values.getMax().getValue();

        // If min/max are equals then create a single interval.
        if (minDate.compareTo(maxDate) == 0) {
            String intervalName = getIntervalName(DateIntervalType.DAY, minDate);
            Interval interval = new Interval(intervalName);
            for (int row = 0; row < values.size(); row++) interval.rows.add(row);
            results.add(interval);
            return results;
        }

        // Calculate the interval type to be used according to the constraints set.
        int maxIntervals = domain.getMaxIntervals();
        if (maxIntervals < 1) maxIntervals = 15;
        DateIntervalType intervalType = DateIntervalType.YEAR;
        long seconds = (maxDate.getTime() - minDate.getTime()) / 1000;
        for (DateIntervalType type : DateIntervalType.values()) {
            long nintervals = seconds / DateIntervalType.getDurationInSeconds(type);
            if (nintervals < maxIntervals) {
                intervalType = type;
                break;
            }
        }

        // Ensure the interval mode obtained is always greater or equals than the preferred interval size.
        DateIntervalType intervalSize = null;
        if (!StringUtils.isBlank(domain.getIntervalSize())) {
            intervalSize = DateIntervalType.getByName(domain.getIntervalSize());
        }
        if (intervalSize != null && DateIntervalType.compare(intervalType, intervalSize) == -1) {
            intervalType = intervalSize;
        }

        // Adjust the minDate according to the interval type.
        Calendar gc = GregorianCalendar.getInstance();
        gc.setLenient(false);
        gc.setTime(minDate);
        if (DateIntervalType.YEAR.equals(intervalType)) {
            gc.add(Calendar.MONTH, gc.get(Calendar.MONTH) * -1);
        }
        if (DateIntervalType.MONTH.equals(intervalType)) {
            gc.add(Calendar.DAY_OF_MONTH, (gc.get(Calendar.DAY_OF_MONTH) - 1) * -1);
        }
        if (DateIntervalType.DAY.equals(intervalType) || DateIntervalType.DAY_OF_WEEK.equals(intervalType)) {
            gc.add(Calendar.HOUR, gc.get(Calendar.HOUR_OF_DAY) * -1);
        }
        if (DateIntervalType.HOUR.equals(intervalType)) {
            gc.add(Calendar.MINUTE, gc.get(Calendar.MINUTE) * -1);
        }
        if (DateIntervalType.MINUTE.equals(intervalType)) {
            gc.add(Calendar.SECOND, gc.get(Calendar.SECOND) * -1);
        }
        if (DateIntervalType.SECOND.equals(intervalType)) {
            gc.add(Calendar.MILLISECOND, gc.get(Calendar.MILLISECOND) * -1);
        }

        // Create the intervals according to the min/max dates.
        int index = 0;
        while (gc.getTime().compareTo(maxDate) <= 0) {
            Date intervalMinDate = gc.getTime();

            // Go to the next interval
            if (DateIntervalType.MILLENIUM.equals(intervalType)) {
                gc.add(Calendar.YEAR, 1000);
            }
            if (DateIntervalType.CENTURY.equals(intervalType)) {
                gc.add(Calendar.YEAR, 100);
            }
            if (DateIntervalType.DECADE.equals(intervalType)) {
                gc.add(Calendar.YEAR, 10);
            }
            if (DateIntervalType.YEAR.equals(intervalType)) {
                gc.add(Calendar.YEAR, 1);
            }
            if (DateIntervalType.QUARTER.equals(intervalType)) {
                gc.add(Calendar.MONTH, 3);
            }
            if (DateIntervalType.MONTH.equals(intervalType)) {
                gc.add(Calendar.MONTH, 1);
            }
            if (DateIntervalType.WEEK.equals(intervalType)) {
                gc.add(Calendar.DAY_OF_MONTH, 7);
            }
            if (DateIntervalType.DAY.equals(intervalType) || DateIntervalType.DAY_OF_WEEK.equals(intervalType)) {
                gc.add(Calendar.DAY_OF_MONTH, 1);
            }
            if (DateIntervalType.HOUR.equals(intervalType)) {
                gc.add(Calendar.HOUR_OF_DAY, 1);
            }
            if (DateIntervalType.MINUTE.equals(intervalType)) {
                gc.add(Calendar.MINUTE, 1);
            }
            if (DateIntervalType.SECOND.equals(intervalType)) {
                gc.add(Calendar.SECOND, 1);
            }

            // Create the interval.
            String intervalName = getIntervalName(intervalType, intervalMinDate);
            Interval interval = new Interval(intervalName);
            results.add(interval);

            // Add the target rows to the interval.
            Date intervalMaxDate = gc.getTime();
            boolean stop = false;
            while (!stop) {
                if (index >= values.size()) {
                    stop = true;
                } else {
                    ComparableValue valueHolder = values.get(index);
                    Date dateValue = (Date) valueHolder.getValue();
                    if (dateValue.before(intervalMaxDate)){
                        interval.rows.add(valueHolder.getRow());
                        index++;
                    } else {
                        stop = true;
                    }
                }
            }
        }
        return results;
    }

    public String getIntervalName(DateIntervalType intervalType, Date d) {
        // TODO: Get the proper locale
        Locale l = Locale.getDefault();
        if (DateIntervalType.MILLENIUM.equals(intervalType)) {
            SimpleDateFormat formatYear  = new SimpleDateFormat("yyyy", l);
            return formatYear.format(d);
        }
        if (DateIntervalType.CENTURY.equals(intervalType)) {
            SimpleDateFormat formatYear  = new SimpleDateFormat("yyyy", l);
            return formatYear.format(d);
        }
        if (DateIntervalType.DECADE.equals(intervalType)) {
            SimpleDateFormat formatYear  = new SimpleDateFormat("yyyy", l);
            return formatYear.format(d);
        }
        if (DateIntervalType.YEAR.equals(intervalType)) {
            SimpleDateFormat format  = new SimpleDateFormat("yyyy", l);
            return format.format(d);
        }
        if (DateIntervalType.QUARTER.equals(intervalType)) {
            SimpleDateFormat format = new SimpleDateFormat("MMM yyyy", l);
            return format.format(d);
        }
        if (DateIntervalType.MONTH.equals(intervalType)) {
            SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", l);
            return format.format(d);
        }
        if (DateIntervalType.WEEK.equals(intervalType)) {
            return DateFormat.getDateInstance(DateFormat.SHORT, l).format(d);
        }
        if (DateIntervalType.DAY.equals(intervalType) || DateIntervalType.DAY_OF_WEEK.equals(intervalType)) {
            SimpleDateFormat format = new SimpleDateFormat("EEE", l);
            return format.format(d) + " " + DateFormat.getDateInstance(DateFormat.SHORT, l).format(d);
        }
        if (DateIntervalType.HOUR.equals(intervalType)) {
            SimpleDateFormat format = new SimpleDateFormat("HH", l);
            return format.format(d) + "h";
        }
        if (DateIntervalType.MINUTE.equals(intervalType)) {
            SimpleDateFormat format = new SimpleDateFormat("mm", l);
            return format.format(d);
        }
        if (DateIntervalType.SECOND.equals(intervalType)) {
            SimpleDateFormat format = new SimpleDateFormat("ss", l);
            return format.format(d);
        }
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", l);
        return format.format(d);
    }
}
