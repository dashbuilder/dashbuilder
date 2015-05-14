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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.ValidationError;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.displayer.client.prototypes.DisplayerPrototypes;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

@Dependent
public class DisplayerEditor implements IsWidget,
        DisplayerTypeSelector.Listener,
        DataSetLookupEditor.Listener,
        DisplayerSettingsEditor.Listener {

    public interface Listener {
        void onClose(DisplayerEditor editor);
        void onSave(DisplayerEditor editor);
    }

    public interface View extends IsWidget {
        void init(DisplayerSettings settings, DisplayerEditor presenter);
        void disableTypeSelection();
        void gotoTypeSelection();
        void gotoDataSetConf();
        void gotoDisplaySettings();
        void updateDataSetLookup(DataSetLookupConstraints constraints, DataSetMetadata metadata);
        void showTypeChangedWarning(DisplayerSettings oldSettings, DisplayerSettings newSettings);
        void error(String msg, Exception e);
        void close();
    }

    protected View view = null;
    protected Listener listener = null;
    protected DisplayerSettings displayerSettings = null;
    protected boolean brandNewDisplayer = true;

    public DisplayerEditor() {
        SyncBeanManager beanManager = IOC.getBeanManager();
        IOCBeanDef iocBeanDef = beanManager.lookupBean(DisplayerSettingsEditor.class);
        DisplayerSettingsEditor settingsEditor = (DisplayerSettingsEditor) iocBeanDef.getInstance();

        iocBeanDef = beanManager.lookupBean(DisplayerTypeSelector.class);
        DisplayerTypeSelector typeSelector = (DisplayerTypeSelector) iocBeanDef.getInstance();

        iocBeanDef = beanManager.lookupBean(DataSetLookupEditor.class);
        DataSetLookupEditor lookupEditor = (DataSetLookupEditor) iocBeanDef.getInstance();

        this.view = new DisplayerEditorView(typeSelector, lookupEditor, settingsEditor);
    }

    @Inject
    public DisplayerEditor(View view) {
        this.view = view;
    }

    public Widget asWidget() {
        return view.asWidget();
    }

    public void init(DisplayerSettings settings, Listener editorListener) {
        this.listener = editorListener;

        if (settings != null) {
            brandNewDisplayer = false;
            displayerSettings = settings;
            view.init(displayerSettings, this);
        } else {
            brandNewDisplayer = true;
            displayerSettings = DisplayerPrototypes.get().getProto(DisplayerType.BARCHART);
            displayerSettings.setTitle("- " + CommonConstants.INSTANCE.displayer_editor_new() + " -");
            view.init(displayerSettings, this);
            view.gotoTypeSelection();
        }
    }

    public boolean isBrandNewDisplayer() {
        return brandNewDisplayer;
    }

    public View getView() {
        return view;
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public void save() {
        view.close();

        // Clear settings before return
        Displayer displayer = DisplayerHelper.lookupDisplayer(displayerSettings);
        DisplayerConstraints displayerConstraints = displayer.getDisplayerConstraints();
        displayerConstraints.removeUnsupportedAttributes(displayerSettings);

        if (listener != null) {
            listener.onSave(this);
        }
    }

    public void close() {
        view.close();
        if (listener != null) {
            listener.onClose(this);
        }
    }

    public void fetchDataSetLookup() {
        try {
            String uuid = displayerSettings.getDataSetLookup().getDataSetUUID();
            DataSetClientServices.get().fetchMetadata(uuid, new DataSetMetadataCallback() {

                public void callback(DataSetMetadata metadata) {
                    Displayer displayer = DisplayerLocator.get().lookupDisplayer(displayerSettings);
                    DataSetLookupConstraints constraints = displayer.getDisplayerConstraints().getDataSetLookupConstraints();
                    view.updateDataSetLookup(constraints, metadata);
                }
                public void notFound() {
                    // Very unlikely since this data set has been selected from a list provided by the backend.
                    view.error(CommonConstants.INSTANCE.displayer_editor_dataset_notfound(), null);
                }

                @Override
                public boolean onError(DataSetClientServiceError error) {
                    // TODO
                    GWT.log("Error occurred in DisplayerEditor#fetchDataSetLookup!");
                    return false;
                }
            });
        } catch (Exception e) {
            view.error(CommonConstants.INSTANCE.displayer_editor_datasetmetadata_fetcherror(), e);
        }
    }

    // Widget listeners callback notifications

    @Override
    public void displayerSettingsChanged(DisplayerSettings settings) {
        displayerSettings = settings;
        view.init(displayerSettings, this);
    }

    @Override
    public void displayerTypeChanged(DisplayerType type, DisplayerSubType displayerSubType) {

        // Create new settings for the selected type
        DisplayerSettings oldSettings = displayerSettings;
        DisplayerSettings newSettings = DisplayerPrototypes.get().getProto(type);
        newSettings.setSubtype(displayerSubType);
        DataSet oldDataSet = oldSettings.getDataSet();
        DataSetLookup oldDataLookup = oldSettings.getDataSetLookup();

        // Check if the current data lookup is compatible with the new displayer type
        if (oldDataSet == null && oldDataLookup != null) {
            Displayer displayer = DisplayerHelper.lookupDisplayer(newSettings);
            DisplayerConstraints displayerConstraints = displayer.getDisplayerConstraints();
            DataSetLookupConstraints dataConstraints = displayerConstraints.getDataSetLookupConstraints();
            DataSetMetadata metadata = DataSetClientServices.get().getMetadata(oldDataLookup.getDataSetUUID());

            // Keep the current data settings provided it satisfies the data constraints
            ValidationError validationError = dataConstraints.check(oldDataLookup, metadata);
            if (validationError == null) {
                newSettings.setDataSet(null);
                newSettings.setDataSetLookup(oldDataLookup);
                changeSettings(oldSettings, newSettings);
            }
            // If the data lookup is not compatible then warn about it
            else {
                view.showTypeChangedWarning(oldSettings, newSettings);
            }
        }
        // If the displayer is static (no data lookup) then just display the selected displayer prototype
        else {
            changeSettings(oldSettings, newSettings);
        }
    }

    public void changeSettings(DisplayerSettings oldSettings, DisplayerSettings newSettings) {
        // Remove the non supported attributes
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.TYPE);
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.SUBTYPE);
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_GROUP);
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP);
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_LEGEND_GROUP);
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.AXIS_GROUP);
        newSettings.getSettingsFlatMap().putAll(oldSettings.getSettingsFlatMap());

        // Ensure the renderer supports the new type
        try {
            // Try to get the displayer for the new type
            DisplayerHelper.lookupDisplayer(newSettings);
        } catch(Exception e) {
            // The new type might not support the selected renderer.
            newSettings.removeDisplayerSetting(DisplayerAttributeDef.RENDERER);
        }

        // Update the view
        displayerSettings = newSettings;
        removeStaleSettings();
        view.init(displayerSettings, this);
    }
    @Override
    public void dataSetChanged(final String uuid) {
        try {
            DataSetClientServices.get().fetchMetadata(uuid, new DataSetMetadataCallback() {
                public void callback(DataSetMetadata metadata) {

                    // Create a dataSetLookup instance for the target data set that fits the displayer constraints
                    Displayer displayer = DisplayerLocator.get().lookupDisplayer(displayerSettings);
                    DataSetLookupConstraints constraints = displayer.getDisplayerConstraints().getDataSetLookupConstraints();
                    DataSetLookup lookup = constraints.newDataSetLookup(metadata);
                    if (lookup == null) view.error(CommonConstants.INSTANCE.displayer_editor_dataset_nolookuprequest(), null);

                    // Make the view to show the new lookup instance
                    displayerSettings.setDataSet(null);
                    displayerSettings.setDataSetLookup(lookup);

                    removeStaleSettings();
                    view.updateDataSetLookup(constraints, metadata);
                }
                public void notFound() {
                    // Very unlikely since this data set has been selected from a list provided by the backend.
                    view.error(CommonConstants.INSTANCE.displayer_editor_dataset_notfound(), null);
                }

                @Override
                public boolean onError(DataSetClientServiceError error) {
                    // TODO
                    GWT.log("Error occurred in DisplayerEditor#dataSetChanged!");
                    return false;
                }
            });
        } catch (Exception e) {
            view.error(CommonConstants.INSTANCE.displayer_editor_datasetmetadata_fetcherror(), e);
        }
    }

    @Override
    public void groupChanged(DataSetGroup groupOp) {
        removeStaleSettings();
        view.init(displayerSettings, this);
    }

    @Override
    public void columnChanged(GroupFunction groupFunction) {
        removeStaleSettings();
        view.init(displayerSettings, this);
    }

    @Override
    public void filterChanged(DataSetFilter filterOp) {
        view.init(displayerSettings, this);
    }

    public void removeStaleSettings() {
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

    public List<String> getExistingDataColumnIds() {
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
}
