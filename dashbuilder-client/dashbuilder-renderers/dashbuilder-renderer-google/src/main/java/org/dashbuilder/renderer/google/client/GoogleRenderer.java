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
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.RendererLibLocator;

/**
 * Google's Visualization API based renderer.
 */
@ApplicationScoped
@Named(GoogleRenderer.UUID + "_renderer")
public class GoogleRenderer extends AbstractRendererLibrary {

    public static final String UUID = "GWT Charts";

    @PostConstruct
    private void init() {
        RendererLibLocator rll = RendererLibLocator.get();
        rll.registerRenderer(DisplayerType.BARCHART, UUID, true);
        rll.registerRenderer(DisplayerType.PIECHART, UUID, true);
        rll.registerRenderer(DisplayerType.AREACHART, UUID, true);
        rll.registerRenderer(DisplayerType.LINECHART, UUID, true);
        rll.registerRenderer(DisplayerType.BUBBLECHART, UUID, true);
        rll.registerRenderer(DisplayerType.METERCHART, UUID, true);
        rll.registerRenderer(DisplayerType.MAP, UUID, true);
        rll.registerRenderer(DisplayerType.TABLE, UUID, true);
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public DisplayerSubType[] getSupportedSubtypes(DisplayerType displayerType) {
        switch (displayerType) {
            case BARCHART: return new DisplayerSubType[]{   DisplayerSubType.BAR,
                                                            DisplayerSubType.BAR_STACKED,
                                                            DisplayerSubType.COLUMN,
                                                            DisplayerSubType.COLUMN_STACKED};
            case PIECHART: return new DisplayerSubType[]{   DisplayerSubType.PIE,
                                                            DisplayerSubType.PIE_3D,
                                                            DisplayerSubType.DONUT};
            case AREACHART: return new DisplayerSubType[]{  DisplayerSubType.AREA,
                                                            DisplayerSubType.AREA_STACKED/*,
                                                            DisplayerSubType.STEPPED*/};
            case LINECHART: return new DisplayerSubType[]{  DisplayerSubType.LINE,
                                                            DisplayerSubType.SMOOTH};
            case MAP: return new DisplayerSubType[]{        DisplayerSubType.MAP_REGIONS,
                                                            DisplayerSubType.MAP_MARKERS};
            default: return new DisplayerSubType[]{};
        }
    }

    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        DisplayerType displayerType = displayerSettings.getType();
        if ( DisplayerType.BARCHART.equals(displayerType)) return new GoogleBarChartDisplayer();
        if ( DisplayerType.PIECHART.equals(displayerType)) return new GooglePieChartDisplayer();
        if ( DisplayerType.AREACHART.equals(displayerType)) return new GoogleAreaChartDisplayer();
        if ( DisplayerType.LINECHART.equals(displayerType)) return new GoogleLineChartDisplayer();
        if ( DisplayerType.BUBBLECHART.equals(displayerType)) return new GoogleBubbleChartDisplayer();
        if ( DisplayerType.METERCHART.equals(displayerType)) return new GoogleMeterChartDisplayer();
        if ( DisplayerType.TABLE.equals(displayerType)) return new GoogleTableDisplayer();
        if ( DisplayerType.MAP.equals(displayerType)) return new GoogleMapDisplayer();

        return null;
    }

    /**
     *  In Google the renderer mechanism is asynchronous.
     */
    public void draw(final List<Displayer> displayerList) {
        // Get the modules to load.
        Set<ChartPackage> packageList = new HashSet<ChartPackage>();
        for (Displayer displayer : displayerList) {
            try {
                GoogleDisplayer googleDisplayer = (GoogleDisplayer ) displayer;
                packageList.add( googleDisplayer.getPackage());
            } catch (ClassCastException e) {
                // Just ignore non Google displayers.
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
                GoogleRenderer.super.draw(displayerList);
            }
        });
    }
}
