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

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.Well;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.client.resources.i18n.AggregateFunctionTypeConstants;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.GroupFunction;

@Dependent
public class DataSetLookupEditorView extends Composite
        implements DataSetLookupEditor.View {

    interface Binder extends UiBinder<Widget, DataSetLookupEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    public DataSetLookupEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
        groupDetailsIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                expandCollapseGroupDetails();
            }
        }, ClickEvent.getType());
    }

    DataSetLookupEditor presenter;

    @UiField
    ListBox dataSetListBox;

    @UiField
    com.github.gwtbootstrap.client.ui.Label statusLabel;

    @UiField
    Panel groupControlPanel;

    @UiField
    Label groupControlLabel;

    @UiField
    Icon groupDetailsIcon;

    @UiField
    ListBox groupColumnListBox;

    @UiField
    Panel groupDatePanel;

    @UiField
    DataSetGroupDateEditor groupDateEditor;

    @UiField
    Panel columnsControlPanel;

    @UiField
    Label columnsControlLabel;

    @UiField
    Panel columnsPanel;

    @UiField
    Panel filtersControlPanel;

    @UiField
    DataSetFilterEditor filterEditor;

    @Override
    public void init(DataSetLookupEditor presenter) {
        this.presenter = presenter;
        filtersControlPanel.setVisible(false);
        groupControlPanel.setVisible(false);
        columnsControlPanel.setVisible(false);
    }

    @Override
    public void updateDataSetLookup() {
        _updateFilterControls();
        _updateGroupControls();
        _updateColumnControls();
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
        String selectedUUID = presenter.getDataSetUUID();

        int offset = 0;
        if (StringUtils.isBlank(selectedUUID)) {
            dataSetListBox.addItem("- Select -");
            offset++;
        }

        boolean found = false;
        for (int i=0; i<dataSetDefs.size(); i++) {
            DataSetDef def = dataSetDefs.get(i);
            addDataSetDef(def);
            if (selectedUUID != null && selectedUUID.equals(def.getUUID())) {
                dataSetListBox.setSelectedIndex(i+offset);
                found = true;
            }
        }
        if (!StringUtils.isBlank(selectedUUID) && !found) {
            errorDataSetNotFound(selectedUUID);
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
        filtersControlPanel.setVisible(false);
        groupControlPanel.setVisible(false);
        columnsControlPanel.setVisible(false);

        String dataSetUUID = dataSetListBox.getValue(dataSetListBox.getSelectedIndex());
        presenter.changeDataSet(dataSetUUID);
    }

    @UiHandler(value = "groupColumnListBox")
    public void onRowColumnChanged(ChangeEvent changeEvent) {
        String columnId = groupColumnListBox.getValue(groupColumnListBox.getSelectedIndex());
        if ("- All - ".equals(columnId)) columnId = null;
        presenter.changeGroupColumn(columnId);

        _updateColumnControls();

        groupDatePanel.setVisible(false);
        groupDetailsIcon.setVisible(false);
        if (presenter.isFirstGroupOpDateBased()) {
            groupDetailsIcon.setVisible(true);
            expandCollapseGroupDetails();
        }
    }

    public void expandCollapseGroupDetails() {
        if (groupDatePanel.isVisible()) {
            groupDatePanel.setVisible(false);
            groupDetailsIcon.setType(IconType.ARROW_DOWN);
        } else {
            groupDatePanel.setVisible(true);
            groupDetailsIcon.setType(IconType.ARROW_UP);
            ColumnGroup columnGroup = presenter.getFirstGroupOp().getColumnGroup();
            groupDateEditor.init(columnGroup, presenter);
        }
    }

    // UI handling stuff

    private void _updateFilterControls() {
        filtersControlPanel.setVisible(true);
        filterEditor.init(presenter.getDataSetMetadata(),
                presenter.getDataSetLookup().getFirstFilterOp(),
                presenter);
    }

    private void _updateGroupControls() {
        DataSetLookupConstraints constraints = presenter.getConstraints();
        String groupColumnId = presenter.getFirstGroupColumnId();
        List<Integer> groupColumnIdxs = presenter.getAvailableGroupColumnIdxs();
        String rowsTitle = constraints.getGroupsTitle();

        groupControlPanel.setVisible(false);
        groupColumnListBox.clear();
        groupDatePanel.setVisible(false);
        groupDetailsIcon.setVisible(false);
        if (presenter.isFirstGroupOpDateBased()) {
            groupDetailsIcon.setVisible(true);
            groupDetailsIcon.setType(IconType.ARROW_DOWN);
        }

        // Only show the group controls if group is enabled
        if (constraints.isGroupRequired() || constraints.isGroupAllowed()) {

            groupControlPanel.setVisible(true);
            if (!StringUtils.isBlank(rowsTitle)) groupControlLabel.setText(rowsTitle);

            int offset = 0;
            if (!constraints.isGroupRequired()) {
                groupColumnListBox.addItem("- All - ");
                offset++;
            }
            for (int i = 0; i < groupColumnIdxs.size(); i++) {
                int idx = groupColumnIdxs.get(i);
                String columnId = presenter.getColumnId(idx);

                groupColumnListBox.addItem(columnId, columnId);
                if (groupColumnId != null && groupColumnId.equals(columnId)) {
                    groupColumnListBox.setSelectedIndex(i+offset);
                }
            }
            // Always ensure a group exists when required
            if (constraints.isGroupRequired() && groupColumnId == null) {
                groupColumnId = presenter.getColumnId(groupColumnIdxs.get(0));
                presenter.createGroupColumn(groupColumnId);
            }
        }
    }

    private void _updateColumnControls() {
        DataSetLookupConstraints constraints = presenter.getConstraints();
        String groupColumnId = presenter.getFirstGroupColumnId();
        List<GroupFunction> groupFunctions = presenter.getFirstGroupFunctions();
        String columnsTitle = constraints.getColumnsTitle();
        boolean functionsRequired = constraints.isFunctionRequired();
        boolean functionsEnabled = (groupColumnId != null || functionsRequired);

        // Show the columns section
        columnsPanel.clear();
        columnsControlPanel.setVisible(true);
        if (!StringUtils.isBlank(columnsTitle)) columnsControlLabel.setText(columnsTitle);

        ColumnType[] targetTypes = constraints.getColumnTypes(groupFunctions.size());
        for (int i=0; i<groupFunctions.size(); i++) {
            try {
                GroupFunction groupFunction = groupFunctions.get(i);
                if (i == 0 && groupColumnId != null && constraints.isGroupColumn()) {
                    continue;
                }

                ColumnType columnType = null;
                if (targetTypes != null && i<targetTypes.length) columnType = targetTypes[i];

                String columnTitle = constraints.getColumnTitle(i);
                columnsPanel.add(_createColumnPanel(groupFunction, columnType, columnTitle, functionsEnabled));
            }
            catch (Exception e) {
                columnsPanel.add(new Label(e.getMessage()));
            }
        }
    }

    private Panel _createColumnPanel(final GroupFunction groupFunction,
            ColumnType targetType,
            String columnTitle,
            boolean functionsEnabled) throws Exception {

        HorizontalPanel panel = new HorizontalPanel();
        final ListBox columnListBox = new ListBox();
        columnListBox.setWidth("130px");
        columnListBox.setTitle(columnTitle);
        panel.add(columnListBox);

        boolean targetNumeric = targetType != null && targetType.equals(ColumnType.NUMBER);
        List<Integer> columnIdxs = presenter.getAvailableFunctionColumnIdxs();
        for (int i=0; i<columnIdxs.size(); i++) {
            int columnIdx = columnIdxs.get(i);
            String columnId = presenter.getColumnId(columnIdx);
            ColumnType columnType = presenter.getColumnType(columnIdx);

            if (targetType == null || targetNumeric || targetType.equals(columnType)) {
                columnListBox.addItem(columnId, columnId);
                if (columnId != null && columnId.equals(groupFunction.getSourceId())) {
                    columnListBox.setSelectedIndex(i);
                }
            }
        }
        if (functionsEnabled && (targetType == null || targetNumeric)) {
            final ListBox funcListBox = _createFunctionListBox(groupFunction, targetNumeric);
            funcListBox.setWidth("70px");
            panel.add(funcListBox);

            columnListBox.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    _changeColumnSettings(groupFunction, columnListBox, funcListBox);
                }
            });
            funcListBox.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    _changeColumnSettings(groupFunction, columnListBox, funcListBox);
                }
            });
        } else {
            columnListBox.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    _changeColumnSettings(groupFunction, columnListBox, null);
                }
            });
        }
        return panel;
    }

    private void _changeColumnSettings(GroupFunction groupFunction, ListBox columnListBox, ListBox functionListBox) {
        String columnId = columnListBox.getValue(columnListBox.getSelectedIndex());
        String function = (functionListBox != null ? functionListBox.getValue(functionListBox.getSelectedIndex()) : null);
        if (function != null && function.equals("---")) function = null;
        presenter.changeGroupFunction(groupFunction, columnId, function);
        _updateColumnControls();
    }

    private ListBox _createFunctionListBox(GroupFunction groupFunction, boolean numericOnly) {
        ListBox lb = new ListBox();
        if (!numericOnly) lb.addItem("---");

        AggregateFunctionType selected = groupFunction.getFunction();
        for (AggregateFunctionType functionType : presenter.getAvailableFunctions(groupFunction)) {
            String functionName = AggregateFunctionTypeConstants.INSTANCE.getString(functionType.name());
            lb.addItem(functionName);
            if (selected != null && selected.equals(functionType)) {
                lb.setSelectedValue(functionName);
            }
        }
        return lb;
    }
}
