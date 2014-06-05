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
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.Gauge.Options;
import org.dashbuilder.model.displayer.MeterChartDisplayer;

public class GoogleMeterChartViewer extends GoogleDisplayerViewer<MeterChartDisplayer> {

    @Override
    public String getPackage() {
        return Gauge.PACKAGE;
    }

    @Override
    public Widget createChart() {
        Gauge chart = new Gauge(createTable(), createOptions());

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
        options.setWidth(dataDisplayer.getWidth());
        options.setHeight(dataDisplayer.getHeight());
        options.setSize(dataDisplayer.getWidth(), dataDisplayer.getHeight());
        options.setGaugeRange((int) dataDisplayer.getMeterStart(), (int) dataDisplayer.getMeterEnd());
        options.setGreenRange((int) dataDisplayer.getMeterStart(), (int) dataDisplayer.getMeterWarning());
        options.setYellowRange((int) dataDisplayer.getMeterWarning(), (int) dataDisplayer.getMeterCritical());
        options.setRedRange((int) dataDisplayer.getMeterCritical(), (int) dataDisplayer.getMeterEnd());
        return options;
    }
}
