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

import java.util.HashMap;
import java.util.Map;

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class CSVDataSetDef extends DataSetDef {

    protected String fileURL;
    protected String filePath;
    protected char separatorChar;
    protected char quoteChar;
    protected char escapeChar;
    protected String datePattern = "MM-dd-yyyy HH:mm:ss";
    protected String numberPattern = "#,###.##";
    protected Map<String,String> datePatternMap = new HashMap<String,String>();
    protected Map<String,String> numberPatternMap = new HashMap<String,String>();

    public CSVDataSetDef() {
        super.setProvider(DataSetProviderType.CSV.toString());
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

    public char getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
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

    public String getNumberPattern(String columnId) {
        if (!numberPatternMap.containsKey(columnId)) {
            return numberPattern;
        }
        return numberPatternMap.get(columnId);
    }

    public void setNumberPattern(String columnId, String numberPattern) {
        dataSet.addColumn(columnId, ColumnType.NUMBER);
        numberPatternMap.put(columnId, numberPattern);
    }

    public String getDatePattern(String columnId) {
        if (!datePatternMap.containsKey(columnId)) {
            return datePattern;
        }
        return datePatternMap.get(columnId);
    }

    public void setDatePattern(String columnId, String datePattern) {
        dataSet.addColumn(columnId, ColumnType.DATE);
        datePatternMap.put(columnId, datePattern);
    }

    public char getNumberGroupSeparator(String columnId) {
        String pattern = getNumberPattern(columnId);
        if (pattern.length() < 2) return ',';
        else return pattern.charAt(1);
    }

    public char getNumberDecimalSeparator(String columnId) {
        String pattern = getNumberPattern(columnId);
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
        out.append("Shared=").append(shared).append("\n");
        out.append("Push enabled=").append(pushEnabled).append("\n");
        out.append("Max push size=").append(maxPushSize).append(" Kb\n");
        out.append("Separator char=").append(separatorChar).append("\n");
        out.append("Quote char=").append(quoteChar).append("\n");
        out.append("Escape char=").append(escapeChar).append("\n");
        out.append("Number pattern=").append(numberPattern).append("\n");
        out.append("Date pattern=").append(datePattern).append("\n");
        return out.toString();
    }
}
