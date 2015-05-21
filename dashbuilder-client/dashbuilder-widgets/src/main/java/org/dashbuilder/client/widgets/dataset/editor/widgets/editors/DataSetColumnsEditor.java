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
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.datacolumn.DataColumnBasicEditor;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.impl.DataColumnImpl;

import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import java.util.*;

/**
 * <p>This is a widget for editing data set's columns.</p>
 * 
 * <p>NOTE that this widget is NOT a GWT editor component for Data Set class.</p>
 *
 * @since 0.3.0 
 *  
 */
@Dependent
public class DataSetColumnsEditor extends AbstractEditor {

    interface DataSetColumnsEditorBinder extends UiBinder<Widget, DataSetColumnsEditor> {}
    private static DataSetColumnsEditorBinder uiBinder = GWT.create(DataSetColumnsEditorBinder.class);

    interface DataSetColumnsEditorStyle extends CssResource {
        String mainPanel();
        String left();
        String clear();
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

    private final Map<DataColumnDef, DataColumnBasicEditor> columnEditors = new LinkedHashMap<DataColumnDef, DataColumnBasicEditor>();
    private final List<DataColumnDef> columns = new LinkedList<DataColumnDef>();

    public interface ColumnsChangedEventHandler extends EventHandler
    {
        void onColumnsChanged(ColumnsChangedEvent event);
    }
    
    public static class ColumnsChangedEvent extends GwtEvent<ColumnsChangedEventHandler> {

        public static Type<ColumnsChangedEventHandler> TYPE = new Type<ColumnsChangedEventHandler>();

        private List<DataColumnDef> columns;
        
        public ColumnsChangedEvent(List<DataColumnDef> columns) {
            super();
            this.columns = columns;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ColumnsChangedEventHandler handler) {
            handler.onColumnsChanged(this);
        }

        public List<DataColumnDef> getColumns() {
            return columns;
        }
    }
    
    public HandlerRegistration addColumnsChangeHandler(final ColumnsChangedEventHandler handler) {
        return addHandler(handler, ColumnsChangedEvent.TYPE);
    }

    private final ValueChangeHandler<ColumnType> columnTypeChangeHandler = new ValueChangeHandler<ColumnType>() {
        @Override
        public void onValueChange(ValueChangeEvent<ColumnType> event) {
            fireColumnsChanged();
        }
    };
    
    public void build(final List<DataColumnDef> columns, final DataSet dataSet, final DataSetDefEditWorkflow workflow) {
        clear();

        if (columns != null && workflow != null) {
            
            // Remove workflow column status.
            workflow.removeAllColumns();
            
            for (final DataColumnDef _column : columns) {
                final DataColumnDef column = _column.clone();
                final DataColumn dataSetColumn = hasColumn(column, dataSet.getColumns());
                final  boolean enabled = dataSetColumn != null;
                if (enabled) {
                    column.setColumnType(dataSetColumn.getColumnType());
                }
                // Create the editor for each column.
                DataColumnBasicEditor columnEditor = new DataColumnBasicEditor();
                columnEditor.setEditMode(enabled);
                columnEditor.addValueChangeHandler(columnTypeChangeHandler);

                columnEditor.setEditorId(column.getId());
                columnEditor.setOriginalType(_column.getColumnType());
                columnEditors.put(column, columnEditor);

                // Link the column editor with workflow driver.
                workflow.edit(columnEditor, column);
                
                // Create the UI panel for the column.
                final boolean canRemove = dataSet != null && dataSet.getColumns().size() > 1;
                Panel columnPanel = createColumn(column, columnEditor, workflow, enabled, canRemove);
                if (enabled) this.columns.add(column);
                columnsPanel.add(columnPanel);
                final FlowPanel separator = new FlowPanel();
                separator.addStyleName(style.clear());
                columnsPanel.add(separator);
            }
        }
        
    }
    
    private DataColumn hasColumn(final DataColumnDef def, final List<DataColumn> columns) {
        if (columns == null || def == null) return null;
        for (final DataColumn c : columns) {
            if (c.getId().equals(def.getId())) return c;
        }
        return null;
    }
    
    private Panel createColumn(final DataColumnDef column, final DataColumnBasicEditor editor, final DataSetDefEditWorkflow workflow, final boolean enabled, final boolean canRemove) {
        final FlowPanel columnPanel = new FlowPanel();
        
        // Checkbox.
        final CheckBox columnStatus = new CheckBox();
        columnStatus.setValue(enabled);
        columnStatus.addStyleName(style.left());
        columnStatus.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean isChecked = event.getValue();
                
                if (isChecked) addColumn(column,workflow);
                else if (canRemove) removeColumn(editor, column, workflow);
            }
        });
        columnPanel.add(columnStatus);

        // Name editor.
        editor.addStyleName(style.left());
        columnPanel.add(editor);

        return columnPanel;
    }

    private void removeColumn(final DataColumnBasicEditor editor, final DataColumnDef column, final DataSetDefEditWorkflow workflow) {
        editor.setEditMode(false);
        workflow.remove(editor, column);
        columnEditors.remove(column);
        columns.remove(column);
        fireColumnsChanged();
    }

    private void addColumn(final DataColumnDef column, final DataSetDefEditWorkflow workflow) {
        DataColumnBasicEditor columnEditor = new DataColumnBasicEditor();
        columnEditor.setEditMode(true);
        columnEditor.setEditorId(column.getId());
        workflow.edit(columnEditor, column);
        columnEditors.put(column, columnEditor);
        columns.add(column);
        fireColumnsChanged();
    }
    
    private void fireColumnsChanged() {
        this.fireEvent(new ColumnsChangedEvent(new LinkedList<DataColumnDef>(columns)));
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

    public void clear() {
        super.clear();
        columnEditors.clear();
        columnsPanel.clear();
        columns.clear();
    }
}
