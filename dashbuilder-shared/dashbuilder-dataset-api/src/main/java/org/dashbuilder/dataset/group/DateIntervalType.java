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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Type of data intervals
 */
@Portable
public enum DateIntervalType {
    MILLISECOND,
    HUNDRETH,
    TENTH,
    SECOND,
    MINUTE,
    HOUR,
    DAY,
    DAY_OF_WEEK,
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
    DECADE,
    CENTURY,
    MILLENIUM;

    /**
     * List of the only DateIntervalType's supported as fixed date intervals
     */
    public static List<DateIntervalType> FIXED_INTERVALS_SUPPORTED = Arrays.asList(
            QUARTER, MONTH, DAY_OF_WEEK, HOUR, MINUTE, SECOND);

    private static final Map<DateIntervalType,Long> DURATION_IN_MILLIS = new HashMap<DateIntervalType, Long>();
    static {
        long milli = 1;
        DURATION_IN_MILLIS.put(MILLISECOND, milli);

        long hundreth = 10;
        DURATION_IN_MILLIS.put(HUNDRETH, hundreth);

        long tenth = 100;
        DURATION_IN_MILLIS.put(TENTH, tenth);

        long second = 1000;
        DURATION_IN_MILLIS.put(SECOND, second);

        long minute = second*60;
        DURATION_IN_MILLIS.put(MINUTE, minute);

        long hour = minute*60;
        DURATION_IN_MILLIS.put(HOUR, hour);

        long day = hour*24;
        DURATION_IN_MILLIS.put(DAY, day);
        DURATION_IN_MILLIS.put(DAY_OF_WEEK, day);

        long week = day*7;
        DURATION_IN_MILLIS.put(WEEK, week);

        long month = day*31;
        DURATION_IN_MILLIS.put(MONTH, month);

        long quarter = month*3;
        DURATION_IN_MILLIS.put(QUARTER, quarter);

        long year = month*12;
        DURATION_IN_MILLIS.put(YEAR, year);

        long decade = year*10;
        DURATION_IN_MILLIS.put(DECADE, decade);

        long century = year*100;
        DURATION_IN_MILLIS.put(CENTURY, century);

        long millenium = year*1000;
        DURATION_IN_MILLIS.put(MILLENIUM, millenium);
    }

    private static DateIntervalType[] _typeArray = values();

    public int getIndex() {
        for (int i = 0; i < _typeArray.length; i++) {
            DateIntervalType type = _typeArray[i];
            if (this.equals(type)) return i;
        }
        return -1;
    }

    public static DateIntervalType getByName(String interval) {
        try {
            return valueOf(interval.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static int compare(DateIntervalType interval1, DateIntervalType interval2) {
        long d1 = getDurationInMillis(interval1);
        long d2 = getDurationInMillis(interval2);
        return Long.valueOf(d1).compareTo(d2);
    }

    public static long getDurationInMillis(DateIntervalType type) {
        if (!DURATION_IN_MILLIS.containsKey(type)) return 0;
        return DURATION_IN_MILLIS.get(type);
    }

    public static long getDurationInMillis(String timeFrame) {
        if (timeFrame == null || timeFrame.length() == 0) {
            return -1;
        }
        String number = "";
        int i = 0;
        for (; i<timeFrame.length(); i++) {
            char ch = timeFrame.charAt(i);
            if (Character.isDigit(ch)) number += ch;
            else break;
        }
        String type = timeFrame.substring(i).trim();
        DateIntervalType intervalType = getByName(type);
        if (intervalType == null) return -1;
        if (number.length() == 0) return -1;

        return Long.parseLong(number) * getDurationInMillis(intervalType);
    }
}
