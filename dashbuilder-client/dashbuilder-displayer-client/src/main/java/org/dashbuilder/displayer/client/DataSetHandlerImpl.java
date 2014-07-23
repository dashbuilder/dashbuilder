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

import org.dashbuilder.dataset.client.DataSetLookupClient;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetOpType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.sort.DataSetSort;

public class DataSetHandlerImpl implements DataSetHandler {

    protected DataSetLookupClient dataSetLookupClient;
    protected DataSetLookup lookupBase;
    protected DataSetLookup lookupCurrent;

    public DataSetHandlerImpl(DataSetLookupClient dataSetLookupClient, DataSetLookup lookup) {
        this.dataSetLookupClient = dataSetLookupClient;
        this.lookupBase = lookup;
        this.lookupCurrent = lookup.cloneInstance();
    }

    public void resetAllOperations() {
        this.lookupCurrent = lookupBase.cloneInstance();
    }

    public void limitDataSetRows(int offset, int rows) {
        lookupCurrent.setRowOffset(offset);
        lookupCurrent.setNumberOfRows(rows);
    }

    public DataSetGroup getGroupOperation(String columnId) {
        for (DataSetGroup op : lookupBase.getOperationList(DataSetGroup.class)) {
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
        // If the operation is not an interval selection just add it.
        if (op.getSelectedIntervalNames().isEmpty()) {
            lookupCurrent.addOperation(0, op);
            return true;
        }

        // Interval selection operations are a bit more complex
        //

        // Get the existing operation from the current lookup settings.
        int found = 0; int last = 0;
        DataSetGroup targetOp = null;
        for (DataSetGroup next : lookupCurrent.getOperationList(DataSetGroup.class)) {
            if (next.getColumnGroup() != null && cg.equals(next.getColumnGroup())) {
                targetOp = next;
                found = last+1;
            }
            last++;
        }

        // For new operations just add it as a new interval selection operation.
        if (targetOp == null) {
            DataSetGroup clone = op.cloneInstance();
            clone.getGroupFunctions().clear();
            int index = lookupCurrent.getLastGroupOpIndex(null, true) + 1;
            lookupCurrent.addOperation(index, clone);
            return true;
        }

        // For existing operations, just set the interval selection.
        targetOp.setSelectedIntervalNames(op.getSelectedIntervalNames());

        // If it was the last existing group operation then the group operation must be applied after the selection.
        if (found == last && !lookupBase.getOperationList(DataSetGroup.class).isEmpty()) {
            DataSetGroup clone = targetOp.cloneInstance();
            clone.getSelectedIntervalNames().clear();
            lookupCurrent.addOperation(clone);
        }
        return false;
    }

    protected boolean belongsToBase(DataSetGroup op) {
        for (DataSetGroup baseGroupOp : lookupBase.getOperationList(DataSetGroup.class)) {
            if (baseGroupOp.getColumnGroup() != null && baseGroupOp.getColumnGroup().equals(op.getColumnGroup())) {
                return true;
            }
        }
        return false;
    }

    public boolean removeGroupOperation(DataSetGroup op) {
        for (DataSetGroup next : lookupCurrent.getOperationList(DataSetGroup.class)) {
            ColumnGroup cg = next.getColumnGroup();
            if (cg == null) continue;

            if (cg.equals(op.getColumnGroup())) {
                if (belongsToBase(next)) next.getSelectedIntervalNames().clear();
                else lookupCurrent.getOperationList().remove(next);
                return true;
            }
        }
        return false;
    }

    public void setSortOperation(DataSetSort op) {
        cleaSortOperations();
        lookupCurrent.addOperation(op);
    }

    public boolean cleaSortOperations() {
        int n = lookupCurrent.removeOperations(DataSetOpType.SORT);
        return n > 0;
    }

    public void lookupDataSet(DataSetReadyCallback callback) throws Exception {
        dataSetLookupClient.lookupDataSet(lookupCurrent, callback);
    }
}