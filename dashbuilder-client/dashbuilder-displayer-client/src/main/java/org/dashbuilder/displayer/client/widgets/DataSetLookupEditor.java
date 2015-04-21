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
package org.dashbuilder.displayer.client.widgets;

import java.util.List;
import java.util.ArrayList;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.DataSetOpType;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;
import org.dashbuilder.displayer.client.widgets.group.DataSetGroupDateEditor;
import org.jboss.errai.common.client.api.RemoteCallback;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Dependent
public class DataSetLookupEditor implements IsWidget,
                                DataSetFilterEditor.Listener,
                                DataSetGroupDateEditor.Listener {

    public interface Listener {
        void dataSetChanged(String uuid);
        void columnChanged(GroupFunction groupFunction);
        void groupChanged(DataSetGroup groupOp);
        void filterChanged(DataSetFilter filterOp);
    }

    public interface View extends IsWidget {
        void init(DataSetLookupEditor presenter);
        void updateDataSetLookup();
        void showDataSetDefs(List<DataSetDef> dataSetDefs);
        void addDataSetDef(DataSetDef dataSetDef);
        void removeDataSetDef(DataSetDef dataSetDef);
        void errorOnInit(Exception e);
        void errorDataSetNotFound(String dataSetUUID);
    }

    Listener listener;
    View view;

    DataSet dataSet = null;
    DataSetLookup dataSetLookup = null;
    DataSetLookupConstraints lookupConstraints = null;
    DataSetMetadata dataSetMetadata = null;

    public DataSetLookupEditor() {
        this.view = new DataSetLookupEditorView();
    }

    @Inject
    public DataSetLookupEditor(DataSetLookupEditorView view) {
        this.view = view;
    }

    public Widget asWidget() {
        return view.asWidget();
    }

    public void init(Listener listener) {
        this.listener = listener;
        this.dataSetLookup = null;
        this.lookupConstraints = null;
        this.dataSetMetadata = null;
        view.init(this);

        DataSetClientServices.get().getRemoteSharedDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {
                view.showDataSetDefs(dataSetDefs);
            }
        });
    }

    public void init(Listener listener,
            DataSetLookup dataSetLookup,
            DataSetLookupConstraints constraints,
            DataSetMetadata metadata) {

        this.listener = listener;
        this.dataSetLookup = dataSetLookup;
        this.lookupConstraints = constraints;
        this.dataSetMetadata = metadata;
        view.init(this);

        DataSetClientServices.get().getRemoteSharedDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {
                view.showDataSetDefs(dataSetDefs);
                view.updateDataSetLookup();
            }
        });
    }

    public View getView() {
        return view;
    }

    public DataSetLookup getDataSetLookup() {
        return dataSetLookup;
    }

    public DataSetLookupConstraints getConstraints() {
        return lookupConstraints;
    }

    public DataSetMetadata getDataSetMetadata() {
        return dataSetMetadata;
    }

    public String getDataSetUUID() {
        if (dataSetLookup == null) return null;
        return dataSetLookup.getDataSetUUID();
    }

    public String getColumnId(int index) {
        return dataSetMetadata.getColumnId(index);
    }

    public ColumnType getColumnType(int index) {
        return dataSetMetadata.getColumnType(index);
    }

    public ColumnType getColumnType(String columnId) {
        if (columnId == null) return null;
        return dataSetMetadata.getColumnType(columnId);
    }

    public DataSetGroup getFirstGroupOp() {
        List<DataSetGroup> groupOpList = dataSetLookup.getOperationList(DataSetGroup.class);
        if (groupOpList.isEmpty()) return null;

        return groupOpList.get(0);
    }

    public boolean isFirstGroupOpDateBased() {
        DataSetGroup first = getFirstGroupOp();
        if (first == null) return false;
        ColumnGroup cg = first.getColumnGroup();
        if (cg == null) return false;

        ColumnType type = getColumnType(cg.getSourceId());
        return ColumnType.DATE.equals(type);
    }

    public List<GroupFunction> getFirstGroupFunctions() {
        List<DataSetGroup> groupOpList = dataSetLookup.getOperationList(DataSetGroup.class);
        if (groupOpList.isEmpty()) return null;

        return groupOpList.get(0).getGroupFunctions();
    }

    public String getFirstGroupColumnId() {
        List<DataSetGroup> groupOpList = dataSetLookup.getOperationList(DataSetGroup.class);
        if (groupOpList.isEmpty()) return null;

        DataSetGroup groupOp = groupOpList.get(0);
        if (groupOp.getColumnGroup() == null) return null;
        return groupOp.getColumnGroup().getSourceId();
    }

    public List<Integer> getAvailableGroupColumnIdxs() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i=0; i<dataSetMetadata.getNumberOfColumns(); i++) {
            ColumnType columnType = dataSetMetadata.getColumnType(i);
            if (ColumnType.LABEL.equals(columnType) || ColumnType.DATE.equals(columnType)) {
                result.add(i);
            }
        }
        return result;
    }

    // UI notifications

    public void changeDataSet(String uuid) {
        if (listener != null) {
            listener.dataSetChanged(uuid);
        }
    }

    public void createGroupColumn(String columnId) {
        DataSetGroup groupOp = new DataSetGroup();
        groupOp.getColumnGroup().setSourceId(columnId);
        groupOp.getColumnGroup().setColumnId(columnId);
        dataSetLookup.addOperation(groupOp);
        if (listener != null) {
            listener.groupChanged(groupOp);
        }
    }

    public void changeGroupColumn(ColumnGroup columnGroup) {
        DataSetGroup groupOp = getFirstGroupOp();
        if (groupOp != null) {
            groupOp.setColumnGroup(columnGroup);

            // Notify listener
            if (listener != null) {
                listener.groupChanged(groupOp);
            }
        }
    }

    public void changeGroupColumn(String columnId) {
        DataSetGroup groupOp = getFirstGroupOp();
        if (groupOp != null) {

            // Group reset
            if (columnId == null) {
                groupOp.setColumnGroup(null);

                if (lookupConstraints.isGroupColumn()) {
                    groupOp.getGroupFunctions().remove(0);
                }
                if (!lookupConstraints.isFunctionRequired()) {
                    for (GroupFunction groupFunction : groupOp.getGroupFunctions()) {
                        groupFunction.setFunction(null);
                    }
                }
            }
            // Group column change
            else {
                groupOp.setColumnGroup(new ColumnGroup(columnId, columnId));
                if (lookupConstraints.isGroupColumn()) {
                    if (groupOp.getGroupFunctions().size() == 1) {
                        GroupFunction groupFunction = new GroupFunction(groupOp.getColumnGroup().getSourceId(), null, null);
                        groupOp.getGroupFunctions().add(0, groupFunction);
                    } else {
                        GroupFunction groupFunction = groupOp.getGroupFunctions().get(0);
                        groupFunction.setSourceId(groupOp.getColumnGroup().getSourceId());
                        groupFunction.setColumnId(null);
                        groupFunction.setFunction(null);
                    }
                }
            }
            // Notify listener
            if (listener != null) {
                listener.groupChanged(groupOp);
            }
        }
    }


    protected int getGroupFunctionLastIdx(List<GroupFunction> groupFunctions, String sourceId) {
        int last = -1;
        for (GroupFunction gf : groupFunctions) {
            if (gf.getSourceId().equals(sourceId)) {
                int idx = getGroupFunctionColumnIdx(gf.getColumnId());
                if (last == -1 || last < idx) {
                    last = idx;
                }
            }
        }
        return last;
    }

    protected int getGroupFunctionColumnIdx(String columnId) {
        int sep = columnId.lastIndexOf('_');
        if (sep != -1) {
            try {
                String str = columnId.substring(sep + 1);
                return Integer.parseInt(str);
            } catch (Exception e) {
                // Ignore
            }
        }
        return 1;
    }

    protected String nextGroupFunctionColumnId(String sourceId, AggregateFunctionType function) {
        int lastIdx = getGroupFunctionLastIdx(getFirstGroupFunctions(), sourceId);
        String next = sourceId;
        if (function != null) next += "_" + function.name().toLowerCase();
        if (lastIdx != -1) next += "_" + (++lastIdx);
        return next;
    }

    public void changeGroupFunction(GroupFunction groupFunction, String sourceId, AggregateFunctionType function) {

        groupFunction.setFunction(null);
        if (function != null) {
            ColumnType columnType = getColumnType(sourceId);
            if (function.supportType(columnType)) {
                groupFunction.setFunction(function);
            } else {
                for (AggregateFunctionType functionType : AggregateFunctionType.values()) {
                    if (functionType.supportType(columnType)) {
                        groupFunction.setFunction(functionType);
                        break;
                    }
                }
            }
        }

        if (!sourceId.equals(groupFunction.getSourceId())) {
            String newColumnId = nextGroupFunctionColumnId(sourceId, groupFunction.getFunction());
            groupFunction.setSourceId(sourceId);
            groupFunction.setColumnId(newColumnId);
        }

        if (listener != null) {
            listener.columnChanged(groupFunction);
        }
    }

    public void changeDataSetFilter(DataSetFilter filterOp) {
        dataSetLookup.removeOperations(DataSetOpType.FILTER);
        if (filterOp != null) {
            dataSetLookup.addOperation(0, filterOp);
        }
        // Notify listener
        if (listener != null) {
            listener.filterChanged(filterOp);
        }
    }

    public void addGroupFunction() {
        if (lookupConstraints.areExtraColumnsAllowed()) {
            DataSetGroup op = getFirstGroupOp();
            List<GroupFunction> functionList = op.getGroupFunctions();
            GroupFunction last = functionList.get(functionList.size() - 1);

            GroupFunction clone = last.cloneInstance();
            String newColumnId = nextGroupFunctionColumnId(last.getSourceId(), last.getFunction());
            clone.setColumnId(newColumnId);
            functionList.add(clone);

            if (listener != null) {
                listener.groupChanged(op);
            }
        }
    }

    public void removeGroupFunction(int columnIdx) {
        DataSetGroup op = getFirstGroupOp();
        op.getGroupFunctions().remove(columnIdx);
        if (listener != null) {
            listener.groupChanged(op);
        }
    }

    // DataSetFilterEditor callback

    public void filterChanged(DataSetFilter filter) {
        changeDataSetFilter(filter);
    }

    // DataSetGroupDateEditor callback

    public void columnGroupChanged(ColumnGroup columnGroup) {
        changeGroupColumn(columnGroup);
    }

    // Be aware of data set lifecycle events

    private void onDataSetDefRegisteredEvent(@Observes DataSetDefRegisteredEvent event) {
        checkNotNull("event", event);

        view.addDataSetDef(event.getDataSetDef());
    }

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);

        view.removeDataSetDef(event.getOldDataSetDef());
        view.addDataSetDef(event.getNewDataSetDef());
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        checkNotNull("event", event);

        view.removeDataSetDef(event.getDataSetDef());
    }
}
