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

import com.github.gwtbootstrap.client.ui.Accordion;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.validation.editors.DataSetDefEditor;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.impl.DataColumnImpl;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.*;

/**
 * <p>This is a widget for editing data set's columns.</p>
 * 
 * <p>NOTE that this widget is not a GWT editor component.</p>
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

    private final List<DataColumnEditor> columnEditors = new LinkedList<DataColumnEditor>();
    
    public void build(final DataSet dataSet, final DataSetDefEditWorkflow workflow) {
        clear();

        if (dataSet != null && workflow != null) {

            List<DataColumn> columns = dataSet.getColumns();
            if (columns != null) {
                for (DataColumn column : columns) {
                    DataColumnImpl columnImpl = (DataColumnImpl) column;
                    DataColumnEditor columnEditor = new DataColumnEditor();
                    workflow.edit(columnEditor, columnImpl);
                    addColumnEditor(columnEditor);
                    addColumnEditor(columnEditor);
                }
            }
        }
        
    }
    
    private void addColumnEditor(final DataColumnEditor editor) {
        columnEditors.add(editor);
        columnsPanel.add(editor);
    }

    @Override
    public Iterable<ConstraintViolation<?>> getViolations() {
        Set<ConstraintViolation<?>> violations = new LinkedHashSet<ConstraintViolation<?>>();
        if (!columnEditors.isEmpty()) {
            for (DataColumnEditor editor : columnEditors) {
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

    private void clear() {
        columnEditors.clear();
    }
}
