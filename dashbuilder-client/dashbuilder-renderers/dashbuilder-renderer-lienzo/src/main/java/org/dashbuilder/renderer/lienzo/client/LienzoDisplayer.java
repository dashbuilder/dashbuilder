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
package org.dashbuilder.renderer.lienzo.client;

import com.ait.lienzo.charts.client.core.AbstractChart;
import com.ait.lienzo.charts.client.core.model.DataTable;
import com.ait.lienzo.charts.client.core.model.DataTableColumn;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.displayer.client.AbstractDisplayer;

import java.util.Date;
import java.util.List;
import java.util.Set;

public abstract class LienzoDisplayer extends AbstractDisplayer {

    public static final int PANEL_MARGIN = 50;
    public static final String PIXEL = "px";
    public static final int ANIMATION_DURATION = 500;

    protected DataTable lienzoTable = null;
    protected DataColumn categoriesColumn = null;

    /**
     * Call back method invoked just after the data set lookup is executed.
     */
    @Override
    protected void afterDataSetLookup(DataSet dataSet) {
        
        // Ensure data model instance is created.
        lienzoTable = createTable();

        // Update the categories column.
        categoriesColumn = getCategoriesColumn();
    }

    protected DataTable createTable() {

        DataTable lienzoTable = new DataTable();
        List<DataColumn> columns = dataSet.getColumns();
        if (columns != null && !columns.isEmpty()) {
            for (int i = 0; i < columns.size(); i++) {
                DataColumn dataColumn = columns.get(i);
                List columnValues = dataColumn.getValues();
                ColumnType columnType = dataColumn.getColumnType();
                String columnId = dataColumn.getId();

                lienzoTable.addColumn(columnId, getColumnType(dataColumn));
                for (int j = 0; j < columnValues.size(); j++) {
                    Object value = columnValues.get(j);
                    if (ColumnType.LABEL.equals(columnType)) value = super.formatValue(value, dataColumn);
                    addTableValue(lienzoTable, columnType, value, columnId);
                }
            }
        }

        return lienzoTable;
    }

    public void addTableValue(DataTable lTable, ColumnType type, Object value, String columnId) {
        if (ColumnType.DATE.equals(type)) {
            if (value == null) lTable.addValue(columnId, new Date());
            else lTable.addValue(columnId, (Date) value);
        }
        else if (ColumnType.NUMBER.equals(type)) {
            if (value == null) {
                lTable.addValue(columnId, 0d);
            } else {
                lTable.addValue(columnId, Double.parseDouble(value.toString()));
            }
        }
        else {
            lTable.addValue(columnId, value.toString());
        }
    }

    public DataTableColumn.DataTableColumnType getColumnType(DataColumn dataColumn) {
        ColumnType type = dataColumn.getColumnType();
        if (ColumnType.LABEL.equals(type)) return DataTableColumn.DataTableColumnType.STRING;
        if (ColumnType.TEXT.equals(type)) return DataTableColumn.DataTableColumnType.STRING;
        if (ColumnType.NUMBER.equals(type)) return DataTableColumn.DataTableColumnType.NUMBER;
        if (ColumnType.DATE.equals(type)) return DataTableColumn.DataTableColumnType.DATE;
        return DataTableColumn.DataTableColumnType.STRING;
    }
    
    protected DataColumn getCategoriesColumn() {
        List<DataColumn> columns = dataSet.getColumns();
        if (columns != null && !columns.isEmpty()) return columns.get(0);
        return null;
    }

    protected DataColumn[] getValuesColumns() {
        List<DataColumn> columns = dataSet.getColumns();
        if (columns != null && !columns.isEmpty()) {
            DataColumn[] result = new DataColumn[columns.size() - 1];
            if (columns != null && !columns.isEmpty()) {
                for (int i = 1; i < columns.size(); i++) {
                    DataColumn dataColumn = columns.get(i);
                    result[i - 1] = dataColumn;
                }
            }
            return result;
        }
        return null;
    }
    
    protected int getChartWidth() {
        return displayerSettings.getChartWidth();
    }
    
    protected int getChartHeight() {
        return  displayerSettings.getChartHeight();
    }

    protected int getWidth() {
        int width = displayerSettings.isResizable() ? displayerSettings.getChartMaxWidth() : displayerSettings.getChartWidth();
        int left = displayerSettings.getChartMarginLeft();
        int right = displayerSettings.getChartMarginRight();
        return displayerSettings.getChartWidth()+right+left;

    }

    protected int getHeight() {
        int height = displayerSettings.isResizable() ? displayerSettings.getChartMaxHeight() : displayerSettings.getChartHeight();
        int top = displayerSettings.getChartMarginTop();
        int bottom = displayerSettings.getChartMarginBottom();
        return displayerSettings.getChartHeight()+top+bottom;
    }

    protected Widget createNoDataMsgPanel() {
        return new org.gwtbootstrap3.client.ui.Label("NO DATA");
    }

    protected Widget createCurrentSelectionWidget() {
        if (!displayerSettings.isFilterEnabled()) return null;

        Set<String> columnFilters = filterColumns();
        if (columnFilters.isEmpty()) return null;

        HorizontalPanel panel = new HorizontalPanel();
        panel.getElement().setAttribute("cellpadding", "2");

        for (String columnId : columnFilters) {
            List<Interval> selectedValues = filterIntervals(columnId);
            DataColumn column = dataSet.getColumnById(columnId);
            for (Interval interval : selectedValues) {
                String formattedValue = formatInterval(interval, column);
                panel.add(new org.gwtbootstrap3.client.ui.Label(formattedValue));
            }
        }

        Anchor anchor = new Anchor( "Reset" );
        panel.add(anchor);
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                filterReset();
                updateVisualization();
            }
        });
        return panel;
    }
    
}
