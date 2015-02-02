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
 * A time frame
 */
@Portable
public class TimeFrame {

    private long amount;
    private DateIntervalType type;

    public TimeFrame() {
        this(1, DateIntervalType.YEAR);
    }

    public TimeFrame(long amount, DateIntervalType type) {
        this.amount = amount;
        this.type = type;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public DateIntervalType getType() {
        return type;
    }

    public void setType(DateIntervalType type) {
        this.type = type;
    }

    public long toMillis() {
        return amount * DateIntervalType.getDurationInMillis(type);
    }

    public String toString() {
        return amount + " " + type.name().toLowerCase();
    }

    public static TimeFrame parse(String timeFrame) {
        if (timeFrame == null || timeFrame.length() == 0) {
            return null;
        }
        String number = "";
        int i = 0;
        for (; i<timeFrame.length(); i++) {
            char ch = timeFrame.charAt(i);
            if (Character.isDigit(ch)) number += ch;
            else break;
        }
        String type = timeFrame.substring(i).trim();
        DateIntervalType intervalType = DateIntervalType.getByName(type);
        if (intervalType == null) return null;
        if (number.length() == 0) return null;

        return new TimeFrame(Long.parseLong(number), intervalType);
    }
}
