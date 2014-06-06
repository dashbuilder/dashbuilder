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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.client.displayer.AbstractDataViewer;
import org.dashbuilder.client.displayer.DataViewer;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerColumn;

public abstract class GoogleViewer<T extends DataDisplayer> extends AbstractDataViewer<T> {

    protected boolean drawn = false;
    protected boolean ready = false;

    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    public GoogleViewer() {
        initWidget(panel);
    }

    /**
     * Invoked by the GoogleRenderer when the chart is ready for display.
     */
    public void ready() {
        ready = true;
        draw();
    }

    /**
     * Draw the displayer by getting first the underlying data set.
     * Ensure the displayer is also ready for display, which means the Google Visualization API has been loaded.
     */
    public void draw() {
        if (!drawn && ready) {
            drawn = true;

            if (dataDisplayer == null) {
                displayMessage("ERROR: DataDisplayer property not set");
            }
            else if (dataSetHandler == null) {
                displayMessage("ERROR: DataSetHandler property not set");
            }
            else {
                try {
                    displayMessage("Initializing '" + dataDisplayer.getTitle() + "'...");
                    dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet result) {
                            dataSet = result;
                            Widget w = createChart();
                            panel.clear();
                            panel.add(w);
                        }
                    });
                } catch (Exception e) {
                    displayMessage("ERROR: " + e.getMessage());
                }
            }
        }
    }

    /**
     * By default, it behaves exactly as draw(). A concrete viewer implementation could decide to provide a more
     * appealing implementation by providing some kind of chart animation.
     */
    public void redraw() {
        if (!drawn) {
            throw new IllegalStateException("draw() must be invoked first!");
        }
        drawn = false;
        draw();
    }

    /**
     * Create the Google Visualization widget this viewer uses for display.
     */
    public abstract Widget createChart();

    /**
     * Get the Google Visualization package this viewer requires.
     */
    public abstract String getPackage();

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage(String msg) {
        panel.clear();
        panel.add(label);
        label.setText(msg);
    }

    // Events received from other viewers

    public void onIntervalsSelected(DataViewer viewer, String columnId, Collection<String> intervalNames) {
        dataSetHandler.selectIntervals(columnId, intervalNames);
        redraw();
    }

    public void onColumnSorted(DataViewer viewer, String columnId, SortOrder order) {
    }

    public void onColumnFiltered(DataViewer viewer, String columnId, Collection<Comparable> allowedValues) {
        dataSetHandler.filterDataSet(columnId, allowedValues);
        redraw();
    }

    public void onColumnFiltered(DataViewer viewer, String columnId, Comparable lowValue, Comparable highValue) {
        dataSetHandler.filterDataSet(columnId, lowValue, highValue);
        redraw();
    }

    // Google DataTable manipulation methods

    protected DataTable googleTable = null;
    protected NumberFormat numberFormat = NumberFormat.getFormat("#0.00");

    public DataTable createTable() {
        List<DataDisplayerColumn> displayerColumns = dataDisplayer.getColumnList();
        if (displayerColumns.isEmpty()) {
            return googleTable = createTableFromDataSet();
        }
        return googleTable = createTableFromDisplayer();
    }

    public DataTable createTableFromDataSet() {
        DataTable gTable = DataTable.create();
        gTable.addRows(dataSet.getRowCount());

        List<DataColumn> columns = dataSet.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            DataColumn dataColumn = columns.get(i);
            List columnValues = dataColumn.getValues();
            ColumnType columnType = dataColumn.getColumnType();
            gTable.addColumn(getColumnType(dataColumn), dataColumn.getId(), dataColumn.getId());
            for (int j = 0; j < columnValues.size(); j++) {
                Object value = columnValues.get(j);
                setTableValue(gTable, columnType, value, j, i);
            }
        }
        return gTable;
    }

    public DataTable createTableFromDisplayer() {
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
            gTable.addColumn(getColumnType(dataColumn), displayerColumn.getDisplayName(), dataColumn.getId());
            for (int j = 0; j < columnValues.size(); j++) {
                Object value = columnValues.get(j);
                setTableValue(gTable, columnType, value, j, i);
            }
        }
        return gTable;
    }

    public String getValueString(int row, int column) {
        return googleTable.getValueString(row, column);
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
