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

import org.dashbuilder.model.displayer.impl.AreaChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.BarChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.BubbleChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.LineChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.MapChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.MeterChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.PieChartBuilderImpl;
import org.dashbuilder.model.displayer.impl.TableDisplayerBuilderImpl;

/**
 * Factory class for creating  DataDisplayerBuilder instances.
 */
public final class DisplayerFactory {

    public static BarChartBuilder<BarChartBuilderImpl> newBarChart() {
        return new BarChartBuilderImpl();
    }

    public static PieChartBuilder<PieChartBuilderImpl> newPieChart() {
        return new PieChartBuilderImpl();
    }

    public static AreaChartBuilder<AreaChartBuilderImpl> newAreaChart() {
        return new AreaChartBuilderImpl();
    }

    public static LineChartBuilder<LineChartBuilderImpl> newLineChart() {
        return new LineChartBuilderImpl();
    }

    public static BubbleChartBuilder<BubbleChartBuilderImpl> newBubbleChart() {
        return new BubbleChartBuilderImpl();
    }

    public static MapChartBuilder<MapChartBuilderImpl> newMapChart() {
        return new MapChartBuilderImpl();
    }

    public static TableDisplayerBuilder<TableDisplayerBuilderImpl> newTable() {
        return new TableDisplayerBuilderImpl();
    }

    public static MeterChartBuilder<MeterChartBuilderImpl> newMeterChart() {
        return new MeterChartBuilderImpl();
    }

}
