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

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class DataSetLookupEditorView extends Composite
        implements DataSetLookupEditor.View {

    interface Binder extends UiBinder<Widget, DataSetLookupEditorView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    public DataSetLookupEditorView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    DataSetLookupEditor presenter;

    @UiField
    Label label;

    @Override
    public void init(DataSetLookupEditor presenter) {
        this.presenter = presenter;
    }

    @Override
    public void notFound() {
        label.setText("NOT FOUND");
    }

    @Override
    public void error(Exception e) {
        label.setText("ERROR");
    }
}
