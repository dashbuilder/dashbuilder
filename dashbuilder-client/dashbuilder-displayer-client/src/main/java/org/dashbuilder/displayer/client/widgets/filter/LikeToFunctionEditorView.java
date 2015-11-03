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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;

@Dependent
public class LikeToFunctionEditorView extends Composite implements LikeToFunctionEditor.View {

    interface Binder extends UiBinder<Widget, LikeToFunctionEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    LikeToFunctionEditor presenter;

    @UiField
    TextBox searchPatternTextBox;

    @UiField
    CheckBox caseSensitiveCheckbox;

    public LikeToFunctionEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void init(LikeToFunctionEditor presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setPattern(String pattern) {
        searchPatternTextBox.setText(pattern);
    }

    @Override
    public void setCaseSensitive(boolean caseSensitive) {
        caseSensitiveCheckbox.setValue(caseSensitive);
    }

    @Override
    public String getPattern() {
        return searchPatternTextBox.getText();
    }

    @Override
    public boolean isCaseSensitive() {
        return caseSensitiveCheckbox.getValue();
    }

    @UiHandler("searchPatternTextBox")
    public void onPatternChanged(ChangeEvent event) {
        presenter.viewUpdated();
    }

    @UiHandler("caseSensitiveCheckbox")
    public void onCaseChanged(ClickEvent event) {
        presenter.viewUpdated();
    }
}
