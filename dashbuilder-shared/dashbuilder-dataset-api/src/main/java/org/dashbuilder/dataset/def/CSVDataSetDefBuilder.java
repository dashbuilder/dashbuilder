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

/**
 * A builder for defining static data sets
 *
 * <pre>
 *    DataSetDef dataSetDef = DataSetDefFactory.newCSVDataSetDef()
 *     .uuid("all_employees")
 *     .url("http://myhost.com/file.csv")
 *     .separatorChar(";")
 *     .label("name")
 *     .date("creationDate", "MM/dd/yyyy")
 *     .number("amount", "#.###,00")
 *     .buildDef();
 * </pre>
 */
public interface CSVDataSetDefBuilder<T extends DataSetDefBuilder> extends DataSetDefBuilder<T> {

    /**
     * Set the CSV column separator char.
     *
     * @param separator An string for separating columns
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T separatorChar(char separator);

    /**
     * Set the quote symbol.
     *
     * @param quote A char representing the quote symbol
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T quoteChar(char quote);

    /**
     * Set the escape char.
     *
     * @param escape The scape char
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T escapeChar(char escape);

    /**
     * Set the pattern for the specified date column.
     *
     * @param columnId The id of the column
     * @return numberPattern The date pattern of the column values
     */
    T date(String columnId, String datePattern);

    /**
     * Set the pattern for the specified numeric column.
     *
     * @param columnId The id of the column
     * @return numberPattern The numeric pattern of the column values
     */
    T number(String columnId, String numberPattern);
}
