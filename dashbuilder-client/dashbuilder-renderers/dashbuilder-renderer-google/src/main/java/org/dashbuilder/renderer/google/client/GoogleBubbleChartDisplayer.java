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

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.corechart.BubbleChart;
import com.googlecode.gwt.charts.client.corechart.BubbleChartOptions;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.AnimationEasing;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.VAxis;
import org.dashbuilder.displayer.DisplayerSettingId;

public class GoogleBubbleChartDisplayer extends GoogleXAxisChartDisplayer {

    private BubbleChart chart;
    protected Panel filterPanel;

    @Override
    public ChartPackage getPackage() {
        return ChartPackage.CORECHART;
    }

    @Override
    public Widget createVisualization() {
        chart = new BubbleChart();
        chart.addSelectHandler(createSelectHandler(chart));
        chart.draw(createTable(), createOptions());

        HTML titleHtml = new HTML();
        if (displayerSettings.isTitleVisible()) {
            titleHtml.setText(displayerSettings.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(filterPanel = new SimplePanel());
        verticalPanel.add(chart);
        return verticalPanel;
    }

    @Override
    public List<DisplayerSettingId> getSupportedDisplayerAttributes() {
        return Arrays.asList(
                new DisplayerSettingId[]{
                        DisplayerSettingId.TITLE_VISIBLE,
                        DisplayerSettingId.TITLE,
                        DisplayerSettingId.COLUMNS,
                        DisplayerSettingId.CHART_WIDTH,
                        DisplayerSettingId.CHART_HEIGHT,
                        DisplayerSettingId.CHART_MARGIN_TOP,
                        DisplayerSettingId.CHART_MARGIN_BOTTOM,
                        DisplayerSettingId.CHART_MARGIN_LEFT,
                        DisplayerSettingId.CHART_MARGIN_RIGHT,
                        DisplayerSettingId.CHART_SHOWLEGEND,
                        DisplayerSettingId.CHART_LEGENDPOSITION,
                        DisplayerSettingId.XAXIS_SHOWLABELS,
                        DisplayerSettingId.XAXIS_TITLE,
                        DisplayerSettingId.YAXIS_SHOWLABELS,
                        DisplayerSettingId.YAXIS_TITLE
                }
        );
    }

    protected void updateVisualization() {
        filterPanel.clear();
        Widget filterReset = createCurrentSelectionWidget();
        if (filterReset != null) filterPanel.add(filterReset);

        chart.draw(createTable(), createOptions());
    }

    private BubbleChartOptions createOptions() {
        Animation anim = Animation.create();
        anim.setDuration(700);
        anim.setEasing(AnimationEasing.IN);

        BubbleChartOptions options = BubbleChartOptions.create();
        options.setWidth(displayerSettings.getChartWidth());
        options.setHeight(displayerSettings.getChartHeight());
        options.setHAxis(HAxis.create(googleTable.getColumnLabel(1)));
        options.setVAxis(VAxis.create(googleTable.getColumnLabel(2)));
        options.setChartArea(createChartArea());
        options.setLegend( createChartLegend( displayerSettings ) );
        options.setAnimation(anim);
        String[] colors = createColorArray(googleTable);
        if (colors.length > 0) options.setColors(colors);
        return options;
    }
}
