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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dashbuilder.client.dataset.DataSetManagerProxy;
import org.dashbuilder.client.dataset.DataSetMetadataCallback;
import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.group.ColumnGroup;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.sort.DataSetSort;

public class DataSetHandlerImpl implements DataSetHandler {

    protected DataSetManagerProxy dataSetManagerProxy;
    protected DataSetMetadata dataSetMetadata;
    protected DataSetLookup lookupBase;
    protected DataSetLookup lookupCurrent;
    protected List<DataSetOp> addedOpList = new ArrayList<DataSetOp>();

    public DataSetHandlerImpl(DataSetManagerProxy dataSetManagerProxy, DataSetLookup lookup) {
        this.dataSetManagerProxy = dataSetManagerProxy;
        this.lookupBase = lookup;
        this.lookupCurrent = lookup.cloneInstance();

        try {
            // Fetch the data set metadata
            dataSetManagerProxy.fetchMetadata(lookupBase, new DataSetMetadataCallback() {
                public void callback(DataSetMetadata metadata) {
                    dataSetMetadata = metadata;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataSetMetadata getDataSetMetadata() {
        return dataSetMetadata;
    }

    public void resetAllOperations() {
        this.lookupCurrent = lookupBase.cloneInstance();
    }

    public void limitDataSetRows(int offset, int rows) {
        lookupCurrent.setRowOffset(offset);
        lookupCurrent.setNumberOfRows(rows);
    }

    public DataSetGroup getGroupOperation(String columnId) {
        for (DataSetGroup op : lookupCurrent.getOperationList(DataSetGroup.class)) {
            if (op.getColumnGroup() == null) continue;

            if (op.getColumnGroup().getColumnId().equals(columnId)) {
                return op;
            }
            if (op.getColumnGroup().getSourceId().equals(columnId)) {
                return op;
            }
        }
        return null;
    }

    public boolean addGroupOperation(DataSetGroup op) {
        ColumnGroup cg = op.getColumnGroup();
        if (cg == null) {
            throw new RuntimeException("Group ops requires to specify a pivot column.");
        }
        if (getGroupOperation(cg.getSourceId()) == null) {
            lookupCurrent.addOperation(0, op);
            return true;
        }
        return false;
    }

    public boolean removeGroupOperation(DataSetGroup op) {
        boolean removed = false;
        Iterator<DataSetGroup> it = lookupCurrent.getOperationList(DataSetGroup.class).iterator();
        while (it.hasNext()) {
            DataSetOp next = it.next();
            if (next.equals(op)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    public void setSortOperation(DataSetSort op) {
        cleaSortOperations();
        lookupCurrent.addOperation(op);
    }

    public boolean cleaSortOperations() {
        boolean removed = false;
        Iterator<DataSetSort> it = lookupCurrent.getOperationList(DataSetSort.class).iterator();
        while (it.hasNext()) {
            it.remove();
            removed = true;
        }
        return removed;
    }

    public void lookupDataSet(final DataSetReadyCallback callback) throws Exception {
        dataSetManagerProxy.lookupDataSet(lookupCurrent, new DataSetReadyCallback() {
            public void callback(DataSet result) {
                callback.callback(result);
            }
        });
    }
}