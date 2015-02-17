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
package org.dashbulider.renderer.lienzo.client;

import com.ait.lienzo.charts.client.axis.CategoryAxis;
import com.ait.lienzo.charts.client.axis.NumericAxis;
import com.ait.lienzo.charts.client.xy.BarChart;
import com.ait.lienzo.charts.client.xy.XYChartData;
import com.ait.lienzo.charts.client.xy.XYChartSerie;
import com.ait.lienzo.charts.shared.core.types.ChartOrientation;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.shared.core.types.ColorName;
import com.google.gwt.core.client.GWT;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;

import java.util.List;

public class LienzoBarChartDisplayer extends LienzoDisplayer {

    protected BarChart chart;
    protected XYChartData chartData;

    @Override
    public IPrimitive createVisualization() {

        // Create the data for the chart instance.
        createChartData();
        
        // Create the BarChart instance.
        chart = new BarChart(this.chartData);
        if (displayerSettings.isBarchartHorizontal()) chart.setOrientation(ChartOrientation.HORIZNONAL);
        else chart.setOrientation(ChartOrientation.VERTICAL);

        chart.setX(0);
        chart.setY(0);
        chart.setName(displayerSettings.getTitle());
        chart.setWidth(getWidth());
        chart.setHeight(getHeight());
        chart.setFontFamily("Verdana");
        chart.setFontStyle("bold");
        chart.setFontSize(12);
        chart.setShowTitle(displayerSettings.isTitleVisible());
        
        // TODO: Category and Number types?
        CategoryAxis categoryAxis = new CategoryAxis(displayerSettings.getXAxisTitle());
        NumericAxis numericAxis = new NumericAxis(displayerSettings.getYAxisTitle());
        
        chart.setCategoriesAxis(categoryAxis);
        chart.setValuesAxis(numericAxis);
        chart.build();

        return chart;
    }

    @Override
    protected void updateVisualization() {
        // TODO
    }

    protected void createChartData() {
        // Ensure data model instance is created.
        super.createTable();

        // Create data instance and the series to display.
        if (chartData == null) {
            chartData = new XYChartData(lienzoTable);
            DataColumn categoriesColumn = getCategoriesColumn();
            DataColumn[] valuesColumns = getValuesColumns();
            
            if (categoriesColumn != null) {
                chartData.setCategoryAxisProperty(categoriesColumn.getId());
                if (valuesColumns != null) {
                    for (int i = 0; i < valuesColumns.length; i++) {
                        DataColumn dataColumn = valuesColumns[i];
                        String columnId = dataColumn.getId();
                        String columnName = dataColumn.getName();
                        if (columnName == null) columnName = columnId;

                        XYChartSerie series = new XYChartSerie(columnName, ColorName.DEEPSKYBLUE, columnId);
                        chartData.addSerie(series);
                    }
                } else {
                    GWT.log("No values columns specified.");
                }
            } else {
                GWT.log("No categories column specified.");
            }
            

            
        }
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns(10)
                .setMinColumns(2)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle("Categories")
                .setColumnsTitle("Series")
                .setColumnTypes(new ColumnType[]{
                        ColumnType.LABEL,
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                   .supportsAttribute( DisplayerAttributeDef.TYPE )
                   .supportsAttribute(DisplayerAttributeDef.COLUMNS)
                   .supportsAttribute( DisplayerAttributeGroupDef.FILTER_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP)
                   .supportsAttribute( DisplayerAttributeGroupDef.TITLE_GROUP)
                   .supportsAttribute( DisplayerAttributeDef.CHART_WIDTH )
                   .supportsAttribute( DisplayerAttributeDef.CHART_HEIGHT )
                    .supportsAttribute(DisplayerAttributeDef.CHART_BGCOLOR)
                    .supportsAttribute(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP)
                   .supportsAttribute( DisplayerAttributeGroupDef.CHART_LEGEND_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.AXIS_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.BARCHART_GROUP );
    }
}
