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

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.corechart.LineChart;
import com.googlecode.gwt.charts.client.corechart.LineChartOptions;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.AnimationEasing;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;

public class GoogleLineChartDisplayer extends GoogleXAxisChartDisplayer {

    protected Panel chartPanel = new FlowPanel();
    protected LineChart chart;
    protected Panel filterPanel = new SimplePanel();

    @Override
    public ChartPackage getPackage() {
        return ChartPackage.CORECHART;
    }

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

        if (dataSet.getRowCount() == 0) {
            chartPanel.add(super.createNoDataMsgPanel());
        } else {
            chart = new LineChart();
            chart.addSelectHandler(createSelectHandler(chart));
            chart.draw(createTable(), createOptions());
            chartPanel.add(chart);
        }
        return verticalPanel;
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {
        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxGroups(1)
                .setMinColumns(2)
                .setMaxColumns(10)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle("Categories")
                .setColumnsTitle("Series")
                .setColumnTypes(new ColumnType[] {
                        ColumnType.LABEL,
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                   .supportsAttribute( DisplayerAttributeDef.TYPE )
                   .supportsAttribute(DisplayerAttributeDef.COLUMNS)
                   .supportsAttribute( DisplayerAttributeGroupDef.FILTER_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP )
                   .supportsAttribute( DisplayerAttributeGroupDef.TITLE_GROUP)
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

        chartPanel.clear();
        if (dataSet.getRowCount() == 0) {
            chartPanel.add(super.createNoDataMsgPanel());
        } else {
            chart.draw(createTable(), createOptions());
            chartPanel.add(chart);
        }
    }

    private LineChartOptions createOptions() {
        Animation anim = Animation.create();
        anim.setDuration(500);
        anim.setEasing(AnimationEasing.LINEAR);

        LineChartOptions options = LineChartOptions.create();
        options.setWidth(displayerSettings.getChartWidth());
        options.setHeight(displayerSettings.getChartHeight());
        options.setBackgroundColor(displayerSettings.getChartBackgroundColor());
        if ( displayerSettings.isXAxisShowLabels() ) options.setHAxis( createHAxis() );
        if ( displayerSettings.isYAxisShowLabels() ) options.setVAxis( createVAxis() );
        options.setLegend( createChartLegend( displayerSettings ) );
        options.setAnimation(anim);
        options.setChartArea(createChartArea());
        options.setColors(createColorArray(googleTable));
        return options;
    }
}
