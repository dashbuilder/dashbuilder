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

public class CSVDataSetDef extends DataSetDef {

    protected String fileURL;
    protected String filePath;
    protected String separator;
    protected String quoteChar;
    protected String escapeChar;
    protected String datePattern;
    protected String numberPattern;
    protected Map<String,String> datePatternMap = new HashMap<String,String>();
    protected Map<String,String> numberPatternMap = new HashMap<String,String>();

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

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
    }

    public String getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(String escapeChar) {
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
        numberPatternMap.put(columnId, numberPattern);
    }

    public String getDatePattern(String columnId) {
        if (!datePatternMap.containsKey(columnId)) {
            return datePattern;
        }
        return datePatternMap.get(columnId);
    }

    public void setDatePattern(String columnId, String datePattern) {
        datePatternMap.put(columnId, datePattern);
    }
}
