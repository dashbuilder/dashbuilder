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
package org.dashbuilder.displayer.client;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.sort.DataSetSort;

/**
 * Interface addressed to issue lookup requests over a data set instance.
 */
public interface DataSetHandler {

    /**
     * Retrieves any group operation present in the current data set lookup for the target column specified.
     * @param columnId The column id. to look for. It can be either the column used to group the data set or
     * the column id. assigned int the grouped data set result.
     *
     * @return The group operation that matches the given column id. Or null if no operation is found.
     */
    DataSetGroup getGroupOperation(String columnId);

    /**
     * Forces the underlying data set to be updated according the group interval selection filter.
     *
     * @param op The group interval selection operation to apply <i>op.getSelectedIntervalNames()</i> MUST NOT BE EMPTY.
     * @return false, if the target interval selection has already been applied - true, otherwise.
     */
    boolean filter(DataSetGroup op);

    /**
     * Reverts the changes applied by a previous <i>filter</i> operation.
     *
     * @param op The operation to remove.
     * @return false, if no filter has been applied for the target operation - true, otherwise.
     */
    boolean unfilter(DataSetGroup op);

    /**
     * Applies the specified group interval selection operation over the existing group op.
     *
     * @param op The group interval selection operation to apply <i>op.getSelectedIntervalNames()</i> MUST NOT BE EMPTY.
     * @return false, if drillDown is not applicable for the target operation - true, otherwise.
     */
    boolean drillDown(DataSetGroup op);

    /**
     * Reverts the changes applied by a previous <i>drillDown</i> operation.
     *
     * @param op The operation to remove.
     * @return false, if no drillDown has been applied for the target operation - true, otherwise.
     */
    boolean drillUp(DataSetGroup op);

    /**
     * Set the sort operation for the current data set lookup instance.
     *
     * @param op The operation to set.
     */
    void sort(DataSetSort op);

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

    /**
     * Get the data set get on the last lookup call (if any)
     */
    DataSet getLastDataSet();
}