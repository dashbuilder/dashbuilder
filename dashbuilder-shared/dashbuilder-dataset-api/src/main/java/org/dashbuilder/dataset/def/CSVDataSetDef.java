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
package org.dashbuilder.dataset.def;

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.validation.groups.CSVDataSetDefFilePathValidation;
import org.dashbuilder.dataset.validation.groups.CSVDataSetDefFileURLValidation;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.errai.common.client.api.annotations.Portable;

import javax.validation.constraints.NotNull;

@Portable
public class CSVDataSetDef extends DataSetDef {

    @NotNull(message = "{dataSetApi_csvDataSetDef_fileURL_notNull}", groups = CSVDataSetDefFileURLValidation.class)
    @NotEmpty(message = "{dataSetApi_csvDataSetDef_fileURL_notNull}", groups = CSVDataSetDefFileURLValidation.class)
    protected String fileURL;
    @NotNull(message = "{dataSetApi_csvDataSetDef_filePath_notNull}", groups = CSVDataSetDefFilePathValidation.class)
    @NotEmpty(message = "{dataSetApi_csvDataSetDef_filePath_notNull}", groups = CSVDataSetDefFilePathValidation.class)
    protected String filePath;
    @NotNull(message = "{dataSetApi_csvDataSetDef_sepChar_notNull}")
    protected Character separatorChar;
    @NotNull(message = "{dataSetApi_csvDataSetDef_quoteChar_notNull}")
    protected Character quoteChar;
    @NotNull(message = "{dataSetApi_csvDataSetDef_escapeChar_notNull}")
    protected Character escapeChar;
    @NotNull(message = "{dataSetApi_csvDataSetDef_datePattern_notNull}")
    @NotEmpty(message = "{dataSetApi_csvDataSetDef_datePattern_notNull}")
    protected String datePattern = "MM-dd-yyyy HH:mm:ss";
    @NotNull(message = "{dataSetApi_csvDataSetDef_numberPattern_notNull}")
    @NotEmpty(message = "{dataSetApi_csvDataSetDef_numberPattern_notNull}")
    protected String numberPattern = "#,###.##";

    public CSVDataSetDef() {
        super.setProvider(DataSetProviderType.CSV);
        separatorChar = ';';
        quoteChar = '\'';
        escapeChar = '\\';
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Character getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(Character separatorChar) {
        this.separatorChar = separatorChar;
    }

    public Character getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(Character quoteChar) {
        this.quoteChar = quoteChar;
    }

    public Character getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(Character escapeChar) {
        this.escapeChar = escapeChar;
    }

    public String getPattern(String columnId) {
        String p = super.getPattern(columnId);
        if (p != null) return p;

        DataColumn c = getDataSet().getColumnById(columnId);
        if (c == null) return null;

        if (c.getColumnType().equals(ColumnType.NUMBER)) return numberPattern;
        if (c.getColumnType().equals(ColumnType.DATE)) return datePattern;
        return null;
    }

    public String getNumberPattern() {
        return numberPattern;
    }

    public void setNumberPattern(String numberPattern) {
        this.numberPattern = numberPattern;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public char getNumberGroupSeparator(String columnId) {
        String pattern = getPattern(columnId);
        if (pattern.length() < 2) return ',';
        else return pattern.charAt(1);
    }

    public char getNumberDecimalSeparator(String columnId) {
        String pattern = getPattern(columnId);
        if (pattern.length() < 6) return '.';
        else return pattern.charAt(5);
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("File=");
        if (filePath != null) out.append(filePath);
        else out.append(fileURL);
        out.append("\n");
        out.append("UUID=").append(UUID).append("\n");
        out.append("Provider=").append(provider).append("\n");
        out.append("Public=").append(isPublic).append("\n");
        out.append("Push enabled=").append(pushEnabled).append("\n");
        out.append("Push max size=").append(pushMaxSize).append(" Kb\n");
        if (refreshTime != null) {
            out.append("Refresh time=").append(refreshTime).append("\n");
            out.append("Refresh always=").append(refreshAlways).append("\n");
        }
        out.append("Separator char=").append(separatorChar).append("\n");
        out.append("Quote char=").append(quoteChar).append("\n");
        out.append("Escape char=").append(escapeChar).append("\n");
        out.append("Number pattern=").append(numberPattern).append("\n");
        out.append("Date pattern=").append(datePattern).append("\n");
        return out.toString();
    }
}
