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

import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.model.displayer.AreaChartBuilder;
import org.dashbuilder.model.displayer.BubbleChartBuilder;
import org.dashbuilder.model.displayer.LineChartBuilder;
import org.dashbuilder.model.displayer.MapChartBuilder;
import org.dashbuilder.model.displayer.MeterChartBuilder;
import org.dashbuilder.model.displayer.BarChartBuilder;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.PieChartBuilder;
import org.dashbuilder.model.displayer.TableDisplayerBuilder;

/**
 * Builder interface for building KPI instances in a friendly manner.
 */
public interface KPIBuilder extends
        DataSetLookupBuilder<KPIBuilder>,
        PieChartBuilder<KPIBuilder>,
        BarChartBuilder<KPIBuilder>,
        AreaChartBuilder<KPIBuilder>,
        LineChartBuilder<KPIBuilder>,
        BubbleChartBuilder<KPIBuilder>,
        MapChartBuilder<KPIBuilder>,
        TableDisplayerBuilder<KPIBuilder>,
        MeterChartBuilder<KPIBuilder> {

    /**
     * The UUID of the KPI
     */
    KPIBuilder uuid(String uuid);

    /**
     * A direct reference to the source data set.
     */
    KPIBuilder dataset(DataSetRef dataSetRef);

    /**
     * A direct reference to the data displayer.
     */
    KPIBuilder displayer(DataDisplayer dataDisplayer);

    /**
     * Build and get the KPI instance.
     */
    KPI buildKPI();

}
