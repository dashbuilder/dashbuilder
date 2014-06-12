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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.dashbuilder.model.dataset.group.DateIntervalType;

import static org.dashbuilder.model.dataset.group.DateIntervalType.*;

/**
 * A date interval holding dates belonging to a given range.
 */
public class IntervalDateRange extends Interval {

    private static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

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
        // TODO: Get the proper locale
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
