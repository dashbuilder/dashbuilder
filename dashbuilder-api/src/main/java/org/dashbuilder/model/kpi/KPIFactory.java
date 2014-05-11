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

import org.dashbuilder.model.displayer.DataDisplayerType;
import org.dashbuilder.model.kpi.impl.KPIBuilderImpl;

/**
 * Factory class for building KPI instances in a friendly manner.
 */
public final class KPIFactory {

    public static KPIBuilder newBarChartKPI() {
        return new KPIBuilderImpl(DataDisplayerType.BARCHART);
    }

    public static KPIBuilder newPieChartKPI() {
        return new KPIBuilderImpl(DataDisplayerType.PIECHART);
    }

    public static KPIBuilder newLineChartKPI() {
        return new KPIBuilderImpl(DataDisplayerType.LINECHART);
    }

    public static KPIBuilder newAreaChartKPI() {
        return new KPIBuilderImpl(DataDisplayerType.AREACHART);
    }

    public static KPIBuilder newMapChartKPI() {
        return new KPIBuilderImpl(DataDisplayerType.MAP);
    }

    public static KPIBuilder newTableKPI() {
        return new KPIBuilderImpl(DataDisplayerType.TABLE);
    }

    public static KPIBuilder newMeterChartKPI() {
        return new KPIBuilderImpl(DataDisplayerType.METERCHART);
    }

}
