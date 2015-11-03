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

import java.util.EnumMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.dataset.uuid.UUIDGenerator;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerType;

import static org.dashbuilder.displayer.client.prototypes.DataSetPrototypes.*;

@ApplicationScoped
public class DisplayerPrototypes {

    protected DataSetPrototypes dataSetPrototypes;

    protected UUIDGenerator uuidGenerator;

    protected Map<DisplayerType,DisplayerSettings> prototypeMap = new EnumMap<DisplayerType,DisplayerSettings>(DisplayerType.class);

    @Inject
    public DisplayerPrototypes(DataSetPrototypes dataSetPrototypes, UUIDGenerator uuidGenerator) {
        this.dataSetPrototypes = dataSetPrototypes;
        this.uuidGenerator = uuidGenerator;
        init();
    }

    public void init() {
        prototypeMap.put(DisplayerType.PIECHART, DisplayerSettingsFactory
                .newPieChartSettings()
                .uuid("pieChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .column(POPULATION)
                .expression("value/1000000")
                .format("Population", "#,### M")
                .width(500).height(300)
                .margins(10, 10, 10, 100)
                .legendOn("right")
                .set3d(true)
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.BARCHART, DisplayerSettingsFactory
                .newBarChartSettings()
                .subType_Bar()
                .uuid("barChartPrototype")
                .dataset(dataSetPrototypes.getTopRichCountries())
                .title("Top Rich Countries")
                .titleVisible(false)
                .column(COUNTRY).format("Country")
                .column(GDP_2013).format("2013", "$ #,### M")
                .column(GDP_2014).format("2014", "$ #,### M")
                .width(500).height(250)
                .margins(10, 40, 100, 50)
                .legendOn("right")
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.LINECHART, DisplayerSettingsFactory
                .newLineChartSettings()
                .uuid("lineChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .column(POPULATION)
                .expression("value/1000000")
                .format("Population", "#,### M")
                .width(500).height(300)
                .margins(10, 40, 90, 10)
                .legendOff()
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.AREACHART, DisplayerSettingsFactory
                .newAreaChartSettings()
                .uuid("areaChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .column(POPULATION)
                .expression("value/1000000")
                .format("Population", "#,### M")
                .width(500).height(300)
                .margins(10, 40, 90, 10)
                .legendOff()
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.BUBBLECHART, DisplayerSettingsFactory
                .newBubbleChartSettings()
                .uuid("bubbleChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulationExt())
                .title("Population per Continent")
                .titleVisible(false)
                .width(500).height(300)
                .margins(10, 30, 50, 10)
                .legendOff()
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.METERCHART, DisplayerSettingsFactory
                .newMeterChartSettings()
                .uuid("meterChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .width(400).height(300)
                .column(POPULATION)
                .expression("value/1000000")
                .format("Population", "#,### M")
                .margins(10, 10, 10, 10)
                .meter(0, 1000L, 3000L, 6000L)
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.METRIC, DisplayerSettingsFactory
                .newMetricSettings()
                .uuid("metricPrototype")
                .dataset(dataSetPrototypes.getTotalPopulation())
                .title("World population")
                .titleVisible(true)
                .width(300).height(150)
                .margins(30, 30, 30, 30)
                .backgroundColor("B5F2D3")
                .filterOn(false, false, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.MAP, DisplayerSettingsFactory
                .newMapChartSettings()
                .uuid("mapChartPrototype")
                .dataset(dataSetPrototypes.getCountryPopulation())
                .title("World Population")
                .titleVisible(false)
                .width(500).height(300)
                .margins(10, 10, 10, 10)
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.TABLE, DisplayerSettingsFactory
                .newTableSettings()
                .uuid("tablePrototype")
                .dataset(dataSetPrototypes.getWorldPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .column(POPULATION)
                .expression("value/1000000")
                .format("Population", "#,### M")
                .tableOrderEnabled(true)
                .tableOrderDefault(POPULATION, SortOrder.DESCENDING)
                .tablePageSize(8)
                .filterOn(false, true, true)
                .buildSettings());
    }

    public DisplayerSettings getProto(DisplayerType type) {
        DisplayerSettings proto = prototypeMap.get(type).cloneInstance();
        proto.setUUID(uuidGenerator.newUuid());
        return proto;
    }
}
