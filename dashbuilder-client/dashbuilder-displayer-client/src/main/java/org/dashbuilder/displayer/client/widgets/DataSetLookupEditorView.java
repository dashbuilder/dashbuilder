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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.dashbuilder.displayer.client.widgets.filter.DataSetFilterEditor;
import org.dashbuilder.displayer.client.widgets.group.ColumnFunctionEditor;
import org.dashbuilder.displayer.client.widgets.group.DataSetGroupDateEditor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.LabelType;

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
    org.gwtbootstrap3.client.ui.Label statusLabel;

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
    Button addColumnButton;

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
        String name = def.getName();
        if (StringUtils.isBlank(name)) name = def.getUUID();
        dataSetListBox.addItem(name, def.getUUID());
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
            dataSetListBox.addItem( CommonConstants.INSTANCE.common_dropdown_select());
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
        statusLabel.setText( CommonConstants.INSTANCE.dataset_lookup_dataset_notfound(dataSetUUID));
        statusLabel.setType( LabelType.WARNING );
    }

    @Override
    public void errorOnInit(Exception e) {
        statusLabel.setVisible(true);
        statusLabel.setText( CommonConstants.INSTANCE.dataset_lookup_init_error());
        statusLabel.setType( LabelType.WARNING );
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

    @UiHandler(value = "addColumnButton")
    public void onAddColumnClicked(ClickEvent clickEvent) {
        presenter.addGroupFunction();
        _updateColumnControls();
    }

    @UiHandler(value = "groupColumnListBox")
    public void onRowColumnChanged(ChangeEvent changeEvent) {
        String columnId = groupColumnListBox.getValue(groupColumnListBox.getSelectedIndex());
        if ( CommonConstants.INSTANCE.dataset_lookup_group_columns_all().equals( columnId )) columnId = null;
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
                groupColumnListBox.addItem( CommonConstants.INSTANCE.dataset_lookup_group_columns_all());
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
        boolean canDelete = groupFunctions.size() > constraints.getMinColumns();
        int n = constraints.getMaxColumns();
        boolean canAdd = constraints.areExtraColumnsAllowed() && (n < 0 || groupFunctions.size() < n);

        // Show the columns section
        columnsPanel.clear();
        columnsControlPanel.setVisible(true);
        if (!StringUtils.isBlank(columnsTitle)) columnsControlLabel.setText(columnsTitle);
        addColumnButton.setVisible(canAdd);

        ColumnType lastTargetType = null;
        ColumnType[] targetTypes = constraints.getColumnTypes(groupFunctions.size());
        for (int i=0; i<groupFunctions.size(); i++) {
            final int columnIdx = i;

            final GroupFunction groupFunction = groupFunctions.get(columnIdx);
            if (targetTypes != null && i < targetTypes.length) {
                lastTargetType = targetTypes[i];
            }
            if (columnIdx == 0 && groupColumnId != null && constraints.isGroupColumn()) {
                continue;
            }
            ColumnType columnType = null;
            if (targetTypes != null && i<targetTypes.length) columnType = targetTypes[columnIdx];
            if (columnType == null) columnType = lastTargetType; // Extra columns

            String columnTitle = constraints.getColumnTitle(columnIdx);
            ColumnFunctionEditor columnEditor = new ColumnFunctionEditor();
            columnEditor.init(presenter.getDataSetMetadata(),  groupFunction, columnType,
                    columnTitle, functionsEnabled, canDelete, new ColumnFunctionEditor.Listener() {

                        public void columnChanged(ColumnFunctionEditor editor) {
                            presenter.changeGroupFunction(groupFunction, editor.getSourceId(), editor.getFunction());
                            _updateColumnControls();
                        }
                        public void columnDeleted(ColumnFunctionEditor editor) {
                            presenter.removeGroupFunction(columnIdx);
                            _updateColumnControls();
                        }
                    });
            columnsPanel.add(columnEditor);
        }
    }
}
