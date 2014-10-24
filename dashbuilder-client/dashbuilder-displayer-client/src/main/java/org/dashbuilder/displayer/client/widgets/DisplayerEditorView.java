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

import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.constants.VisibilityChange;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerLocator;

@Dependent
public class DisplayerEditorView extends Composite
        implements DisplayerEditor.View {

    interface Binder extends UiBinder<Widget, DisplayerEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    public DisplayerEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Inject
    public DisplayerEditorView(DisplayerTypeSelector displayerTypeSelector,
            DataSetLookupEditor lookupEditor,
            DisplayerSettingsEditorForm settingsEditor) {
        this();
        this.typeSelector = displayerTypeSelector;
        this.lookupEditor = lookupEditor;
        this.settingsEditor = settingsEditor;
    }

    DisplayerEditor presenter;
    DisplayerSettings settings;
    DisplayerTypeSelector typeSelector;
    DataSetLookupEditor lookupEditor;
    DisplayerSettingsEditorForm settingsEditor;

    @UiField
    SimplePanel leftPanel;

    @UiField
    SimplePanel centerPanel;

    @UiField
    TabPanel optionsPanel;

    @UiField
    Tab optionType;

    @UiField
    Tab optionData;

    @UiField
    Tab optionSettings;

    @Override
    public void init(DisplayerSettings settings, DisplayerEditor presenter) {
        this.settings = settings;
        this.presenter = presenter;
        refreshDisplayer();
    }

    @Override
    public void disableTypeSelection() {
        optionType.addStyle(VisibilityChange.HIDE);
    }

    @Override
    public void gotoTypeSelection() {
        optionsPanel.selectTab(0);

        typeSelector.init(presenter);
        typeSelector.select(settings.getType());
        leftPanel.clear();
        leftPanel.add(typeSelector);

        refreshDisplayer();
    }

    @Override
    public void gotoDataSetConf() {
        optionsPanel.selectTab(1);

        if (settings.getDataSet() == null && settings.getDataSetLookup() != null) {
            // Fetch before initializing the editor
            presenter.fetchDataSetLookup();
        }
        else {
            // Just init the lookup editor
            lookupEditor.init(presenter);
        }

        leftPanel.clear();
        leftPanel.add(lookupEditor);

        refreshDisplayer();
    }

    @Override
    public void updateDataSetLookup(DataSetLookupConstraints constraints, DataSetMetadata metadata) {
        DataSetLookup dataSetLookup = settings.getDataSetLookup();
        lookupEditor.init(presenter, dataSetLookup, constraints, metadata);

        refreshDisplayer();
    }

    @Override
    public void gotoDisplaySettings() {
        optionsPanel.selectTab(2);
        optionSettings.setActive(true);

        settingsEditor.init(settings, presenter);
        leftPanel.clear();
        leftPanel.add(settingsEditor);

        refreshDisplayer();
    }

    @Override
    public void error(String msg, Exception e) {
        centerPanel.clear();
        centerPanel.add(new Label(msg));
        GWT.log(msg, e);
    }

    public void refreshDisplayer() {
        Displayer displayer = null;
        if (optionsPanel.getSelectedTab() != 1) {
            displayer = DisplayerLocator.get().lookupDisplayer(settings);
        } else {
            DisplayerSettings tableSettings = settings.cloneInstance();
            tableSettings.setTitleVisible(false);
            tableSettings.setType(DisplayerType.TABLE);
            displayer = DisplayerLocator.get().lookupDisplayer(tableSettings);
        }
        centerPanel.clear();
        centerPanel.add(displayer);
        DisplayerHelper.draw(displayer);
    }

    @UiHandler(value = "optionType")
    public void onTypeSelected(ClickEvent clickEvent) {
        gotoTypeSelection();
    }

    @UiHandler(value = "optionData")
    public void onDataSelected(ClickEvent clickEvent) {
        gotoDataSetConf();
    }

    @UiHandler(value = "optionSettings")
    public void onSettingsSelected(ClickEvent clickEvent) {
        gotoDisplaySettings();
    }
}
