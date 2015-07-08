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
package org.dashbuilder.displayer.client.widgets.group;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.group.GroupFunction;
import org.gwtbootstrap3.client.ui.TextBox;

@Dependent
public class ColumnDetailsEditor extends Composite {

    public interface Listener {
        void columnChanged(ColumnDetailsEditor editor);
    }

    interface Binder extends UiBinder<Widget, ColumnDetailsEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    GroupFunction column = null;
    DataSetMetadata metadata = null;

    @UiField
    Label columnIdLabel;

    @UiField
    TextBox columnIdTextBox;

    public ColumnDetailsEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(DataSetMetadata metadata, GroupFunction groupFunction, Listener listener) {

        this.column = groupFunction;
        this.listener = listener;
        this.metadata = metadata;

        if (StringUtils.isBlank(column.getColumnId())) columnIdTextBox.setText(column.getSourceId());
        else columnIdTextBox.setText(column.getColumnId());
    }

    public String getNewColumnId() {
        return columnIdTextBox.getValue();
    }

    // UI events

    @UiHandler(value = "columnIdTextBox")
    public void onColumnNameChanged(ChangeEvent changeEvent) {
        String text = columnIdTextBox.getValue();
        if (!StringUtils.isBlank(text)) {
            listener.columnChanged(this);
        }
    }
}
