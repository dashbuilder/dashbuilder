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
package org.dashbuilder.dataset.impl;

import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.CSVDataSetDefBuilder;
import org.dashbuilder.dataset.def.DataSetDef;

public class CSVDataSetDefBuilderImpl extends AbstractDataSetDefBuilder<CSVDataSetDefBuilderImpl> implements CSVDataSetDefBuilder<CSVDataSetDefBuilderImpl> {

    protected DataSetDef createDataSetDef() {
        return new CSVDataSetDef();
    }

    public CSVDataSetDefBuilderImpl separator(String separator) {
        ((CSVDataSetDef) def).setSeparator(separator);
        return this;
    }

    public CSVDataSetDefBuilderImpl quoteChar(String quote) {
        ((CSVDataSetDef) def).setQuoteChar(quote);
        return this;
    }

    public CSVDataSetDefBuilderImpl escapeChar(String escape) {
        ((CSVDataSetDef) def).setEscapeChar(escape);
        return this;
    }

    public CSVDataSetDefBuilderImpl date(String columnId, String datePattern) {
        ((CSVDataSetDef) def).setDatePattern(columnId, datePattern);
        super.date(columnId);
        return this;
    }

    public CSVDataSetDefBuilderImpl number(String columnId, String numberPattern) {
        ((CSVDataSetDef) def).setNumberPattern(columnId, numberPattern);
        super.number(columnId);
        return this;
    }
}
