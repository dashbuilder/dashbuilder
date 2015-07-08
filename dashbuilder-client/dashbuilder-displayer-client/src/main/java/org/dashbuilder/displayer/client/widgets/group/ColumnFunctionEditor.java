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

import java.util.List;
import java.util.ArrayList;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.resources.i18n.AggregateFunctionTypeConstants;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.GroupFunction;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.constants.IconType;

@Dependent
public class ColumnFunctionEditor extends Composite implements ColumnDetailsEditor.Listener {

    public interface Listener {
        void columnChanged(ColumnFunctionEditor editor);
        void columnDeleted(ColumnFunctionEditor editor);
    }

    interface Binder extends UiBinder<Widget, ColumnFunctionEditor> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    Listener listener = null;
    GroupFunction column = null;
    ColumnType targetType = null;
    DataSetMetadata metadata = null;

    @UiField
    ListBox columnListBox;

    @UiField
    ListBox functionListBox;

    @UiField
    Icon columnDeleteIcon;

    @UiField
    Icon columnExpandIcon;

    @UiField
    Panel columnDetailsPanel;

    @UiField
    ColumnDetailsEditor columnDetailsEditor;

    public ColumnFunctionEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        columnExpandIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                expandOrCollapse();
            }
        }, ClickEvent.getType());
        columnDeleteIcon.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                delete();
            }
        }, ClickEvent.getType());
    }

    public void init(DataSetMetadata metadata, final GroupFunction groupFunction,
            ColumnType targetType,
            String columnTitle,
            boolean functionsEnabled,
            boolean canDelete,
            final Listener listener) {

        this.column = groupFunction;
        this.targetType = targetType;
        this.listener = listener;
        this.metadata = metadata;

        columnExpandIcon.setType(IconType.ARROW_DOWN);
        columnDeleteIcon.setVisible(canDelete);
        columnListBox.setTitle(columnTitle);
        initColumnListBox();

        if (functionsEnabled && (targetType == null || isColumnNumeric())) {
            columnListBox.setWidth("120px");
            functionListBox.setVisible(true);
            initFunctionListBox();
        } else {
            columnListBox.setWidth("200px");
            functionListBox.setVisible(false);
        }
    }

    public void expand() {
        columnExpandIcon.setType(IconType.ARROW_UP);
        columnDetailsPanel.setVisible(true);
        columnDetailsEditor.init(metadata, column, this);
    }

    public void collapse() {
        columnDetailsPanel.setVisible(false);
        columnExpandIcon.setType(IconType.ARROW_DOWN);
    }

    public void expandOrCollapse() {
        if (columnDetailsPanel.isVisible()) {
            collapse();
        } else {
            expand();
        }
    }

    public void delete() {
        listener.columnDeleted(this);
    }

    public String getSourceId() {
        return columnListBox.getValue(columnListBox.getSelectedIndex());
    }

    public String getColumnId() {
        return columnDetailsEditor.getNewColumnId();
    }

    public AggregateFunctionType getFunction() {
        int idx = functionListBox.getSelectedIndex();
        if (!isColumnNumeric()) idx--;

        if (idx < 0) return null;
        return getSupportedFunctions().get(idx);
    }

    // UI events

    @UiHandler(value = "columnListBox")
    public void onColumnSelected(ChangeEvent changeEvent) {
        listener.columnChanged(this);
    }

    @UiHandler(value = "functionListBox")
    public void onFunctionSelected(ChangeEvent changeEvent) {
        listener.columnChanged(this);
    }

    // ColumnDetailsEditor callback

    public void columnChanged(ColumnDetailsEditor editor) {
        listener.columnChanged(this);
    }

    // Internals

    protected boolean isColumnNumeric() {
        return targetType != null && targetType.equals(ColumnType.NUMBER);
    }

    protected void initColumnListBox() {
        columnListBox.clear();

        for (int i=0; i<metadata.getNumberOfColumns(); i++) {
            String columnId = metadata.getColumnId(i);
            ColumnType columnType = metadata.getColumnType(i);

            // Only add columns that match the target type.
            // When the target is not specified or is numeric then all the columns are eligible
            if (targetType == null || columnType == null || isColumnNumeric() || targetType.equals(columnType)) {
                columnListBox.addItem(columnId, columnId);
                if (columnId != null && columnId.equals(column.getSourceId())) {
                    columnListBox.setSelectedIndex(i);
                }
            }
        }
    }

    protected void initFunctionListBox() {
        functionListBox.clear();
        if (!isColumnNumeric()) functionListBox.addItem("---");

        AggregateFunctionType selected = column.getFunction();
        for (AggregateFunctionType functionType : getSupportedFunctions()) {
            String functionName = AggregateFunctionTypeConstants.INSTANCE.getString(functionType.name());
            functionListBox.addItem(functionName);
            if (selected != null && functionType.equals(selected)) {
                setSelectedValue(functionListBox, functionName);
            }
        }
    }

    private void setSelectedValue( final ListBox functionListBox,
                                   final String functionName ) {
        for ( int i = 0; i < functionListBox.getItemCount(); i++ ) {
            if (functionListBox.getValue( i ).equals( functionName )){
                functionListBox.setSelectedIndex( i );
                break;
            }
        }
    }

    protected List<AggregateFunctionType> getSupportedFunctions() {
        ColumnType targetType = metadata.getColumnType(column.getSourceId());
        List<AggregateFunctionType> result = new ArrayList<AggregateFunctionType>();
        for (AggregateFunctionType function : AggregateFunctionType.values()) {
            if (function.supportType(targetType)) {
                result.add(function);
            }
        }
        return result;
    }
}
