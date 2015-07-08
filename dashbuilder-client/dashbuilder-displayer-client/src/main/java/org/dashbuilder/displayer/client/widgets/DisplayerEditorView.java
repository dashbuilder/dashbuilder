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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractDisplayerListener;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerListener;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabListItem;

@Dependent
public class DisplayerEditorView extends Composite
        implements DisplayerEditor.View {

    interface Binder extends UiBinder<Widget, DisplayerEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @Inject
    public DisplayerEditorView(DisplayerTypeSelector typeSelector,
            DataSetLookupEditor lookupEditor,
            DisplayerSettingsEditor settingsEditor) {

        this.typeSelector = typeSelector;
        this.lookupEditor = lookupEditor;
        this.settingsEditor = settingsEditor;

        initWidget(uiBinder.createAndBindUi(this));
        dataTablePanel.getElement().setAttribute("cellpadding", "5");
    }

    protected DisplayerEditor presenter;
    protected DisplayerSettings settings;
    protected DisplayerTypeSelector typeSelector;
    protected DataSetLookupEditor lookupEditor;
    protected DisplayerSettingsEditor settingsEditor;
    protected Displayer displayer;
    protected DisplayerError errorWidget = new DisplayerError();

    DisplayerListener displayerListener = new AbstractDisplayerListener() {
        public void onError(Displayer displayer, ClientRuntimeError error) {
            error(error);
        }
    };

    @UiField
    public Panel leftPanel;

    @UiField
    public Panel centerPanel;

    @UiField
    public TabListItem optionType;

    @UiField
    public TabListItem optionData;

    @UiField
    public TabListItem optionSettings;

    @UiField
    public Panel dataTablePanel;

    @UiField
    public CheckBox viewAsTable;

    @Override
    public void init(DisplayerSettings settings, DisplayerEditor presenter) {
        this.settings = settings;
        this.presenter = presenter;
        showDisplayer();
        gotoLastTab();
    }

    @Override
    public void disableTypeSelection() {
        optionType.setVisible( false );
    }

    public void gotoLastTab() {
        int lastTab = DisplayerEditorStatus.get().getSelectedTab(settings.getUUID());
        int selectedTab = optionType.isActive() ? 0 : optionData.isActive() ? 1 : optionSettings.isActive() ? 2 : -1;
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
        saveLastTab(0);

        typeSelector.init(presenter);
        typeSelector.select(settings.getRenderer(), settings.getType(), settings.getSubtype());
        leftPanel.clear();
        leftPanel.add(typeSelector);

        dataTablePanel.setVisible(false);
        optionData.setActive(false);
        optionSettings.setActive(false);
        optionType.setActive(true);
        showDisplayer();
    }

    @Override
    public void gotoDataSetConf() {
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
        optionSettings.setActive(false);
        optionType.setActive(false);
        optionData.setActive(true);
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
        saveLastTab(2);
        optionSettings.setActive(true);

        settingsEditor.init(settings, presenter);
        leftPanel.clear();
        leftPanel.add(settingsEditor);

        dataTablePanel.setVisible(false);
        optionType.setActive(false);
        optionData.setActive(false);
        optionSettings.setActive(true);
        showDisplayer();
    }

    @Override
    public void error(String error) {
        centerPanel.clear();
        centerPanel.add(errorWidget);
        errorWidget.show(error, null);

        GWT.log(error);
    }

    @Override
    public void error(ClientRuntimeError e) {
        centerPanel.clear();
        centerPanel.add(errorWidget);
        errorWidget.show(e.getMessage(), e.getCause());

        if (e.getThrowable() != null) GWT.log(e.getMessage(), e.getThrowable());
        else GWT.log(e.getMessage());
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
                displayer.addListener(displayerListener);
                displayer.setRefreshOn(false);
                centerPanel.clear();
                centerPanel.add(displayer);
                displayer.draw();
            } else {
                displayer = DisplayerLocator.get().lookupDisplayer(settings);
                displayer.addListener(displayerListener);
                displayer.setRefreshOn(false);
                centerPanel.clear();
                centerPanel.add(displayer);
                displayer.draw();
            }
        } catch (Exception e) {
            error(new ClientRuntimeError(e));
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
