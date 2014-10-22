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

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerType;

@Dependent
public class DisplayerTypeSelector extends Composite {

    public interface Listener {
        void changeDisplayerType(DisplayerType type);
    }

    interface ViewBinder extends
            UiBinder<Widget, DisplayerTypeSelector> {

    }
    private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

    public DisplayerTypeSelector() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    Listener listener = null;
    DisplayerType selectedType = DisplayerType.BARCHART;

    @UiField
    TabPanel optionsPanel;

    @UiField
    Tab optionBar;

    public void init(Listener listener) {
        this.listener = listener;
    }

    public void select(DisplayerType type) {
        boolean change = !selectedType.equals(type);
        selectedType = type;
        if (change && listener != null) {
            listener.changeDisplayerType(type);
        }
    }

    @UiHandler(value = "optionBar")
    public void onBarSelected(ClickEvent clickEvent) {
        select(DisplayerType.BARCHART);
    }

    @UiHandler(value = "optionPie")
    public void onPieSelected(ClickEvent clickEvent) {
        select(DisplayerType.PIECHART);
    }

    @UiHandler(value = "optionLine")
    public void onLineSelected(ClickEvent clickEvent) {
        select(DisplayerType.LINECHART);
    }

    @UiHandler(value = "optionArea")
    public void onAreaSelected(ClickEvent clickEvent) {
        select(DisplayerType.AREACHART);
    }

    @UiHandler(value = "optionBubble")
    public void onBubbleSelected(ClickEvent clickEvent) {
        select(DisplayerType.BUBBLECHART);
    }

    @UiHandler(value = "optionMeter")
    public void onMeterSelected(ClickEvent clickEvent) {
        select(DisplayerType.METERCHART);
    }

    @UiHandler(value = "optionMap")
    public void onMapSelected(ClickEvent clickEvent) {
        select(DisplayerType.MAP);
    }

    @UiHandler(value = "optionTable")
    public void onTableSelected(ClickEvent clickEvent) {
        select(DisplayerType.TABLE);
    }
}
