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

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.Selection;
import com.googlecode.gwt.charts.client.corechart.CoreChartWidget;
import com.googlecode.gwt.charts.client.event.SelectEvent;
import com.googlecode.gwt.charts.client.event.SelectHandler;
import com.googlecode.gwt.charts.client.geochart.GeoChart;
import com.googlecode.gwt.charts.client.options.ChartArea;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.VAxis;
import org.dashbuilder.renderer.google.client.resources.i18n.GoogleDisplayerConstants;

public abstract class GoogleXAxisChartDisplayer extends AbstractGoogleChartDisplayer {

    public static final String[] COLOR_ARRAY = new String[] {"aqua", "red", "orange", "brown", "coral", "blue", "fuchsia", "gold",
            "green", "lime", "magenta", "pink", "silver", "yellow"};

    public static final String COLOR_NOT_SELECTED = "grey";

    public String[] createColorArray(DataTable table) {
        String[] colorArray = new String[table.getNumberOfRows()];
        for (int i = 0, j = 0; i < table.getNumberOfRows(); i++, j++) {
            if (j >= COLOR_ARRAY.length) j = 0;
            String intervalSelected = getValueString(i, 0);
            List<String> selectedIntervals = filterValues(googleTable.getColumnId(0));
            if (selectedIntervals != null && !selectedIntervals.isEmpty() && !selectedIntervals.contains(intervalSelected)) {
                colorArray[i] = COLOR_NOT_SELECTED;
            } else {
                colorArray[i] = COLOR_ARRAY[j];
            }
        }
        return colorArray;
    }

    public SelectHandler createSelectHandler(final CoreChartWidget selectable) {
        return new SelectHandler() {
            public void onSelect(SelectEvent event) {
                if (!displayerSettings.isFilterEnabled()) return;

                JsArray<Selection> selections = selectable.getSelection();
                for (int i = 0; i < selections.length(); i++) {
                    Selection selection = selections.get(i);
                    int row = selection.getRow();

                    String intervalSelected = getValueString(row, 0);
                    filterUpdate(googleTable.getColumnId(0), intervalSelected, googleTable.getNumberOfRows());
                }
                // Redraw the char in order to reflect the current selection
                updateVisualization();
            }
        };
    }

    public SelectHandler createSelectHandler(final GeoChart selectable) {
        return new SelectHandler() {
            public void onSelect(SelectEvent event) {
                if (!displayerSettings.isFilterEnabled()) return;

                JsArray<Selection> selections = selectable.getSelection();
                for (int i = 0; i < selections.length(); i++) {
                    Selection selection = selections.get(i);
                    int row = selection.getRow();

                    String intervalSelected = getValueString(row, 0);
                    filterUpdate(googleTable.getColumnId(0), intervalSelected, googleTable.getNumberOfRows());
                }
                // Redraw the char in order to reflect the current selection
                updateVisualization();
            }
        };
    }

    protected Widget createCurrentSelectionWidget() {
        if (!displayerSettings.isFilterEnabled()) return null;

        Set<String> columnFilters = filterColumns();
        if (columnFilters.isEmpty()) return null;

        HorizontalPanel panel = new HorizontalPanel();
        panel.getElement().setAttribute("cellpadding", "2");

        for (String columnId : columnFilters) {
            List<String> selectedValues = filterValues(columnId);
            for (String interval : selectedValues) {
                panel.add(new Label(interval));
            }
        }

        Anchor anchor = new Anchor( GoogleDisplayerConstants.INSTANCE.googleDisplayer_resetAnchor() );
        panel.add(anchor);
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                filterReset();
                updateVisualization();
            }
        });
        return panel;
    }

    protected ChartArea createChartArea() {
        int width = displayerSettings.getChartWidth();
        int height = displayerSettings.getChartHeight();
        int top = displayerSettings.getChartMarginTop();
        int bottom = displayerSettings.getChartMarginBottom();
        int left = displayerSettings.getChartMarginLeft();
        int right = displayerSettings.getChartMarginRight();

        int chartWidth = width-right-left;
        int chartHeight = height-top-bottom;

        ChartArea chartArea = ChartArea.create();
        chartArea.setLeft(left);
        chartArea.setTop(top);
        chartArea.setWidth( chartWidth );
        chartArea.setHeight( chartHeight );
        return chartArea;
    }

    protected HAxis createHAxis() {
        HAxis hAxis = HAxis.create( displayerSettings.getXAxisTitle() );
        hAxis.setSlantedText( false );
        return hAxis;
    }

    protected VAxis createVAxis() {
        return VAxis.create( displayerSettings.getYAxisTitle() );
    }
}
