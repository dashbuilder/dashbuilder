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

import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendAlignment;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.renderer.google.client.resources.i18n.GoogleDisplayerConstants;
import org.gwtbootstrap3.client.ui.Label;

/**
 * Base class for all the Google chart like displayers
 */
public abstract class GoogleChartDisplayer extends GoogleDisplayer {

    protected Legend createChartLegend() {
        GoogleLegendWrapper legend = GoogleLegendWrapper.create();
        legend.setLegendPosition( getLegendPosition() );
        legend.setAligment( LegendAlignment.CENTER );
        return legend;
    }

    protected String getLegendPosition() {
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

    protected Widget createNoDataMsgPanel() {
        return new Label(GoogleDisplayerConstants.INSTANCE.common_noData());
    }

    protected Widget createCurrentSelectionWidget() {
        if (!displayerSettings.isFilterEnabled()) return null;

        Set<String> columnFilters = filterColumns();
        if (columnFilters.isEmpty()) return null;

        HorizontalPanel panel = new HorizontalPanel();
        panel.getElement().setAttribute("cellpadding", "2");

        for (String columnId : columnFilters) {
            List<Interval> selectedValues = filterIntervals(columnId);
            DataColumn column = dataSet.getColumnById(columnId);
            for (Interval interval : selectedValues) {
                String formattedValue = formatInterval(interval, column);
                panel.add(new Label(formattedValue));
            }
        }

        Anchor anchor = new Anchor( GoogleDisplayerConstants.INSTANCE.googleDisplayer_resetAnchor() );
        panel.add(anchor);
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                filterReset();

                // Update the chart view in order to reflect the current selection
                // (only if not has already been redrawn in the previous filterUpdate() call)
                if (!displayerSettings.isFilterSelfApplyEnabled()) {
                    updateVisualization();
                }
            }
        });
        return panel;
    }


}
