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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.ValidationError;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.displayer.client.prototypes.DisplayerPrototypes;
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

    View view = null;
    Listener listener = null;

    DisplayerSettings displayerSettings = null;
    boolean brandNewDisplayer = true;

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
            displayerSettings.setTitle("- New displayer - ");
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
                    view.error("Selected data set not found", null);
                }
            });
        } catch (Exception e) {
            view.error("Error fetching the data set metadata", e);
        }
    }

    // Widget listeners callback notifications

    @Override
    public void displayerSettingsChanged(DisplayerSettings settings) {
        displayerSettings = settings;
        view.init(displayerSettings, this);
    }

    @Override
    public void displayerTypeChanged(DisplayerType type) {

        // Create new settings for the selected type
        DisplayerSettings oldSettings = displayerSettings;
        DisplayerSettings newSettings = DisplayerPrototypes.get().getProto(type);
        DataSet oldDataSet = displayerSettings.getDataSet();
        DataSetLookup oldDataLookup = displayerSettings.getDataSetLookup();

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
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_GROUP);
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP);
        oldSettings.removeDisplayerSetting(DisplayerAttributeGroupDef.CHART_LEGEND_GROUP);
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
                    if (lookup == null) view.error("Is not possible to create a data lookup request for the selected data set", null);

                    // Make the view to show the new lookup instance
                    displayerSettings.setDataSet(null);
                    displayerSettings.setDataSetLookup(lookup);
                    view.updateDataSetLookup(constraints, metadata);
                }
                public void notFound() {
                    // Very unlikely since this data set has been selected from a list provided by the backend.
                    view.error("Selected data set not found", null);
                }
            });
        } catch (Exception e) {
            view.error("Error fetching the data set metadata", e);
        }
    }

    @Override
    public void groupChanged(DataSetGroup groupOp) {
        view.init(displayerSettings, this);
    }

    @Override
    public void columnChanged(GroupFunction groupFunction) {
        view.init(displayerSettings, this);
    }

    @Override
    public void filterChanged(DataSetFilter filterOp) {
        view.init(displayerSettings, this);
    }
}
