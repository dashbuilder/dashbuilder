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
package org.dashbuilder.displayer.client.prototypes;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerType;

import static org.dashbuilder.displayer.client.prototypes.DataSetPrototypes.*;

@ApplicationScoped
public class DisplayerPrototypes {

    private Map<DisplayerType,DisplayerSettings> prototypeMap = new HashMap<DisplayerType,DisplayerSettings>();


    public static final DisplayerSettings BAR_CHART_PROTO = DisplayerSettingsFactory
            .newBarChartSettings()
            .uuid("barChartPrototype")
            .dataset(CONTINENT_POPULATION)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .width(500).height(300)
            .margins(10, 20, 70, 10)
            .horizontal().set3d(false)
            .filterOff(false)
            .buildSettings();

    public static final DisplayerSettings PIE_CHART_PROTO = DisplayerSettingsFactory
            .newPieChartSettings()
            .uuid("pieChartPrototype")
            .dataset(CONTINENT_POPULATION)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .width(400).height(300)
            .margins(10, 10, 10, 10)
            .set3d(false)
            .filterOff(false)
            .buildSettings();

    public static final DisplayerSettings LINE_CHART_PROTO = DisplayerSettingsFactory
            .newLineChartSettings()
            .uuid("lineChartPrototype")
            .dataset(CONTINENT_POPULATION)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .width(400).height(300)
            .margins(10, 10, 10, 10)
            .filterOff(false)
            .buildSettings();

    public static final DisplayerSettings AREA_CHART_PROTO = DisplayerSettingsFactory
            .newAreaChartSettings()
            .uuid("areaChartPrototype")
            .dataset(CONTINENT_POPULATION)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .width(400).height(300)
            .margins(10, 10, 10, 10)
            .filterOff(false)
            .buildSettings();

    public static final DisplayerSettings BUBBLE_CHART_PROTO = DisplayerSettingsFactory
            .newBubbleChartSettings()
            .uuid("bubbleChartPrototype")
            .dataset(CONTINENT_POPULATION_EXT)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .width(400).height(300)
            .margins(10, 10, 10, 10)
            .filterOff(false)
            .buildSettings();

    public static final DisplayerSettings METER_CHART_PROTO = DisplayerSettingsFactory
            .newMeterChartSettings()
            .uuid("meterChartPrototype")
            .dataset(CONTINENT_POPULATION)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .width(100).height(100)
            .margins(10, 10, 10, 10)
            .meter(0, 500000000L, 1000000000L, 6000000000L)
            .filterOff(false)
            .buildSettings();

    public static final DisplayerSettings MAP_CHART_PROTO = DisplayerSettingsFactory
            .newMapChartSettings()
            .uuid("mapChartPrototype")
            .dataset(CONTINENT_POPULATION)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .width(400).height(300)
            .margins(10, 10, 10, 10)
            .filterOff(false)
            .buildSettings();

    public static final DisplayerSettings TABLE_PROTO = DisplayerSettingsFactory
            .newTableSettings()
            .uuid("tablePrototype")
            .dataset(CONTINENT_POPULATION)
            .title("Population per Continent (2013)")
            .titleVisible(true)
            .tableOrderEnabled(true)
            .tableOrderDefault(POPULATION, SortOrder.DESCENDING)
            .tableWidth(400)
            .tablePageSize(10)
            .filterOff(false)
            .buildSettings();

    @PostConstruct
    private void init() {
        prototypeMap.put(DisplayerType.BARCHART, BAR_CHART_PROTO);
        prototypeMap.put(DisplayerType.PIECHART, PIE_CHART_PROTO);
        prototypeMap.put(DisplayerType.LINECHART, LINE_CHART_PROTO);
        prototypeMap.put(DisplayerType.AREACHART, AREA_CHART_PROTO);
        prototypeMap.put(DisplayerType.BUBBLECHART, BUBBLE_CHART_PROTO);
        prototypeMap.put(DisplayerType.METERCHART, METER_CHART_PROTO);
        prototypeMap.put(DisplayerType.MAP, MAP_CHART_PROTO);
        prototypeMap.put(DisplayerType.TABLE, TABLE_PROTO);
    }

    public DisplayerSettings get(DisplayerType type   ) {
        return prototypeMap.get(type);
    }
}
