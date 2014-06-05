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

import org.dashbuilder.client.dataset.ClientDataSetManager;
import org.dashbuilder.client.dataset.DataSetMetadataCallback;
import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.sort.ColumnSort;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.model.dataset.sort.SortOrder;

public class DataSetLookupHandler implements DataSetHandler {

    protected ClientDataSetManager dataSetManager;
    protected DataSetMetadata dataSetMetadata;
    protected DataSetLookup lookupBase;
    protected DataSetLookup lookupExt;

    public DataSetLookupHandler(ClientDataSetManager dataSetManager, DataSetLookup lookup) {
        this.dataSetManager = dataSetManager;
        this.lookupBase = lookup;
        this.lookupExt = lookup.cloneInstance();

        // Fetch the data set metadata
        dataSetManager.fetchMetadata(lookupBase.getDataSetUUID(), new DataSetMetadataCallback() {
            public void callback(DataSetMetadata metadata) {
                dataSetMetadata = metadata;
            }
        });
    }

    public DataSetHandler lookupDataSet(final DataSetReadyCallback callback) {
        // Lookup the data set
        dataSetManager.lookupDataSet(lookupExt, new DataSetReadyCallback() {
            public void callback(DataSet result) {
                callback.callback(result);
            }
        });
        return this;
    }

    public DataSetMetadata getDataSetMetadata() {
        return dataSetMetadata;
    }

    public DataSetHandler resetAllOperations() {
        this.lookupExt = lookupBase.cloneInstance();
        return this;
    }

    public DataSetHandler selectIntervals(String columnId, Collection<String> intervalNames) {
        return this;
    }

    public DataSetHandler filterDataSet(String columnId, Comparable... allowedValues) {
        return this;
    }

    public DataSetHandler filterDataSet(String columnId, Comparable lowValue, Comparable highValue) {
        return this;
    }

    public DataSetHandler sortDataSet(String columnId, SortOrder order) {
        lookupExt.removeOperations(DataSetOpType.SORT);
        DataSetSort sortOp = new DataSetSort();
        sortOp.addSortColumn(new ColumnSort(columnId, order));
        lookupExt.addOperation(sortOp);
        return this;
    }

    public DataSetHandler trimDataSet(int offset, int rows) {
        lookupExt.setRowOffset(offset);
        lookupExt.setNumberOfRows(rows);
        return this;
    }
}