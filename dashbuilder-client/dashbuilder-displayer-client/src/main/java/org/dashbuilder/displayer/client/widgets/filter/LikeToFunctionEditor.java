/**
 * Copyright (C) 2015 JBoss Inc
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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class LikeToFunctionEditor extends Composite {

    interface Listener {
        void valueChanged(String pattern, boolean caseSensitive);
    }

    interface Binder extends UiBinder<Widget, LikeToFunctionEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;

    @UiField
    TextBox searchPattern;

    @UiField
    CheckBox caseSensitive;

    public LikeToFunctionEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(String value, boolean enabled, final Listener listener) {
        this.listener = listener;
        if (value != null) {
            searchPattern.setValue(value);
        }
        caseSensitive.setValue(enabled);
    }

    @UiHandler("searchPattern")
    public void onPatternChanged(ChangeEvent event) {
        listener.valueChanged(searchPattern.getValue(), caseSensitive.getValue());
    }

    @UiHandler("caseSensitive")
    public void onCaseChanged(ClickEvent event) {
        listener.valueChanged(searchPattern.getValue(), caseSensitive.getValue());
    }
}
