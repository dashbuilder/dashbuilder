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
package org.dashbuilder.renderer.google.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.format.DateFormat;
import com.googlecode.gwt.charts.client.format.DateFormatOptions;
import com.googlecode.gwt.charts.client.format.NumberFormat;
import com.googlecode.gwt.charts.client.format.NumberFormatOptions;
import com.googlecode.gwt.charts.client.options.FormatType;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.displayer.DisplayerSettingsColumn;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.displayer.DisplayerSettings;

public abstract class GoogleViewer<T extends DisplayerSettings> extends AbstractDisplayer<T> {

    protected boolean drawn = false;
    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    protected DataSet dataSet;
    protected DataTable googleTable = null;

    public GoogleViewer() {
        initWidget(panel);
    }

    /**
     * Draw the displayer by getting first the underlying data set.
     * Ensure the displayer is also ready for display, which means the Google Visualization API has been loaded.
     */
    public void draw() {
        if (!drawn) {
            drawn = true;

            if (displayerSettings == null) {
                displayMessage("ERROR: DisplayerSettings property not set");
            }
            else if (dataSetHandler == null) {
                displayMessage("ERROR: DataSetHandler property not set");
            }
            else {
                try {
                    displayMessage("Initializing '" + displayerSettings.getTitle() + "'...");
                    beforeDataSetLookup();
                    dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet result) {
                            dataSet = result;
                            afterDataSetLookup(result);
                            Widget w = createVisualization();
                            panel.clear();
                            panel.add(w);
                        }
                        public void notFound() {
                            displayMessage("ERROR: Data set not found.");
                        }
                    });
                } catch (Exception e) {
                    displayMessage("ERROR: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Just reload the data set and make the current Google Viewer to redraw.
     */
    public void redraw() {
        if (!drawn) {
            draw();
        } else {
            try {
                beforeDataSetLookup();
                dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                    public void callback(DataSet result) {
                        dataSet = result;
                        afterDataSetLookup(result);
                        updateVisualization();
                    }
                    public void notFound() {
                        displayMessage("ERROR: Data set not found.");
                    }
                });
            } catch (Exception e) {
                displayMessage("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * Get the Google Visualization package this displayer requires.
     */
    protected abstract ChartPackage getPackage();

    /**
     * Create the widget used by concrete Google displayer implementation.
     */
    protected abstract Widget createVisualization();

    /**
     * Update the widget used by concrete Google displayer implementation.
     */
    protected abstract void updateVisualization();

    /**
     * Call back method invoked just before the data set lookup is executed.
     */
    protected void beforeDataSetLookup() {
    }

    /**
     * Call back method invoked just after the data set lookup is executed.
     */
    protected void afterDataSetLookup(DataSet dataSet) {
    }

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage(String msg) {
        panel.clear();
        panel.add(label);
        label.setText(msg);
    }

    // Google DataTable manipulation methods

    public DataTable createTable() {
        List<DisplayerSettingsColumn> displayerSettingsColumns = displayerSettings.getColumnList();
        if (displayerSettingsColumns.isEmpty()) {
            return googleTable = formatTable(createTableFromDataSet());
        }
        return googleTable = formatTable(createTableFromDisplayerSettings());
    }

    protected DataTable formatTable(DataTable gTable) {
        DateFormatOptions dateFormatOptions = DateFormatOptions.create();
        dateFormatOptions.setFormatType(FormatType.MEDIUM);
        DateFormat dateFormat = DateFormat.create(dateFormatOptions);

        NumberFormatOptions numberFormatOptions = NumberFormatOptions.create();
        numberFormatOptions.setPattern("#,###.##");
        NumberFormat numberFormat = NumberFormat.create(numberFormatOptions);

        for (int i = 0; i < gTable.getNumberOfColumns(); i++) {
            com.googlecode.gwt.charts.client.ColumnType type = gTable.getColumnType(i);
            if (com.googlecode.gwt.charts.client.ColumnType.DATE.equals(type)) {
                dateFormat.format(gTable, i);
            }
            else if (com.googlecode.gwt.charts.client.ColumnType.NUMBER.equals(type)) {
                numberFormat.format(gTable, i);
            }
        }
        return gTable;
    }

    protected DataTable createTableFromDataSet() {
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

    public DataTable createTableFromDisplayerSettings() {
        DataTable gTable = DataTable.create();
        gTable.addRows(dataSet.getRowCount());
        int columnIndex = 0;

        List<DisplayerSettingsColumn> displayerSettingsColumns = displayerSettings.getColumnList();
        for (int i = 0; i < displayerSettingsColumns.size(); i++) {
            DisplayerSettingsColumn displayerSettingsColumn = displayerSettingsColumns.get(i);
            DataColumn dataColumn = null;
            if (displayerSettingsColumn.getColumnId() != null) dataColumn = dataSet.getColumnById(displayerSettingsColumn.getColumnId());
            else dataColumn = dataSet.getColumnByIndex(columnIndex++);
            if (dataColumn == null) {
                throw new RuntimeException("Displayer column not found in the data set: " + displayerSettingsColumn.getDisplayName());
            }

            ColumnType columnType = dataColumn.getColumnType();
            List columnValues = dataColumn.getValues();
            gTable.addColumn(getColumnType(dataColumn), displayerSettingsColumn.getDisplayName(), dataColumn.getId());
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

    protected com.google.gwt.i18n.client.NumberFormat numberFormat = com.google.gwt.i18n.client.NumberFormat.getFormat("#0.00");

    public void setTableValue(DataTable gTable, ColumnType type, Object value, int row, int column) {
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

    public com.googlecode.gwt.charts.client.ColumnType getColumnType(DataColumn dataColumn) {
        ColumnType type = dataColumn.getColumnType();
        if (ColumnType.LABEL.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.STRING;
        if (ColumnType.NUMBER.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.NUMBER;
        if (ColumnType.DATE.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.DATE;
        return com.googlecode.gwt.charts.client.ColumnType.STRING;
    }
}
