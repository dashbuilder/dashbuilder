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
package org.dashbuilder.displayer.client.widgets.filter;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.resources.i18n.MonthConstants;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.date.TimeFrame;
import org.dashbuilder.dataset.date.TimeInstant;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.gwtbootstrap3.client.ui.ListBox;

@Dependent
public class TimeFrameEditor extends Composite {

    interface Listener {
        void valueChanged(TimeFrame tf);
    }

    interface Binder extends UiBinder<Widget, TimeFrameEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    TimeFrame timeFrame = null;

    @UiField
    TimeInstantEditor fromEditor;

    @UiField
    TimeInstantEditor toEditor;

    @UiField
    Label firstMonthLabel;

    @UiField
    ListBox firstMonthList;

    public TimeFrameEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(final TimeFrame frame, final Listener listener) {
        this.listener = listener;
        this.timeFrame = frame;
        if (timeFrame == null) {
            this.timeFrame = TimeFrame.parse("begin[year] till end[year]");
        }

        initFirstMonthListBox();
        changeFirstMonthVisibility();

        TimeInstant instantFrom = timeFrame.getFrom();
        fromEditor.init(instantFrom.cloneInstance(), true, new TimeInstantEditor.Listener() {
            public void valueChanged(TimeInstant timeInstant) {
                TimeFrameEditor.this.timeFrame.setFrom(timeInstant);
                TimeFrameEditor.this.timeFrame.setFrom(timeInstant);
                timeInstant.setFirstMonthOfYear(getFirstMonthOfYear());
                changeFirstMonthVisibility();
                listener.valueChanged(timeFrame);
            }
        });

        TimeInstant instantTo = timeFrame.getTo();
        toEditor.init(instantTo.cloneInstance(), true, new TimeInstantEditor.Listener() {
            public void valueChanged(TimeInstant timeInstant) {
                TimeFrameEditor.this.timeFrame.setTo(timeInstant);
                timeInstant.setFirstMonthOfYear(getFirstMonthOfYear());
                changeFirstMonthVisibility();
                listener.valueChanged(timeFrame);
            }
        });
    }

    protected void changeFirstMonthVisibility() {
        firstMonthLabel.setVisible(false);
        firstMonthList.setVisible(false);

        if (isFirstMonthVisible()) {
            firstMonthLabel.setVisible(true);
            firstMonthList.setVisible(true);
        }
    }

    protected boolean isFirstMonthVisible() {

        TimeInstant instantFrom = timeFrame.getFrom();
        TimeInstant.TimeMode modeFrom = instantFrom.getTimeMode();
        if (modeFrom != null && !modeFrom.equals(TimeInstant.TimeMode.NOW)) {
            DateIntervalType intervalType = instantFrom.getIntervalType();
            if (intervalType != null && intervalType.getIndex() > DateIntervalType.MONTH.getIndex()) {
                return true;
            }
        }
        TimeInstant instantTo = timeFrame.getTo();
        TimeInstant.TimeMode modeTo = instantTo.getTimeMode();
        if (modeTo != null && !modeTo.equals(TimeInstant.TimeMode.NOW)) {
            DateIntervalType intervalType = instantTo.getIntervalType();
            if (intervalType != null && intervalType.getIndex() > DateIntervalType.MONTH.getIndex()) {
                return true;
            }
        }
        return false;
    }

    protected Month getFirstMonthOfYear() {

        TimeInstant instantFrom = timeFrame.getFrom();
        TimeInstant.TimeMode modeFrom = instantFrom.getTimeMode();
        if (modeFrom != null && !modeFrom.equals(TimeInstant.TimeMode.NOW)) {
            DateIntervalType intervalType = instantFrom.getIntervalType();
            if (intervalType != null && intervalType.getIndex() > DateIntervalType.MONTH.getIndex()) {
                return instantFrom.getFirstMonthOfYear();
            }
        }
        TimeInstant instantTo = timeFrame.getTo();
        TimeInstant.TimeMode modeTo = instantTo.getTimeMode();
        if (modeTo != null && !modeTo.equals(TimeInstant.TimeMode.NOW)) {
            DateIntervalType intervalType = instantTo.getIntervalType();
            if (intervalType != null && intervalType.getIndex() > DateIntervalType.MONTH.getIndex()) {
                return instantTo.getFirstMonthOfYear();
            }
        }
        return null;
    }

    protected void setFirstMonthOfYear(Month month) {

        TimeInstant instantFrom = timeFrame.getFrom();
        TimeInstant.TimeMode modeFrom = instantFrom.getTimeMode();
        if (modeFrom != null && !modeFrom.equals(TimeInstant.TimeMode.NOW)) {
            DateIntervalType intervalType = instantFrom.getIntervalType();
            if (intervalType != null && intervalType.getIndex() > DateIntervalType.MONTH.getIndex()) {
                instantFrom.setFirstMonthOfYear(month);
            }
        }
        TimeInstant instantTo = timeFrame.getTo();
        TimeInstant.TimeMode modeTo = instantTo.getTimeMode();
        if (modeTo != null && !modeTo.equals(TimeInstant.TimeMode.NOW)) {
            DateIntervalType intervalType = instantTo.getIntervalType();
            if (intervalType != null && intervalType.getIndex() > DateIntervalType.MONTH.getIndex()) {
                instantTo.setFirstMonthOfYear(month);
            }
        }
    }

    protected void initFirstMonthListBox() {
        firstMonthList.clear();
        Month current = getFirstMonthOfYear();
        Month[] entries = Month.values();
        for (int i = 0; i < entries.length; i++) {
            Month entry = entries[i];
            firstMonthList.addItem(MonthConstants.INSTANCE.getString(entry.name()));
            if (current != null && current.equals(entry)) {
                firstMonthList.setSelectedIndex(i);
            }
        }
    }

    @UiHandler(value = "firstMonthList")
    public void onFirstMonthSelected(ChangeEvent changeEvent) {
        int selectedIdx = firstMonthList.getSelectedIndex();
        Month month = Month.getByIndex(selectedIdx+1);
        setFirstMonthOfYear(month);
        listener.valueChanged(timeFrame);
    }
}
