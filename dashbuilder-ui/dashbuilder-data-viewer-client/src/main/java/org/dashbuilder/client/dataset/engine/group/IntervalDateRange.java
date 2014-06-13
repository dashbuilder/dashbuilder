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
package org.dashbuilder.client.dataset.engine.group;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.dashbuilder.model.dataset.group.DateIntervalType;

import static org.dashbuilder.model.dataset.group.DateIntervalType.*;

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
