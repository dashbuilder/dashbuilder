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

import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.renderer.chartjs.lib.Chart;
import org.dashbuilder.renderer.chartjs.lib.data.AreaChartData;
import org.dashbuilder.renderer.chartjs.lib.data.AreaChartDataProvider;
import org.dashbuilder.renderer.chartjs.lib.data.AreaSeries;
import org.dashbuilder.renderer.chartjs.lib.data.SeriesBuilder;
import org.dashbuilder.renderer.chartjs.resources.i18n.ChartJsDisplayerConstants;
import org.dom4j.tree.AbstractCDATA;

public abstract class ChartJsDisplayer extends AbstractDisplayer {

    public static final String[] COLOR_ARRAY = new String[] {
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_blue(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_red(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_orange(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_brown(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_coral(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_aqua(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_fuchsia(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_gold(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_green(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_lime(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_magenta(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_pink(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_silver(),
            ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_yellow() };

    public static final String COLOR_NOT_SELECTED = ChartJsDisplayerConstants.INSTANCE.chartjsCategoriesDisplayer_color_grey();

    protected FlowPanel panel = new FlowPanel();

    protected Label label = new Label();

    protected DataSet dataSet;

    public ChartJsDisplayer() {
        initWidget(panel);
    }

    /**
     * Draw the displayer by getting first the underlying data set.
     * Ensure the displayer is also ready for display, which means the Google Visualization API has been loaded.
     */
    public void draw() {
        if (!super.isDrawn()) {

            if (displayerSettings == null) {
                displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error_settings_unset());
            }
            else if (dataSetHandler == null) {
                displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error_handler_unset());
            }
            else {
                try {
                    String initMsg = ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_initalizing();
                    displayMessage(initMsg + " ...");

                    beforeDataSetLookup();
                    dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet result) {
                            dataSet = result;
                            afterDataSetLookup(result);
                            Widget w = createVisualization();
                            panel.clear();
                            panel.add(w);

                            // Set the id of the container panel so that the displayer can be easily located
                            // by testing tools for instance.
                            String id = getDisplayerId();
                            if (!StringUtils.isBlank(id)) {
                                panel.getElement().setId(id);
                            }
                            // Draw done
                            afterDraw();
                        }
                        public void notFound() {
                            displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error_dataset_notfound());
                        }
                        public boolean onError(DataSetClientServiceError error) {
                            displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + error.getThrowable().getMessage());
                            afterError(ChartJsDisplayer.this, error);
                            return false;
                        }
                    });
                } catch (Exception e) {
                    displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + e.getMessage());
                }
            }
        }
    }

    /**
     * Just reload the data set and make the current Google Displayer redraw.
     */
    public void redraw() {
        if (!isDrawn()) {
            draw();
        } else {
            try {
                beforeDataSetLookup();
                dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                    public void callback(DataSet result) {
                        dataSet = result;
                        afterDataSetLookup(result);
                        updateVisualization();

                        // Redraw done
                        afterRedraw();
                    }
                    public void notFound() {
                        displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error_dataset_notfound());
                    }
                    public boolean onError(DataSetClientServiceError error) {
                        displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + error.getThrowable().getMessage());
                        afterError(ChartJsDisplayer.this, error);
                        return false;
                    }
                });
            } catch (Exception e) {
                    displayMessage(ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_error() + e.getMessage());
            }
        }
    }

    /**
     * Close the displayer
     */
    public void close() {
        panel.clear();
        ChartJsRenderer.closeDisplayer(this);

        // Close done
        afterClose();
    }

    /**
     * Create the widget used by concrete Google displayer implementation.
     */
    protected abstract Widget createVisualization();

    /**
     * Update the widget used by concrete Google displayer implementation.
     */
    protected abstract void updateVisualization();

    /**
     * Call back method invoked just before the data set lookup is executed.
     */
    protected void beforeDataSetLookup() {
    }

    /**
     * Call back method invoked just after the data set lookup is executed.
     */
    protected void afterDataSetLookup(DataSet dataSet) {
    }

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage(String msg) {
        panel.clear();
        panel.add(label);
        label.setText(msg);
    }

    protected void adjustChartSize(Chart chart) {
        int width = displayerSettings.getChartWidth();
        int height = displayerSettings.getChartHeight();
        int top = displayerSettings.getChartMarginTop();
        int bottom = displayerSettings.getChartMarginBottom();
        int left = displayerSettings.getChartMarginLeft();
        int right = displayerSettings.getChartMarginRight();

        int chartWidth = width-left;
        int chartHeight = height-top;

        chart.getElement().getStyle().setPaddingTop(top, Style.Unit.PX);
        chart.getElement().getStyle().setPaddingLeft(left, Style.Unit.PX);
        chart.setPixelWidth(chartWidth);
        chart.setPixelHeight(chartHeight);
    }

    protected Widget createNoDataMsgPanel() {
        return new com.github.gwtbootstrap.client.ui.Label(ChartJsDisplayerConstants.INSTANCE.common_noData());
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
                panel.add(new com.github.gwtbootstrap.client.ui.Label(formattedValue));
            }
        }

        Anchor anchor = new Anchor( ChartJsDisplayerConstants.INSTANCE.chartjsDisplayer_resetAnchor() );
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

/*
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
    }*/

    // Chart data generation

    protected AreaChartDataProvider createAreaDataProvider() {
        return new AreaChartDataProvider() {
            public JavaScriptObject getData() {
                return createChartData();
            }
            public void reload(AsyncCallback<AreaChartData> callback) {
                AreaChartData data = createChartData();
                callback.onSuccess(data);
            }
        };
    }

    private AreaChartData createChartData() {

        List<DataColumn> columns = dataSet.getColumns();
        String[] labels = new String[dataSet.getRowCount()];
        DataColumn labelColumn = columns.get(0);
        for (int i=0; i<dataSet.getRowCount(); i++) {
            String label = super.formatValue(dataSet.getValueAt(i, 0), labelColumn);
            labels[i] = label;
        }

        JsArray<AreaSeries> series = JavaScriptObject.createArray().cast();
        for (int i=1; i<columns.size(); i++) {
            DataColumn seriesColumn = columns.get(0);
            ColumnSettings columnSettings = displayerSettings.getColumnSettings(seriesColumn);

            double[] values = new double[dataSet.getRowCount()];
            for (int j=0; j<dataSet.getRowCount(); j++) {
                values[j] = ((Number) dataSet.getValueAt(j, i)).doubleValue();
            }

            series.push(SeriesBuilder.create()
                    .withLabel(columnSettings.getColumnName())
                    .withFillColor(COLOR_ARRAY[i-1])
                    .withStoreColor(COLOR_ARRAY[i - 1])
                    .withPointColor(COLOR_ARRAY[i - 1])
                    .withPointStrokeColor("#fff")
                    .withData(values)
                    .get());
        }

        AreaChartData data = JavaScriptObject.createObject().cast();
        data.setLabels(labels);
        data.setSeries(series);
        return data;
    }
}
