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
import java.util.ArrayList;
import javax.enterprise.context.Dependent;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.resources.Bootstrap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.resources.i18n.DisplayerTypeLiterals;

@Dependent
public class DisplayerTypeSelector extends Composite implements DisplayerSubtypeSelector.SubTypeChangeListener {

    public interface Listener {
        void displayerTypeChanged(DisplayerType type, DisplayerSubType subtype);
    }

    interface ViewBinder extends UiBinder<Widget, DisplayerTypeSelector> {}
    private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

    Listener listener = null;
    DisplayerType selectedType = DisplayerType.BARCHART;
    DisplayerSubType selectedSubType = null;
    List<DisplayerTab> tabList = new ArrayList<DisplayerTab>();

    @UiField(provided = true)
    TabPanel displayerTypePanel;

    @UiField
    VerticalPanel displayerSubtypePanel;

    private DisplayerSubtypeSelector subtypeSelector;

    public DisplayerTypeSelector() {
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_bar(), DisplayerType.BARCHART));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_pie(), DisplayerType.PIECHART));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_line(), DisplayerType.LINECHART));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_area(), DisplayerType.AREACHART));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_bubble(), DisplayerType.BUBBLECHART));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_meter(), DisplayerType.METERCHART));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_metric(), DisplayerType.METRIC));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_map(), DisplayerType.MAP));
        tabList.add(new DisplayerTab(DisplayerTypeLiterals.INSTANCE.displayer_type_selector_tab_table(), DisplayerType.TABLE));

        displayerTypePanel = new TabPanel(Bootstrap.Tabs.LEFT);
        displayerTypePanel.getElement().setId("dispTypes"); //for selenium
        for (DisplayerTab tab : tabList) displayerTypePanel.add(tab);

        initWidget(uiBinder.createAndBindUi(this));

        subtypeSelector = new DisplayerSubtypeSelector(this);
        displayerSubtypePanel.add(subtypeSelector);
        displayerSubtypePanel.getElement().setId("dispSubtypes"); //for selenium
    }

    public void init(Listener listener) {
        this.listener = listener;
        draw();
    }


    protected void draw() {
        displayerTypePanel.clear();

        for (int i = 0; i < tabList.size(); i++) {
            DisplayerTab tab = tabList.get(i);
            tab.setActive(false);
            displayerTypePanel.add(tab);

            if (tab.type.equals(selectedType)) {
                tab.setActive(true);
                displayerTypePanel.selectTab(i);
            }
        }
    }

    public void select(String renderer, final DisplayerType type, final DisplayerSubType subtype) {
        selectedType = type;
        selectedSubType = subtype;
        subtypeSelector.select(renderer, type, subtype);
        draw();
    }

    @Override
    public void displayerSubtypeChanged(DisplayerSubType displayerSubType) {
        if (displayerSubType != selectedSubType) {
            selectedSubType = displayerSubType;
            listener.displayerTypeChanged(selectedType, displayerSubType);
        }
    }

    private class DisplayerTab extends Tab {
        
        String name;
        DisplayerType type;

        public DisplayerTab(String name, final DisplayerType type) {
            super();
            super.setHeading(name);

            this.name = name;
            this.type = type;
            
            super.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    event.stopPropagation();
                    boolean change = !selectedType.equals(type);
                    selectedType = type;
                    if (change && listener != null) {
                        listener.displayerTypeChanged(type, null);
                    }
                }
            });
        }
    }
}
