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
package org.dashbuilder.dataprovider.backend.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.def.CSVDataSetDef;

public class CSVParser {

    protected transient Map<String,DateFormat> _dateFormatMap = new HashMap<String,DateFormat>();
    protected transient Map<String,DecimalFormat> _numberFormatMap = new HashMap<String,DecimalFormat>();
    protected CSVDataSetDef dataSetDef;

    public CSVParser(CSVDataSetDef def) {
        this.dataSetDef = def;
    }

    protected DataSet load() throws Exception {
        InputStream is = getCSVInputStream(dataSetDef);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            CSVReader csvReader = new CSVReader(br, dataSetDef.getSeparator(), dataSetDef.getQuoteChar(), dataSetDef.getEscapeChar());

            String[] header = csvReader.readNext();
            if (header == null) throw new IOException("The CSV has no header: " + dataSetDef);

            String[] firstRow = csvReader.readNext();
            if (firstRow == null) throw new IOException("The CSV  has no entries: " + dataSetDef);

            // Build the data set structure
            DataSet dataSet = DataSetFactory.newEmptyDataSet();
            for (int i = 0; i < firstRow.length; i++) {
                String columnId = header[i];
                String value = firstRow[i];
                ColumnType type = calculateType(columnId, value);
                dataSet.addColumn(columnId, type);
            }

            // Load & insert the CSV rows
            Object[] row = processLine(dataSet, firstRow);
            dataSet.setValuesAt(dataSet.getRowCount(), row);
            String[] line = csvReader.readNext();
            while (line != null) {
                row = processLine(dataSet, line);
                dataSet.setValuesAt(dataSet.getRowCount(), row);
                line = csvReader.readNext();
            }
            return dataSet;
        } finally {
            is.close();
        }
    }


    protected InputStream getCSVInputStream(CSVDataSetDef def) throws Exception {
        if (!StringUtils.isBlank(def.getFilePath())) {
            File f = new File(def.getFilePath());
            if (!f.exists()) throw new IllegalArgumentException("CSV file not found: " + def.getFilePath());
            return new FileInputStream(f);
        }
        else if (!StringUtils.isBlank(def.getFileURL())) {
            URL url = new URL(def.getFileURL());
            return url.openStream();
        }
        throw new IllegalArgumentException("CSV location not specified: " + def);
    }


    protected ColumnType calculateType(String columnId, String value) {
        DataColumn column = dataSetDef.getDataSet().getColumnById(columnId);
        if (column != null) return column.getColumnType();

        try {
            DateFormat dateFormat = getDateFormat(columnId);
            dateFormat.parse(value);
            return ColumnType.DATE;
        } catch (Exception e) {
            try {
                DecimalFormat numberFormat = getNumberFormat(columnId);
                numberFormat.parse(value);
                return ColumnType.NUMBER;
            } catch (Exception ee) {
                return ColumnType.LABEL;
            }
        }
    }

    protected Object[] processLine(DataSet dataSet, String[] line) throws Exception {
        Object[] row = new Object[line.length];
        for (int j=0; j<line.length; j++) {
            String valueStr = line[j];
            DataColumn column = dataSet.getColumnByIndex(j);
            if (!StringUtils.isBlank(valueStr)){
                row[j] = parseValue(column, valueStr);
            } else {
                row[j] = null;
            }
        }
        return row;
    }

    public Object parseValue(DataColumn column, String value) throws Exception {
        ColumnType type = column.getColumnType();
        try {
            if (type.equals(ColumnType.DATE)) {
                DateFormat dateFormat = getDateFormat(column.getId());
                return dateFormat.parse(value);
            } else if (type.equals(ColumnType.NUMBER)) {
                DecimalFormat numberFormat = getNumberFormat(column.getId());
                return numberFormat.parse(value).doubleValue();
            } else {
                return value;
            }
        } catch (ParseException e) {
            String msg = "Error parsing value: " + value + ", " + e.getMessage() + ". Check column\u0027s data type consistency!";
            throw new Exception(msg);
        }
    }

    protected DateFormat getDateFormat(String columnId) {
        DateFormat format = _dateFormatMap.get(columnId);
        if (format == null) {
            format = new SimpleDateFormat(dataSetDef.getDatePattern(columnId));
            _dateFormatMap.put(columnId, format);
        }
        return format;
    }

    protected DecimalFormat getNumberFormat(String columnId) {
        DecimalFormat format = _numberFormatMap.get(columnId);
        if (format == null) {
            DecimalFormatSymbols numberSymbols = new DecimalFormatSymbols();
            numberSymbols.setGroupingSeparator(dataSetDef.getNumberGroupSeparator(columnId));
            numberSymbols.setDecimalSeparator(dataSetDef.getNumberDecimalSeparator(columnId));
            format = new DecimalFormat(dataSetDef.getNumberPattern(columnId), numberSymbols);
            _numberFormatMap.put(columnId, format);
        }
        return format;
    }
}
