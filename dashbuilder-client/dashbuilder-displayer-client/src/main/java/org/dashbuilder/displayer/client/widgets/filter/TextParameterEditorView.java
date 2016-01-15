/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;

@Dependent
public class TextParameterEditorView extends Composite implements TextParameterEditor.View {

    interface Binder extends UiBinder<Widget, TextParameterEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    TextParameterEditor presenter;

    @UiField
    FormGroup form;

    @UiField
    TextBox input;

    @UiField
    Icon hintIcon;

    public TextParameterEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void init(final TextParameterEditor presenter) {
        this.presenter = presenter;
        input.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                form.setValidationState(ValidationState.NONE);
                presenter.valueChanged();
            }
        });
    }

    @Override
    public void setMultipleHintEnabled(boolean enabled) {
        hintIcon.setVisible(enabled);
    }

    @Override
    public void clear() {
        input.clear();
        form.setValidationState(ValidationState.NONE);
    }

    @Override
    public String getValue() {
        return input.getValue();
    }

    @Override
    public void setValue(String value) {
        input.setValue(value);
    }

    @Override
    public void error() {
        form.setValidationState(ValidationState.ERROR);
    }
}
