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
package org.dashbuilder.renderer.chartjs;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.renderer.chartjs.lib.BarChart;
import org.dashbuilder.renderer.chartjs.lib.Chart;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.renderer.chartjs.lib.event.DataSelectionEvent;
import org.dashbuilder.renderer.chartjs.lib.event.DataSelectionHandler;
import org.dashbuilder.renderer.chartjs.resources.i18n.ChartJsDisplayerConstants;

public class ChartJsBarChartDisplayer extends ChartJsDisplayer {

    protected Panel chartPanel = new FlowPanel();
    protected Chart chart;
    protected Panel filterPanel = new SimplePanel();

    @Override
    public Widget createVisualization() {
        HTML titleHtml = new HTML();
        if (displayerSettings.isTitleVisible()) {
            titleHtml.setText(displayerSettings.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(filterPanel);
        verticalPanel.add(chartPanel);

        BarChart barChart = new BarChart();
        barChart.setTitle(displayerSettings.getTitle());
        barChart.setTooltipTemplate("<%= label %>: <%= window.chartJsFormatValue('" + displayerSettings.getUUID() + "', value, 1) %>");
        barChart.setMultiTooltipTemplate("<%= datasetLabel %>: <%= window.chartJsFormatValue('" + displayerSettings.getUUID() + "', value, 1) %>");
        barChart.setScaleStepWidth(10);
        barChart.setLegendTemplate("<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li><span style=\"background-color:<%=datasets[i].fillColor%>\"></span><%if(datasets[i].label){%><%=datasets[i].label%><%}%></li><%}%></ul>");
        barChart.setDataProvider(super.createAreaDataProvider());
        barChart.addDataSelectionHandler(new DataSelectionHandler() {
            public void onDataSelected(DataSelectionEvent event) {
                Object o = event.getSource();
            }
        });
        chart = barChart;
        updateChartPanel();
        return verticalPanel;
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns( 10 )
                .setMinColumns( 2 )
                .setExtraColumnsAllowed( true )
                .setExtraColumnsType( ColumnType.NUMBER )
                .setGroupsTitle(ChartJsDisplayerConstants.INSTANCE.common_Categories())
                .setColumnsTitle(ChartJsDisplayerConstants.INSTANCE.common_Series())
                .setColumnTypes(new ColumnType[]{
                        ColumnType.LABEL,
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute(DisplayerAttributeDef.TYPE)
                .supportsAttribute(DisplayerAttributeDef.RENDERER)
                .supportsAttribute( DisplayerAttributeGroupDef.COLUMNS_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.FILTER_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP)
                .supportsAttribute( DisplayerAttributeGroupDef.GENERAL_GROUP)
                .supportsAttribute( DisplayerAttributeDef.CHART_WIDTH )
                .supportsAttribute( DisplayerAttributeDef.CHART_HEIGHT )
                .supportsAttribute(DisplayerAttributeDef.CHART_BGCOLOR)
                .supportsAttribute(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP)
                .supportsAttribute( DisplayerAttributeGroupDef.CHART_LEGEND_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.AXIS_GROUP );
    }

    protected void updateVisualization() {
        filterPanel.clear();
        Widget filterReset = createCurrentSelectionWidget();
        if (filterReset != null) filterPanel.add(filterReset);

        updateChartPanel();
    }

    protected void updateChartPanel() {
        chartPanel.clear();
        if (dataSet.getRowCount() == 0) {
            chartPanel.add(super.createNoDataMsgPanel());
        } else {
            super.adjustChartSize(chart);
            chartPanel.add(chart);
            chart.update();
        }
    }
}
