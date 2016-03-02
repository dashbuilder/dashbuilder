/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.ValidationError;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractDisplayerListener;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.displayer.client.events.DataSetLookupChangedEvent;
import org.dashbuilder.displayer.client.events.DisplayerEditorClosedEvent;
import org.dashbuilder.displayer.client.events.DisplayerEditorSavedEvent;
import org.dashbuilder.displayer.client.events.DisplayerSettingsChangedEvent;
import org.dashbuilder.displayer.client.events.DisplayerSubtypeSelectedEvent;
import org.dashbuilder.displayer.client.events.DisplayerTypeSelectedEvent;
import org.dashbuilder.displayer.client.prototypes.DisplayerPrototypes;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.Command;

@Dependent
public class DisplayerEditor implements IsWidget {

    public interface View extends UberView<DisplayerEditor> {

        String getBrandNewDisplayerTitle();

        boolean isTableDisplayModeOn();

        void setTableDisplayModeEnabled(boolean enabled);

        void showDisplayer(Displayer displayer);

        void setTypeSelectionEnabled(boolean enabled);

        void setDisplaySettingsEnabled(boolean enabled);

        void setDataSetLookupConfEnabled(boolean enabled);

        void gotoTypeSelection(DisplayerTypeSelector typeSelector);

        void gotoDataSetLookupConf(DataSetLookupEditor lookupEditor);

        void gotoDisplaySettings(DisplayerSettingsEditor settingsEditor);

        void showTypeChangedWarning(Command yes, Command no);

        void error(String error);

        void error(ClientRuntimeError error);
    }

    protected View view = null;
    protected DataSetClientServices clientServices = null;
    protected DisplayerLocator displayerLocator = null;
    protected DisplayerPrototypes displayerPrototypes = null;
    protected DisplayerSettings displayerSettings = null;
    protected DisplayerSettings selectedTypeSettings = null;
    protected boolean brandNewDisplayer = true;
    protected DisplayerTypeSelector typeSelector;
    protected DataSetLookupEditor lookupEditor;
    protected DisplayerSettingsEditor settingsEditor;
    protected DisplayerEditorStatus editorStatus;
    protected Displayer displayer = null;
    protected int activeSection = -1;
    protected boolean typeSelectionEnabled = true;
    protected boolean dataLookupConfEnabled = true;
    protected boolean displaySettingsEnabled = true;
    protected Event<DisplayerEditorSavedEvent> saveEvent;
    protected Event<DisplayerEditorClosedEvent> closeEvent;
    protected Command onCloseCommand = new Command() { public void execute() {}};
    protected Command onSaveCommand = new Command() { public void execute() {}};

    DisplayerListener displayerListener = new AbstractDisplayerListener() {
        public void onError(Displayer displayer, ClientRuntimeError error) {
            view.error(error);
        }
    };

    @Inject
    public DisplayerEditor(View view,
                           DataSetClientServices clientServices,
                           DisplayerLocator displayerLocator,
                           DisplayerPrototypes displayerPrototypes,
                           DisplayerTypeSelector typeSelector,
                           DataSetLookupEditor lookupEditor,
                           DisplayerSettingsEditor settingsEditor,
                           DisplayerEditorStatus editorStatus,
                           Event<DisplayerEditorSavedEvent> savedEvent,
                           Event<DisplayerEditorClosedEvent> closedEvent) {
        this.view = view;
        this.displayerLocator = displayerLocator;
        this.clientServices = clientServices;
        this.displayerPrototypes = displayerPrototypes;
        this.typeSelector = typeSelector;
        this.lookupEditor = lookupEditor;
        this.settingsEditor = settingsEditor;
        this.editorStatus = editorStatus;
        this.saveEvent = savedEvent;
        this.closeEvent = closedEvent;

        view.init(this);
    }

    public void init(DisplayerSettings settings) {
        if (settings != null) {
            brandNewDisplayer = false;
            displayerSettings = settings;
        } else {
            brandNewDisplayer = true;
            displayerSettings = displayerPrototypes.getProto(DisplayerType.BARCHART);
            displayerSettings.setTitle(view.getBrandNewDisplayerTitle());
        }
        selectedTypeSettings = displayerSettings;

        initDisplayer();
        initTypeSelector();
        initLookupEditor();
        initSettingsEditor();
        gotoLastSection();
        showDisplayer();
    }

    protected void initDisplayer() {
        if (displayer != null) {
            displayer.close();
        }
        displayer = displayerLocator.lookupDisplayer(displayerSettings);
        displayer.addListener(displayerListener);
        displayer.setRefreshOn(false);
        displayer.draw();
    }

    protected void initLookupEditor() {
        DataSetLookupConstraints lookupConstraints = displayer.getDisplayerConstraints().getDataSetLookupConstraints();
        lookupEditor.init(lookupConstraints, displayerSettings.getDataSetLookup());
    }

    protected void initTypeSelector() {
        typeSelector.init(displayerSettings.getType(), displayerSettings.getSubtype());
    }

    protected void initSettingsEditor() {
        settingsEditor.init(displayerSettings);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public View getView() {
        return view;
    }

    public boolean isBrandNewDisplayer() {
        return brandNewDisplayer;
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public Displayer getDisplayer() {
        return displayer;
    }

    public DisplayerTypeSelector getTypeSelector() {
        return typeSelector;
    }

    public DataSetLookupEditor getLookupEditor() {
        return lookupEditor;
    }

    public DisplayerSettingsEditor getSettingsEditor() {
        return settingsEditor;
    }

    public void setTypeSelectorEnabled(boolean enabled) {
        typeSelectionEnabled = enabled;
        view.setTypeSelectionEnabled(enabled);
    }

    public void setDataSetLookupConfEnabled(boolean enabled) {
        dataLookupConfEnabled = enabled;
        view.setDataSetLookupConfEnabled(enabled);
    }

    public void setDisplaySettingsEnabled(boolean enabled) {
        displaySettingsEnabled = enabled;
        view.setDisplaySettingsEnabled(enabled);
    }

    public void setOnSaveCommand(Command saveCommand) {
        this.onSaveCommand = saveCommand != null ? saveCommand : onCloseCommand;
    }

    public void setOnCloseCommand(Command closeCommand) {
        this.onCloseCommand = closeCommand != null ? closeCommand : onCloseCommand;
    }

    public void showDisplayer() {
        if (view.isTableDisplayModeOn()) {
            try {
                DisplayerSettings tableSettings = displayerSettings.cloneInstance();
                tableSettings.setTitleVisible(false);
                tableSettings.setType(DisplayerType.TABLE);
                tableSettings.setTablePageSize(8);
                tableSettings.setTableWidth(800);
                tableSettings.setRenderer("default");
                Displayer tableDisplayer = displayerLocator.lookupDisplayer(tableSettings);
                tableDisplayer.addListener(displayerListener);
                tableDisplayer.setRefreshOn(false);
                tableDisplayer.draw();
                view.showDisplayer(tableDisplayer);
            } catch (Exception e) {
                view.error(new ClientRuntimeError(e));
            }
        } else {
            view.showDisplayer(displayer);
        }
    }

    public void gotoFirstSectionEnabled() {
        if (typeSelectionEnabled) {
            gotoTypeSelection();
        }
        else if (dataLookupConfEnabled) {
            gotoDataSetLookupConf();
        }
        else if (displaySettingsEnabled) {
            gotoDisplaySettings();
        } else {
            view.error("Nothing to show!");
        }
    }

    public void gotoLastSection() {
        int lastOption = editorStatus.getSelectedOption(displayerSettings.getUUID());
        if (activeSection < 0 || activeSection != lastOption) {
            switch (lastOption) {
                case 2:
                    gotoDisplaySettings();
                    break;
                case 1:
                    gotoDataSetLookupConf();
                    break;
                default:
                    gotoFirstSectionEnabled();
                    break;
            }
        }
    }

    public void gotoTypeSelection() {
        activeSection = 0;
        editorStatus.saveSelectedOption(displayerSettings.getUUID(), activeSection);
        view.gotoTypeSelection(typeSelector);
    }

    public void gotoDataSetLookupConf() {
        activeSection = 1;
        editorStatus.saveSelectedOption(displayerSettings.getUUID(), activeSection);
        view.gotoDataSetLookupConf(lookupEditor);
        view.setTableDisplayModeEnabled(!DisplayerType.TABLE.equals(displayerSettings.getType()));
    }

    public void gotoDisplaySettings() {
        activeSection = 2;
        editorStatus.saveSelectedOption(displayerSettings.getUUID(), activeSection);
        view.gotoDisplaySettings(settingsEditor);
    }

    public void save() {
        // Clear settings before return
        DisplayerConstraints displayerConstraints = displayer.getDisplayerConstraints();
        displayerConstraints.removeUnsupportedAttributes(displayerSettings);

        // Dispose the displayer
        if (displayer != null) {
            displayer.close();
        }
        // Notify event
        onSaveCommand.execute();
        saveEvent.fire(new DisplayerEditorSavedEvent(displayerSettings));
    }

    public void close() {
        if (displayer != null) {
            displayer.close();
        }
        onCloseCommand.execute();
        closeEvent.fire(new DisplayerEditorClosedEvent(displayerSettings));
    }

    // Widget listeners callback notifications

    void onDataSetLookupChanged(@Observes DataSetLookupChangedEvent event) {
        DataSetLookup dataSetLookup = event.getDataSetLookup();
        displayerSettings.setDataSet(null);
        displayerSettings.setDataSetLookup(dataSetLookup);
        removeStaleSettings();
        initDisplayer();
        showDisplayer();
    }

    void onDisplayerSettingsChanged(@Observes DisplayerSettingsChangedEvent event) {
        displayerSettings = event.getDisplayerSettings();
        initDisplayer();
        showDisplayer();
    }

    void onDisplayerTypeChanged(@Observes DisplayerTypeSelectedEvent event) {
        displayerTypeChanged(event.getSelectedType(), null);
    }

    void onDisplayerSubtypeChanged(@Observes DisplayerSubtypeSelectedEvent event) {
        displayerTypeChanged(selectedTypeSettings.getType(), event.getSelectedSubType());
    }

    void displayerTypeChanged(DisplayerType type, DisplayerSubType displayerSubType) {

        // Create new settings for the selected type
        selectedTypeSettings = displayerPrototypes.getProto(type);
        selectedTypeSettings.setSubtype(displayerSubType);
        DataSet oldDataSet = displayerSettings.getDataSet();
        DataSetLookup oldDataLookup = displayerSettings.getDataSetLookup();

        // Check if the current data lookup is compatible with the new displayer type
        if (oldDataSet == null && oldDataLookup != null) {
            Displayer displayer = displayerLocator.lookupDisplayer(selectedTypeSettings);
            DisplayerConstraints displayerConstraints = displayer.getDisplayerConstraints();
            DataSetLookupConstraints dataConstraints = displayerConstraints.getDataSetLookupConstraints();
            DataSetMetadata metadata = clientServices.getMetadata(oldDataLookup.getDataSetUUID());

            // Keep the current data settings provided it satisfies the data constraints
            ValidationError validationError = dataConstraints.check(oldDataLookup, metadata);
            if (validationError == null) {
                selectedTypeSettings.setDataSet(null);
                selectedTypeSettings.setDataSetLookup(oldDataLookup);
                applySelectedType();
            }
            // If the data lookup is not compatible then ask the user what to do
            else {
                view.showTypeChangedWarning(
                        new Command() {
                            public void execute() {
                                applySelectedType();
                            }
                        },
                        new Command() {
                            public void execute() {
                                abortSelectedType();
                            }
                        });
            }
        }
        // If the displayer is static (no data lookup) then just display the selected displayer prototype
        else {
            applySelectedType();
        }
    }

    void applySelectedType() {
        // Remove the non supported attributes
        displayerSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.TYPE);
        displayerSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.SUBTYPE);
        displayerSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_GROUP);
        displayerSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP);
        displayerSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_LEGEND_GROUP);
        displayerSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.AXIS_GROUP);
        selectedTypeSettings.getSettingsFlatMap().putAll(displayerSettings.getSettingsFlatMap());

        try {
            // Ensure the renderer supports the new type
            displayerLocator.lookupDisplayer(selectedTypeSettings);
        } catch(Exception e) {
            // The new type might not support the selected renderer.
            selectedTypeSettings.removeDisplayerSetting(DisplayerAttributeDef.RENDERER);
        }

        // Re-initialize the editor with the new settings
        init(selectedTypeSettings);
        removeStaleSettings();
    }

    void abortSelectedType() {
        selectedTypeSettings = displayerSettings;
        typeSelector.init(displayerSettings.getType(), displayerSettings.getSubtype());
        view.showDisplayer(displayer);
    }

    List<String> getExistingDataColumnIds() {
        DataSet dataSet = displayerSettings.getDataSet();
        DataSetLookup dataSetLookup = displayerSettings.getDataSetLookup();

        List<String> columnIds = new ArrayList<String>();
        if (dataSet != null) {
            for (DataColumn dataColumn : dataSet.getColumns()) {
                columnIds.add(dataColumn.getId());
            }
        }
        else if (dataSetLookup != null) {
            int idx = dataSetLookup.getLastGroupOpIndex(0);
            if (idx != -1) {
                DataSetGroup groupOp = dataSetLookup.getOperation(idx);
                for (GroupFunction groupFunction : groupOp.getGroupFunctions()) {
                    columnIds.add(groupFunction.getColumnId());
                }
            }
        }
        return columnIds;
    }

    void removeStaleSettings() {
        List<String> columnIds = getExistingDataColumnIds();

        // Remove the settings for non existing columns
        Iterator<ColumnSettings> it = displayerSettings.getColumnSettingsList().iterator();
        while (it.hasNext()) {
            ColumnSettings columnSettings = it.next();
            if (!columnIds.contains(columnSettings.getColumnId())) {
                it.remove();
            }
        }
        // Reset table sort column
        if (!columnIds.contains(displayerSettings.getTableDefaultSortColumnId())) {
            displayerSettings.setTableDefaultSortColumnId(null);
        }
    }
}
