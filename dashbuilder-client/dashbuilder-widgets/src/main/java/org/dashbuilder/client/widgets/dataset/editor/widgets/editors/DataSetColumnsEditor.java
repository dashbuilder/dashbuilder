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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
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

    interface DataSetColumnsEditorStyle extends CssResource {
        String mainPanel();
        String left();
    }

    @UiField
    DataSetColumnsEditorStyle style;
    
    @UiField
    FlowPanel columnsPanel;

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

    private DataSet dataSet = null;
    private final Map<DataColumn, DataColumnBasicEditor> columnEditors = new LinkedHashMap<DataColumn, DataColumnBasicEditor>();
    
    public void build(final DataSet dataSet, final DataSetDefEditWorkflow workflow) {
        clear();

        this.dataSet = dataSet;
        
        if (dataSet != null && workflow != null) {
            
            List<DataColumn> columns = dataSet.getColumns();
            if (columns != null) {
                for (DataColumn column : columns) {
                    DataColumnImpl columnImpl = (DataColumnImpl) column;
                    DataColumnBasicEditor columnEditor = new DataColumnBasicEditor();
                    workflow.edit(columnEditor, columnImpl);
                    columnEditors.put(column, columnEditor);
                    Panel columnPanel = createColumn(column, columnEditor);
                    columnsPanel.add(columnPanel);
                }
            }
        }
        
    }
    
    private Panel createColumn(final DataColumn column, final DataColumnBasicEditor editor) {
        final FlowPanel columnPanel = new FlowPanel();
        columnPanel.addStyleName(style.left());
        
        // Checkbox.
        final CheckBox columnStatus = new CheckBox();
        columnStatus.setValue(Boolean.TRUE);
        columnStatus.addStyleName(style.left());
        columnStatus.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean isChecked = event.getValue();
                
                /*
                TODO
                if (isChecked) addColumn(column, editor);
                else removeColumn(column, columnPanel);
                */
            }
        });
        columnPanel.add(columnStatus);

        // Name editor.
        editor.addStyleName(style.left());
        columnPanel.add(editor);

        return columnPanel;
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
        this.dataSet = null;
        columnEditors.clear();
        columnsPanel.clear();
    }
}
