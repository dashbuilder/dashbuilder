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

import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.group.ColumnGroup;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.sort.DataSetSort;

/**
 * Interface addressed to issue lookup requests over a data set instance.
 */
public interface DataSetHandler {

    /**
     * Get the metadata of the data set.
     */
    DataSetMetadata getDataSetMetadata();

    /**
     * Retrieves any group operation present in the current data set lookup for the target column specified.
     * @param columnId The column id. to look for. It can be either the column used to group the data set or
     * the column id. assigned int the grouped data set result.
     *
     * @return The group operation that matches the given column id. Or null if no operation is found.
     */
    DataSetGroup getGroupOperation(String columnId);

    /**
     * Adds a group operation to the current data set lookup instance.
     *
     * @param op The operation to add.
     * @return false, if a group operation is already defined for the target group column - true, otherwise.
     */
    boolean addGroupOperation(DataSetGroup op);

    /**
     * Removes the first group operation defined that matches the specified column.
     *
     * @param columnGroup The column group to look for.
     * @return false, if no group operations for the specified column are found - true, otherwise.
     */
    boolean removeFirstGroupOperation(ColumnGroup columnGroup);

    /**
     * Set the sort operation for the current data set lookup instance.
     *
     * @param op The operation to set.
     */
    void setSortOperation(DataSetSort op);

    /**
     * Forces the next data set lookup request to retrieve only the specified row sub set.
     *
     * @param offset The position where the row sub set starts.
     * @param rows The number of rows to get.
     */
    void limitDataSetRows(int offset, int rows);

    /**
     * Restore the current data set lookup instance to its base status.
     */
    void resetAllOperations();

    /**
     * Executes the current data set lookup request configured within this handler.
     *
     * @param callback The callback interface that is invoked right after the data is available.
     */
    void lookupDataSet(DataSetReadyCallback callback) throws Exception;
}