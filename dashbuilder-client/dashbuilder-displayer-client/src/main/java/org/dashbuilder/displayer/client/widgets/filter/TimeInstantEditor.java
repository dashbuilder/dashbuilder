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

import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.resources.i18n.DateIntervalTypeConstants;
import org.dashbuilder.dataset.client.resources.i18n.TimeModeConstants;
import org.dashbuilder.dataset.date.TimeAmount;
import org.dashbuilder.dataset.date.TimeInstant;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.gwtbootstrap3.client.ui.ListBox;

@Dependent
public class TimeInstantEditor extends Composite {

    interface Listener {
        void valueChanged(TimeInstant timeInstant);
    }

    interface Binder extends UiBinder<Widget, TimeInstantEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    TimeInstant timeInstant = null;
    boolean timeModeRequired = true;

    @UiField
    ListBox timeModeList;

    @UiField
    ListBox intervalTypeList;

    @UiField
    TimeAmountEditor timeAmountEditor;

    static List<DateIntervalType> ALLOWED_TYPES = Arrays.asList(
            DateIntervalType.MINUTE,
            DateIntervalType.HOUR,
            DateIntervalType.DAY,
            DateIntervalType.MONTH,
            DateIntervalType.QUARTER,
            DateIntervalType.YEAR,
            DateIntervalType.CENTURY,
            DateIntervalType.MILLENIUM);

    public TimeInstantEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(final TimeInstant instant, boolean timeModeRequired, final Listener listener) {
        this.timeModeRequired = timeModeRequired;
        this.listener = listener;
        this.timeInstant = instant != null ? instant : new TimeInstant();
        refreshUI();
    }

    public void refreshUI() {

        initTimeModeListBox();
        initIntervalTypeListBox();

        intervalTypeList.setVisible(false);
        TimeInstant.TimeMode timeMode = timeInstant.getTimeMode();
        if (timeMode != null && !timeMode.equals(TimeInstant.TimeMode.NOW)) {
            intervalTypeList.setVisible(true);
        }

        TimeAmount timeAmount = timeInstant.getTimeAmount();
        timeAmountEditor.init(timeAmount, new TimeAmountEditor.Listener() {
            public void valueChanged(TimeAmount timeAmount) {
                onTimeAmountChanged(timeAmount);
            }
        });
    }

    protected void initTimeModeListBox() {
        timeModeList.clear();
        if (!timeModeRequired) {
            timeModeList.addItem(CommonConstants.INSTANCE.common_dropdown_select());
        }
        TimeInstant.TimeMode current = timeInstant.getTimeMode();
        TimeInstant.TimeMode[] modes = TimeInstant.TimeMode.values();
        for (int i=0; i<modes.length ; i++) {
            TimeInstant.TimeMode mode = modes[i];
            timeModeList.addItem(TimeModeConstants.INSTANCE.getString(mode.name()));
            if (current != null && current.equals(mode)) {
                timeModeList.setSelectedIndex(timeModeRequired ? i : i+1);
            }
        }
    }

    protected void initIntervalTypeListBox() {
        intervalTypeList.clear();
        DateIntervalType current = timeInstant.getIntervalType();
        for (int i=0; i< ALLOWED_TYPES.size(); i++) {
            DateIntervalType type = ALLOWED_TYPES.get(i);
            intervalTypeList.addItem(DateIntervalTypeConstants.INSTANCE.getString(type.name()));
            if (current != null && current.equals(type)) {
                intervalTypeList.setSelectedIndex(i);
            }
        }
    }

    // UI events

    @UiHandler(value = "timeModeList")
    public void onTimeModeSelected(ChangeEvent changeEvent) {
        int selectedIdx = timeModeList.getSelectedIndex();

        TimeInstant.TimeMode mode = null;
        if (timeModeRequired) mode = TimeInstant.TimeMode.getByIndex(selectedIdx);
        else mode = selectedIdx == 0 ? null : TimeInstant.TimeMode.getByIndex(selectedIdx-1);

        timeInstant.setTimeMode(mode);
        TimeAmount timeAmount = timeInstant.getTimeAmount();
        if (timeAmount != null) timeAmount.setQuantity(0);

        listener.valueChanged(timeInstant);
        refreshUI();
    }

    @UiHandler(value = "intervalTypeList")
    public void onIntervalTypeSelected(ChangeEvent changeEvent) {
        int selectedIdx = intervalTypeList.getSelectedIndex();
        DateIntervalType intervalType = ALLOWED_TYPES.get(selectedIdx);
        timeInstant.setIntervalType(intervalType);
        listener.valueChanged(timeInstant);
        refreshUI();
    }

    public void onTimeAmountChanged(TimeAmount timeAmount) {
        timeInstant.setTimeAmount(timeAmount);
        listener.valueChanged(timeInstant);
    }
}
