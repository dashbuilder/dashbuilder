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
package org.dashbuilder.storage.memory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.config.Config;

@ApplicationScoped
public class SizeEstimator {

    @Inject @Config("20")
    protected int sizeOfDate;

    @Inject @Config("24")
    protected int sizeOfTimestamp;

    @Inject @Config("9")
    protected int sizeOfBoolean;

    @Inject @Config("9")
    protected int sizeOfByte;

    @Inject @Config("10")
    protected int sizeOfShort;

    @Inject @Config("12")
    protected int sizeOfInteger;

    @Inject @Config("16")
    protected int sizeOfLong;

    @Inject @Config("12")
    protected int sizeOfFloat;

    @Inject @Config("16")
    protected int sizeOfDouble;

    @Inject @Config("32")
    protected int sizeOfBigDecimal;

    protected Map<Class,Integer> sizeOfMap = new HashMap<Class, Integer>();

    @PostConstruct
    protected void init() {
        sizeOfMap.put(Date.class, getSizeOfDate());
        sizeOfMap.put(Timestamp.class, getSizeOfTimestamp());
        sizeOfMap.put(Boolean.class, getSizeOfBoolean());
        sizeOfMap.put(Byte.class, getSizeOfByte());
        sizeOfMap.put(Short.class, getSizeOfShort());
        sizeOfMap.put(Integer.class, getSizeOfInteger());
        sizeOfMap.put(Long.class, getSizeOfLong());
        sizeOfMap.put(Float.class, getSizeOfFloat());
        sizeOfMap.put(Double.class, getSizeOfDouble());
        sizeOfMap.put(BigDecimal.class, getSizeOfBigDecimal());
    }

    public int getSizeOfDate() {
        return sizeOfDate;
    }

    public void setSizeOfDate(int sizeOfDate) {
        this.sizeOfDate = sizeOfDate;
    }

    public int getSizeOfTimestamp() {
        return sizeOfTimestamp;
    }

    public void setSizeOfTimestamp(int sizeOfTimestamp) {
        this.sizeOfTimestamp = sizeOfTimestamp;
    }

    public int getSizeOfBoolean() {
        return sizeOfBoolean;
    }

    public void setSizeOfBoolean(int sizeOfBoolean) {
        this.sizeOfBoolean = sizeOfBoolean;
    }

    public int getSizeOfByte() {
        return sizeOfByte;
    }

    public void setSizeOfByte(int sizeOfByte) {
        this.sizeOfByte = sizeOfByte;
    }

    public int getSizeOfShort() {
        return sizeOfShort;
    }

    public void setSizeOfShort(int sizeOfShort) {
        this.sizeOfShort = sizeOfShort;
    }

    public int getSizeOfInteger() {
        return sizeOfInteger;
    }

    public void setSizeOfInteger(int sizeOfInteger) {
        this.sizeOfInteger = sizeOfInteger;
    }

    public int getSizeOfLong() {
        return sizeOfLong;
    }

    public void setSizeOfLong(int sizeOfLong) {
        this.sizeOfLong = sizeOfLong;
    }

    public int getSizeOfFloat() {
        return sizeOfFloat;
    }

    public void setSizeOfFloat(int sizeOfFloat) {
        this.sizeOfFloat = sizeOfFloat;
    }

    public int getSizeOfDouble() {
        return sizeOfDouble;
    }

    public void setSizeOfDouble(int sizeOfDouble) {
        this.sizeOfDouble = sizeOfDouble;
    }

    public int getSizeOfBigDecimal() {
        return sizeOfBigDecimal;
    }

    public void setSizeOfBigDecimal(int sizeOfBigDecimal) {
        this.sizeOfBigDecimal = sizeOfBigDecimal;
    }

    public int sizeOf(Object o) {
        if (o == null) return 0;

        Integer size = sizeOfMap.get(o.getClass());
        return (size != null ? size : 0);
    }

    public int sizeOfString(String s) {
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
