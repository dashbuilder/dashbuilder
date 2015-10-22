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
package org.dashbuilder.dataset.client;

import java.util.Date;
import javax.enterprise.context.Dependent;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import org.dashbuilder.dataset.DataSetValueFormatter;

@Dependent
public final class ClientDataSetValueFormatter implements DataSetValueFormatter {

    private static final DateTimeFormat _dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
    protected NumberFormat _numberFormat = NumberFormat.getFormat("#,###.##");

    public String formatValue(Object value) {
        try {
            return _numberFormat.format((Number) value);
        } catch (Exception e) {
            try {
                return _dateFormat.format((Date) value);
            } catch (Exception e1) {
                return value.toString();
            }
        }
    }

    public Comparable parseValue(String str) {
        try {
            return _numberFormat.parse(str);
        } catch (Exception e) {
            try {
                return _dateFormat.parse(str);
            } catch (Exception e1) {
                return str;
            }
        }
    }
}
