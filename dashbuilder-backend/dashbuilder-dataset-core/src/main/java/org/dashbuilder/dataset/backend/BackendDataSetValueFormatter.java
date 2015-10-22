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
package org.dashbuilder.dataset.backend;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.enterprise.context.Dependent;

import org.dashbuilder.dataset.DataSetValueFormatter;
import org.dashbuilder.dataset.date.TimeFrame;

@Dependent
public final class BackendDataSetValueFormatter implements DataSetValueFormatter {

    private static final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat _numberFormat;
    static {
        DecimalFormatSymbols numberSymbols = new DecimalFormatSymbols();
        numberSymbols.setGroupingSeparator(',');
        numberSymbols.setDecimalSeparator('.');
        _numberFormat = new DecimalFormat("#,###.##", numberSymbols);
    }
    
    public String formatValue(Object value) {
        try {
            Number n = (Number) value;
            return _numberFormat.format(n);
        } catch (Exception e) {
            try {
                Date d = (Date) value;
                return _dateFormat.format(d);
            } catch (Exception e1) {
                return value.toString();
            }
        }
    }

    public Comparable parseValue(String str) {
        try {
            return _dateFormat.parse(str);
        } catch (Exception e) {
            try {
                return (Comparable) _numberFormat.parse(str);
            } catch (Exception e1) {
                return str;
            }
        }
    }
}
