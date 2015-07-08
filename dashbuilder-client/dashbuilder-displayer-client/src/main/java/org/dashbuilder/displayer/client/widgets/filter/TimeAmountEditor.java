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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.client.resources.i18n.DateIntervalTypeConstants;
import org.dashbuilder.dataset.date.TimeAmount;
import org.dashbuilder.dataset.date.TimeFrame;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupAddon;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.uberfire.ext.widgets.common.client.common.NumericLongTextBox;

@Dependent
public class TimeAmountEditor extends Composite {

    interface Listener {
        void valueChanged(TimeAmount timeAmount);
    }

    interface Binder extends UiBinder<Widget, TimeAmountEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    TimeAmount timeAmount = null;

    @UiField
    NumericLongTextBox input;

    @UiField
    InputGroupAddon minusIcon;

    @UiField
    InputGroupAddon plusIcon;

    @UiField
    ListBox typeList;

    static List<DateIntervalType> ALLOWED_SIZES = Arrays.asList(
            DateIntervalType.SECOND,
            DateIntervalType.MINUTE,
            DateIntervalType.HOUR,
            DateIntervalType.DAY,
            DateIntervalType.WEEK,
            DateIntervalType.MONTH,
            DateIntervalType.QUARTER,
            DateIntervalType.YEAR,
            DateIntervalType.CENTURY);

    public TimeAmountEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        plusIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                increaseQuantity();
            }
        }, ClickEvent.getType());

        minusIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                decreaseQuantity();
            }
        }, ClickEvent.getType());
    }

    public void init(final TimeAmount amount, final Listener listener) {
        this.listener = listener;
        this.timeAmount = amount;
        initListBox();

        if (timeAmount == null) input.setValue("0");
        else input.setValue(Long.toString(timeAmount.getQuantity()));

        input.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                if (StringUtils.isBlank(event.getValue())) changeQuantity(0);
                else changeQuantity(Long.parseLong(event.getValue()));
            }
        });
    }

    protected void initListBox() {
        typeList.clear();
        for (int i=0; i< ALLOWED_SIZES.size(); i++) {
            DateIntervalType type = ALLOWED_SIZES.get(i);
            typeList.addItem(DateIntervalTypeConstants.INSTANCE.getString(type.name()));
            if (timeAmount != null && timeAmount.getType().equals(type)) {
                typeList.setSelectedIndex(i);
            }
        }
    }

    protected void increaseQuantity() {
        changeQuantity(getQuantity()+1);
        input.setValue(Long.toString(getQuantity()));
    }

    protected void decreaseQuantity() {
        changeQuantity(getQuantity()-1);
        input.setValue(Long.toString(getQuantity()));
    }

    protected long getQuantity() {
        if (timeAmount == null) return 0;
        return timeAmount.getQuantity();
    }

    protected void changeQuantity(long q) {
        if (timeAmount == null) {
            timeAmount = new TimeAmount();
            int selectedIdx = typeList.getSelectedIndex();
            DateIntervalType type = ALLOWED_SIZES.get(selectedIdx);
            timeAmount.setType(type);
        }
        timeAmount.setQuantity(q);
        listener.valueChanged(timeAmount);
    }

    // UI events

    @UiHandler(value = "typeList")
    public void onFilterSelected(ChangeEvent changeEvent) {
        int selectedIdx = typeList.getSelectedIndex();
        DateIntervalType type = ALLOWED_SIZES.get(selectedIdx);
        if (timeAmount != null) {
            timeAmount.setType(type);
            listener.valueChanged(timeAmount);
        }
    }
}
