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

import java.util.Date;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;

@Dependent
public class DateParameterEditor extends Composite {

    interface Listener {
        void valueChanged(Date d);
    }

    interface Binder extends UiBinder<Widget, DateParameterEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;

    @UiField
    DateTimePicker input;

    public DateParameterEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(Date value, final Listener listener) {
        this.listener = listener;
        if (value != null) input.setValue(value);
        input.addValueChangeHandler(new ValueChangeHandler<Date>() {
            public void onValueChange(ValueChangeEvent<Date> event) {
                listener.valueChanged(event.getValue());
            }
        });
        input.setAutoClose(true); // hide datepicker when time selected
    }

    public DateTimePicker getInput() {
        return input;
    }
}
