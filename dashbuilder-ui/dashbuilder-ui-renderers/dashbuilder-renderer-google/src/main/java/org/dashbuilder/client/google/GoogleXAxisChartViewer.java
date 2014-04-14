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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.displayer.XAxis;
import org.dashbuilder.model.displayer.XAxisChart;
import org.dashbuilder.model.displayer.YAxis;

public abstract class GoogleXAxisChartViewer extends GoogleChartViewer {

    public AbstractDataTable createTable() {
        DataTable data = DataTable.create();
        XAxisChart xAxisChart = (XAxisChart) dataDisplayer;

        // Add the xAxis column
        XAxis xAxis = xAxisChart.getXAxis();
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
        List<YAxis> yAxes = xAxisChart.getYAxes();
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
}
