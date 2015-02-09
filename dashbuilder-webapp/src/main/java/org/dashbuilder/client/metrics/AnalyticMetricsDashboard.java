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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.backend.ClusterMetricsDataSetGenerator;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.MAX;
import static org.dashbuilder.dataset.group.DateIntervalType.MINUTE;
import static org.dashbuilder.dataset.group.DateIntervalType.SECOND;

public class AnalyticMetricsDashboard extends Composite {

    interface AnalyticMetricsDashboardBinder extends UiBinder<Widget, AnalyticMetricsDashboard>{}
    private static final AnalyticMetricsDashboardBinder uiBinder = GWT.create(AnalyticMetricsDashboardBinder.class);

    public static final String METRICS_DATASET_UUID = "clusterMetrics";
    public static final String BACKGROUND_COLOR = "#F8F8FF";

    @UiField
    TabPanel tabPanel;

    /* *********************************** OVERALL ***************************************** */
    @UiField(provided = true)
    Displayer maxCPUxServer;

    @UiField(provided = true)
    Displayer maxMemxServerSettings;

    @UiField(provided = true)
    Displayer maxProcessesxServerSettings;

    @UiField(provided = true)
    Displayer maxNetworkxServerSettings;

    /* *********************************** CPU ***************************************** */
    @UiField(provided = true)
    Displayer cpuDisplayer1;
    
    /* *********************************** MEMORY ***************************************** */
    @UiField(provided = true)
    Displayer memoryDisplayer1;
    
    /* *********************************** NETWORK ***************************************** */
    @UiField(provided = true)
    Displayer networkDisplayer1;
    
    
    /* *********************************** PROCESSES ***************************************** */
    @UiField(provided = true)
    Displayer processDisplayer1;
    
    /* *********************************** DISK ***************************************** */
    @UiField(provided = true)
    Displayer diskDisplayer1;
    
    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "System Metrics Dashboard (Analytic)";
    }

    public AnalyticMetricsDashboard() {

        // Build summary.
        buildSummary();

        // TODO: Use server value from filter value.
        String selectedServer = "server1";
        
        // Build server detail tabs.
        buildCPU(selectedServer);
        buildMemory(selectedServer);
        buildNetwork(selectedServer);
        buildProcesses(selectedServer);
        buildDisk(selectedServer);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Select overall tab by default.
        tabPanel.selectTab(0);
        
        // Draw the charts
        displayerCoordinator.drawAll();
    }
    
    protected void buildCPU(String server) {

        cpuDisplayer1 = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(999, MINUTE, true)
                .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, "Minute")
                .column(ClusterMetricsDataSetGenerator.COLUMN_CPU0, "CPU0 (%)")
                .column(ClusterMetricsDataSetGenerator.COLUMN_CPU1, "CPU1 (%)")
                .title("CPU usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .width(1200).height(400)
                .buildSettings());


        displayerCoordinator.addDisplayer(cpuDisplayer1);
        
    }

    protected void buildMemory(String server) {

        memoryDisplayer1 = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(999, MINUTE, true)
                .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, "Minute")
                .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_USED, "Used memory (Gb)")
                .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_FREE, "Free memory (Gb)")
                .title("Memory usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .width(1200).height(400)
                .buildSettings());


        displayerCoordinator.addDisplayer(memoryDisplayer1);

    }

    protected void buildNetwork(String server) {

        networkDisplayer1 = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(999, MINUTE, true)
                .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, "Minute")
                .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_TX, "Upstream (kbps)")
                .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_RX, "Downstream (kbps)")
                .title("Network usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .width(1200).height(400)
                .buildSettings());


        displayerCoordinator.addDisplayer(networkDisplayer1);

    }

    protected void buildProcesses(String server) {

        processDisplayer1 = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(999, MINUTE, true)
                .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, "Minute")
                .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_RUNNING, "Running processes")
                .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_SLEEPING, "Sleeping processes")
                .title("Process usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .width(1200).height(400)
                .buildSettings());


        displayerCoordinator.addDisplayer(processDisplayer1);

    }

    protected void buildDisk(String server) {

        diskDisplayer1 = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(999, MINUTE, true)
                .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, "Minute")
                .column(ClusterMetricsDataSetGenerator.COLUMN_DISK_USED, "Used disk space (Mb)")
                .column(ClusterMetricsDataSetGenerator.COLUMN_DISK_FREE, "Free disk space (Mb)")
                .title("Disk usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .width(1200).height(400)
                .buildSettings());


        displayerCoordinator.addDisplayer(diskDisplayer1);

    }

    protected void buildSummary() {
        maxCPUxServer = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_CPU0, MAX, "CPU0 Max")
                .column(ClusterMetricsDataSetGenerator.COLUMN_CPU1, MAX, "CPU1 Max")
                .title("Max CPU usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .vertical()
                .buildSettings());

        maxMemxServerSettings = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_USED, MAX, "Max used memory")
                .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_FREE, MAX, "Max free memory")
                .title("Max Memory usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .vertical()
                .buildSettings());

        maxProcessesxServerSettings = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_RUNNING, MAX, "Max running processes")
                .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_SLEEPING, MAX, "Max sleeping processes")
                .title("Max processes usage")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .vertical()
                .buildSettings());

        maxNetworkxServerSettings = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(METRICS_DATASET_UUID)
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_TX, MAX, "Max upstream speed")
                .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_RX, MAX, "Max downstream speed")
                .title("Max network speed")
                .titleVisible(true)
                .backgroundColor(BACKGROUND_COLOR)
                .vertical()
                .buildSettings());

        // TODO: Disk (pie)
        
        // Make that charts interact among them
        displayerCoordinator.addDisplayer(maxCPUxServer);
        displayerCoordinator.addDisplayer(maxMemxServerSettings);
        displayerCoordinator.addDisplayer(maxProcessesxServerSettings);
        displayerCoordinator.addDisplayer(maxNetworkxServerSettings);
    }

}