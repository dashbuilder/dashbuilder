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
package org.dashbuilder.client.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.gallery.GalleryWidget;
import org.dashbuilder.dataset.DataSetBuilder;
import org.dashbuilder.displayer.BarChartSettingsBuilder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsBuilder;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.impl.BarChartSettingsBuilderImpl;
import org.dashbuilder.renderer.client.DefaultRenderer;

import static org.dashbuilder.backend.ClusterMetricsGenerator.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of Displayer instances.</p>
 */
public class ClusterMetricsDashboard extends Composite implements GalleryWidget {

    interface Binder extends UiBinder<Widget, ClusterMetricsDashboard> {}
    private static final Binder uiBinder = GWT.create(Binder.class);

    @UiField
    Panel messagePanel;

    @UiField
    Panel mainPanel;

    @UiField
    Panel leftPanel;

    @UiField
    Panel rightPanel;

    @UiField
    ListBox metricSelector;

    @UiField
    ListBox chartTypeSelector;

    @UiField
    Panel metricChartPanel;

    @UiField(provided = true)
    Displayer metricsTable;

    List<ClusterMetric> metricDefList = new ArrayList<ClusterMetric>();
    Map<String,List<Integer>> metricChartDef = new HashMap<String, List<Integer>>();
    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();
    Displayer currentMetricChart = null;

    Timer refreshTimer = new Timer() {
        public void run() {
            displayerCoordinator.redrawAll(new Callback() {
                public void onFailure(Object reason) {
                }
                public void onSuccess(Object result) {
                    refreshTimer.schedule(1000);
                }
            });
        }
    };

    @Override
    public String getTitle() {
        return "Cluster metrics";
    }

    @Override
    public void onClose() {
        displayerCoordinator.closeAll();
        refreshTimer.cancel();
    }

    @Override
    public boolean feedsFrom(String dataSetId) {
        return "clusterMetrics".equals(dataSetId);
    }

    @Override
    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }

    class ClusterMetric {
        String column;
        String title;
        String format;
        String expression;
        String bgColor;
        boolean tableVisible;
        String units;

        public ClusterMetric(String column, String title, String format, String expression, String bgColor, boolean tableVisible, String units) {
            this.column = column;
            this.title = title;
            this.format = format;
            this.expression = expression;
            this.bgColor = bgColor;
            this.tableVisible = tableVisible;
            this.units = units;
        }
    }

    public static final String CPU = "CPU %";
    public static final String MEMORY = "Memory";
    public static final String DISK = "Disk";
    public static final String NETWORK = "Network";

    public ClusterMetricsDashboard() {

        // Create the metric definitions
        metricDefList.add(new ClusterMetric(COLUMN_CPU0, "CPU %", "#,##0", null, "84ADF4", true, "CPU %"));
        metricDefList.add(new ClusterMetric(COLUMN_DISK_FREE, "Disk free", "#,##0 Gb", null, "BCF3EE", false, "Gigabytes"));
        metricDefList.add(new ClusterMetric(COLUMN_DISK_USED, "Disk used", "#,##0 Gb", null, "BCF3EE", true, "Gigabytes"));
        metricDefList.add(new ClusterMetric(COLUMN_MEMORY_FREE, "Mem. free", "#,##0.00 Gb", null, "F9AEAF", false, "Gigabytes"));
        metricDefList.add(new ClusterMetric(COLUMN_MEMORY_USED, "Mem. used", "#,##0.00 Gb", null, "F9AEAF", true, "Gigabytes"));
        metricDefList.add(new ClusterMetric(COLUMN_PROCESSES_RUNNING, "Proc. running", "#,##0", null, "A4EEC8", false, "Processes"));
        metricDefList.add(new ClusterMetric(COLUMN_PROCESSES_SLEEPING, "Proc. sleeping", "#,##0", null, "A4EEC8", true, "Processes"));
        metricDefList.add(new ClusterMetric(COLUMN_NETWORK_RX, "Net. Rx", "#,##0 Kb/s", null, "F5AC47", false, "Kb / s"));
        metricDefList.add(new ClusterMetric(COLUMN_NETWORK_TX, "Net. Tx", "#,##0 Kb/s", null, "F5AC47", true, "Kb / s"));

        metricChartDef.put(CPU, Arrays.asList(0));
        metricChartDef.put(DISK, Arrays.asList(1,2));
        metricChartDef.put(MEMORY, Arrays.asList(3,4));
        metricChartDef.put(NETWORK, Arrays.asList(7,8));

        // Init the metrics table
        DisplayerSettingsBuilder tableBuilder = DisplayerSettingsFactory.newTableSettings()
                .renderer(DefaultRenderer.UUID)
                .tableWidth(700)
                .tableOrderDefault(COLUMN_SERVER, ASCENDING)
                .filterOn(true, true, false)
                .dataset("clusterMetrics")
                .filter(COLUMN_TIMESTAMP, timeFrame("now -2second till now"))
                .group(COLUMN_SERVER)
                .column(COLUMN_SERVER).format("Server")
                .column(COLUMN_TIMESTAMP).format("Time", "HH:mm:ss");

        for (ClusterMetric metric : metricDefList) {
            if (metric.tableVisible) {
                tableBuilder.column(metric.column, AVERAGE);
                tableBuilder.format(metric.title, metric.format);
                tableBuilder.expression(metric.column, metric.expression);
            }
        }

        metricsTable = DisplayerHelper.lookupDisplayer(tableBuilder.buildSettings());
        displayerCoordinator.addDisplayer(metricsTable);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        mainPanel.getElement().setAttribute("cellpadding", "5");

        // Init the box metrics
        leftPanel.clear();
        for (Integer metricIdx : Arrays.asList(0, 1, 3, 5, 7)) {
            ClusterMetric metric = metricDefList.get(metricIdx);
            Displayer metricDisplayer = DisplayerHelper.lookupDisplayer(
                    DisplayerSettingsFactory.newMetricSettings()
                            .dataset("clusterMetrics")
                            .filter(COLUMN_TIMESTAMP, timeFrame("now -2second till now"))
                            .column(metric.column, AVERAGE)
                            .expression(metric.expression)
                            .format(metric.title, metric.format)
                            .title(metric.title)
                            .titleVisible(true)
                            .width(200).height(90)
                            .margins(10, 10, 10, 10)
                            .backgroundColor(metric.bgColor)
                            .filterOff(true)
                            .buildSettings());

            displayerCoordinator.addDisplayer(metricDisplayer);
            leftPanel.add(metricDisplayer);
        }

        // Init the metric selector
        metricSelector.clear();
        metricSelector.addItem("CPU %");
        metricSelector.addItem("Memory");
        metricSelector.addItem("Disk");
        metricSelector.addItem("Network");

        // Init the chart type selector
        chartTypeSelector.clear();
        chartTypeSelector.addItem("Bar");
        chartTypeSelector.addItem("Line");
        chartTypeSelector.addItem("Area");

        // Init the metric chart
        currentMetricChart = createChartMetric(CPU);
        metricChartPanel.clear();
        metricChartPanel.add(currentMetricChart);
        displayerCoordinator.addDisplayer(currentMetricChart);

        // Draw the charts
        displayerCoordinator.drawAll(new Callback() {
            public void onFailure(Object reason) {
            }

            public void onSuccess(Object result) {
                messagePanel.setVisible(false);
                mainPanel.setVisible(true);
                refreshTimer.schedule(1000);
            }
        });
    }

    protected Displayer createChartMetric(String group) {

        DisplayerType type = DisplayerType.BARCHART;
        switch (chartTypeSelector.getSelectedIndex()) {
            case 1: type = DisplayerType.LINECHART; break;
            case 2: type = DisplayerType.AREACHART; break;
        }

        BarChartSettingsBuilder<BarChartSettingsBuilderImpl> builder = DisplayerSettingsFactory.newBarChartSettings()
                .title(group)
                .titleVisible(false)
                .width(700).height(200)
                .margins(30, 5, 60, 10)
                .legendOn("top")
                .filterOff(true)
                .dataset("clusterMetrics");

        if (DisplayerType.BARCHART.equals(type)) {
            builder.filter(COLUMN_TIMESTAMP, timeFrame("begin[minute] till end[minute]"));
            builder.group(COLUMN_TIMESTAMP).fixed(SECOND, true);
            builder.column(COLUMN_TIMESTAMP).format("Time");
        } else {
            builder.filter(COLUMN_TIMESTAMP, timeFrame("-60second till now"));
            builder.group(COLUMN_TIMESTAMP).dynamic(60, SECOND, true);
            builder.column(COLUMN_TIMESTAMP).format("Time");
        }

        List<Integer> metricIdxs = metricChartDef.get(group);
        for (Integer metricIdx : metricIdxs) {
            ClusterMetric metric = metricDefList.get(metricIdx);
                builder.column(metric.column, AVERAGE);
                builder.expression(metric.expression);
                builder.format(metric.title, metric.format);
                builder.yAxisTitle(metric.units);
        }

        DisplayerSettings settings = builder.buildSettings();
        settings.setType(type);
        return DisplayerHelper.lookupDisplayer(settings);
    }

    @UiHandler("chartTypeSelector")
    public void onChartTypeSelected(ChangeEvent changeEvent) {
        onMetricSelected(changeEvent);
    }

    @UiHandler("metricSelector")
    public void onMetricSelected(ChangeEvent changeEvent) {

        // Dispose the current metric chart
        currentMetricChart.close();
        displayerCoordinator.removeDisplayer(currentMetricChart);

        // Create the metric chart
        String title = metricSelector.getValue(metricSelector.getSelectedIndex());
        currentMetricChart = createChartMetric(title);
        currentMetricChart.draw();
        displayerCoordinator.addDisplayer(currentMetricChart);

        // Update the dashboard view
        metricChartPanel.clear();
        metricChartPanel.add(currentMetricChart);
    }
}
