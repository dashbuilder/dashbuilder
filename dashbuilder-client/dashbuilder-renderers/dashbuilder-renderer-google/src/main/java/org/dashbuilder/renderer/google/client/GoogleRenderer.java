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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.Displayer;

import static org.dashbuilder.displayer.DisplayerType.*;
import static org.dashbuilder.displayer.DisplayerSubType.*;

import java.util.EnumSet;

/**
 * Google's Visualization API based renderer.
 */
@ApplicationScoped
public class GoogleRenderer extends AbstractRendererLibrary {

    public static final String UUID = "gwtcharts";

    private List<DisplayerType> _supportedTypes = Arrays.asList(
            BARCHART,
            PIECHART,
            AREACHART,
            LINECHART,
            BUBBLECHART,
            METERCHART,
            TABLE,
            MAP);

    public String getUUID() {
        return UUID;
    }

    @Override
    public String getName() {
        return "GWT Charts";
    }

    @Override
    public boolean isDefault(DisplayerType type) {
        return _supportedTypes.contains(type);
    }

    @Override
    public List<DisplayerType> getSupportedTypes() {
        return _supportedTypes;
    }
    
    @Override
    public List<DisplayerSubType> getSupportedSubtypes(DisplayerType displayerType) {
        switch (displayerType) {
            case BARCHART: 
                return Arrays.asList(BAR, BAR_STACKED, COLUMN, COLUMN_STACKED);
            case PIECHART: 
                return Arrays.asList(PIE, PIE_3D, DONUT);
            case AREACHART: 
                return Arrays.asList(AREA, AREA_STACKED /*,STEPPED*/);
            case LINECHART: 
                return Arrays.asList(LINE, SMOOTH);
            case MAP: 
                return Arrays.asList(MAP_REGIONS, MAP_MARKERS);
            default: 
                return Arrays.asList();
        }
    }

    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        DisplayerType displayerType = displayerSettings.getType();
        if (BARCHART.equals(displayerType)) return new GoogleBarChartDisplayer();
        if (PIECHART.equals(displayerType)) return new GooglePieChartDisplayer();
        if (AREACHART.equals(displayerType)) return new GoogleAreaChartDisplayer();
        if (LINECHART.equals(displayerType)) return new GoogleLineChartDisplayer();
        if (BUBBLECHART.equals(displayerType)) return new GoogleBubbleChartDisplayer();
        if (METERCHART.equals(displayerType)) return new GoogleMeterChartDisplayer();
        if (TABLE.equals(displayerType)) return new GoogleTableDisplayer();
        if (MAP.equals(displayerType)) return new GoogleMapDisplayer();

        return null;
    }

    /**
     *  In Google the renderer mechanism is asynchronous.
     */
    public void draw(final List<Displayer> displayerList) {
        // Get the modules to load.
        Set<ChartPackage> packageList = EnumSet.noneOf(ChartPackage.class);
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
