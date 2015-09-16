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
import java.util.ArrayList;

import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.format.DateFormat;
import com.googlecode.gwt.charts.client.format.DateFormatOptions;
import com.googlecode.gwt.charts.client.format.NumberFormat;
import com.googlecode.gwt.charts.client.format.NumberFormatOptions;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.displayer.client.Displayer;

public abstract class GoogleDisplayer extends AbstractDisplayer {

    protected GoogleRenderer googleRenderer;
    protected DataTable googleTable = null;

    public GoogleDisplayer setRenderer(GoogleRenderer googleRenderer) {
        this.googleRenderer = googleRenderer;
        return this;
    }

    /**
     * GCharts drawing is done asynchronously via its renderer (see ready() method below)
     */
    @Override
    public void draw() {
        if (googleRenderer == null)  {
            afterError("Google renderer not set");
        }
        else if (!isDrawn())  {
            List<Displayer> displayerList = new ArrayList<Displayer>();
            displayerList.add(this);
            googleRenderer.draw(displayerList);
        }
    }

    /**
     * Invoked asynchronously by the GoogleRenderer when the displayer is ready for being displayed
     */
    void ready() {
        super.draw();
    }

    /**
     * Get the Google Visualization package this displayer requires.
     */
    protected abstract ChartPackage getPackage();


    // Google DataTable manipulation methods

    public DataTable createTable() {

        // Init & populate the table
        googleTable = DataTable.create();
        googleTable.addRows(dataSet.getRowCount());
        List<DataColumn> columns = dataSet.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            DataColumn dataColumn = columns.get(i);
            String columnId = dataColumn.getId();
            ColumnType columnType = dataColumn.getColumnType();
            ColumnSettings columnSettings = displayerSettings.getColumnSettings(dataColumn);
            googleTable.addColumn(getColumnType(dataColumn), columnSettings.getColumnName(), columnId);

            List columnValues = dataColumn.getValues();
            for (int j = 0; j < columnValues.size(); j++) {
                Object value = columnValues.get(j);

                if (ColumnType.DATE.equals(columnType)) {
                    if (value == null) googleTable.setValue(j, i, new Date());
                    else googleTable.setValue(j, i, (Date) value);
                }
                else if (ColumnType.NUMBER.equals(columnType)) {
                    if (value == null) {
                        googleTable.setValue(j, i, 0d);
                    } else {
                        value = super.applyExpression(value.toString(), columnSettings.getValueExpression());
                        googleTable.setValue(j, i, Double.parseDouble(value.toString()));
                    }
                }
                else {
                    value = super.formatValue(j, i);
                    googleTable.setValue(j, i, value.toString());
                }
            }
        }

        // Format the table values
        for (int i = 0; i < googleTable.getNumberOfColumns(); i++) {
            DataColumn dataColumn = columns.get(i);
            com.googlecode.gwt.charts.client.ColumnType type = googleTable.getColumnType(i);
            ColumnSettings columnSettings = displayerSettings.getColumnSettings(dataColumn);
            String pattern = columnSettings.getValuePattern();

            if (com.googlecode.gwt.charts.client.ColumnType.DATE.equals(type)) {

                DateFormatOptions dateFormatOptions = DateFormatOptions.create();
                dateFormatOptions.setPattern(pattern);
                DateFormat dateFormat = DateFormat.create(dateFormatOptions);
                dateFormat.format(googleTable, i);
            }
            else if (com.googlecode.gwt.charts.client.ColumnType.NUMBER.equals(type)) {

                NumberFormatOptions numberFormatOptions = NumberFormatOptions.create();
                numberFormatOptions.setPattern(pattern);
                NumberFormat numberFormat = NumberFormat.create(numberFormatOptions);
                numberFormat.format(googleTable, i);
            }
        }
        return googleTable;
    }

    public com.googlecode.gwt.charts.client.ColumnType getColumnType(DataColumn dataColumn) {
        ColumnType type = dataColumn.getColumnType();
        if (ColumnType.LABEL.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.STRING;
        if (ColumnType.TEXT.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.STRING;
        if (ColumnType.NUMBER.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.NUMBER;
        if (ColumnType.DATE.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.DATE;
        return com.googlecode.gwt.charts.client.ColumnType.STRING;
    }
}
