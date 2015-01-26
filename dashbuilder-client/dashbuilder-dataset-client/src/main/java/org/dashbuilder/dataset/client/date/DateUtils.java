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
package org.dashbuilder.dataset.client.date;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.client.resources.i18n.DayOfWeekConstants;
import org.dashbuilder.dataset.client.resources.i18n.MonthConstants;
import org.dashbuilder.dataset.client.resources.i18n.QuarterConstants;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.date.Quarter;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.*;

public class DateUtils {

    public static final DateTimeFormat PARSER_YEAR  = DateTimeFormat.getFormat("yyyy");
    public static final DateTimeFormat PARSER_MONTH = DateTimeFormat.getFormat("yyyy-MM");
    public static final DateTimeFormat PARSER_DAY = DateTimeFormat.getFormat("yyyy-MM-dd");
    public static final DateTimeFormat PARSER_HOUR = DateTimeFormat.getFormat("yyyy-MM-dd HH");
    public static final DateTimeFormat PARSER_MINUTE = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm");
    public static final DateTimeFormat PARSER_SECOND = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormat FORMAT_YEAR  = DateTimeFormat.getFormat("yyyy");
    public static final DateTimeFormat FORMAT_MONTH = DateTimeFormat.getFormat("MMM yyyy");
    public static final DateTimeFormat FORMAT_QUARTER = DateTimeFormat.getFormat("MMM yyyy");
    public static final DateTimeFormat FORMAT_DAY = DateTimeFormat.getFormat("dd MMM");
    public static final DateTimeFormat FORMAT_WEEK = DateTimeFormat.getFormat("'Week' dd MMM");
    public static final DateTimeFormat FORMAT_HOUR = DateTimeFormat.getFormat("HH'h'");
    public static final DateTimeFormat FORMAT_MINUTE = DateTimeFormat.getFormat("mm'm'");
    public static final DateTimeFormat FORMAT_SECOND = DateTimeFormat.getFormat("ss's'");

    public static Date parseDynamicGroupDate(DateIntervalType type, String date) {
        if (type.getIndex() <= DateIntervalType.SECOND.getIndex()) {
            return PARSER_SECOND.parse(date);
        }
        if (DateIntervalType.MINUTE.equals(type)) {
            return PARSER_MINUTE.parse(date);
        }
        if (DateIntervalType.HOUR.equals(type)) {
            return PARSER_HOUR.parse(date);
        }
        if (DateIntervalType.DAY.equals(type) || DateIntervalType.WEEK.equals(type)) {
            return PARSER_DAY.parse(date);
        }
        if (DateIntervalType.MONTH.equals(type) || DateIntervalType.QUARTER.equals(type)) {
            return PARSER_MONTH.parse(date);
        }
        return PARSER_YEAR.parse(date);
    }
    
    public static String formatDate(DateIntervalType type, GroupStrategy strategy, String date) {
        if (date == null) return null;

        // Fixed grouping
        if (GroupStrategy.FIXED.equals(strategy)) {
            if (DateIntervalType.SECOND.equals(type)) {
                return date + "s";
            }
            if (DateIntervalType.MINUTE.equals(type)) {
                return date + "'";
            }
            if (DateIntervalType.HOUR.equals(type)) {
                return date + "h";
            }
            if (DateIntervalType.DAY_OF_WEEK.equals(type)) {
                DayOfWeek dayOfWeek = DayOfWeek.getByIndex(Integer.parseInt(date));
                return DayOfWeekConstants.INSTANCE.getString(dayOfWeek.name());
            }
            if (DateIntervalType.MONTH.equals(type)) {
                Month month = Month.getByIndex(Integer.parseInt(date));
                return MonthConstants.INSTANCE.getString(month.name());
            }
            if (DateIntervalType.QUARTER.equals(type)) {
                Quarter quarter = Quarter.getByIndex(Integer.parseInt(date));
                return QuarterConstants.INSTANCE.getString(quarter.name());
            }
            throw new IllegalArgumentException("Interval size '" + type + "' not supported for " +
                    "fixed date intervals. The only supported sizes are: " +
                    StringUtils.join(DateIntervalType.FIXED_INTERVALS_SUPPORTED, ","));
        }

        // Dynamic grouping
        Date d = parseDynamicGroupDate(type, date);
        if (type.getIndex() <= DateIntervalType.SECOND.getIndex()) {
            return FORMAT_SECOND.format(d);
        }
        if (DateIntervalType.MINUTE.equals(type)) {
            return FORMAT_MINUTE.format(d);
        }
        if (DateIntervalType.HOUR.equals(type)) {
            return FORMAT_HOUR.format(d);
        }
        if (DateIntervalType.DAY.equals(type)) {
            return FORMAT_DAY.format(d);
        }
        if (DateIntervalType.WEEK.equals(type)) {
            return FORMAT_WEEK.format(d);
        }
        if (DateIntervalType.MONTH.equals(type)) {
            return FORMAT_MONTH.format(d);
        }
        if (DateIntervalType.QUARTER.equals(type)) {
            String result = FORMAT_QUARTER.format(d);
            int endMonth = d.getMonth() + 2;
            d.setMonth(endMonth % 12);
            if (endMonth > 11) d.setYear(d.getYear() + 1);
            return result + " - " + FORMAT_QUARTER.format(d);
        }
        return FORMAT_YEAR.format(d);
    }
}
