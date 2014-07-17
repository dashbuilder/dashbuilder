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
import org.dashbuilder.displayer.AreaChartSettingsBuilder;
import org.dashbuilder.displayer.BarChartSettingsBuilder;
import org.dashbuilder.displayer.BubbleChartSettingsBuilder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.LineChartSettingsBuilder;
import org.dashbuilder.displayer.MapChartSettingsBuilder;
import org.dashbuilder.displayer.MeterChartSettingsBuilder;
import org.dashbuilder.displayer.PieChartSettingsBuilder;
import org.dashbuilder.displayer.TableDisplayerSettingsBuilder;

/**
 * Builder interface for building KPI instances in a friendly manner.
 *
 * @see org.dashbuilder.dataset.DataSetLookupBuilder
 * @see PieChartSettingsBuilder
 * @see BarChartSettingsBuilder
 * @see AreaChartSettingsBuilder
 * @see LineChartSettingsBuilder
 * @see BarChartSettingsBuilder
 * @see MapChartSettingsBuilder
 * @see TableDisplayerSettingsBuilder
 * @see MapChartSettingsBuilder
 */
public interface KPIBuilder extends
        DataSetLookupBuilder<KPIBuilder>,
        PieChartSettingsBuilder<KPIBuilder>,
        BarChartSettingsBuilder<KPIBuilder>,
        AreaChartSettingsBuilder<KPIBuilder>,
        LineChartSettingsBuilder<KPIBuilder>,
        BubbleChartSettingsBuilder<KPIBuilder>,
        MapChartSettingsBuilder<KPIBuilder>,
        TableDisplayerSettingsBuilder<KPIBuilder>,
        MeterChartSettingsBuilder<KPIBuilder> {

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
     * @param displayerSettings The reference to the DisplayerSettings.
     * @return The KPIBuilder instance that this method was invoked upon.
     * @see DisplayerSettings
     */
    KPIBuilder displayer(DisplayerSettings displayerSettings );

    /**
     * Build and return the KPI instance.
     *
     * @return The KPI instance that was built according to the provided configuration.
     * @see org.dashbuilder.kpi.KPI
     */
    KPI buildKPI();

}
