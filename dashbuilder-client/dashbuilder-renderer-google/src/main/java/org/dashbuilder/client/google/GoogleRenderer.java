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

import java.util.List;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.PieChart;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.events.PerspectiveChange;

@ApplicationScoped
public class GoogleRenderer {

    private List<GoogleChartViewer> chartViewerList = new ArrayList<GoogleChartViewer>();

    // Listen to UF events in order to render charts at the proper time.

    private void onAppReady(@Observes final ApplicationReadyEvent event) {
        renderCharts();
    }

    private void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        renderCharts();
    }

    // Register and draw charts using the asynchronous Google API

    public void registerChart(GoogleChartViewer viewer) {
        chartViewerList.add(viewer);
    }

    public void renderCharts() {
        // Create a callback to be called when the visualization API has been loaded.
        Runnable onLoadCallback = new Runnable() {
            public void run() {
                for (GoogleChartViewer viewer : chartViewerList) {
                    viewer.drawChart();
                }
                chartViewerList.clear();
            }

        };

        // Load the visualization api, passing the onLoadCallback to be called when loading is done.
        VisualizationUtils.loadVisualizationApi(onLoadCallback, PieChart.PACKAGE);
    }
}
