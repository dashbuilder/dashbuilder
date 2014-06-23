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

import java.util.List;

public interface DataSet extends DataSetRef {

    /**
     * The metadata
     */
    DataSetMetadata getMetadata();

    /**
     * The unique data set identifier.
     */
    String getUUID();

    /**
     * Set an unique identifier to this data set.
     */
    void setUUID(String uuid);

    /**
     * The dataset columns
     */
    List<DataColumn> getColumns();

    /**
     * Get a column by its id.
     */
    DataColumn getColumnById(String id);

    /**
     * Get a column by its index (starting at 0).
     */
    DataColumn getColumnByIndex(int index);

    /**
     * Get a column's internal DataSet index.
     */
    int getColumnIndex(DataColumn dataColumn);

    /**
     * Add a brand new column.
     */
    DataSet addColumn(String id, ColumnType type);

    /**
     * Get the number of rows in the dataset.
     */
    int getRowCount();

    /**
     * Get the value at a given cell.
     * @param row The cell row (the first row is 0).
     * @param column The cell column (the first column is 0).
     */
    Object getValueAt(int row, int column);

    /**
     * Get the value at a given cell.
     * @param row The cell row (the first row is 0).
     * @param columnId The cell column id.
     */
    Object getValueAt(int row, String columnId);

    /**
     * Set the value at a given cell.
     * @param row The cell row (the first row is 0).
     * @param column The cell column (the first column is 0).
     */
    DataSet setValueAt(int row, int column, Object value);

    /**
     * Set all the values for a given row.
     * @param row The cell row (the first row is 0).
     */
    DataSet setValuesAt(int row, Object... values);

    /**
     * Set all the values in the given array.
     * @param values A 2-dim array containing an array of rows where each row is an array of values.
     */
    DataSet setValues(Object[][] values);

    /**
     * Returns a data set containing only the specified row sub set.
     * @param offset The position where the row sub set starts.
     * @param rows The number of rows to get.
     * @return A trimmed data set.
     */
    DataSet trim(int offset, int rows);

    /**
     * Returns a data set containing only the specified row sub set.
     * @param rows The row ordinals to add to the resulting data set.
     * @return A trimmed data set.
     */
    DataSet trim(List<Integer> rows);

    /**
     * If this data set is teh result of a trim operation on a data set this method will return
     * the total number of rows existing before trim.
     */
    int getRowCountNonTrimmed();

    /**
     * Build a data set with the same structure as this but containing no data.
     */
    DataSet cloneEmpty();

    /**
     * Return the estimated memory (in bytes) the data set is consuming.
     * @return The number of bytes
     */
    long getEstimatedSize();
}