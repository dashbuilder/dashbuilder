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
package org.dashbuilder.renderer.lienzo.client;

import com.ait.lienzo.charts.client.core.xy.XYChartData;
import com.ait.lienzo.charts.client.core.xy.line.LineChart;
import com.ait.lienzo.client.core.animation.AnimationTweener;
import com.ait.lienzo.shared.core.types.ColorName;
import org.dashbuilder.renderer.lienzo.client.LienzoXYChartDisplayer;

public class LienzoLineChartDisplayer extends LienzoXYChartDisplayer<LineChart> {

    private static final ColorName[] DEFAULT_SERIE_COLORS = new ColorName[] {
            ColorName.DEEPSKYBLUE, ColorName.RED, ColorName.YELLOWGREEN            
    };
    
    @Override
    public LineChart createChart() {
        // Create the LineChart instance.
        return new LineChart();
    }

    @Override
    public void reloadChart(XYChartData newData) {
        chart.reload(newData, AnimationTweener.LINEAR, ANIMATION_DURATION);
    }

    }
