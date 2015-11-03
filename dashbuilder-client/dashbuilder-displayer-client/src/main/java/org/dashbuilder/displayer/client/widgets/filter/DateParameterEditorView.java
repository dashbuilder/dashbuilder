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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.Command;

@Dependent
public class DateParameterEditorView extends Composite implements DateParameterEditor.View {

    interface Binder extends UiBinder<Widget, DateParameterEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    DateParameterEditor presenter;

    @UiField
    DateTimePicker input;

    public DateParameterEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void init(final DateParameterEditor presenter) {
        this.presenter = presenter;
        input.addValueChangeHandler(new ValueChangeHandler<Date>() {
            public void onValueChange(ValueChangeEvent<Date> event) {
                presenter.valueChanged(event.getValue());
            }
        });
    }

    @Override
    public Date getCurrentValue() {
        return input.getValue();
    }

    @Override
    public void setCurrentValue(Date value) {
        input.setValue(value);
    }

    public DateTimePicker getInput() {
        return input;
    }
}
