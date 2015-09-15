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
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.DataSetDefEditWorkflow;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.datacolumn.DataColumnBasicEditor;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.gwtbootstrap3.client.ui.CheckBox;

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

    @UiField
    VerticalPanel columnsContainer;

    private List<DataColumnDef> originalColumns;
    private final List<DataColumnDef> columns = new LinkedList<DataColumnDef>();
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

    public void build(final List<DataColumnDef> columns, final DataSetDef dataSetDef, final DataSet dataSet, final DataSetDefEditWorkflow workflow) {
        clear();

        if (columns != null && workflow != null) {
            originalColumns = new LinkedList<DataColumnDef>(columns);
            
            // Remove workflow column status.
            workflow.removeAllColumns();

            for (final DataColumnDef _column : columns) {
                final DataColumnDef column = _column.clone();
                final DataColumn dataSetColumn = hasColumn(column, dataSet.getColumns());
                final  boolean isActive = dataSetColumn != null;
                if (isActive) {
                    column.setColumnType(dataSetColumn.getColumnType());
                }
                // Create the editor for each column.
                DataColumnBasicEditor columnEditor = new DataColumnBasicEditor();
                columnEditor.addValueChangeHandler(new ValueChangeHandler<ColumnType>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<ColumnType> event) {
                        updateColumnType(column, event.getValue());
                    }
                });

                columnEditor.setEditorId(column.getId());
                columnEditor.setOriginalType(_column.getColumnType());

                // Restrictions for selecting / unselecting the column.
                boolean isEnabled = true;
                String statusTitle = "";
                
                if (isActive) {
                    // If is the only one column selected. cannot remove it, as data set must have at least one column 
                    final boolean isSingleColumn = dataSet.getColumns().size() <= 1;
                    if (isSingleColumn) {
                        isEnabled = false;
                        statusTitle = DataSetEditorConstants.INSTANCE.dataSetMustHaveAtLeastOneColumn();
                    } else {
                        // If the column is used in datasetdef filter, cannot remove it. 
                        final boolean isUsedInFilter = isColumnUsedInFilter(column.getId(), dataSetDef.getDataSetFilter());
                        if (isUsedInFilter) {
                            isEnabled = false;
                            statusTitle = DataSetEditorConstants.INSTANCE.columnIsUsedInFilter();
                        }
                    }
                }

                // Link the column editor with workflow driver.
                workflow.edit(columnEditor, column);
                columnEditor.setEditMode(isEditMode() && isEnabled && isActive);

                // Create the UI panel for the column.
                Panel columnPanel = createColumn(column, columnEditor, isActive, isEnabled, statusTitle);
                if (isActive) {
                    this.columns.add(column);
                }
                columnsContainer.add(columnPanel);
            }
        }

    }

    private boolean isColumnUsedInFilter(final String columnId, final DataSetFilter filter) {
        if (filter != null && columnId != null) {
            final List<ColumnFilter> columnFilters = filter.getColumnFilterList();
            if (columnFilters != null) {
                for (final ColumnFilter columnFilter : columnFilters) {
                    String filterColumnId = columnFilter.getColumnId();
                    if (columnId.equalsIgnoreCase(filterColumnId)) return true;
                }
            }
        }

        return false;
    }

    private DataColumn hasColumn(final DataColumnDef def, final List<DataColumn> columns) {
        if (columns == null || def == null) return null;
        for (final DataColumn c : columns) {
            if (c.getId().equals(def.getId())) return c;
        }
        return null;
    }

    private Panel createColumn(final DataColumnDef column, final DataColumnBasicEditor editor, final boolean isActive, final boolean isEnabled, final String title) {
        final HorizontalPanel row = new HorizontalPanel();

        // Data column statuc (Checkbox).
        final CheckBox columnStatus = new CheckBox();
        columnStatus.setValue(isActive);
        columnStatus.setEnabled(isEnabled);
        columnStatus.setTitle(title != null ? title : "");
        columnStatus.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        // Vertical middle alignment into columns (work-around).
        columnStatus.getElement().getStyle().setTop(-7, Style.Unit.PX);
        
        // Enable / disable change handlers.
        columnStatus.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean isChecked = event.getValue();
                
                if (isChecked) addColumn(column);
                else removeColumn(editor, column);
            }
        });
        row.add(columnStatus);
        
        // Data column editor component (name & column type).
        row.add(editor.asWidget());

        return row;
    }

    private void removeColumn(final DataColumnBasicEditor editor, final DataColumnDef column) {
        columns.remove(column);
        fireColumnsChanged();
    }

    private void addColumn(final DataColumnDef column) {
        columns.add(column);
        fireColumnsChanged();
    }
    
    private void updateColumnType(final DataColumnDef column, final ColumnType type) {
        column.setColumnType(type);
        fireColumnsChanged();
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
    
    public void clear() {
        super.clear();
        columnsContainer.clear();
        columns.clear();
        originalColumns = null;
    }
}
