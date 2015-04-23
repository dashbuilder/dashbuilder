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
package org.dashbuilder.renderer.chartjs;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.RendererLibLocator;
import org.dashbuilder.renderer.chartjs.lib.ChartJs;

/**
 * Chart JS renderer
 *
 * <p>Pending stuff:
 * <ul>
 *     <li>Values format</li>
 * </ul>
 * </p>
 */
@ApplicationScoped
@Named(ChartJsRenderer.UUID + "_renderer")
public class ChartJsRenderer extends AbstractRendererLibrary {

    public static final String UUID = "Chart JS";

    @PostConstruct
    private void init() {
        RendererLibLocator.get().registerRenderer(DisplayerType.BARCHART, UUID, false);
        publishChartJsFunctions();
    }

    @Override
    public String getUUID() {
        return UUID;
    }

    @Override
    public DisplayerSubType[] getSupportedSubtypes(DisplayerType displayerType) {
        switch (displayerType) {
            case BARCHART:
                return new DisplayerSubType[] {
                        DisplayerSubType.COLUMN
                };
            case PIECHART:
                return new DisplayerSubType[] {
                        DisplayerSubType.PIE,
                        DisplayerSubType.DONUT
                };
            case AREACHART:
                return new DisplayerSubType[] {
                        DisplayerSubType.AREA
                };
            case LINECHART:
                return new DisplayerSubType[] {
                        DisplayerSubType.LINE
                };
            default:
                return new DisplayerSubType[] {};
        }
    }

    @Override
    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        ChartJsDisplayer displayer = _lookupDisplayer(displayerSettings);
        if (displayer != null) {
            _displayerMap.put(displayerSettings.getUUID(), displayer);
        }
        return displayer;
    }

    protected ChartJsDisplayer _lookupDisplayer(DisplayerSettings displayerSettings) {
        ChartJs.ensureInjected();
        DisplayerType type = displayerSettings.getType();
        if ( DisplayerType.BARCHART.equals(type)) return new ChartJsBarChartDisplayer();
        return null;
    }

    private native void publishChartJsFunctions() /*-{
        $wnd.chartJsFormatValue = $entry(@org.dashbuilder.renderer.chartjs.ChartJsRenderer::formatValue(Ljava/lang/String;DI));
    }-*/;

    protected static Map<String,ChartJsDisplayer> _displayerMap = new HashMap<String, ChartJsDisplayer>();

    public static String formatValue(String displayerId, double value, int column) {
        ChartJsDisplayer displayer = _displayerMap.get(displayerId);
        if (displayer == null) return Double.toString(value);

        DataColumn dataColumn = displayer.getDataSetHandler().getLastDataSet().getColumnByIndex(column);
        return displayer.formatValue(value, dataColumn);
    }

    public static void closeDisplayer(ChartJsDisplayer displayer) {
        _displayerMap.remove(displayer.getDisplayerId());
    }
}
