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

import com.ait.lienzo.charts.client.AbstractChart;
import com.ait.lienzo.charts.client.pie.PieChart;
import com.ait.lienzo.charts.client.pie.PieChartData;
import com.ait.lienzo.charts.client.pie.event.DataReloadedEvent;
import com.ait.lienzo.charts.client.pie.event.DataReloadedEventHandler;
import com.ait.lienzo.charts.client.pie.event.ValueSelectedEvent;
import com.ait.lienzo.charts.client.pie.event.ValueSelectedHandler;
import com.ait.lienzo.charts.client.resizer.ChartResizeEvent;
import com.ait.lienzo.charts.client.resizer.ChartResizeEventHandler;
import com.ait.lienzo.charts.shared.core.types.ChartOrientation;
import com.ait.lienzo.shared.core.types.ColorName;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;

public class LienzoPieChartDisplayer extends LienzoDisplayer {

    private static final ColorName[] DEFAULT_SERIE_COLORS = new ColorName[] {
            ColorName.DEEPSKYBLUE, ColorName.RED, ColorName.YELLOWGREEN            
    };
    
    protected PieChart chart = null;
    private boolean isConfigured = false;

    @Override
    public AbstractChart createVisualization() {

        // Create the data for the chart instance.
        PieChartData chartData = createChartData();

        // Create the BarChart instance.
        chart = new PieChart(new DataReloadedEventHandler() {
            @Override
            public void onDataReloaded(DataReloadedEvent event) {
                configurePieChart(event.getChart());
            }
        });
        
        // Data.
        chart.setData(chartData);
        
        return chart;
    }
    
    private void configurePieChart(PieChart chart) {
        if (isConfigured) return;
        
        if (displayerSettings.isBarchartHorizontal()) chart.setOrientation(ChartOrientation.HORIZNONAL);
        else chart.setOrientation(ChartOrientation.VERTICAL);

        chart.setX(0).setY(0);
        chart.setName(displayerSettings.getTitle());
        chart.setWidth(getChartWidth());
        chart.setHeight(getChartHeight());
        chart.setMarginLeft(displayerSettings.getChartMarginLeft());
        chart.setMarginRight(displayerSettings.getChartMarginRight());
        chart.setMarginTop(displayerSettings.getChartMarginTop());
        chart.setMarginBotom(displayerSettings.getChartMarginBottom());
        chart.setFontFamily("Verdana");
        chart.setFontStyle("bold");
        chart.setFontSize(8);
        chart.setShowTitle(displayerSettings.isTitleVisible());
        chart.setResizable(displayerSettings.isResizable());
        chart.setAnimated(true); // TODO: Custom displayer parameter.

        // Filtering event.
        if (displayerSettings.isFilterEnabled()) {
            chart.addValueSelectedHandler(new PieValueSelectedHandler());
        }

        // Resize event.
        if (displayerSettings.isResizable()) {
            chart.addChartResizeEventHandler(new ChartResizeEventHandler() {
                @Override
                public void onChartResize(ChartResizeEvent event) {
                    resizePanel((int) event.getWidth(), (int) event.getHeight());
                }
            });
        }


        chart.build();
        
        isConfigured=true;
    }

    @Override
    protected void updateVisualization() {
        filterPanel.clear();
        Widget filterReset = super.createCurrentSelectionWidget();
        if (filterReset != null) filterPanel.add(filterReset);

        if (dataSet.getRowCount() == 0) {
            mainPanel.add(super.createNoDataMsgPanel());
            chart = null;
        } else {
            PieChartData newData = createChartData();
            chart.setData(newData);
        }
    }

    protected PieChartData createChartData() {

        // Create data instance and the series to display.
        DataColumn categoriesColumn = getCategoriesColumn();
        DataColumn[] valuesColumns = getValuesColumns();
        PieChartData chartData = new PieChartData(lienzoTable, categoriesColumn.getId(), valuesColumns[0].getId());
        
        return chartData;
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {
        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns(2)
                .setMinColumns(2)
                .setExtraColumnsAllowed(false)
                .setGroupsTitle("Categories")
                .setColumnsTitle("Values")
                .setColumnTypes(new ColumnType[] {
                        ColumnType.LABEL,
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute(DisplayerAttributeDef.TYPE)
                .supportsAttribute(DisplayerAttributeDef.RENDERER)
                .supportsAttribute(DisplayerAttributeDef.COLUMNS)
                .supportsAttribute(DisplayerAttributeGroupDef.FILTER_GROUP)
                .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.TITLE_GROUP)
                .supportsAttribute( DisplayerAttributeGroupDef.CHART_GROUP );
    }

    public class PieValueSelectedHandler implements ValueSelectedHandler {

        @Override
        public void onValueSelected(ValueSelectedEvent event) {
            GWT.log("filtering by column [" + event.getColumn() + "], row [" + event.getRow() + "]");
            filterUpdate(event.getColumn(), event.getRow());
        }
        
    }
}
