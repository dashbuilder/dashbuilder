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
package org.dashbuilder.dataset.date;

import java.sql.Time;
import java.util.Date;

import org.dashbuilder.dataset.group.DateIntervalType;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * This class is used to represent a given time instant relative to the current time.
 * <p>Some examples of time instants are:
 * <ul>
 * <li><i>now</i></li>
 * <li><i>now -10second</i> or just <i>-10second</i></li>
 * <li><i>begin[minute]</i> => begin of current minute. It turns back the clock to first second.</li>
 * <li><i>begin[year March]</i> => begin of current year (year starting on March)</li>
 * <li><i>end[year March] +1year</i> => end of next year (year starting on March)</li>
 * <li><i>begin[year March] -7day</i> => last year's last week start</li>
 * </ul>
 * </p>
 */
@Portable
public class TimeInstant {

    public static enum TimeMode {
        NOW,
        BEGIN,
        END;

        public static TimeMode getByName(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }

    private TimeMode timeMode = null;
    private DateIntervalType intervalType = DateIntervalType.YEAR;
    private Month firstMonthOfYear = Month.JANUARY;
    private TimeAmount timeAmount = null;

    /**
     * The date used as the relative time from which NOW based calculations must be done.
     */
    private transient Date startTime = null;

    public TimeInstant() {
        this(TimeMode.NOW, null, null, null);
    }

    public TimeInstant(TimeMode timeMode, DateIntervalType intervalType, Month firstMonthOfYear, TimeAmount timeAmount) {
        this.timeMode = timeMode;
        this.intervalType = intervalType;
        this.firstMonthOfYear = firstMonthOfYear;
        this.timeAmount = timeAmount;
    }

    public TimeMode getTimeMode() {
        return timeMode;
    }

    public void setTimeMode(TimeMode timeMode) {
        this.timeMode = timeMode;
    }

    public DateIntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(DateIntervalType intervalType) {
        this.intervalType = intervalType;
    }

    public Month getFirstMonthOfYear() {
        return firstMonthOfYear;
    }

    public void setFirstMonthOfYear(Month firstMonthOfYear) {
        this.firstMonthOfYear = firstMonthOfYear;
    }

    public TimeAmount getTimeAmount() {
        return timeAmount;
    }

    public void setTimeAmount(TimeAmount timeAmount) {
        this.timeAmount = timeAmount;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        if (timeMode != null && !TimeMode.NOW.equals(timeMode)) {
            out.append(timeMode).append("[").append(intervalType);
            if (intervalType != null && intervalType.getIndex() > DateIntervalType.MONTH.getIndex()) {
                out.append(" ").append(firstMonthOfYear);
            }
            out.append("] ");
        } else {
            out.append(TimeMode.NOW).append(" ");
        }
        if (timeAmount != null) out.append(timeAmount);
        return out.toString();
    }

    public Date getTimeInstant() {
        Date _start = calculateStartTime();
        if (timeAmount != null) timeAmount.adjustDate(_start);
        return _start;
    }

    public void setStartTime(Date now) {
        this.startTime = now;
    }

    public static Date START_TIME = null;
    
    public Date getStartTime() {
        if (startTime == null) {
            if (START_TIME != null) return new Date(START_TIME.getTime());
            return new Date();
        }
        return startTime;
    }

    protected Date calculateStartTime() {
        Date startDate = getStartTime();
        if (timeMode == null || TimeMode.NOW.equals(timeMode)) {
            return startDate;
        }

        if (DateIntervalType.MILLENIUM.equals(intervalType)) {
            int base = startDate.getYear() / 1000;
            int inc =  TimeMode.END.equals(timeMode) ? 1 : 0;
            startDate.setYear((base + inc) * 1000);
            startDate.setMonth(firstMonthOfYear.getIndex()-1);
            startDate.setDate(1);
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
        }
        if (DateIntervalType.CENTURY.equals(intervalType)) {
            int base = startDate.getYear() / 100;
            int inc =  TimeMode.END.equals(timeMode) ? 1 : 0;
            startDate.setYear((base + inc) * 100);
            startDate.setMonth(firstMonthOfYear.getIndex()-1);
            startDate.setDate(1);
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
        }
        if (DateIntervalType.DECADE.equals(intervalType)) {
            int base = startDate.getYear() / 10;
            int inc =  TimeMode.END.equals(timeMode) ? 1 : 0;
            startDate.setYear((base + inc) * 10);
            startDate.setMonth(firstMonthOfYear.getIndex()-1);
            startDate.setDate(1);
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
        }
        if (DateIntervalType.YEAR.equals(intervalType)) {
            int month = startDate.getMonth();
            int firstMonth = firstMonthOfYear.getIndex()-1;
            int inc =  0;
            if (TimeMode.BEGIN.equals(timeMode)) inc = (month < firstMonth ? -1 : 0);
            else inc = (month < firstMonth ? 0 : 1);

            startDate.setYear(startDate.getYear() + inc);
            startDate.setMonth(firstMonth);
            startDate.setDate(1);
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
        }
        if (DateIntervalType.QUARTER.equals(intervalType)) {
            int month = startDate.getMonth();
            int firstMonth = Quarter.getQuarterFirstMonth(firstMonthOfYear.getIndex(), month + 1)-1;
            int inc = 0;
            if (TimeMode.BEGIN.equals(timeMode)) inc = (month > 0 && firstMonth<12 ? -1 : 0);
            else inc = (month<12 && firstMonth>=0 ? 1 : 0);

            startDate.setYear(startDate.getYear() + inc);
            startDate.setMonth(firstMonth);
            startDate.setDate(1);
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
        }
        if (DateIntervalType.MONTH.equals(intervalType)) {
            startDate.setDate(1);
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
            if (TimeMode.END.equals(timeMode)) {
                startDate.setMonth(startDate.getMonth()+1);
            }
        }
        if (DateIntervalType.DAY.equals(intervalType)) {
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
            if (TimeMode.END.equals(timeMode)) {
                startDate.setDate(startDate.getDate()+1);
            }
        }
        if (DateIntervalType.HOUR.equals(intervalType)) {
            startDate.setMinutes(0);
            startDate.setSeconds(0);
            if (TimeMode.END.equals(timeMode)) {
                startDate.setHours(startDate.getHours()+1);
            }
        }
        if (DateIntervalType.MINUTE.equals(intervalType)) {
            startDate.setSeconds(0);
            if (TimeMode.END.equals(timeMode)) {
                startDate.setMinutes(startDate.getMinutes()+1);
            }
        }
        return startDate;
    }

    /**
     * Return a time instant representing the current time.
     */
    public static TimeInstant now() {
        return new TimeInstant(TimeMode.NOW, null, null, null);
    }

    /**
     * Parses a time instant expression.
     *
     * @param timeInstantExpr A valid time instant expression (<i>see TimeInstant class javadoc</i>)
     * @return A TimeInstant instance
     * @throws IllegalArgumentException If the expression is not valid
     */
    public static TimeInstant parse(String timeInstantExpr) {
        if (timeInstantExpr == null || timeInstantExpr.length() == 0) {
            throw new IllegalArgumentException("Empty time instant expression");
        }
        TimeInstant instant = new TimeInstant();
        String expr = timeInstantExpr.toLowerCase().trim();

        // now + time amount (optional)
        boolean begin = expr.startsWith("begin");
        boolean end  = expr.startsWith("end");
        if (!begin && !end) {
            if (expr.startsWith("now")) {
                instant.setTimeMode(TimeMode.NOW);
                if (expr.length() > 3) {
                    instant.setTimeAmount(TimeAmount.parse(expr.substring(3)));
                }
            } else {
                instant.setTimeMode(null);
                instant.setTimeAmount(TimeAmount.parse(expr));
            }
            return instant;
        }
        // begin/end modes
        instant.setTimeMode(begin ? TimeMode.BEGIN : TimeMode.END);

        // Look for braces limits "begin[year March]"
        String example = begin ? "begin[year March]" : "end[year March]";
        int bracesBegin = expr.indexOf("[");
        int bracesEnd = expr.indexOf("]");
        if (bracesBegin == -1 || bracesEnd == -1 || bracesBegin >= bracesEnd) {
            throw new IllegalArgumentException("Missing braces (ex '" + example + "'):  " + timeInstantExpr);
        }
        // Interval type
        String[] intervalTerms = expr.substring(bracesBegin+1, bracesEnd).split("\\s+");
        if (intervalTerms.length > 2) {
            throw new IllegalArgumentException("Too many settings (ex '" + example + "'):  " + timeInstantExpr);
        }
        instant.setIntervalType(DateIntervalType.getByName(intervalTerms[0]));
        if (instant.getIntervalType() == null) {
            throw new IllegalArgumentException("Invalid interval (ex '" + example + "'): " + timeInstantExpr);
        }

        // First month of year
        if (intervalTerms.length == 2) {
            instant.setFirstMonthOfYear(Month.getByName(intervalTerms[1]));
            if (instant.getFirstMonthOfYear() == null) {
                throw new IllegalArgumentException("Invalid first year month (ex '" + example + "'): " + timeInstantExpr);
            }
        }
        // Time amount
        if (bracesEnd < expr.length()) {
            expr = expr.substring(bracesEnd + 1).trim();
            if (!expr.isEmpty()) {
                TimeAmount timeAmount = TimeAmount.parse(expr);
                instant.setTimeAmount(timeAmount);
            }
        }
        return instant;
    }
}
