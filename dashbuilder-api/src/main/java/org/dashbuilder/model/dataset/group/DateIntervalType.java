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
package org.dashbuilder.model.dataset.group;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Type of data intervals
 */
@Portable
public enum DateIntervalType {
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

    public static DateIntervalType getByName(String interval) {
        return valueOf(interval.toUpperCase());
    }

    public static int compare(DateIntervalType interval1, DateIntervalType interval2) {
        long d1 = getDurationInSeconds(interval1);
        long d2 = getDurationInSeconds(interval2);
        return Long.valueOf(d1).compareTo(d2);
    }

    public static long getDurationInSeconds(String interval) {
        return getDurationInSeconds(getByName(interval));
    }

    public static long getDurationInSeconds(DateIntervalType type) {
        switch (type) {
            case SECOND: return 1;
            case MINUTE: return getDurationInSeconds(SECOND)*60;
            case HOUR: return getDurationInSeconds(MINUTE)*60;
            case DAY: return getDurationInSeconds(HOUR)*24;
            case DAY_OF_WEEK: return getDurationInSeconds(DAY);
            case WEEK: return getDurationInSeconds(DAY)*7;
            case MONTH: return getDurationInSeconds(DAY)*31;
            case QUARTER: return getDurationInSeconds(MONTH)*4;
            case YEAR: return getDurationInSeconds(MONTH)*12;
            case DECADE: return getDurationInSeconds(YEAR)*10;
            case CENTURY: return getDurationInSeconds(YEAR)*100;
            case MILLENIUM: return getDurationInSeconds(YEAR)*1000;
            default: return 0;
        }
    }
}
