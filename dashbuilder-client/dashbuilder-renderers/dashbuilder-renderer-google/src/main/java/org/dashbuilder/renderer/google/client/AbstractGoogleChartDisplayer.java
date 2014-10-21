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
package org.dashbuilder.renderer.google.client;

import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendAlignment;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import org.dashbuilder.displayer.DisplayerSettings;

/**
 * Abstract base class for all displayers that support the basic ChartDisplayerSettings configuration options
 */
public abstract class AbstractGoogleChartDisplayer extends GoogleDisplayer {

    protected Legend createChartLegend( DisplayerSettings displayerSettings ) {
        GoogleLegendWrapper legend = GoogleLegendWrapper.create();
        legend.setLegendPosition( getLegendPosition( displayerSettings ) );
        legend.setAligment( LegendAlignment.CENTER );
        return legend;
    }

    protected String getLegendPosition( DisplayerSettings displayerSettings ) {
        if ( !displayerSettings.isChartShowLegend() ) return LegendPosition.NONE.toString().toLowerCase();
        switch ( displayerSettings.getChartLegendPosition() ) {
            case TOP: return LegendPosition.TOP.toString().toLowerCase();
            case BOTTOM: return LegendPosition.BOTTOM.toString().toLowerCase();
            case RIGHT: return LegendPosition.RIGHT.toString().toLowerCase();
            case IN: return LegendPosition.IN.toString().toLowerCase();
            case LEFT: return "left";
            default: return LegendPosition.RIGHT.toString().toLowerCase();
        }
    }

}
