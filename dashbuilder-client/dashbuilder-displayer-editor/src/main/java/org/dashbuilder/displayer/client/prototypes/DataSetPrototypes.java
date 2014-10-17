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

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerType;

@ApplicationScoped
public class DataSetPrototypes {

    public static final String COUNTRY = "Country";
    public static final String AREA_SIZE = "Area size (km2)";
    public static final String DENSITY = "Density (people/km2)";
    public static final String POPULATION = "Population";

    public static final DataSet CONTINENT_POPULATION = DataSetFactory
            .newDataSetBuilder()
            .label(COUNTRY)
            .label(POPULATION)
            .row("Asia", 4298723000L)
            .row("Africa", 1110635000L)
            .row("North America", 972005000L)
            .row("South America", 972005000L)
            .row("Europe", 742452000L)
            .row("Oceania", 38304000L)
            .buildDataSet();

    public static final DataSet CONTINENT_POPULATION_EXT = DataSetFactory
            .newDataSetBuilder()
            .label(COUNTRY)
            .label(AREA_SIZE)
            .label(DENSITY)
            .label(POPULATION)
            .row("Asia", 43,820,000, 95.0, "Asia", 4298723000L)
            .row("Africa", 30370000L, 33.7, "Africa", 1110635000L)
            .row("North America", 24490000L, 22.1, "North America", 972005000L)
            .row("South America", 17840000L, 22.0, "South America", 972005000L)
            .row("Europe", 10180000L, 72.5, "Europe", 742452000L)
            .row("Oceania", 9008500L, 3.2, "Oceania", 38304000L)
            .buildDataSet();

}
