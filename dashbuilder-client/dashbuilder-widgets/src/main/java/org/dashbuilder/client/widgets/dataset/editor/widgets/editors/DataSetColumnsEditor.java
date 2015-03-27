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
package org.dashbuilder.client.widgets.dataset.editor.widgets.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.datacolumn.DataColumnBasicEditor;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.impl.DataColumnImpl;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.*;

/**
 * <p>This is a widget for editing data set's columns.</p>
 * 
 * <p>NOTE that this widget is NOT a GWT editor component for Data Set class.</p>
 *  
 */
@Dependent
public class DataSetColumnsEditor extends AbstractEditor {

    interface DataSetColumnsEditorBinder extends UiBinder<Widget, DataSetColumnsEditor> {}
    private static DataSetColumnsEditorBinder uiBinder = GWT.create(DataSetColumnsEditorBinder.class);

    @UiField
    FlowPanel columnsPanel;

    @UiField
    FlowPanel columnsListPanel;
    
    private boolean isEditMode;

    public DataSetColumnsEditor() {
        // Initialize the widget.
        initWidget(uiBinder.createAndBindUi(this));
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    private final Map<DataColumn, DataColumnBasicEditor> columnEditors = new LinkedHashMap<DataColumn, DataColumnBasicEditor>();
    
    public void build(final DataSet dataSet, final DataSetDefEditWorkflow workflow) {
        clear();

        if (dataSet != null && workflow != null) {

            List<DataColumn> columns = dataSet.getColumns();
            if (columns != null) {
                for (DataColumn column : columns) {
                    DataColumnImpl columnImpl = (DataColumnImpl) column;
                    DataColumnBasicEditor columnEditor = new DataColumnBasicEditor();
                    workflow.edit(columnEditor, columnImpl);
                    addColumnEditor(column, columnEditor);
                }
            }
        }
        
    }
    
    private void addColumnEditor(final DataColumn column, final DataColumnBasicEditor editor) {
        columnEditors.put(column, editor);
        columnsPanel.add(editor);
    }

    @Override
    public Iterable<ConstraintViolation<?>> getViolations() {
        Set<ConstraintViolation<?>> violations = new LinkedHashSet<ConstraintViolation<?>>();
        if (!columnEditors.isEmpty()) {
            for (DataColumnBasicEditor editor : columnEditors.values()) {
                Iterable<ConstraintViolation<?>> editorViolations = editor.getViolations();
                if (editorViolations != null) {
                    for (ConstraintViolation<?> violation : editorViolations) {
                        violations.add(violation);
                    }
                }
            }
        }
        
        return violations;
    }

    @Override
    public void setViolations(Iterable<ConstraintViolation<?>> violations) {
        super.setViolations(violations);

        if (!columnEditors.isEmpty()) {
            for (DataColumnBasicEditor editor : columnEditors.values()) {
                editor.setViolations(violations);
            }
        }
    }

    private void clear() {
        columnEditors.clear();
        columnsPanel.clear();
    }
}
