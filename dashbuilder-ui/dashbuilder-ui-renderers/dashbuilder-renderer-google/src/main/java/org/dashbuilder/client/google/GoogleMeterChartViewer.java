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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.Gauge.Options;
import org.dashbuilder.model.displayer.Chart;
import org.dashbuilder.model.displayer.MeterChart;

@Dependent
@Named("google_meterchart_viewer")
public class GoogleMeterChartViewer extends GoogleChartViewer {

    @Override
    public String getPackage() {
        return Gauge.PACKAGE;
    }

    @Override
    public Widget createChart() {
        HTML titleHtml = new HTML(dataDisplayer.getTitle());
        Gauge chart = new Gauge(createTable(), createOptions());

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(chart);
        return verticalPanel;
    }

    private Options createOptions() {
        Options options = Options.create();
        if (dataDisplayer instanceof Chart) {
            Chart chart = (Chart) dataDisplayer;
            options.setWidth(chart.getWidth());
            options.setHeight(chart.getHeight());
            options.setSize(chart.getWidth(), chart.getHeight());
        }
        if (dataDisplayer instanceof MeterChart) {
            MeterChart mc = (MeterChart) dataDisplayer;
            options.setGaugeRange((int) mc.getMeterStart(), (int) mc.getMeterEnd());
            options.setGreenRange((int) mc.getMeterStart(), (int) mc.getMeterWarning());
            options.setYellowRange((int) mc.getMeterWarning(), (int) mc.getMeterCritical());
            options.setRedRange((int) mc.getMeterCritical(), (int) mc.getMeterEnd());
        }
        return options;
    }
}
