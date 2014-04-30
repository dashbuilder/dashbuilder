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
package org.dashbuilder.client.google;

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.model.displayer.DataDisplayerColumn;

public abstract class GoogleChartViewer extends DataDisplayerViewer {

    @Inject protected GoogleRenderer googleRenderer;
    protected boolean isDrawn = false;
    protected boolean isApiReady = false;
    protected FlowPanel panel = new FlowPanel();
    protected NumberFormat numberFormat = NumberFormat.getFormat("#0.00");

    @PostConstruct
    public void init() {
        initWidget(panel);
        googleRenderer.registerChart(this);
    }

    public void onApiReady() {
        isApiReady = true;
        if (dataSet != null && dataDisplayer != null) {
            draw();
        }
    }

    public void draw() {
        if (!isDrawn && isApiReady) {

            if (dataSet == null) throw new IllegalStateException("DataSet property not set");
            if (dataDisplayer== null) throw new IllegalStateException("DataDisplayer property not set");

            Widget w = createChart();
            panel.clear();
            panel.add(w);
            isDrawn = true;
        }
    }

    public abstract Widget createChart();
    public abstract String getPackage();

    public AbstractDataTable createTable() {
        List<DataDisplayerColumn> displayerColumns = dataDisplayer.getColumnList();
        if (displayerColumns.isEmpty()) {
            return createTableFromDataSet();
        }
        return createTableFromDisplayer();
    }

    public AbstractDataTable createTableFromDataSet() {
        DataTable gTable = DataTable.create();
        gTable.addRows(dataSet.getRowCount());

        List<DataColumn> columns = dataSet.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            DataColumn dataColumn = columns.get(i);
            List columnValues = dataColumn.getValues();
            ColumnType columnType = dataColumn.getColumnType();
            gTable.addColumn(getColumnType(dataColumn), dataColumn.getId());
            for (int j = 0; j < columnValues.size(); j++) {
                Object value = columnValues.get(j);
                setTableValue(gTable, columnType, value, j, i);
            }
        }
        return gTable;
    }

    public AbstractDataTable createTableFromDisplayer() {

        DataTable gTable = DataTable.create();
        gTable.addRows(dataSet.getRowCount());
        int columnIndex = 0;

        List<DataDisplayerColumn> displayerColumns = dataDisplayer.getColumnList();
        for (int i = 0; i < displayerColumns.size(); i++) {
            DataDisplayerColumn displayerColumn = displayerColumns.get(i);
            DataColumn dataColumn = null;
            if (displayerColumn.getColumnId() != null) dataColumn = dataSet.getColumnById(displayerColumn.getColumnId());
            else dataColumn = dataSet.getColumnByIndex(columnIndex++);
            if (dataColumn == null) {
                GWT.log("Displayer column not found in the data set: " + displayerColumn.getDisplayName());
            }

            ColumnType columnType = dataColumn.getColumnType();
            List columnValues = dataColumn.getValues();
            gTable.addColumn(getColumnType(dataColumn), displayerColumn.getDisplayName());
            for (int j = 0; j < columnValues.size(); j++) {
                Object value = columnValues.get(j);
                setTableValue(gTable, columnType, value, j, i);
            }
        }
        return gTable;
    }

    public void setTableValue(DataTable gTable, ColumnType type, Object value, int row, int column) {
        //GWT.log("Row="+j+" Col="+i+" Val="+value);

        if (ColumnType.DATE.equals(type)) {
            gTable.setValue(row, column, (Date) value);
        }
        else if (ColumnType.NUMBER.equals(type)) {
            String valueStr = numberFormat.format((Number) value);
            gTable.setValue(row, column, Double.parseDouble(valueStr));
        }
        else {
            gTable.setValue(row, column, value.toString());
        }
    }

    public AbstractDataTable.ColumnType getColumnType(DataColumn dataColumn) {
        ColumnType type = dataColumn.getColumnType();
        if (ColumnType.LABEL.equals(type)) return AbstractDataTable.ColumnType.STRING;
        if (ColumnType.NUMBER.equals(type)) return AbstractDataTable.ColumnType.NUMBER;
        if (ColumnType.DATE.equals(type)) return AbstractDataTable.ColumnType.DATETIME;
        return AbstractDataTable.ColumnType.STRING;
    }
}
