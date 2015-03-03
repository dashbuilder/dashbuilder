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
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerType;

@Dependent
public class DisplayerTypeSelector extends Composite {

    public interface Listener {
        void displayerTypeChanged(DisplayerType type);
    }

    interface ViewBinder extends
            UiBinder<Widget, DisplayerTypeSelector> {

    }
    private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

    Listener listener = null;
    DisplayerType selectedType = DisplayerType.BARCHART;
    List<DisplayerTab> tabList = new ArrayList<DisplayerTab>();

    @UiField(provided = true)
    TabPanel optionsPanel;

    public DisplayerTypeSelector() {
        tabList.add(new DisplayerTab("Bar", DisplayerType.BARCHART));
        tabList.add(new DisplayerTab("Pie", DisplayerType.PIECHART));
        tabList.add(new DisplayerTab("Line", DisplayerType.LINECHART));
        tabList.add(new DisplayerTab("Area", DisplayerType.AREACHART));
        tabList.add(new DisplayerTab("Bubble", DisplayerType.BUBBLECHART));
        tabList.add(new DisplayerTab("Meter", DisplayerType.METERCHART));
        tabList.add(new DisplayerTab("Map", DisplayerType.MAP));
        tabList.add(new DisplayerTab("Table", DisplayerType.TABLE));

        optionsPanel = new TabPanel(Bootstrap.Tabs.LEFT);
        for (DisplayerTab tab : tabList) optionsPanel.add(tab);

        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(Listener listener) {
        this.listener = listener;
        draw();
    }


    protected void draw() {
        optionsPanel.clear();

        for (int i = 0; i < tabList.size(); i++) {
            DisplayerTab tab = tabList.get(i);
            tab.setActive(false);
            optionsPanel.add(tab);

            if (tab.type.equals(selectedType)) {
                tab.setActive(true);
                optionsPanel.selectTab(i);
            }
        }
    }

    public void select(DisplayerType type) {
        selectedType = type;
        draw();
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
                        listener.displayerTypeChanged(type);
                    }
                }
            });
        }
    }
}
