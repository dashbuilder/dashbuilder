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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.backend.ClusterMetricsDataSetGenerator;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.client.metrics.MetricsConstants.CLUSTER_METRICS_UUID;
import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.MAX;
import static org.dashbuilder.dataset.group.DateIntervalType.SECOND;

public class MetricsDashboard extends Composite {

    interface MetricsDashboardBinder extends UiBinder<Widget, MetricsDashboard>{}
    private static final MetricsDashboardBinder uiBinder = GWT.create(MetricsDashboardBinder.class);

    private static final DisplayerSettings MaxCPUxServerSettings = DisplayerSettingsFactory.newBarChartSettings()
            .dataset(CLUSTER_METRICS_UUID)
            .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
            .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
            .column(ClusterMetricsDataSetGenerator.COLUMN_CPU0, MAX, "CPU0 Max")
            .column(ClusterMetricsDataSetGenerator.COLUMN_CPU1, MAX, "CPU1 Max")
            .title("Max CPU usage")
            .titleVisible(true)
            .vertical()
            .buildSettings();

    private static final DisplayerSettings MaxMemxServerSettings = DisplayerSettingsFactory.newBarChartSettings()
            .dataset(CLUSTER_METRICS_UUID)
            .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
            .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
            .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_USED, MAX, "Max used memory")
            .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_FREE, MAX, "Max free memory")
            .title("Max Memory usage")
            .titleVisible(true)
            .vertical()
            .buildSettings();

    private static final DisplayerSettings MaxProcessesxServerSettings = DisplayerSettingsFactory.newBarChartSettings()
            .dataset(CLUSTER_METRICS_UUID)
            .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
            .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
            .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_RUNNING, MAX, "Max running processes")
            .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_SLEEPING, MAX, "Max sleeping processes")
            .title("Max processes usage")
            .titleVisible(true)
            .vertical()
            .buildSettings();

    private static final DisplayerSettings MaxNetworkxServerSettings = DisplayerSettingsFactory.newBarChartSettings()
            .dataset(CLUSTER_METRICS_UUID)
            .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
            .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
            .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_TX, MAX, "Max upstream speed")
            .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_RX, MAX, "Max downstream speed")
            .title("Max network speed")
            .titleVisible(true)
            .vertical()
            .buildSettings();

    // The client bundle for this widget.
    @UiField
    MetricsDashboardClientBundle resources;

    @UiField()
    HTML title;

    @UiField()
    VerticalPanel serverIconsPanel;

    @UiField()
    FocusPanel serverIconArea1;

    @UiField()
    FocusPanel serverIconArea2;
    
    @UiField()
    HorizontalPanel summaryArea;

    @UiField()
    HorizontalPanel serverDetailsArea;

    @UiField(provided = true)
    Displayer maxCPUxServer;

    @UiField(provided = true)
    Displayer maxMemxServerSettings;

    @UiField(provided = true)
    Displayer maxProcessesxServerSettings;

    @UiField(provided = true)
    Displayer maxNetworkxServerSettings;

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
    Displayer serverProcessesSleeping;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "System metrics dashboard";
    }

    public MetricsDashboard() {

        // Build the provided displayer widgets.
        buildDisplayers();

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        title.setText("System metrics summary");

        serverIconArea1.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                GWT.log("Server1 click");
                showServerDetails(serverIconArea1, "Server1");
            }
        });
        // Draw the charts
        displayerCoordinator.drawAll();

    }

    protected void showServerDetails(FocusPanel serverIconArea, String server) {

        title.setText(server + " details");

        summaryArea.setVisible(false);
        serverDetailsArea.setVisible(true);

    }

    protected void buildDisplayers() {

        maxCPUxServer = DisplayerHelper.lookupDisplayer(MaxCPUxServerSettings);
        maxMemxServerSettings = DisplayerHelper.lookupDisplayer(MaxMemxServerSettings);
        maxProcessesxServerSettings = DisplayerHelper.lookupDisplayer(MaxProcessesxServerSettings);
        maxNetworkxServerSettings = DisplayerHelper.lookupDisplayer(MaxNetworkxServerSettings);

        buildServerDetailsDisplayers("server1");

        // Make that charts interact among them
        displayerCoordinator.addDisplayer(maxCPUxServer);
        displayerCoordinator.addDisplayer(maxMemxServerSettings);
        displayerCoordinator.addDisplayer(maxProcessesxServerSettings);
        displayerCoordinator.addDisplayer(maxNetworkxServerSettings);

    }

    protected void buildServerDetailsDisplayers(String server) {
        serverCPU0 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMeterChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .column(ClusterMetricsDataSetGenerator.COLUMN_CPU0, MAX, "CPU0")
                        .title("CPU0")
                        .width(400).height(200)
                        .meter(0, 25, 50, 100)
                        .refreshOn(1, false)
                        .buildSettings());

        serverCPU1 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMeterChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .column(ClusterMetricsDataSetGenerator.COLUMN_CPU1, MAX, "CPU1")
                        .title("CPU1")
                        .width(400).height(200)
                        .meter(0, 25, 50, 100)
                        .refreshOn(1, false)
                        .buildSettings());

        serverMemory = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("10second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_USED, MAX, "Used memory")
                        .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_FREE, MAX, "Free memory")
                        .title("Memory consumption")
                        .titleVisible(false)
                        .width(400).height(250)
                        .refreshOn(2, false)
                        .buildSettings());

        serverNetwork = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("10second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_RX, MAX, "Downstream")
                        .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_TX, MAX, "Upstream")
                        .title("Network bandwidth")
                        .titleVisible(false)
                        .width(400).height(250)
                        .refreshOn(2, false)
                        .buildSettings());

        serverDisk = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("10second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(1, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_DISK_FREE, MAX, "Free disk space")
                        .column(ClusterMetricsDataSetGenerator.COLUMN_DISK_USED, MAX, "Used disk space")
                        .title("Disk usage")
                        .titleVisible(false)
                        .width(200).height(200)
                        .refreshOn(2, false)
                        .buildSettings());

        serverProcessesRunning = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_RUNNING, "Running processes")
                        .title("Running processes")
                        .titleVisible(false)
                        .tableWidth(100)
                        .refreshOn(1, false)
                        .buildSettings());

        serverProcessesSleeping = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_SLEEPING, "Sleeping processes")
                        .title("Sleeping processes")
                        .titleVisible(false)
                        .tableWidth(100)
                        .refreshOn(1, false)
                        .buildSettings());
        
        
        displayerCoordinator.addDisplayer(serverCPU0);
        displayerCoordinator.addDisplayer(serverCPU1);
        displayerCoordinator.addDisplayer(serverMemory);
        displayerCoordinator.addDisplayer(serverNetwork);
        displayerCoordinator.addDisplayer(serverDisk);
        displayerCoordinator.addDisplayer(serverProcessesRunning);
        displayerCoordinator.addDisplayer(serverProcessesSleeping);
        serverCPU0.refreshOn();
        serverCPU1.refreshOn();
        serverMemory.refreshOn();
        serverNetwork.refreshOn();
        serverDisk.refreshOn();
        serverProcessesRunning.refreshOn();
        serverProcessesSleeping.refreshOn();
    }



    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }
}