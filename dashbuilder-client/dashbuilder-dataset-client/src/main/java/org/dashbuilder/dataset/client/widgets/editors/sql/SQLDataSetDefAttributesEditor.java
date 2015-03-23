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
package org.dashbuilder.dataset.client.widgets.editors.sql;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;
import org.dashbuilder.dataset.client.validation.editors.SQLDataSetDefEditor;
import org.dashbuilder.dataset.client.widgets.editors.AbstractDataSetDefEditor;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * <p>This is the view implementation for Data Set Editor widget for editing sql provider specific attributes.</p>
 */
@Dependent
public class SQLDataSetDefAttributesEditor extends AbstractDataSetDefEditor implements SQLDataSetDefEditor {
    
    interface SQLDataSetDefAttributesEditorBinder extends UiBinder<Widget, SQLDataSetDefAttributesEditor> {}
    private static SQLDataSetDefAttributesEditorBinder uiBinder = GWT.create(SQLDataSetDefAttributesEditorBinder.class);

    @UiField
    HorizontalPanel sqlAttributesPanel;

    @UiField
    @Path("dataSource")
    ValueBoxEditorDecorator<String> dataSource;

    @UiField
    @Path("dbSchema")
    ValueBoxEditorDecorator<String> dbSchema;

    @UiField
    @Path("dbTable")
    ValueBoxEditorDecorator<String> dbTable;

    private boolean isEditMode;

    public SQLDataSetDefAttributesEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        consumeErrors(errors);
    }
}
