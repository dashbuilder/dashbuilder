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
import com.google.gwt.visualization.client.VisualizationUtils;
import org.dashbuilder.client.displayer.DataDisplayerRenderer;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.events.PerspectiveChange;

/**
 * Google's GWT Visualization API based renderer.
 */
@ApplicationScoped
@Named("google_renderer")
public class GoogleRenderer implements DataDisplayerRenderer {

    private List<GoogleDisplayerViewer> viewerList = new ArrayList<GoogleDisplayerViewer>();
    private Set<String> packageList = new HashSet<String>();

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

    public DataDisplayerViewer lookupViewer(DataDisplayer displayer) {
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

    public GoogleDisplayerViewer registerViewer(GoogleDisplayerViewer viewer) {
        viewerList.add(viewer);
        packageList.add(viewer.getPackage());
        return viewer;
    }

    public void renderCharts() {
        // Create a callback to be called when the visualization API has been loaded.
        Runnable onLoadCallback = new Runnable() {
            public void run() {
                for (GoogleDisplayerViewer viewer : viewerList) {
                    viewer.ready();
                }
                viewerList.clear();
            }

        };

        // Load the visualization api, passing the onLoadCallback to be called when loading is done.
        JsArrayString packageArray = JsArrayString.createArray().cast();
        for (String pkg : packageList) {
            packageArray.push(pkg);
        }
        VisualizationUtils.loadVisualizationApi("1", onLoadCallback, packageArray);
    }

}
