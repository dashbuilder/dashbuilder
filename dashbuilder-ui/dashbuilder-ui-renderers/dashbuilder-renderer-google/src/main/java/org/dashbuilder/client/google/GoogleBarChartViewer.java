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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.visualizations.BarChart;
import com.google.gwt.visualization.client.visualizations.ColumnChart;
import org.dashbuilder.model.displayer.BarChartDisplayer;

public class GoogleBarChartViewer extends GoogleXAxisChartViewer<BarChartDisplayer> {

    @Override
    public String getPackage() {
        return BarChart.PACKAGE;
    }

    @Override
    public Widget createVisualization() {

        Widget chart = null;

        if (dataDisplayer.isHorizontal()) {
            BarChart bchart = new BarChart(createTable(), createBarOptions());
            bchart.addSelectHandler(createSelectHandler(bchart));
            chart = bchart;
            googleViewer = bchart;
        } else {
            ColumnChart cchart = new ColumnChart(createTable(), createColumnOptions());
            cchart.addSelectHandler(createSelectHandler(cchart));
            chart = cchart;
            googleViewer = cchart;
        }

        HTML titleHtml = new HTML();
        if (dataDisplayer.isTitleVisible()) {
            titleHtml.setText(dataDisplayer.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(chart);
        return verticalPanel;
    }

    private BarChart.Options createBarOptions() {
        BarChart.Options options = BarChart.Options.create();
        options.setWidth(dataDisplayer.getWidth());
        options.setHeight(dataDisplayer.getHeight());
        options.set3D(dataDisplayer.is3d());
        options.setColors(createColorArray(googleTable));
        googleOptions = options;
        return options;
    }

    private ColumnChart.Options createColumnOptions() {
        ColumnChart.Options options = ColumnChart.Options.create();
        options.setWidth(dataDisplayer.getWidth());
        options.setHeight(dataDisplayer.getHeight());
        options.set3D(dataDisplayer.is3d());
        options.setColors(createColorArray(googleTable));
        googleOptions = options;
        return options;
    }
}
