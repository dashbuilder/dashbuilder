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
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.displayer.client.prototypes.DisplayerPrototypes;

@Dependent
public class DisplayerEditor implements IsWidget,
        DisplayerTypeSelector.Listener,
        DataSetLookupEditor.Listener,
        DisplayerSettingsEditorForm.Listener {

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
        void error(String msg, Exception e);
    }

    View view = null;
    Listener listener = null;

    DisplayerPrototypes prototypes;
    DisplayerSettings displayerSettings = null;
    boolean brandNewDisplayer = true;

    public DisplayerEditor() {
        this.prototypes = DisplayerPrototypes.get();
        this.view = new DisplayerEditorView(
                new DisplayerTypeSelector(),
                new DataSetLookupEditor(),
                new DisplayerSettingsEditorForm());
    }

    @Inject
    public DisplayerEditor(
            View view,
            DisplayerPrototypes prototypes) {

        this.view = view;
        this.prototypes = prototypes;
    }

    public Widget asWidget() {
        return view.asWidget();
    }

    public void init(DisplayerSettings settings, Listener editorListener) {
        this.listener = editorListener;

        if (settings != null) {
            brandNewDisplayer = false;
            displayerSettings = settings.cloneInstance();
            view.init(displayerSettings, this);
            view.disableTypeSelection();
            view.gotoDisplaySettings();
        } else {
            brandNewDisplayer = true;
            displayerSettings = prototypes.getProto(DisplayerType.BARCHART).cloneInstance();
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
        if (listener != null) {
            listener.onSave(this);
        }
    }

    public void close() {
        if (listener != null) {
            listener.onClose(this);
        }
    }

    public void fetchDataSetLookup() {
        try {
            String uuid = displayerSettings.getDataSetLookup().getDataSetUUID();
            DataSetLookup metadataLookup = DataSetFactory.newDataSetLookupBuilder().dataset(uuid).buildLookup();
            DataSetClientServices.get().fetchMetadata(metadataLookup, new DataSetMetadataCallback() {
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
        displayerSettings = prototypes.getProto(type).cloneInstance();
        view.init(displayerSettings, this);
    }

    @Override
    public void dataSetChanged(final String uuid) {
        try {
            DataSetClientServices.get().fetchMetadata(DataSetFactory.newDataSetLookupBuilder().dataset(uuid).buildLookup(), new DataSetMetadataCallback() {
                public void callback(DataSetMetadata metadata) {

                    // Create a dataSetLookup instance for the target data set that fits the displayer constraints
                    Displayer displayer = DisplayerLocator.get().lookupDisplayer(displayerSettings);
                    DataSetLookupConstraints constraints = displayer.getDisplayerConstraints().getDataSetLookupConstraints();
                    DataSetLookup lookup = constraints.newDataSetLookup(metadata);
                    if (lookup == null) view.error("Is not possible to create a data lookup request for the selected data set", null);

                    // Make the view show the new lookup instance
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
    public void groupColumnChanged(DataSetGroup groupOp) {
        view.init(displayerSettings, this);
    }

    @Override
    public void groupFunctionColumnChanged(GroupFunction groupFunction) {
        view.init(displayerSettings, this);
    }
}
