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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.DataViewer;
import org.dashbuilder.displayer.DisplayerSettings;

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

    public DataViewer lookupViewer(DisplayerSettings displayerSettings) {
        DisplayerType displayerType = displayerSettings.getType();
        if ( DisplayerType.BARCHART.equals(displayerType)) return new GoogleBarChartViewer();
        if ( DisplayerType.PIECHART.equals(displayerType)) return new GooglePieChartViewer();
        if ( DisplayerType.AREACHART.equals(displayerType)) return new GoogleAreaChartViewer();
        if ( DisplayerType.LINECHART.equals(displayerType)) return new GoogleLineChartViewer();
        if ( DisplayerType.BUBBLECHART.equals(displayerType)) return new GoogleBubbleChartViewer();
        if ( DisplayerType.METERCHART.equals(displayerType)) return new GoogleMeterChartViewer();
        if ( DisplayerType.TABLE.equals(displayerType)) return new GoogleTableViewer();
        if ( DisplayerType.MAP.equals(displayerType)) return new GoogleMapViewer();

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
