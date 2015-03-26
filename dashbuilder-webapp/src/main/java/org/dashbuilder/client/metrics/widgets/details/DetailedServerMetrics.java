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
package org.dashbuilder.client.metrics.widgets.details;

import com.github.gwtbootstrap.client.ui.Tooltip;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.metrics.RealTimeMetricsDashboard;
import org.dashbuilder.client.metrics.MetricsDashboardClientBundle;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.dashbuilder.dataset.group.DateIntervalType.MINUTE;
import static org.dashbuilder.dataset.group.DateIntervalType.SECOND;
import static org.dashbuilder.backend.ClusterMetricsGenerator.*;
import static org.dashbuilder.client.metrics.RealTimeMetricsDashboard.*;

public class DetailedServerMetrics extends Composite {

    interface DetailedServerMetricsBinder extends UiBinder<Widget, DetailedServerMetrics>{}
    private static final DetailedServerMetricsBinder uiBinder = GWT.create(DetailedServerMetricsBinder.class);

    @UiField(provided = true)
    Displayer serverCPU0;

    @UiField(provided = true)
    Displayer serverCPU1;

    @UiField(provided = true)
    Displayer serverMemory;

    @UiField(provided = true)
    Displayer serverNetwork;

    @UiField(provided = true)
    Displayer serverDisk;

    @UiField(provided = true)
    Displayer serverProcessesRunning;

    @UiField(provided = true)
    Displayer serverTable;

    @UiField(provided = true)
    Displayer serverProcessesSleeping;

    @UiField
    Image backIcon;

    @UiField
    Image modeIcon;

    @UiField
    Tooltip modeIconTooltip;

    @UiField
    HorizontalPanel chartsArea;

    @UiField
    VerticalPanel tableArea;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    private int refreshInterval;
    private boolean isChartMode;
    Timer refreshTimer;

    public String getTitle() {
        return "Server metrics (Vertical)";
    }

    public DetailedServerMetrics(final RealTimeMetricsDashboard metricsDashboard, String server) {
        this(metricsDashboard, server, 1);    
    }
    
    public DetailedServerMetrics(final RealTimeMetricsDashboard metricsDashboard, String server, int refreshInterval) {
        this.refreshInterval = refreshInterval;
        
        buildServerDetailsDisplayers(metricsDashboard, server);
        
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Configure user actions.
        backIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                metricsDashboard.init();
            }
        });
        
        modeIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isChartMode) enableTableMode();
                else enableChartMode();
            }
        });
    }

    @Override
    protected void onLoad() {
        // By default use charts mode.
        enableChartMode();

        // Draw the charts and enable automatic refresh.
        displayerCoordinator.drawAll();

        // Refresh timer
        refreshTimer = new Timer() {
            public void run() {
                displayerCoordinator.redrawAll();
                refreshTimer.schedule(refreshInterval);
            }
        };
        refreshTimer.schedule(refreshInterval);
    }

    @Override
    protected void onUnload() {
        refreshTimer.cancel();
        displayerCoordinator.closeAll();
    }

    protected void buildServerDetailsDisplayers(RealTimeMetricsDashboard metricsDashboard, String server) {
        serverCPU0 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMetricSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-1second"))
                        .column(COLUMN_CPU0, MAX, "CPU0")
                        .title("CPU 1 %")
                        .titleVisible(true)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(200).height(200)
                        .buildSettings());

        serverCPU1 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMetricSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-1second"))
                        .column(COLUMN_CPU1, MAX, "CPU1")
                        .title("CPU 2 %")
                        .titleVisible(true)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(200).height(200)
                        .buildSettings());

        serverMemory = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newLineChartSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("begin[minute] till now"))
                        .group(COLUMN_TIMESTAMP).fixed(SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_MEMORY_USED, MAX, "Used memory")
                        .column(COLUMN_MEMORY_FREE, MAX, "Free memory")
                        .title("Memory consumption")
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(650).height(190)
                        .margins(20, 30, 30, 10)
                        .buildSettings());

        serverNetwork = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-60second"))
                        .group(COLUMN_TIMESTAMP).dynamic(60, SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_NETWORK_RX, MAX, "Downstream")
                        .column(COLUMN_NETWORK_TX, MAX, "Upstream")
                        .title("Network bandwidth")
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(300).height(190)
                        .margins(20, 30, 30, 10)
                        .buildSettings());

        serverDisk = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .group(COLUMN_TIMESTAMP)
                        .column(COLUMN_DISK_FREE, MAX, "Free disk space")
                        .column(COLUMN_DISK_USED, MAX, "Used disk space")
                        .title("Disk usage")
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .tableWidth(170)
                        .buildSettings());

        serverProcessesRunning = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .column(COLUMN_PROCESSES_RUNNING, AVERAGE, "Running")
                        .title("Running processes")
                        .titleVisible(false)
                        .tableWidth(100)
                        .refreshOn(this.refreshInterval, false)
                        .buildSettings());

        serverProcessesSleeping = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .column(COLUMN_PROCESSES_SLEEPING, AVERAGE, "Sleeping")
                        .title("Sleeping processes")
                        .titleVisible(false)
                        .tableWidth(100)
                        .buildSettings());

        serverTable = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-60minute"))
                        .group(COLUMN_TIMESTAMP).dynamic(1000, MINUTE, true)
                        .column(COLUMN_TIMESTAMP, "Minute")
                        .column(COLUMN_CPU0, "CPU0")
                        .column(COLUMN_CPU1, "CPU1")
                        .column(COLUMN_MEMORY_USED, "Used memory (Gb)")
                        .column(COLUMN_MEMORY_FREE, "Free memory (Gb)")
                        .column(COLUMN_NETWORK_TX, "Upstream (kbps)")
                        .column(COLUMN_NETWORK_RX, "Downstream (kbps)")
                        .column(COLUMN_PROCESSES_RUNNING, "Running processes")
                        .column(COLUMN_PROCESSES_SLEEPING, "Sleeping processes")
                        .column(COLUMN_DISK_USED, "Used disk (Mb)")
                        .column(COLUMN_DISK_FREE, "Free disk (Mb)")
                        .sort(COLUMN_TIMESTAMP, SortOrder.DESCENDING)
                        .title("Real-time " + server + " metrics")
                        .titleVisible(false)
                        .tableWidth(1020)
                        .buildSettings());

        displayerCoordinator.addDisplayer(serverCPU0);
        displayerCoordinator.addDisplayer(serverCPU1);
        displayerCoordinator.addDisplayer(serverMemory);
        displayerCoordinator.addDisplayer(serverNetwork);
        displayerCoordinator.addDisplayer(serverDisk);
        displayerCoordinator.addDisplayer(serverProcessesRunning);
        displayerCoordinator.addDisplayer(serverProcessesSleeping);
        displayerCoordinator.addDisplayer(serverTable);
    }
    
    private void enableChartMode() {
        isChartMode = true;
        chartsArea.setVisible(true);
        tableArea.setVisible(false);
        modeIcon.setResource(MetricsDashboardClientBundle.INSTANCE.tableIcon());
        modeIconTooltip.setText("View as table");
    }

    private void enableTableMode() {
        isChartMode = false;
        chartsArea.setVisible(false);
        tableArea.setVisible(true);
        modeIcon.setResource(MetricsDashboardClientBundle.INSTANCE.chartIcon());
        modeIconTooltip.setText("View as charts");
    }

}