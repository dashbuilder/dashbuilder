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

import org.dashbuilder.dataset.DataSetRef;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.kpi.impl.KPIBuilderImpl;
import org.dashbuilder.kpi.impl.KPIImpl;

/**
 * Factory class for building KPI instances in a friendly manner.
 */
public final class KPIFactory {

    public static KPI newKPI(String uuid, DataSetRef dataRef, DisplayerSettings displayerSettings) {
        KPIImpl kpi = new KPIImpl();
        kpi.setUUID(uuid);
        kpi.setDataSetRef(dataRef);
        kpi.setDisplayerSettings( displayerSettings );
        return kpi;
    }

    public static KPIBuilder newBarChartKPI() {
        return new KPIBuilderImpl( DisplayerType.BARCHART);
    }

    public static KPIBuilder newPieChartKPI() {
        return new KPIBuilderImpl( DisplayerType.PIECHART);
    }

    public static KPIBuilder newLineChartKPI() {
        return new KPIBuilderImpl( DisplayerType.LINECHART);
    }

    public static KPIBuilder newAreaChartKPI() {
        return new KPIBuilderImpl( DisplayerType.AREACHART);
    }

    public static KPIBuilder newBubbleChartKPI() {
        return new KPIBuilderImpl( DisplayerType.BUBBLECHART);
    }

    public static KPIBuilder newMapChartKPI() {
        return new KPIBuilderImpl( DisplayerType.MAP);
    }

    public static KPIBuilder newTableKPI() {
        return new KPIBuilderImpl( DisplayerType.TABLE);
    }

    public static KPIBuilder newMeterChartKPI() {
        return new KPIBuilderImpl( DisplayerType.METERCHART);
    }

}
