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
import com.googlecode.gwt.charts.client.corechart.BarChart;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.corechart.ColumnChart;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.corechart.CoreChartWidget;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.AnimationEasing;
import com.googlecode.gwt.charts.client.options.CoreOptions;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.renderer.google.client.resources.i18n.GoogleDisplayerConstants;

public class GoogleBarChartDisplayer extends GoogleCategoriesDisplayer {

    protected Panel chartPanel = new FlowPanel();
    protected CoreChartWidget chart;
    protected Panel filterPanel = new SimplePanel();

    private boolean isBarChart = false;
    private boolean isStacked = false;

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

        isBarChart = DisplayerSubType.BAR.equals(displayerSettings.getSubtype()) ||
                     DisplayerSubType.BAR_STACKED.equals(displayerSettings.getSubtype());
        isStacked = DisplayerSubType.BAR_STACKED.equals(displayerSettings.getSubtype()) ||
                    DisplayerSubType.COLUMN_STACKED.equals(displayerSettings.getSubtype());

        chart = isBarChart ? new BarChart() : new ColumnChart();
        chart.addSelectHandler(createSelectHandler(chart));

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
                .setGroupsTitle(GoogleDisplayerConstants.INSTANCE.common_Categories())
                .setColumnsTitle(GoogleDisplayerConstants.INSTANCE.common_Series())
                .setColumnTypes(new ColumnType[]{
                        ColumnType.LABEL,
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                   .supportsAttribute(DisplayerAttributeDef.TYPE)
                   .supportsAttribute(DisplayerAttributeDef.SUBTYPE)
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
            chart.draw(createTable(), createOptions());
            chartPanel.add(chart);
        }
    }

    private CoreOptions createOptions() {
        Animation anim = Animation.create();
        anim.setDuration(700);
        anim.setEasing(AnimationEasing.IN_AND_OUT);

        if (isBarChart) {
            BarChartOptions options = BarChartOptions.create();
            options.setWidth(displayerSettings.getChartWidth());
            options.setHeight( displayerSettings.getChartHeight() );
            options.setBackgroundColor(displayerSettings.getChartBackgroundColor());
            options.setLegend( createChartLegend() );
            options.setIsStacked(isStacked);
            if ( displayerSettings.isXAxisShowLabels() ) options.setHAxis( createHAxis() );
            if ( displayerSettings.isYAxisShowLabels() ) options.setVAxis( createVAxis() );
            options.setAnimation( anim );
            options.setChartArea(createChartArea());
            return options;
        }
        else {
            ColumnChartOptions options = ColumnChartOptions.create();
            options.setWidth(displayerSettings.getChartWidth());
            options.setHeight(displayerSettings.getChartHeight());
            options.setBackgroundColor(displayerSettings.getChartBackgroundColor());
            options.setLegend(createChartLegend());
            options.setIsStacked(isStacked);
            if ( displayerSettings.isXAxisShowLabels() ) options.setHAxis( createHAxis() );
            if ( displayerSettings.isYAxisShowLabels() ) options.setVAxis( createVAxis() );
            options.setAnimation(anim);
            // TODO: options.set3D(displayerSettings.is3d());
            options.setChartArea(createChartArea());
            options.setColors(createColorArray(googleTable));
            return options;
        }
    }
}
