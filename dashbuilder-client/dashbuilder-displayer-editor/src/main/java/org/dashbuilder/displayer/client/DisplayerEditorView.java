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
package org.dashbuilder.displayer.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.constants.VisibilityChange;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.widgets.DisplayerTypeSelector;

@Dependent
public class DisplayerEditorView extends Composite {

    interface DisplayerEditorViewBinder extends
            UiBinder<Widget, DisplayerEditorView> {

    }
    private static DisplayerEditorViewBinder uiBinder = GWT.create(DisplayerEditorViewBinder.class);

    public DisplayerEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    DisplayerEditorPresenter presenter;

    @Inject
    DisplayerTypeSelector typeSelector;

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

    public void init(DisplayerEditorPresenter presenter) {
        this.presenter = presenter;
        this.typeSelector.init(presenter);
    }

    public void showDisplayer() {
        Displayer displayer = DisplayerLocator.get().lookupDisplayer(presenter.getCurrentSettings());
        DisplayerHelper.draw(displayer);
        centerPanel.clear();
        centerPanel.add(displayer);
    }

    public void disableTypeSelection() {
        optionType.addStyle(VisibilityChange.HIDE);
    }

    public void gotoTypeSelection() {
        optionsPanel.selectTab(0);

        typeSelector.select(presenter.getCurrentSettings().getType());
        leftPanel.clear();
        leftPanel.add(typeSelector);

        showDisplayer();
    }

    public void gotoDataSetConf() {
        optionsPanel.selectTab(1);

        leftPanel.clear();
        showDisplayer();
    }

    public void gotoDisplaySettings() {
        optionsPanel.selectTab(2);
        optionSettings.setActive(true);

        leftPanel.clear();
        showDisplayer();
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
