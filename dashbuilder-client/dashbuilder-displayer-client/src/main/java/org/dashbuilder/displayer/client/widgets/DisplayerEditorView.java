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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.constants.VisibilityChange;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
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
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;

@Dependent
public class DisplayerEditorView extends Composite
        implements DisplayerEditor.View {

    interface Binder extends UiBinder<Widget, DisplayerEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    public DisplayerEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
        dataTablePanel.getElement().setAttribute("cellpadding", "5");
    }

    @Inject
    public DisplayerEditorView(DisplayerTypeSelector typeSelector,
            DataSetLookupEditor lookupEditor,
            DisplayerSettingsEditor settingsEditor) {
        this();
        this.typeSelector = typeSelector;
        this.lookupEditor = lookupEditor;
        this.settingsEditor = settingsEditor;
    }

    protected DisplayerEditor presenter;
    protected DisplayerSettings settings;
    protected DisplayerTypeSelector typeSelector;
    protected DataSetLookupEditor lookupEditor;
    protected DisplayerSettingsEditor settingsEditor;
    protected Displayer displayer;

    @UiField
    protected Panel leftPanel;

    @UiField
    protected Panel centerPanel;

    @UiField
    protected TabPanel optionsPanel;

    @UiField
    protected Tab optionType;

    @UiField
    protected Tab optionData;

    @UiField
    protected Tab optionSettings;

    @UiField
    protected Panel dataTablePanel;

    @UiField
    protected CheckBox viewAsTable;

    @Override
    public void init(DisplayerSettings settings, DisplayerEditor presenter) {
        this.settings = settings;
        this.presenter = presenter;
        showDisplayer();
        gotoLastTab();
    }

    @Override
    public void disableTypeSelection() {
        optionType.addStyle(VisibilityChange.HIDE);
    }

    public void gotoLastTab() {
        int selectedTab = optionsPanel.getSelectedTab();
        int lastTab = DisplayerEditorStatus.get().getSelectedTab(settings.getUUID());
        if (selectedTab < 0 || selectedTab != lastTab) {
            switch (lastTab) {
                case 2:
                    gotoDisplaySettings();
                    break;
                case 1:
                    gotoDataSetConf();
                    break;
                default:
                    gotoTypeSelection();
                    break;
            }
        }
    }

    private void saveLastTab(int tab) {
        DisplayerEditorStatus.get().saveSelectedTab(settings.getUUID(), tab);
    }

    @Override
    public void gotoTypeSelection() {
        optionsPanel.selectTab(0);
        saveLastTab(0);

        typeSelector.init(presenter);
        typeSelector.select(settings.getRenderer(), settings.getType(), settings.getSubtype());
        leftPanel.clear();
        leftPanel.add(typeSelector);

        dataTablePanel.setVisible(false);
        showDisplayer();
    }

    @Override
    public void gotoDataSetConf() {
        optionsPanel.selectTab(1);
        saveLastTab(1);

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

        if (DisplayerType.TABLE.equals(settings.getType())) {
            dataTablePanel.setVisible(false);
        } else {
            dataTablePanel.setVisible(true);
        }
        showDisplayer();
    }

    @Override
    public void showTypeChangedWarning(DisplayerSettings oldSettings, DisplayerSettings newSettings) {

        if (Window.confirm(CommonConstants.INSTANCE.displayer_editor_incompatible_settings())) {
            presenter.changeSettings(oldSettings, newSettings);
        } else {
            typeSelector.select(oldSettings.getRenderer(), oldSettings.getType(), oldSettings.getSubtype());
        }
    }

    @Override
    public void updateDataSetLookup(DataSetLookupConstraints constraints, DataSetMetadata metadata) {
        DataSetLookup dataSetLookup = settings.getDataSetLookup();
        lookupEditor.init(presenter, dataSetLookup, constraints, metadata);

        showDisplayer();
    }

    @Override
    public void gotoDisplaySettings() {
        optionsPanel.selectTab(2);
        saveLastTab(2);
        optionSettings.setActive(true);

        settingsEditor.init(settings, presenter);
        leftPanel.clear();
        leftPanel.add(settingsEditor);

        dataTablePanel.setVisible(false);
        showDisplayer();
    }

    @Override
    public void error(String msg, Exception e) {
        centerPanel.clear();
        centerPanel.add(new Label(msg));
        GWT.log(msg, e);
    }

    @Override
    public void close() {
        if (displayer != null) {
            displayer.close();
        }
    }

    public void showDisplayer() {
        if (displayer != null) {
            displayer.close();
        }
        try {
            if (dataTablePanel.isVisible() && viewAsTable.getValue()) {
                DisplayerSettings tableSettings = settings.cloneInstance();
                tableSettings.setTitleVisible(false);
                tableSettings.setType(DisplayerType.TABLE);
                tableSettings.setTablePageSize(8);
                tableSettings.setTableWidth(-1);
                displayer = DisplayerLocator.get().lookupDisplayer(tableSettings);
                displayer.setRefreshOn(false);
                centerPanel.clear();
                centerPanel.add(displayer);
                DisplayerHelper.draw(displayer);
            } else {
                displayer = DisplayerLocator.get().lookupDisplayer(settings);
                displayer.setRefreshOn(false);
                centerPanel.clear();
                centerPanel.add(displayer);
                DisplayerHelper.draw(displayer);
            }
        } catch (Exception e) {
            centerPanel.clear();
            centerPanel.add(new Label(e.getMessage()));
        }
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

    @UiHandler(value = "viewAsTable")
    public void onRawTableChecked(ClickEvent clickEvent) {
        showDisplayer();
    }
}
