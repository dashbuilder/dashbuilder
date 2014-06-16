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

import com.google.gwt.core.client.JsArray;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.Selection;
import com.googlecode.gwt.charts.client.corechart.CoreChartWidget;
import com.googlecode.gwt.charts.client.event.SelectEvent;
import com.googlecode.gwt.charts.client.event.SelectHandler;
import com.googlecode.gwt.charts.client.geochart.GeoChart;
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
}
