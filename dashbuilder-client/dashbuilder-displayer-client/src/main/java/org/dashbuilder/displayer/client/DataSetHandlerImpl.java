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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.DataSetOp;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetOpType;
import org.dashbuilder.dataset.engine.group.IntervalBuilder;
import org.dashbuilder.dataset.engine.group.IntervalBuilderLocator;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.sort.DataSetSort;

public class DataSetHandlerImpl implements DataSetHandler {

    protected DataSetClientServices dataSetLookupClient = DataSetClientServices.get();
    protected DataSetLookup lookupBase;
    protected DataSetLookup lookupCurrent;
    protected DataSet lastLookedUpDataSet;

    public DataSetHandlerImpl(DataSetLookup lookup) {
        this.lookupBase = lookup;
        this.lookupCurrent = lookup.cloneInstance();
    }

    public DataSet getLastDataSet() {
        return lastLookedUpDataSet;
    }

    public void resetAllOperations() {
        this.lookupCurrent = lookupBase.cloneInstance();
    }

    public void limitDataSetRows(int offset, int rows) {
        lookupCurrent.setRowOffset(offset);
        if (lookupCurrent.getNumberOfRows() != -1 && lookupCurrent.getNumberOfRows() > rows) {
            lookupCurrent.setNumberOfRows(rows);
        }
    }

    public DataSetGroup getGroupOperation(String columnId) {
        int index = lookupCurrent.getLastGroupOpIndex(0, columnId, false);
        if (index == -1) return null;
        return lookupCurrent.getOperation(index);
    }

    public boolean filter(DataSetGroup op) {
        ColumnGroup cg = op.getColumnGroup();
        if (cg == null) throw new RuntimeException("Group ops requires to specify a pivot column.");
        if (!op.isSelect()) throw new RuntimeException("Group intervals not specified");

        // Avoid duplicates
        for (DataSetGroup next : lookupCurrent.getOperationList(DataSetGroup.class)) {
            if (op.equals(next)) {
                return false;
            }
        }
        // The interval selection op. must be added right before the first existing group op.
        DataSetGroup clone = op.cloneInstance();
        clone.getGroupFunctions().clear();
        int idx = lookupCurrent.getFirstGroupOpIndex(0, null, null);
        _filter(idx, clone, false);
        return true;
    }

    public boolean drillDown(DataSetGroup op) {
        ColumnGroup cg = op.getColumnGroup();
        if (cg == null) throw new RuntimeException("Group ops requires to specify a pivot column.");
        if (!op.isSelect()) throw new RuntimeException("Group intervals not specified");

        // Avoid duplicates
        for (DataSetGroup next : lookupCurrent.getOperationList(DataSetGroup.class)) {
            if (op.equals(next)) {
                return false;
            }
        }
        // Get the latest group op. for the target column being selected.
        int lastSelection = lookupCurrent.getLastGroupOpIndex(0, null, true) + 1;
        int targetGroup = lookupCurrent.getLastGroupOpIndex(lastSelection, cg.getColumnId(), false);

        // If the selection does not exists just add it.
        if (targetGroup == -1) {
            DataSetGroup clone = op.cloneInstance();
            clone.getGroupFunctions().clear();
            _filter(lastSelection, clone, true);
            return true;
        }
        // If there not exists a group op after the target then the target op must be propagated along the selection.
        DataSetGroup targetOp = lookupCurrent.getOperation(targetGroup);
        int latestGroup = lookupCurrent.getLastGroupOpIndex(targetGroup + 1, null, false);
        if (latestGroup == -1) {
            DataSetGroup clone = targetOp.cloneInstance();
            _filter(targetGroup + 1, clone, true);
        }
        // Enable the selection
        _select(targetOp, op.getSelectedIntervalList());
        return true;
    }

    public boolean unfilter(DataSetGroup op) {
        return _unfilter(op, false);
    }

    public boolean drillUp(DataSetGroup op) {
        return _unfilter(op, true);
    }

    public void sort(DataSetSort op) {
        unsort();
        lookupCurrent.addOperation(op);
    }

    public boolean unsort() {
        int n = lookupCurrent.removeOperations(DataSetOpType.SORT);
        return n > 0;
    }

    public void lookupDataSet(final DataSetReadyCallback callback) throws Exception {
        dataSetLookupClient.lookupDataSet(lookupCurrent, new DataSetReadyCallback() {
            public void callback(DataSet dataSet) {
                lastLookedUpDataSet = dataSet;
                callback.callback(dataSet);
            }
            public void notFound() {
                callback.notFound();
            }
        });
    }

    public Interval getInterval(String columnId, int row) {
        if (lastLookedUpDataSet == null) return null;

        DataColumn column = lastLookedUpDataSet.getColumnById(columnId);
        if (column == null) return null;

        // For grouped by date data sets, locate the interval corresponding to the row specified
        ColumnGroup cg = column.getColumnGroup();
        DataSetClientServices dataServices = DataSetClientServices.get();
        DataSetMetadata metadata = dataServices.getMetadata(lookupBase.getDataSetUUID());
        if (cg != null && metadata != null) {
            IntervalBuilderLocator intervalBuilderLocator = dataServices.getIntervalBuilderLocator();
            ColumnType columnType = metadata.getColumnType(cg.getSourceId());
            IntervalBuilder intervalBuilder = intervalBuilderLocator.lookup(columnType, cg.getStrategy());
            Interval target = intervalBuilder.locate(column, row);

            // The resulting interval must be portable.
            Interval result = new Interval(target.getName(), target.getIndex());
            result.setType(target.getType());
            result.setMinValue(target.getMinValue());
            result.setMaxValue(target.getMaxValue());
            return result;
        }

        // Return the interval by name.
        List values = column.getValues();
        if (row >= values.size()) return null;

        Object value = values.get(row);
        if (value == null) return null;

        return new Interval(value.toString());
    }

    // Internal filter/drillDown implementation logic

    protected Map<String,List<GroupOpFilter>> _groupOpsAdded = new HashMap<String,List<GroupOpFilter>>();
    protected Map<String,List<GroupOpFilter>> _groupOpsSelected = new HashMap<String,List<GroupOpFilter>>();

    protected void _filter(int index, DataSetGroup op, boolean drillDown) {

        ColumnGroup cgroup = op.getColumnGroup();
        String columnId = cgroup.getColumnId();
        if (!_groupOpsAdded.containsKey(columnId)) _groupOpsAdded.put(columnId, new ArrayList<GroupOpFilter>());
        List<GroupOpFilter> filterOps = _groupOpsAdded.get(columnId);

        // When adding an external filter, look first if it exists an existing filter already.
        if (!drillDown) {
            for (GroupOpFilter filterOp : filterOps) {
                if (!filterOp.drillDown && filterOp.groupOp.getColumnGroup().equals(cgroup)) {
                    filterOp.groupOp.getSelectedIntervalList().clear();
                    filterOp.groupOp.getSelectedIntervalList().addAll(op.getSelectedIntervalList());
                    return;
                }
            }
        }
        GroupOpFilter groupOpFilter = new GroupOpFilter(op, drillDown);
        filterOps.add(groupOpFilter);
        lookupCurrent.addOperation(index, op);
    }

    protected void _select(DataSetGroup op, List<Interval> intervalList) {
        GroupOpFilter groupOpFilter = new GroupOpFilter(op, true);

        op.setSelectedIntervalList(intervalList);
        op.getGroupFunctions().clear();

        String columnId = op.getColumnGroup().getColumnId();
        if (!_groupOpsSelected.containsKey(columnId)) {
            _groupOpsSelected.put(columnId, new ArrayList<GroupOpFilter>());
        }
        _groupOpsSelected.get(columnId).add(groupOpFilter);
    }

    protected boolean _unfilter(DataSetGroup op, boolean drillDown) {
        boolean opFound = false;
        String columnId = op.getColumnGroup().getColumnId();

        if (_groupOpsAdded.containsKey(columnId)) {

            Iterator<GroupOpFilter> it1 = _groupOpsAdded.get(columnId).iterator();
            while (it1.hasNext()) {
                GroupOpFilter target = it1.next();

                Iterator<DataSetOp> it2 = lookupCurrent.getOperationList().iterator();
                while (it2.hasNext()) {
                    DataSetOp next = it2.next();
                    if (next == target.groupOp && target.drillDown == drillDown) {
                        it1.remove();
                        it2.remove();
                        opFound = true;
                    }
                }
            }
        }

        if (_groupOpsSelected.containsKey(columnId)) {

            Iterator<GroupOpFilter> it1 = _groupOpsSelected.get(columnId).iterator();
            while (it1.hasNext()) {
                GroupOpFilter target = it1.next();

                Iterator<DataSetGroup> it2 = lookupCurrent.getOperationList(DataSetGroup.class).iterator();
                while (it2.hasNext()) {
                    DataSetGroup next = it2.next();
                    if (next == target.groupOp && target.drillDown == drillDown) {
                        it1.remove();
                        next.getSelectedIntervalList().clear();
                        next.getGroupFunctions().clear();
                        next.getSelectedIntervalList().addAll(target.intervalList);
                        next.getGroupFunctions().addAll(target.groupFunctions);
                        opFound = true;
                    }
                }
            }
        }
        return opFound;
    }

    protected static class GroupOpFilter {
        DataSetGroup groupOp;
        boolean drillDown = false;
        List<GroupFunction> groupFunctions;
        List<Interval> intervalList;

        private GroupOpFilter(DataSetGroup op, boolean drillDown) {
            this.groupOp = op;
            this.drillDown = drillDown;
            this.groupFunctions = new ArrayList<GroupFunction>(op.getGroupFunctions());
            this.intervalList = new ArrayList<Interval>(op.getSelectedIntervalList());
        }

        public String toString() {
            StringBuilder out = new StringBuilder();
            out.append("drillDown(").append(drillDown).append(") ");
            if (groupOp != null) out.append("groupOp(").append(groupOp).append(")");
            return out.toString();
        }
    }
}