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
package org.dashbuilder.displayer.client.widgets;

import javax.enterprise.context.Dependent;

import com.github.gwtbootstrap.client.ui.Row;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class DisplayerEditorError extends Composite {

    interface Binder extends UiBinder<Widget, DisplayerEditorError> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    HTML errorMessage;

    @UiField
    HTML errorCause;

    @UiField
    Row errorMessageRow;

    @UiField
    Row errorCauseRow;

    public DisplayerEditorError() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void show(final String message, final String cause) {
        errorMessage.setText(message != null ? message : "");
        errorMessageRow.setVisible(message != null);
        errorCause.setText(cause != null ? cause : "");
        errorCauseRow.setVisible(cause != null);
    }
}
