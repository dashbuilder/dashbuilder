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
package org.dashbuilder.displayer;

import org.dashbuilder.displayer.impl.AreaChartSettingsBuilderImpl;
import org.dashbuilder.displayer.impl.BarChartSettingsBuilderImpl;
import org.dashbuilder.displayer.impl.BubbleChartSettingsBuilderImpl;
import org.dashbuilder.displayer.impl.LineChartSettingsBuilderImpl;
import org.dashbuilder.displayer.impl.MapChartSettingsBuilderImpl;
import org.dashbuilder.displayer.impl.MeterChartSettingsBuilderImpl;
import org.dashbuilder.displayer.impl.PieChartSettingsBuilderImpl;
import org.dashbuilder.displayer.impl.TableDisplayerSettingsBuilderImpl;

/**
 * Factory class for creating  DisplayerSettingsBuilder instances.
 */
public final class DisplayerFactory {

    public static BarChartSettingsBuilder<BarChartSettingsBuilderImpl> newBarChart() {
        return new BarChartSettingsBuilderImpl();
    }

    public static PieChartSettingsBuilder<PieChartSettingsBuilderImpl> newPieChart() {
        return new PieChartSettingsBuilderImpl();
    }

    public static AreaChartSettingsBuilder<AreaChartSettingsBuilderImpl> newAreaChart() {
        return new AreaChartSettingsBuilderImpl();
    }

    public static LineChartSettingsBuilder<LineChartSettingsBuilderImpl> newLineChart() {
        return new LineChartSettingsBuilderImpl();
    }

    public static BubbleChartSettingsBuilder<BubbleChartSettingsBuilderImpl> newBubbleChart() {
        return new BubbleChartSettingsBuilderImpl();
    }

    public static MapChartSettingsBuilder<MapChartSettingsBuilderImpl> newMapChart() {
        return new MapChartSettingsBuilderImpl();
    }

    public static TableDisplayerSettingsBuilder<TableDisplayerSettingsBuilderImpl> newTable() {
        return new TableDisplayerSettingsBuilderImpl();
    }

    public static MeterChartSettingsBuilder<MeterChartSettingsBuilderImpl> newMeterChart() {
        return new MeterChartSettingsBuilderImpl();
    }

}
