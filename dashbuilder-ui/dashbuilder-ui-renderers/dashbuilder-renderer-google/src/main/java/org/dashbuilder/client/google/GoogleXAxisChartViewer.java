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
package org.dashbuilder.client.google;

import java.util.ArrayList;
import java.util.List;

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
import org.dashbuilder.model.displayer.DataDisplayerColumn;
import org.dashbuilder.model.displayer.XAxisChartDisplayer;

public abstract class GoogleXAxisChartViewer<T extends XAxisChartDisplayer> extends GoogleViewer<T> {

    public static final String[] COLOR_ARRAY = new String[] {"aqua", "red", "orange", "brown", "coral", "blue", "fuchsia", "gold",
            "green", "lime", "magenta", "pink", "silver", "yellow"};

    public static final String COLOR_NOT_SELECTED = "grey";

    protected List<String> intervalSelectedList = new ArrayList<String>();

    public DataTable createTable() {
        List<DataDisplayerColumn> displayerColumns = dataDisplayer.getColumnList();
        if (displayerColumns.size() == 1) {
            throw new IllegalArgumentException("XAxis charts require to specify at least 2 columns. The X axis plus one ore more columns for the Y axis.");
        }
        return super.createTable();
    }

    public String[] createColorArray(DataTable table) {
        String[] colorArray = new String[table.getNumberOfRows()];
        for (int i = 0, j = 0; i < table.getNumberOfRows(); i++, j++) {
            if (j >= COLOR_ARRAY.length) j = 0;
            String intervalSelected = getValueString(i, 0);
            if (!intervalSelectedList.isEmpty() && !intervalSelectedList.contains(intervalSelected)) {
                colorArray[j] = COLOR_NOT_SELECTED;
            } else {
                colorArray[j] = COLOR_ARRAY[j];
            }
        }
        return colorArray;
    }

    public SelectHandler createSelectHandler(final CoreChartWidget selectable) {
        return new SelectHandler() {
            public void onSelect(SelectEvent event) {
                JsArray<Selection> selections = selectable.getSelection();
                for (int i = 0; i < selections.length(); i++) {
                    Selection selection = selections.get(i);
                    int row = selection.getRow();

                    String intervalSelected = getValueString(row, 0);
                    updateSelection(intervalSelected);
                }
                // Redraw the char in order to reflect the current selection
                updateVisualization();
            }
        };
    }

    public SelectHandler createSelectHandler(final GeoChart selectable) {
        return new SelectHandler() {
            public void onSelect(SelectEvent event) {
                JsArray<Selection> selections = selectable.getSelection();
                for (int i = 0; i < selections.length(); i++) {
                    Selection selection = selections.get(i);
                    int row = selection.getRow();

                    String intervalSelected = getValueString(row, 0);
                    updateSelection(intervalSelected);
                }
                // Redraw the char in order to reflect the current selection
                updateVisualization();
            }
        };
    }

    protected void updateSelection(String intervalSelected) {
        if (!intervalSelectedList.contains(intervalSelected)) {
            intervalSelectedList.add(intervalSelected);
            if (intervalSelectedList.size() < dataSet.getRowCount()) {
                selectGroupIntervals(googleTable.getColumnId(0), intervalSelectedList);
            } else {
                intervalSelectedList.clear();
                resetGroupIntervals(googleTable.getColumnId(0));
            }
        } else {
            intervalSelectedList.remove(intervalSelected);
            if (!intervalSelectedList.isEmpty()) {
                selectGroupIntervals(googleTable.getColumnId(0), intervalSelectedList);
            } else {
                resetGroupIntervals(googleTable.getColumnId(0));
            }
        }
    }

    protected Widget createCurrentSelectionWidget() {
        if (!intervalSelectedList.isEmpty()) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.getElement().setAttribute("cellpadding", "2");
            for (String interval : intervalSelectedList) {
                panel.add(new Label(interval));
            }
            Anchor anchor = new Anchor("reset");
            panel.add(anchor);
            anchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    intervalSelectedList.clear();
                    resetGroupIntervals(googleTable.getColumnId(0));
                    updateVisualization();
                }
            });
            return panel;
        }
        return null;
    }

    protected ChartArea createChartArea() {
        int width = dataDisplayer.getWidth();
        int height = dataDisplayer.getHeight();
        int top = dataDisplayer.getMarginTop();
        int bottom = dataDisplayer.getMarginBottom();
        int left = dataDisplayer.getMarginLeft();
        int right = dataDisplayer.getMarginRight();

        int chartWidth = width-right-left;
        int chartHeight = height-top-bottom;

        ChartArea chartArea = ChartArea.create();
        chartArea.setLeft(left);
        chartArea.setTop(top);
        chartArea.setWidth(chartWidth);
        chartArea.setHeight(chartHeight);
        return chartArea;
    }

}
