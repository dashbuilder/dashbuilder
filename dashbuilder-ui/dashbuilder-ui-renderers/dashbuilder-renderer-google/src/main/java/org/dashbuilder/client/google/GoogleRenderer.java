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
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import org.dashbuilder.client.displayer.AbstractRendererLibrary;
import org.dashbuilder.client.displayer.DataViewer;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerType;

/**
 * Google's Visualization API based renderer.
 */
@ApplicationScoped
@Named(GoogleRenderer.UUID + "_renderer")
public class GoogleRenderer extends AbstractRendererLibrary {

    public static final String UUID = "google";

    public String getUUID() {
        return UUID;
    }

    public DataViewer lookupViewer(DataDisplayer displayer) {
        DataDisplayerType type = displayer.getType();
        if (DataDisplayerType.BARCHART.equals(type)) return new GoogleBarChartViewer();
        if (DataDisplayerType.PIECHART.equals(type)) return new GooglePieChartViewer();
        if (DataDisplayerType.AREACHART.equals(type)) return new GoogleAreaChartViewer();
        if (DataDisplayerType.LINECHART.equals(type)) return new GoogleLineChartViewer();
        if (DataDisplayerType.BUBBLECHART.equals(type)) return new GoogleBubbleChartViewer();
        if (DataDisplayerType.METERCHART.equals(type)) return new GoogleMeterChartViewer();
        if (DataDisplayerType.TABLE.equals(type)) return new GoogleTableViewer();
        if (DataDisplayerType.MAP.equals(type)) return new GoogleMapViewer();

        return null;
    }

    /**
     *  In Google the renderer mechanism is asynchronous.
     */
    public void draw(final List<DataViewer> viewerList) {
        // Get the modules to load.
        Set<ChartPackage> packageList = new HashSet<ChartPackage>();
        for (DataViewer viewer : viewerList) {
            try {
                GoogleViewer googleViewer = (GoogleViewer) viewer;
                packageList.add(googleViewer.getPackage());
            } catch (ClassCastException e) {
                // Just ignore non Google viewers.
            }
        }
        // Create an array of packages.
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
                GoogleRenderer.super.draw(viewerList);
            }
        });
    }
}
