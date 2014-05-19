/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.dataset.index.stats;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SizeEstimator {

    public static int sizeOfDate = 20;
    public static int sizeOfTimestamp = 24;
    public static int sizeOfBoolean = 9;
    public static int sizeOfByte = 9;
    public static int sizeOfShort = 10;
    public static int sizeOfInteger = 12;
    public static int sizeOfLong = 16;
    public static int sizeOfFloat = 12;
    public static int sizeOfDouble = 16;
    public static int sizeOfBigDecimal = 32;

    static Map<Class,Integer> sizeOfMap = new HashMap<Class, Integer>();

    static {
        sizeOfMap.put(Date.class, sizeOfDate);
        sizeOfMap.put(Timestamp.class, sizeOfTimestamp);
        sizeOfMap.put(Boolean.class, sizeOfBoolean);
        sizeOfMap.put(Byte.class, sizeOfByte);
        sizeOfMap.put(Short.class, sizeOfShort);
        sizeOfMap.put(Integer.class, sizeOfInteger);
        sizeOfMap.put(Long.class, sizeOfLong);
        sizeOfMap.put(Float.class, sizeOfFloat);
        sizeOfMap.put(Double.class, sizeOfDouble);
        sizeOfMap.put(BigDecimal.class, sizeOfBigDecimal);
    }

    public static int sizeOf(Object o) {
        if (o == null) return 0;

        Integer size = sizeOfMap.get(o.getClass());
        return (size != null ? size : 0);
    }

    public static int sizeOfString(String s) {
        if (s == null) return 0;

        return 40 + s.length()*2;
    }

    public static final String SIZE_UNITS[] = new String[] {"bytes", "Kb", "Mb", "Gb", "Tb", "Pb"};

    public static String formatSize(long bytes) {
        for (int exp=SIZE_UNITS.length-1; exp>=0; exp--) {
            String sizeUnit = SIZE_UNITS[exp];
            double size = bytes / Math.pow(1024, exp);
            if (((long) size) > 0) {
                NumberFormat df = DecimalFormat.getInstance();
                return df.format(size) + " " + sizeUnit;
            }
        }
        return bytes + " bytes";
    }
}
