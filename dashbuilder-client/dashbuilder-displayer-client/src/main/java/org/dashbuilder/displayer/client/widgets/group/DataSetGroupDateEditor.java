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
package org.dashbuilder.displayer.client.widgets.group;

import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.resources.i18n.DateIntervalTypeConstants;
import org.dashbuilder.dataset.client.resources.i18n.DayOfWeekConstants;
import org.dashbuilder.dataset.client.resources.i18n.MonthConstants;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;

@Dependent
public class DataSetGroupDateEditor extends Composite {

    public interface Listener {
        void columnGroupChanged(ColumnGroup columnGroup);
    }

    interface Binder extends UiBinder<Widget, DataSetGroupDateEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    ColumnGroup columnGroup = null;

    @UiField
    CheckBox fixedStrategyCheckBox;

    @UiField
    ListBox intervalTypeListBox;

    @UiField
    VerticalPanel maxIntervalsGroup;

    @UiField
    VerticalPanel firstDayPanel;

    @UiField
    VerticalPanel firstMonthPanel;

    @UiField
    TextBox maxIntervalsTextBox;

    @UiField
    CheckBox emptyIntervalsCheckBox;

    @UiField
    ListBox firstDayListBox;

    @UiField
    ListBox firstMonthListBox;

    public DataSetGroupDateEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(ColumnGroup columnGroup, Listener listener) {
        this.columnGroup = columnGroup;
        this.listener = listener;
        if (columnGroup != null) {
            if (isFixedStrategy()) {
                gotoFixedMode();
            } else {
                gotoDynamicMode();
            }
        }
    }

    protected boolean isFixedStrategy() {
        return GroupStrategy.FIXED.equals(columnGroup.getStrategy());
    }

    protected void initIntervalTypeListBox() {
        intervalTypeListBox.clear();
        DateIntervalType current = DateIntervalType.getByName(columnGroup.getIntervalSize());
        List<DateIntervalType> entries = getListOfIntervalTypes();
        for (int i = 0; i < entries.size(); i++) {
            DateIntervalType entry = entries.get(i);
            intervalTypeListBox.addItem(DateIntervalTypeConstants.INSTANCE.getString(entry.name()));
            if (current != null && current.equals(entry)) {
                intervalTypeListBox.setSelectedIndex(i);
            }
        }
    }

    protected List<DateIntervalType> getListOfIntervalTypes() {
        if (isFixedStrategy()) return DateIntervalType.FIXED_INTERVALS_SUPPORTED;
        return Arrays.asList(DateIntervalType.values());
    }

    protected void initFirstDayListBox() {
        firstDayPanel.setVisible(true);
        firstDayListBox.clear();
        DayOfWeek current = columnGroup.getFirstDayOfWeek();
        DayOfWeek[] entries = DayOfWeek.values();
        for (int i = 0; i < entries.length; i++) {
            DayOfWeek entry = entries[i];
            firstDayListBox.addItem(DayOfWeekConstants.INSTANCE.getString(entry.name()));
            if (current != null && current.equals(entry)) {
                firstDayListBox.setSelectedIndex(i);
            }
        }
    }

    protected void initFirstMonthListBox() {
        firstMonthPanel.setVisible(true);
        firstMonthListBox.clear();
        Month current = columnGroup.getFirstMonthOfYear();
        Month[] entries = Month.values();
        for (int i = 0; i < entries.length; i++) {
            Month entry = entries[i];
            firstMonthListBox.addItem(MonthConstants.INSTANCE.getString(entry.name()));
            if (current != null && current.equals(entry)) {
                firstMonthListBox.setSelectedIndex(i);
            }
        }
    }

    protected void initMaxIntervalsTextBox() {
        maxIntervalsGroup.setVisible(true);
        maxIntervalsTextBox.setText(Integer.toString(columnGroup.getMaxIntervals()));
    }

    protected void initEmptyIntervalsCheckBox() {
        emptyIntervalsCheckBox.setValue(columnGroup.areEmptyIntervalsAllowed());
    }

    protected void resetCommon() {
        fixedStrategyCheckBox.setValue(isFixedStrategy());
        maxIntervalsGroup.setVisible(false);
        firstDayPanel.setVisible(false);
        firstMonthPanel.setVisible(false);

        initIntervalTypeListBox();
        initEmptyIntervalsCheckBox();
    }

    public void gotoDynamicMode() {
        resetCommon();
        initMaxIntervalsTextBox();
    }

    public void gotoFixedMode() {
        resetCommon();

        DateIntervalType current = DateIntervalType.getByName(columnGroup.getIntervalSize());
        if (DateIntervalType.DAY_OF_WEEK.equals(current)) {
            initFirstDayListBox();
        }
        else if (DateIntervalType.MONTH.equals(current)) {
            initFirstMonthListBox();
        }
    }

    @UiHandler(value = "fixedStrategyCheckBox")
    public void onFixedModeSelected(ClickEvent clickEvent) {
        columnGroup.setFirstMonthOfYear(null);
        columnGroup.setFirstDayOfWeek(null);

        if (fixedStrategyCheckBox.getValue()) {

            // Reset current interval type selected if not allowed.
            DateIntervalType intervalType = DateIntervalType.getByIndex(intervalTypeListBox.getSelectedIndex());
            if (!DateIntervalType.FIXED_INTERVALS_SUPPORTED.contains(intervalType)) {
                intervalTypeListBox.setSelectedIndex(DateIntervalType.MONTH.getIndex());
                columnGroup.setIntervalSize(DateIntervalType.MONTH.name());
            }
            columnGroup.setStrategy(GroupStrategy.FIXED);
            gotoFixedMode();
        } else {
            columnGroup.setStrategy(GroupStrategy.DYNAMIC);
            gotoDynamicMode();
        }
        if (listener != null) {
            listener.columnGroupChanged(columnGroup);
        }
    }

    @UiHandler(value = "intervalTypeListBox")
    public void onIntervalTypeSelected(ChangeEvent changeEvent) {
        DateIntervalType intervalType = DateIntervalType.getByIndex(intervalTypeListBox.getSelectedIndex());
        if (isFixedStrategy()) intervalType = DateIntervalType.FIXED_INTERVALS_SUPPORTED.get(intervalTypeListBox.getSelectedIndex());

        columnGroup.setIntervalSize(intervalType.name());
        columnGroup.setFirstMonthOfYear(null);
        columnGroup.setFirstDayOfWeek(null);

        firstMonthPanel.setVisible(false);
        firstDayPanel.setVisible(false);

        if (GroupStrategy.FIXED.equals(columnGroup.getStrategy())) {
            if (DateIntervalType.MONTH.equals(DateIntervalType.getByName(columnGroup.getIntervalSize()))) {
                firstMonthPanel.setVisible(true);
                initFirstMonthListBox();
            }
            else if (DateIntervalType.DAY_OF_WEEK.equals(DateIntervalType.getByName(columnGroup.getIntervalSize()))) {
                firstDayPanel.setVisible(true);
                initFirstDayListBox();
            }
        }
        if (listener != null) {
            listener.columnGroupChanged(columnGroup);
        }
    }

    @UiHandler(value = "emptyIntervalsCheckBox")
    public void onEmptyIntervalsChanged(ClickEvent clickEvent) {
        columnGroup.setEmptyIntervalsAllowed(emptyIntervalsCheckBox.getValue());
        if (listener != null) {
            listener.columnGroupChanged(columnGroup);
        }
    }

    @UiHandler(value = "maxIntervalsTextBox")
    public void onMaxIntervalsChanged(ChangeEvent changeEvent) {
        try {
            columnGroup.setMaxIntervals(Integer.parseInt(maxIntervalsTextBox.getValue()));
            if (listener != null) {
                listener.columnGroupChanged(columnGroup);
            }
        } catch (Exception e) {
            // Just ignore
        }
    }

    @UiHandler(value = "firstDayListBox")
    public void onFirstDaySelected(ChangeEvent changeEvent) {
        DayOfWeek dayOfWeek = DayOfWeek.getByIndex(firstDayListBox.getSelectedIndex()+1);
        columnGroup.setFirstDayOfWeek(dayOfWeek);
        if (listener != null) {
            listener.columnGroupChanged(columnGroup);
        }
    }

    @UiHandler(value = "firstMonthListBox")
    public void onFirstMonthSelected(ChangeEvent changeEvent) {
        Month month = Month.getByIndex(firstMonthListBox.getSelectedIndex()+1);
        columnGroup.setFirstMonthOfYear(month);
        if (listener != null) {
            listener.columnGroupChanged(columnGroup);
        }
    }
}
