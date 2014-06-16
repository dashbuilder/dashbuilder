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

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

import com.google.gwt.core.client.JsArrayString;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import org.dashbuilder.client.displayer.RendererLibrary;
import org.dashbuilder.client.displayer.DataViewer;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.events.PerspectiveChange;

/**
 * Google's GWT Visualization API based renderer.
 */
@ApplicationScoped
@Named(GoogleRenderer.UUID + "_renderer")
public class GoogleRenderer implements RendererLibrary {

    private List<GoogleViewer> viewerList = new ArrayList<GoogleViewer>();
    private Set<ChartPackage> packageList = new HashSet<ChartPackage>();

    public static final String UUID = "google";

    public String getUUID() {
        return UUID;
    }

    // Listen to UF events in order to render charts at the proper time.

    private void onAppReady(@Observes final ApplicationReadyEvent event) {
        renderCharts();
    }

    private void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        renderCharts();
    }

    // Register and draw charts using the asynchronous Google API

    public DataViewer lookupViewer(DataDisplayer displayer) {
        DataDisplayerType type = displayer.getType();
        if (DataDisplayerType.BARCHART.equals(type)) return registerViewer(new GoogleBarChartViewer());
        if (DataDisplayerType.PIECHART.equals(type)) return registerViewer(new GooglePieChartViewer());
        if (DataDisplayerType.AREACHART.equals(type)) return registerViewer(new GoogleAreaChartViewer());
        if (DataDisplayerType.LINECHART.equals(type)) return registerViewer(new GoogleLineChartViewer());
        if (DataDisplayerType.METERCHART.equals(type)) return registerViewer(new GoogleMeterChartViewer());
        if (DataDisplayerType.TABLE.equals(type)) return registerViewer(new GoogleTableViewer());
        if (DataDisplayerType.MAP.equals(type)) return registerViewer(new GoogleMapViewer());

        return null;
    }

    public GoogleViewer registerViewer(GoogleViewer viewer) {
        viewerList.add(viewer);
        packageList.add(viewer.getPackage());
        return viewer;
    }

    public void renderCharts() {
        // Get the Google packages to load.
        ChartPackage[] packageArray = new ChartPackage[packageList.size()];
        int i = 0;
        for (ChartPackage pkg : packageList) {
            packageArray[i++] = pkg;
        }
        // Load the visualization API
        ChartLoader chartLoader = new ChartLoader(packageArray);
        chartLoader.loadApi(new Runnable() {

            // Called when the visualization API has been loaded.
            public void run() {
                for (GoogleViewer viewer : viewerList) {
                    viewer.ready();
                }
                viewerList.clear();
            }
        });
    }

}
