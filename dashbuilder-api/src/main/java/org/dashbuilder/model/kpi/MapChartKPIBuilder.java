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
package org.dashbuilder.model.kpi;

import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.dashbuilder.model.displayer.MapChartBuilder;
import org.dashbuilder.model.displayer.XAxisChartBuilder;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A KPI builder for maps
 *
 * <pre>
 new MapChartKPIBuilder()
 .dataset("target-dataset-uuid")
 .group(COUNTRY)
 .sum(AMOUNT)
 .title("By Country")
 .width(700).height(500)
 .column("Country")
 .column("Total amount")
 .build()
 </pre>
 */
@Portable
public class MapChartKPIBuilder extends AbstractChartKPIBuilder<MapChartKPIBuilder> {

    protected DataDisplayerBuilder createDisplayerBuilder() {
        return new MapChartBuilder();
    }

    public MapChartKPIBuilder() {
        super();
        displayerBuilder.type(DataDisplayerType.MAP);
    }
}
