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
package org.dashbuilder.dataset;

import java.util.List;

import org.dashbuilder.dataset.group.ColumnGroup;

/**
 * A data set is a matrix of values composed by a fixed number of columns.
 */
public interface DataColumn {

    /**
     * @return The DataSet instance associated to this DataColumn.
     * @see org.dashbuilder.dataset.DataSet
     */
    DataSet getDataSet();

    /**
     * @return The identifier of this DataColumn
     */
    String getId();

    /**
     * @return The name of this DataColumn
     */
    String getName();

    /**
     * @return The column's type.
     * @see org.dashbuilder.dataset.ColumnType
     */
    ColumnType getColumnType();

    /**
     * The column group settings
     *
     * @return null if this column is not the result of a group operation.
     */
    ColumnGroup getColumnGroup();

    /**
     * The interval type used to group this column.
     *
     * @return null if this column is not the result of a group operation.
     */
    String getIntervalType();

    /**
     * The minimum column value
     *
     * @return null if this column is not the result of a group operation.
     */
    Object getMinValue();

    /**
     * The maximum column value
     *
     * @return null if this column is not the result of a group operation.
     */
    Object getMaxValue();

    /**
     * @return A List of the values for this DataColumn.
     */
    List getValues();
}
