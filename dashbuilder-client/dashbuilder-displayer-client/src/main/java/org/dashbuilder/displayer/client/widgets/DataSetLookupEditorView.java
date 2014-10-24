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
import javax.enterprise.context.Dependent;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.def.DataSetDef;

@Dependent
public class DataSetLookupEditorView extends Composite
        implements DataSetLookupEditor.View {

    interface Binder extends UiBinder<Widget, DataSetLookupEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    public DataSetLookupEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    DataSetLookupEditor presenter;

    @UiField
    ListBox dataSetListBox;

    @UiField
    Label statusLabel;

    @UiField
    ControlGroup rowsControlGroup;

    @UiField
    ListBox rowColumnListBox;

    @UiField
    ControlGroup columnsControlGroup;

    @Override
    public void init(DataSetLookupEditor presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateDataSetLookup() {
        DataSetLookupConstraints constraints = presenter.getConstraints();
        if (constraints.isGroupAllowed() || constraints.isGroupRequired()) {
            rowsControlGroup.setVisible(true);
            rowColumnListBox.clear();

            String groupColumnId = presenter.getGroupColumnId();
            List<Integer> groupColumnIdxs = presenter.getGroupColumnIdxs();
            for (int i=0; i<groupColumnIdxs.size(); i++) {
                int columnIdx = groupColumnIdxs.get(i);
                String columnId = presenter.getColumnId(columnIdx);
                ColumnType columnType = presenter.getColumnType(columnIdx);
                rowColumnListBox.addItem(columnId + " (" + columnType + ")", columnId);
                if (groupColumnId != null && groupColumnId.equals(columnId)) {
                    rowColumnListBox.setSelectedIndex(i);
                }
            }
        }

        columnsControlGroup.clear();

    }

    @Override
    public void showAvailableDataSets(List<DataSetDef> dataSetDefs) {
        dataSetListBox.clear();
        dataSetListBox.addItem("- Select Data Set -");
        String selectedUUID = presenter.getDataSetUUID();
        for (int i=0; i<dataSetDefs.size(); i++) {
            DataSetDef def = dataSetDefs.get(i);
            dataSetListBox.addItem(def.getUUID() + " (" + def.getProvider() + ")", def.getUUID());
            if (selectedUUID != null && selectedUUID.equals(def.getUUID())) {
                dataSetListBox.setSelectedIndex(i);
            }
        }
    }

    @Override
    public void errorDataSetNotFound(String dataSetUUID) {
        statusLabel.setVisible(true);
        statusLabel.setText("Data set '" + dataSetUUID + "' not found");
        statusLabel.setType(LabelType.WARNING);
    }

    @Override
    public void errorOnInit(Exception e) {
        statusLabel.setVisible(true);
        statusLabel.setText("Initialization error");
        statusLabel.setType(LabelType.WARNING);
        GWT.log(e.getMessage(), e);
    }

    @UiHandler(value = "dataSetListBox")
    public void onDataSetSelected(ChangeEvent changeEvent) {
        rowsControlGroup.setVisible(false);
        columnsControlGroup.setVisible(false);
        if (presenter != null) {
            String dataSetUUID = dataSetListBox.getValue(dataSetListBox.getSelectedIndex());
            presenter.selectDataSet(dataSetUUID);
        }
    }
}
