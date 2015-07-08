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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;

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
import org.gwtbootstrap3.client.ui.CheckBox;

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

    private List<DataColumnDef> originalColumns;
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
            originalColumns = new LinkedList<DataColumnDef>(columns);

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
                columnEditor.addValueChangeHandler(columnTypeChangeHandler);

                columnEditor.setEditorId(column.getId());
                columnEditor.setOriginalType(_column.getColumnType());
                columnEditors.put(column, columnEditor);

                // Link the column editor with workflow driver.
                workflow.edit(columnEditor, column);
                columnEditor.setEditMode(isEditMode() && enabled);

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
        columns.remove(column);
        fireColumnsChanged();
    }

    private void addColumn(final DataColumnDef column, final DataSetDefEditWorkflow workflow) {
        final DataColumnBasicEditor columnEditor = getEditor(column);
        assert columnEditor != null;
        columnEditor.setEditMode(isEditMode());
        columns.add(column);
        fireColumnsChanged();
    }

    private DataColumnBasicEditor getEditor(final DataColumnDef columnDef) {
        if (!columnEditors.isEmpty()) {
            for (Map.Entry<DataColumnDef, DataColumnBasicEditor> entry : columnEditors.entrySet()) {
                final DataColumnDef _c = entry.getKey();
                if (_c != null && _c.equals(columnDef)) return entry.getValue();
            }
        }
        return null;
    }

    private void fireColumnsChanged() {
        // Return the columns in same order as the original ones.
        if (columns != null) {
            final List<DataColumnDef> result = new ArrayList<DataColumnDef>();
            for (final DataColumnDef column : originalColumns) {
                final int _ci = columns.indexOf(column);
                if (_ci > -1) {
                    final DataColumnDef _c = columns.get(_ci);
                    result.add(_c);
                }
            }
            this.fireEvent(new ColumnsChangedEvent(new LinkedList<DataColumnDef>(result)));
        }
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
        originalColumns = null;
    }
}
