/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static Map<DisplayerType,ChartPackage> _packageTypes = new HashMap<DisplayerType,ChartPackage>();
    static {
        _packageTypes.put(BARCHART, ChartPackage.CORECHART);
        _packageTypes.put(PIECHART, ChartPackage.CORECHART);
        _packageTypes.put(AREACHART, ChartPackage.CORECHART);
        _packageTypes.put(LINECHART, ChartPackage.CORECHART);
        _packageTypes.put(BUBBLECHART, ChartPackage.CORECHART);
        _packageTypes.put(METERCHART, ChartPackage.GAUGE);
        _packageTypes.put(TABLE, ChartPackage.TABLE);
        _packageTypes.put(MAP, ChartPackage.GEOCHART);
    }

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
        return BARCHART.equals(type) ||
                PIECHART.equals(type) ||
                AREACHART.equals(type) ||
                LINECHART.equals(type) ||
                BUBBLECHART.equals(type) ||
                METERCHART.equals(type) ||
                MAP.equals(type);
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
        if (BARCHART.equals(displayerType)) {
            GoogleBarChartDisplayer displayer = new GoogleBarChartDisplayer();
            ((GoogleBarChartDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }
        if (PIECHART.equals(displayerType)) {
            GooglePieChartDisplayer displayer = new GooglePieChartDisplayer();
            ((GooglePieChartDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }
        if (AREACHART.equals(displayerType)) {
            GoogleAreaChartDisplayer displayer = new GoogleAreaChartDisplayer();
            ((GoogleAreaChartDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }
        if (LINECHART.equals(displayerType)) {
            GoogleLineChartDisplayer displayer = new GoogleLineChartDisplayer();
            ((GoogleLineChartDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }
        if (BUBBLECHART.equals(displayerType)) {
            GoogleBubbleChartDisplayer displayer = new GoogleBubbleChartDisplayer();
            ((GoogleBubbleChartDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }
        if (METERCHART.equals(displayerType)) {
            GoogleMeterChartDisplayer displayer = new GoogleMeterChartDisplayer();
            ((GoogleMeterChartDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }
        if (TABLE.equals(displayerType)) {
            GoogleTableDisplayer displayer = new GoogleTableDisplayer();
            ((GoogleTableDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }
        if (MAP.equals(displayerType)) {
            GoogleMapDisplayer displayer = new GoogleMapDisplayer();
            ((GoogleMapDisplayerView) displayer.getView()).setRenderer(this);
            return displayer;
        }

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
                GoogleDisplayer googleDisplayer = (GoogleDisplayer) displayer;
                packageList.add(_packageTypes.get(googleDisplayer.getDisplayerSettings().getType()));
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
                for (Displayer displayer : displayerList) {
                    try {
                        GoogleDisplayer googleDisplayer = (GoogleDisplayer) displayer;
                        googleDisplayer.ready();
                    } catch (ClassCastException e) {
                        // Just ignore non Google displayers.
                    }
                }
            }
        });
    }
}
