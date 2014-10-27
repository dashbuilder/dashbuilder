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
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.group.AggregateFunction;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.GroupFunction;

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

    @UiField
    VerticalPanel columnsPanel;

    @Override
    public void init(DataSetLookupEditor presenter) {
        this.presenter = presenter;
        rowsControlGroup.setVisible(false);
        columnsControlGroup.setVisible(false);
        dataSetListBox.setWidth("200px");
        rowColumnListBox.setWidth("200px");
    }

    @Override
    public void updateDataSetLookup() {
        String groupColumnId = presenter.getFirstGroupColumnId();
        if (groupColumnId != null) {
            updateDataSetGroup(groupColumnId);
        } else {
            // TODO: support for non grouped visualizations (table, single meter)
        }
    }

    @Override
    public void addDataSetDef(DataSetDef def) {
        dataSetListBox.addItem(def.getUUID() + " (" + def.getProvider() + ")", def.getUUID());
    }

    @Override
    public void removeDataSetDef(DataSetDef def) {
        int target = -1;
        for (int i=0; i<dataSetListBox.getItemCount(); i++) {
            String uuid = dataSetListBox.getValue(i);
            if (uuid.equals(def.getUUID())) target = i;
        }
        if (target != -1) {
            dataSetListBox.removeItem(target);
        }
    }

    @Override
    public void showDataSetDefs(List<DataSetDef> dataSetDefs) {
        dataSetListBox.clear();
        dataSetListBox.addItem("- Select -");
        String selectedUUID = presenter.getDataSetUUID();
        for (int i=0; i<dataSetDefs.size(); i++) {
            DataSetDef def = dataSetDefs.get(i);
            addDataSetDef(def);
            if (selectedUUID != null && selectedUUID.equals(def.getUUID())) {
                dataSetListBox.setSelectedIndex(i+1);
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

        String dataSetUUID = dataSetListBox.getValue(dataSetListBox.getSelectedIndex());
        presenter.changeDataSet(dataSetUUID);
    }

    @UiHandler(value = "rowColumnListBox")
    public void onRowColumnChanged(ChangeEvent changeEvent) {
        String columnId = rowColumnListBox.getValue(rowColumnListBox.getSelectedIndex());
        presenter.changeGroupColumn(columnId);
    }

    // UI handling stuff

    private void updateDataSetGroup(String groupColumnId) {
        rowsControlGroup.setVisible(true);
        columnsControlGroup.setVisible(true);
        rowColumnListBox.clear();
        columnsPanel.clear();

        List<Integer> groupColumnIdxs = presenter.getAvailableGroupColumnIdxs();
        for (int i=0; i<groupColumnIdxs.size(); i++) {
            int idx = groupColumnIdxs.get(i);
            String columnId = presenter.getColumnId(idx);
            ColumnType columnType = presenter.getColumnType(idx);
            rowColumnListBox.addItem(columnId + " (" + columnType + ")", columnId);
            if (groupColumnId != null && groupColumnId.equals(columnId)) {
                rowColumnListBox.setSelectedIndex(i);
            }
        }
        List<GroupFunction> groupFunctions = presenter.getFirstGroupFunctions();
        for (GroupFunction groupFunction : groupFunctions) {
            columnsPanel.add(createColumnPanel(groupFunction));
        }
    }

    private Panel createColumnPanel(final GroupFunction groupFunction) {
        HorizontalPanel panel = new HorizontalPanel();
        final ListBox columnListBox = new ListBox();
        List<Integer> columnIdxs = presenter.getAvailableFunctionColumnIdxs();

        for (int i=0; i<columnIdxs.size(); i++) {
            int columnIdx = columnIdxs.get(i);
            String columnId = presenter.getColumnId(columnIdx);
            ColumnType columnType = presenter.getColumnType(columnIdx);
            columnListBox.addItem(columnId + " (" + columnType + ")", columnId);
            if (columnId != null && columnId.equals(groupFunction.getSourceId())) {
                columnListBox.setSelectedIndex(i);
            }
        }

        final ListBox funcListBox = createFunctionListBox(groupFunction.getFunction());
        columnListBox.setWidth("130px");
        funcListBox.setWidth("70px");
        panel.add(columnListBox);
        panel.add(funcListBox);

        columnListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                String columnId = columnListBox.getValue(columnListBox.getSelectedIndex());
                String function = funcListBox.getValue(funcListBox.getSelectedIndex());
                presenter.changeGroupFunction(groupFunction, columnId, function);
            }
        });
        funcListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                String columnId = columnListBox.getValue(columnListBox.getSelectedIndex());
                String function = funcListBox.getValue(funcListBox.getSelectedIndex());
                presenter.changeGroupFunction(groupFunction, columnId, function);
            }
        });
        return panel;
    }

    private ListBox createFunctionListBox(AggregateFunctionType selected) {
        ListBox lb = new ListBox();
        for (AggregateFunction function : DataSetClientServices.get().getAggregateFunctionManager().getAllFunctions()) {
            lb.addItem(function.getType().name());
            if (selected != null && selected.equals(function.getType())) {
                lb.setSelectedValue(function.getType().name());
            }
        }
        return lb;
    }
}
