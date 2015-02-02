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

import com.github.gwtbootstrap.client.ui.ListBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.client.resources.i18n.DateIntervalTypeConstants;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.TimeFrame;
import org.uberfire.ext.widgets.common.client.common.NumericDoubleTextBox;
import org.uberfire.ext.widgets.common.client.common.NumericLongTextBox;

@Dependent
public class TimeFrameParameterEditor extends Composite {

    interface Listener {
        void valueChanged(TimeFrame tf);
    }

    interface Binder extends UiBinder<Widget, TimeFrameParameterEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    TimeFrame timeFrame = null;

    @UiField
    NumericLongTextBox input;

    @UiField
    ListBox typeList;

    static List<DateIntervalType> ALLOWED_TYPES = Arrays.asList(
            DateIntervalType.SECOND,
            DateIntervalType.MINUTE,
            DateIntervalType.HOUR,
            DateIntervalType.DAY,
            DateIntervalType.WEEK,
            DateIntervalType.MONTH,
            DateIntervalType.YEAR,
            DateIntervalType.CENTURY);

    public TimeFrameParameterEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(final TimeFrame timeFrame, final Listener listener) {
        this.listener = listener;
        this.timeFrame = timeFrame;
        initListBox();
        if (timeFrame != null) {
            input.setValue(Long.toString(timeFrame.getAmount()));
        }

        input.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                timeFrame.setAmount(Long.parseLong(event.getValue()));
                listener.valueChanged(timeFrame);
            }
        });
    }

    protected void initListBox() {
        typeList.clear();
        for (int i=0; i<ALLOWED_TYPES.size(); i++) {
            DateIntervalType type = ALLOWED_TYPES.get(i);
            typeList.addItem(DateIntervalTypeConstants.INSTANCE.getString(type.name()));
            if (timeFrame != null && timeFrame.getType().equals(type)) {
                typeList.setSelectedIndex(i);
            }
        }
    }

    // UI events

    @UiHandler(value = "typeList")
    public void onFilterSelected(ChangeEvent changeEvent) {
        int selectedIdx = typeList.getSelectedIndex();
        DateIntervalType type = ALLOWED_TYPES.get(selectedIdx);
        if (timeFrame != null) {
            timeFrame.setType(type);
            listener.valueChanged(timeFrame);
        }
    }
}
