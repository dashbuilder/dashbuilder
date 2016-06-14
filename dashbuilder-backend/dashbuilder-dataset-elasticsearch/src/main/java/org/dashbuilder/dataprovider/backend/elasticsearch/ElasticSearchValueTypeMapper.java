/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.FieldMappingResponse;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * <p>Data Types mapper for String serialization and de-serialization.</p>
 * <p>Supported types:</p>
 * <ul>
 *     <li>TEXT or LABEL</li>
 *     <li>NUMERIC</li>
 *     <li>DATE</li>
 * </ul>
 * @since 0.3.0
 */
public class ElasticSearchValueTypeMapper {
    
    // float, double, byte, short, integer, and long + custom
    private static final String DATE_DEFAULT_FORMAT_KEY = "dateOptionalTime";
    private static final DateTimeFormatter DATE_DEFAULT_FORMAT_PARSER = ISODateTimeFormat.dateOptionalTimeParser();
    private static final DateTimeFormatter DATE_DEFAULT_FORMAT_PRINTER = ISODateTimeFormat.dateTime();
    private static final String NUMERIC_FLOAT = FieldMappingResponse.FieldType.FLOAT.name().toLowerCase();
    private static final String NUMERIC_DOUBLE = FieldMappingResponse.FieldType.DOUBLE.name().toLowerCase();
    private static final String NUMERIC_SHORT = FieldMappingResponse.FieldType.SHORT.name().toLowerCase();
    private static final String NUMERIC_INTEGER = FieldMappingResponse.FieldType.INTEGER.name().toLowerCase();
    private static final String NUMERIC_LONG = FieldMappingResponse.FieldType.LONG.name().toLowerCase();
    
    public String defaultDateFormat() {
        return DATE_DEFAULT_FORMAT_KEY;
    }

    public String defaulNumberFormat() {
        return NUMERIC_DOUBLE;
    }

    public String parseText(ElasticSearchDataSetDef definition, String columnId, String string) throws ParseException {
        return parseString(definition, columnId, string, false);
    }

    public String parseLabel(ElasticSearchDataSetDef definition, String columnId, String string, boolean isColumnGroup) throws ParseException {
        return parseString(definition, columnId, string, true);
    }


    protected String parseString(ElasticSearchDataSetDef definition, String columnId, String string, boolean isColumnGroup) throws ParseException {
        if (string == null) return "";
        
        // FIXED column groups specific parsing. Remove the 0 character at initial position. 
        if (isColumnGroup && string.startsWith("0")) {
            return string.substring(1);
        }
        
        return string;
    }

    public Double parseNumeric(ElasticSearchDataSetDef definition, String columnId, String number) throws ParseException {
        if (isEmpty(number)) return 0d;
        return new Double(number);
    }

    public Date parseDate(ElasticSearchDataSetDef definition, String columnId, String date) throws ParseException {
        if (isEmpty(date)) return null;
        
        String datePattern = definition.getPattern(columnId);
        boolean isDefaultDateFormat = isEmpty(datePattern) || datePattern.equalsIgnoreCase(DATE_DEFAULT_FORMAT_KEY);
        DateTimeFormatter formatter = isDefaultDateFormat ? DATE_DEFAULT_FORMAT_PARSER : DateTimeFormat.forPattern(datePattern);
        DateTime dateTime = formatter.parseDateTime( date );
        return dateTime.toDate();
    }

    public Date parseDate(ElasticSearchDataSetDef definition, String columnId, long date) {
        return new Date(date);
    }

    public String formatText(ElasticSearchDataSetDef definition, String columnId, String string) {
        return formatString(definition, columnId, string);
    }

    public String formatLabel(ElasticSearchDataSetDef definition, String columnId, String string) {
        return formatString(definition, columnId, string);
    }
    
    protected String formatString(ElasticSearchDataSetDef definition, String columnId, String string) {
        return string;
    }
    
    public String formatNumeric(ElasticSearchDataSetDef definition, String columnId, Number number) {
        if (number == null) {
            number = 0d;
        }
        
        String coreType = definition.getPattern(columnId);
        if (isEmpty(coreType)) coreType = defaulNumberFormat();

        String result;
        if (coreType.equalsIgnoreCase(NUMERIC_FLOAT)) {
            result = Float.toString(number.floatValue());
        } else if (coreType.equalsIgnoreCase(NUMERIC_DOUBLE)) {
            result = Double.toString(number.doubleValue());
        } else if (coreType.equalsIgnoreCase(NUMERIC_SHORT)) {
            result = Short.toString(number.shortValue());
        } else if (coreType.equalsIgnoreCase(NUMERIC_INTEGER)) {
            result = Integer.toString(number.intValue());
        } else if (coreType.equalsIgnoreCase(NUMERIC_LONG)) {
            result = Long.toString(number.longValue());
        } else {
            // Custom format.
            DecimalFormat format = new DecimalFormat(coreType);
            result = format.format(number);
        }
        return result;
    }

    public String formatDate(ElasticSearchDataSetDef definition, String columnId, Date date) {
        if (date == null) return "";
        
        String datePattern = definition.getPattern(columnId);
        boolean isDefaultDateFormat = isEmpty(datePattern) || datePattern.equalsIgnoreCase(DATE_DEFAULT_FORMAT_KEY);
        DateTimeFormatter formatter = isDefaultDateFormat ? DATE_DEFAULT_FORMAT_PRINTER : DateTimeFormat.forPattern(datePattern);
        return formatter.print(date.getTime());
    }

    protected boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
    
}
