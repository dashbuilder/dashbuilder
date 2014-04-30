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

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.BarChart;
import com.google.gwt.visualization.client.visualizations.ColumnChart;
import org.dashbuilder.model.displayer.AbstractChartDisplayer;
import org.dashbuilder.model.displayer.BarChartDisplayer;

@Dependent
@Named("google_barchart_viewer")
public class GoogleBarChartViewer extends GoogleXAxisChartViewer {

    @Override
    public String getPackage() {
        return BarChart.PACKAGE;
    }

    @Override
    public Widget createChart() {
        BarChartDisplayer barDisplayer = (BarChartDisplayer) dataDisplayer;
        Widget chart = null;

        if (barDisplayer.isHorizontal()) {
            chart = new BarChart(createTable(), createBarOptions());
        } else {
            chart = new ColumnChart(createTable(), createColumnOptions());
        }

        HTML titleHtml = new HTML();
        if (barDisplayer.isTitleVisible()) {
            titleHtml.setText(dataDisplayer.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(chart);
        return verticalPanel;
    }

    private BarChart.Options createBarOptions() {
        BarChart.Options options = BarChart.Options.create();
        BarChartDisplayer chart = (BarChartDisplayer) dataDisplayer;
        options.setWidth(chart.getWidth());
        options.setHeight(chart.getHeight());
        options.set3D(chart.is3d());
        return options;
    }

    private ColumnChart.Options createColumnOptions() {
        ColumnChart.Options options = ColumnChart.Options.create();
        BarChartDisplayer chart = (BarChartDisplayer) dataDisplayer;
        options.setWidth(chart.getWidth());
        options.setHeight(chart.getHeight());
        options.set3D(chart.is3d());
        return options;
    }
}
