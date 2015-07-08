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

import com.google.gwt.core.client.JsArray;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.Selection;
import com.googlecode.gwt.charts.client.corechart.CoreChartWidget;
import com.googlecode.gwt.charts.client.event.SelectEvent;
import com.googlecode.gwt.charts.client.event.SelectHandler;
import com.googlecode.gwt.charts.client.options.ChartArea;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.VAxis;
import org.dashbuilder.renderer.google.client.resources.i18n.GoogleDisplayerConstants;

public abstract class GoogleCategoriesDisplayer extends GoogleChartDisplayer {

    public static final String[] COLOR_ARRAY = new String[] {
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_blue(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_red(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_orange(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_brown(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_coral(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_aqua(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_fuchsia(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_gold(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_green(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_lime(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_magenta(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_pink(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_silver(),
            GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_yellow() };

    public static final String COLOR_NOT_SELECTED = GoogleDisplayerConstants.INSTANCE.googleCategoriesDisplayer_color_grey();

    public String[] createColorArray(DataTable table) {
        String[] colorArray = new String[table.getNumberOfRows()];
        for (int i = 0, j = 0; i < table.getNumberOfRows(); i++, j++) {
            if (j >= COLOR_ARRAY.length) j = 0;
            colorArray[i] = COLOR_ARRAY[j];

            List<Integer> selectedIdxs = filterIndexes(googleTable.getColumnId(0));
            if (!displayerSettings.isFilterSelfApplyEnabled()
                    && selectedIdxs != null
                    && !selectedIdxs.isEmpty() && !selectedIdxs.contains(i)) {

                colorArray[i] = COLOR_NOT_SELECTED;
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

                    Integer maxSelections = displayerSettings.isFilterSelfApplyEnabled() ? null : googleTable.getNumberOfRows();
                    filterUpdate(googleTable.getColumnId(0), row, maxSelections);
                }
                // Update the chart view in order to reflect the current selection
                // (only if not has already been redrawn in the previous filterUpdate() call)
                if (!displayerSettings.isFilterSelfApplyEnabled()) {
                    updateVisualization();
                }
            }
        };
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
        return hAxis;
    }

    protected VAxis createVAxis() {
        return VAxis.create( displayerSettings.getYAxisTitle() );
    }
}
