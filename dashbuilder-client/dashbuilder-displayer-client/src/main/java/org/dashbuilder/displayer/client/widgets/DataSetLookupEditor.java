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
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.jboss.errai.common.client.api.RemoteCallback;

@Dependent
public class DataSetLookupEditor implements IsWidget {

    public interface Listener {
        void dataSetSelected(String uuid);
    }

    public interface View extends IsWidget {
        void init(DataSetLookupEditor presenter);
        void updateDataSetLookup();
        void showAvailableDataSets(List<DataSetDef> dataSetDefs);
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
        view.init(this);
        fetchAvailableDataSets();
    }

    public void update(DataSetLookup dataSetLookup, DataSetLookupConstraints constraints, DataSetMetadata metadata) {
        this.dataSetLookup = dataSetLookup.cloneInstance();
        this.lookupConstraints = constraints;
        this.dataSetMetadata = metadata;
        view.updateDataSetLookup();
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

    public String getGroupColumnId() {
        DataSetGroup groupOp = dataSetLookup.getOperationList(DataSetGroup.class).get(0);
        return groupOp.getColumnGroup().getSourceId();
    }

    public List<Integer> getGroupColumnIdxs() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i=0; i<dataSetMetadata.getNumberOfColumns(); i++) {
            ColumnType columnType = dataSetMetadata.getColumnType(i);
            if (ColumnType.LABEL.equals(columnType) || ColumnType.DATE.equals(columnType)) {
                result.add(i);
            }
        }
        return result;
    }

    public void fetchAvailableDataSets() {
        DataSetClientServices.get().getRemoteSharedDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {
                view.showAvailableDataSets(dataSetDefs);
            }
        });
    }

    public void selectDataSet(String uuid) {
        if (listener != null) {
            listener.dataSetSelected(uuid);
        }
    }
}
