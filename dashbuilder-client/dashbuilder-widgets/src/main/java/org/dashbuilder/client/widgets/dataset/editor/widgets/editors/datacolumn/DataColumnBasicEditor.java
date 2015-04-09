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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors.datacolumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractEditor;
import org.dashbuilder.common.client.validation.editors.ValueBoxEditorDecorator;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * <p>This is the view implementation widget for editing data a given set column's name and type.</p>
 * 
 */
@Dependent
public class DataColumnBasicEditor extends AbstractEditor implements org.dashbuilder.dataset.client.validation.editors.DataColumnEditor {

    private static final int ICONS_SIZE = 16;
    
    interface DataColumnBasicEditorBinder extends UiBinder<Widget, DataColumnBasicEditor> {}
    private static DataColumnBasicEditorBinder uiBinder = GWT.create(DataColumnBasicEditorBinder.class);

    private String editorId;
    
    @UiField
    FlowPanel columnPanel;

    @UiField
    ValueBoxEditorDecorator<String> id;

    @UiField
    DataColumnTypeEditor columnType;
    
    private boolean isEditMode;

    public DataColumnBasicEditor() {
        // Initialize the widget.
        initWidget(uiBinder.createAndBindUi(this));

        setEditMode(true);
        
        columnType.setSize(ICONS_SIZE, ICONS_SIZE);
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
         columnType.setEditMode(isEditMode);
    }

    public void setEditorId(String editorId) {
        this.editorId = editorId;
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        consumeErrors(errors);
    }

    protected void consumeErrors(List<EditorError> errors) {
        for (EditorError error : errors) {
            if (error.getEditor().equals(this)) {
                error.setConsumed(true);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        
        try {
            DataColumnBasicEditor e  = (DataColumnBasicEditor) obj;
            return (this.editorId.equals(e.editorId));
        } catch (ClassCastException e) {
            return false;
        }
    }
}
