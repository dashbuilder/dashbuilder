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
package org.dashbuilder.model.displayer;

import org.dashbuilder.model.displayer.impl.BarChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.MeterChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.XAxisChartBuilderImpl;

/**
 * Factory class for building DataDisplayer instances.
 */
public final class DisplayerFactory {

    public static BarChartBuilder newBarChartDisplayer() {
        return new BarChartBuilderImpl();
    }

    public static ChartBuilder newPieChartDisplayer() {
        return new XAxisChartBuilderImpl();
    }

    public static ChartBuilder newAreaChartDisplayer() {
        return new XAxisChartBuilderImpl();
    }

    public static ChartBuilder newLineChartDisplayer() {
        return new XAxisChartBuilderImpl();
    }

    public static ChartBuilder newMapChartDisplayer() {
        return new XAxisChartBuilderImpl();
    }

    public static ChartBuilder newTableDisplayer() {
        return new XAxisChartBuilderImpl();
    }

    public static ChartBuilder newMeterChartDisplayer() {
        return new MeterChartBuilderImpl();
    }

}
