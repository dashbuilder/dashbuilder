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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.TextBox;

@Dependent
public class TextParameterEditor extends Composite {

    interface Listener {
        void valueChanged(String s);
    }

    interface Binder extends UiBinder<Widget, TextParameterEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;

    @UiField
    TextBox input;

    public TextParameterEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(String value, final Listener listener) {
        this.listener = listener;
        if (value != null) input.setValue(value);
        input.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                listener.valueChanged(event.getValue());
            }
        });
    }
}
