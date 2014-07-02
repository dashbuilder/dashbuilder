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
package org.dashbuilder.kpi;

import org.dashbuilder.dataset.DataSetLookupBuilder;
import org.dashbuilder.dataset.DataSetRef;
import org.dashbuilder.displayer.AreaChartBuilder;
import org.dashbuilder.displayer.BubbleChartBuilder;
import org.dashbuilder.displayer.LineChartBuilder;
import org.dashbuilder.displayer.MapChartBuilder;
import org.dashbuilder.displayer.MeterChartBuilder;
import org.dashbuilder.displayer.BarChartBuilder;
import org.dashbuilder.displayer.DataDisplayer;
import org.dashbuilder.displayer.PieChartBuilder;
import org.dashbuilder.displayer.TableDisplayerBuilder;

/**
 * Builder interface for building KPI instances in a friendly manner.
 *
 * @see org.dashbuilder.dataset.DataSetLookupBuilder
 * @see org.dashbuilder.displayer.PieChartBuilder
 * @see org.dashbuilder.displayer.BarChartBuilder
 * @see org.dashbuilder.displayer.AreaChartBuilder
 * @see org.dashbuilder.displayer.LineChartBuilder
 * @see org.dashbuilder.displayer.BarChartBuilder
 * @see org.dashbuilder.displayer.MapChartBuilder
 * @see org.dashbuilder.displayer.TableDisplayerBuilder
 * @see org.dashbuilder.displayer.MapChartBuilder
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
     * Set the KPI's UUID.
     *
     * @param uuid The UUID of the KPI that is being assembled.
     * @return The KPIBuilder instance that this method was invoked upon.
     */
    KPIBuilder uuid(String uuid);

    /**
     * Set a direct reference to the source data set that will be used by the KPI that is being assembled.
     *
     * @param dataSetRef The reference to the DataSet.
     * @return The KPIBuilder instance that this method was invoked upon.
     * @see org.dashbuilder.dataset.DataSetRef
     */
    KPIBuilder dataset(DataSetRef dataSetRef);

    /**
     * Set a direct reference to the data displayer set that will be used by the KPI that is being assembled.
     *
     * @param dataDisplayer The reference to the DataDisplayer.
     * @return The KPIBuilder instance that this method was invoked upon.
     * @see org.dashbuilder.displayer.DataDisplayer
     */
    KPIBuilder displayer(DataDisplayer dataDisplayer);

    /**
     * Build and return the KPI instance.
     *
     * @return The KPI instance that was built according to the provided configuration.
     * @see org.dashbuilder.kpi.KPI
     */
    KPI buildKPI();

}
