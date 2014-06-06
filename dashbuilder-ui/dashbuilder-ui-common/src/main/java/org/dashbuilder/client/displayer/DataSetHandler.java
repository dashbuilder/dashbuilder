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
package org.dashbuilder.client.displayer;

import java.util.Collection;

import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.sort.SortOrder;

/**
 * Interface addressed to manipulate a data set instance.
 */
public interface DataSetHandler {

    /**
     * Get the data set metadata.
     */
    DataSetMetadata getDataSetMetadata();

    /**
     * Set a set of intervals being displayed we want the data to be filtered.
     * @param columnId The column which interval values are to be selected.
     * @param intervalNames The names of the intervals to select.
     */
    DataSetHandler selectIntervals(String columnId, Collection<String> intervalNames);

    /**
     * Filter the underlying data set by the given column.
     * @param columnId The column which values are to be filtered.
     * @param allowedValues The set of values the filter operation must satisfy.
     */
    DataSetHandler filterDataSet(String columnId, Collection<Comparable> allowedValues);

    /**
     * Filter the underlying data set by the given column.
     * @param columnId The column which values are to be filtered.
     * @param lowValue The filtered data set values must be lower than this.
     * @param highValue The filtered data set values must be greater than this.
     */
    DataSetHandler filterDataSet(String columnId, Comparable lowValue, Comparable highValue);

    /**
     * Sort the underlying data set by the given column.
     * @param columnId The column which values are to be sort.
     * @param order The sort ordering to apply.
     */
    DataSetHandler sortDataSet(String columnId, SortOrder order);

    /**
     * Forces the data set to contain only the specified row sub set.
     * @param offset The position where the row sub set starts.
     * @param rows The number of rows to get.
     */
    DataSetHandler trimDataSet(int offset, int rows);

    /**
     * Clear all the operations set.
     */
    DataSetHandler resetAllOperations();

    /**
     * Get the data set reflecting all the data set operation currently set within this handler.
     * @param callback The callback interface that is invoked right after the data is available.
     */
    DataSetHandler lookupDataSet(DataSetReadyCallback callback);
}