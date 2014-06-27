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
package org.dashbuilder.model.dataset;

/**
 * Metadata associated with a data set instance
 */
public interface DataSetMetadata {

    /**
     * The unique identifier for this data set.
     */
    String getUUID();

    /**
     * Get the number of rows.
     */
    int getNumberOfRows();

    /**
     * Get the number of columns.
     */
    int getNumberOfColumns();

    /**
     * Get the identifier of the specified column.
     * @param columnIndex The column index (starting at 0).
     */
    String getColumnId(int columnIndex);

    /**
     * Get the type of the specified column.
     * @param columnIndex The column index (starting at 0).
     */
    ColumnType getColumnType(int columnIndex);

    /**
     * Get the estimated size in kilobytes.
     */
    int getEstimatedSize();
}