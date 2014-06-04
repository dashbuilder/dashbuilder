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
import com.google.gwt.visualization.client.visualizations.LineChart.Options;
import com.google.gwt.visualization.client.visualizations.LineChart;
import org.dashbuilder.model.displayer.AbstractChartDisplayer;
import org.dashbuilder.model.displayer.XAxisChartDisplayer;

@Dependent
@Named("google_linechart_viewer")
public class GoogleLineChartViewer extends GoogleXAxisChartViewer {

    @Override
    public String getPackage() {
        return LineChart.PACKAGE;
    }

    @Override
    public Widget createChart() {
        LineChart chart = new LineChart(createTable(), createOptions());
        chart.addSelectHandler(createSelectHandler(chart));

        HTML titleHtml = new HTML();
        if (dataDisplayer.isTitleVisible()) {
            titleHtml.setText(dataDisplayer.getTitle());
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(chart);
        return verticalPanel;
    }

    private Options createOptions() {
        Options options = Options.create();
        if (dataDisplayer instanceof AbstractChartDisplayer) {
            AbstractChartDisplayer chart = (AbstractChartDisplayer) dataDisplayer;
            options.setWidth(chart.getWidth());
            options.setHeight(chart.getHeight());
        }
        return options;
    }
}
