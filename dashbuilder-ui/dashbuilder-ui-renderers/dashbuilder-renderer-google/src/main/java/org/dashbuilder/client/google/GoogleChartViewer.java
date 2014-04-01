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

    public AbstractDataTable createTable() {
        DataTable data = DataTable.create();

        // Add the xAxis column
        XAxis xAxis = dataDisplayer.getXAxis();
        DataColumn xAxisColumn = dataSet.getColumnById(xAxis.getColumnId());
        List xAxisValues = xAxisColumn.getValues();
        data.addRows(xAxisValues.size());
        data.addColumn(getColumnType(xAxisColumn), xAxisColumn.getName());
        for (int i = 0; i < xAxisValues.size(); i++) {
            data.setValue(i, 0, xAxisValues.get(i).toString());
        }

        // Add the range columns
        List<YAxis> yAxes = dataDisplayer.getYAxes();
        for (int i = 0; i < yAxes.size(); i++) {
            YAxis yAxis = yAxes.get(i);
            DataColumn yAxisColumn = dataSet.getColumnById(yAxis.getColumnId());
            List yAxisValues = yAxisColumn.getValues();
            data.addColumn(AbstractDataTable.ColumnType.NUMBER, yAxisColumn.getName());
            for (int j = 0; j < yAxisValues.size(); j++) {
                data.setValue(j, i+1, Double.parseDouble(yAxisValues.get(j).toString()));
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
