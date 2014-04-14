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

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.displayer.XAxis;
import org.dashbuilder.model.displayer.YAxis;
import org.dashbuilder.client.displayer.DataDisplayerViewer;

public abstract class GoogleChartViewer extends DataDisplayerViewer {

    public abstract Widget drawChart();
    public abstract String getPackage();

    protected boolean isApiReady = false;
    protected boolean isDataReady = false;

    public boolean isDataReady() {
        return isDataReady;
    }

    public boolean isApiReady() {
        return isApiReady;
    }

    public boolean isDisplayReady() {
        return isApiReady && isDataReady;
    }

    public void onDataReady() {
        isDataReady = true;
        if (isDisplayReady()) drawChart();
    }

    public void onApiReady() {
        isApiReady = true;
        if (isDisplayReady()) {
            drawChart();
        }
    }

    public AbstractDataTable createTable() {
        DataTable data = DataTable.create();

        // Add the xAxis column
        XAxis xAxis = dataDisplayer.getXAxis();
        DataColumn xAxisColumn = dataSet.getColumnById(xAxis.getColumnId());
        if (xAxisColumn == null) {
            GWT.log("Domain column not found in the data set: " + xAxis.getColumnId());
        }

        List xAxisValues = xAxisColumn.getValues();
        data.addRows(xAxisValues.size());
        data.addColumn(getColumnType(xAxisColumn), xAxis.getDisplayName());
        for (int i = 0; i < xAxisValues.size(); i++) {
            data.setValue(i, 0, xAxisValues.get(i).toString());
        }

        // Add the range columns
        List<YAxis> yAxes = dataDisplayer.getYAxes();
        for (int i = 0; i < yAxes.size(); i++) {
            YAxis yAxis = yAxes.get(i);
            DataColumn yAxisColumn = dataSet.getColumnById(yAxis.getColumnId());
            if (yAxisColumn == null) {
                GWT.log("Range column not found in the data set: " + xAxis.getColumnId());
            }

            List yAxisValues = yAxisColumn.getValues();
            data.addColumn(AbstractDataTable.ColumnType.NUMBER, yAxis.getDisplayName());
            for (int j = 0; j < yAxisValues.size(); j++) {
                // TODO: format decimal number
                double value = ((Double) yAxisValues.get(j)).doubleValue();
                data.setValue(j, i+1, value);
                //GWT.log("Row="+j+" Col="+(i+1)+" Val="+value);
            }
        }
        return data;
    }

    public AbstractDataTable.ColumnType getColumnType(DataColumn dataColumn) {
        ColumnType type = dataColumn.getColumnType();
        if (ColumnType.LABEL.equals(type)) return AbstractDataTable.ColumnType.STRING;
        if (ColumnType.NUMBER.equals(type)) return AbstractDataTable.ColumnType.NUMBER;
        if (ColumnType.DATE.equals(type)) return AbstractDataTable.ColumnType.DATETIME;
        return AbstractDataTable.ColumnType.STRING;
    }
}
