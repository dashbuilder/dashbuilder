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
package org.dashbuilder.client.metrics.widgets.vertical;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.backend.ClusterMetricsDataSetGenerator;
import org.dashbuilder.client.metrics.MetricsDashboard;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.client.metrics.MetricsConstants.CLUSTER_METRICS_UUID;
import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.MAX;
import static org.dashbuilder.dataset.group.DateIntervalType.SECOND;

public class VerticalServerMetrics extends Composite {

    interface VerticalServerMetricsBinder extends UiBinder<Widget, VerticalServerMetrics>{}
    private static final VerticalServerMetricsBinder uiBinder = GWT.create(VerticalServerMetricsBinder.class);
    
    @UiField
    VerticalPanel mainPanel;

    @UiField
    VerticalPanel labelsPanel;

    @UiField
    FocusPanel serverIcon;
    
    @UiField
    HTML serverName;
    
    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "Server metrics (Vertical)";
    }

    public VerticalServerMetrics(MetricsDashboard metricsDashboard, String server) {
        this(metricsDashboard, server, false);
    }
    
    public VerticalServerMetrics(final MetricsDashboard metricsDashboard, final String server, boolean showLabels) {

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // Labels.
        if (showLabels) {
            labelsPanel.setVisible(true);
        }
        
        // The server name.
        serverName.setText(server);
        

        // CPU0
        Displayer serverCPU0 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMeterChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .column(ClusterMetricsDataSetGenerator.COLUMN_CPU0, MAX, "CPU0")
                        .title("CPU0")
                        .titleVisible(false)
                        .width(100).height(100)
                        .meter(0, 25, 50, 100)
                        .refreshOn(1, false)
                        .buildSettings());

        addDisplayer(serverCPU0);
        
        // CPU1
        Displayer serverCPU1 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMeterChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .column(ClusterMetricsDataSetGenerator.COLUMN_CPU1, MAX, "CPU1")
                        .title("CPU1")
                        .titleVisible(false)
                        .width(100).height(100)
                        .meter(0, 25, 50, 100)
                        .refreshOn(1, false)
                        .buildSettings());

        addDisplayer(serverCPU1);

        // Used memory
        Displayer serverMemory = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(CLUSTER_METRICS_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("10second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_USED, MAX, "Used memory")
                        .title("Memory consumption")
                        .titleVisible(false)
                        .width(200).height(200)
                        .legendOff()
                        .refreshOn(2, false)
                        .buildSettings());

        addDisplayer(serverMemory);

        // TX/RX
        Displayer serverNetwork = DisplayerHelper.lookupDisplayer(
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
                        .legendOff()
                        .width(200).height(200)
                        .refreshOn(2, false)
                        .buildSettings());

        addDisplayer(serverNetwork);

        // Processes
        Displayer serverProcessesRunning = DisplayerHelper.lookupDisplayer(
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

        addDisplayer(serverProcessesRunning);

        // Disk
        Displayer serverDisk = DisplayerHelper.lookupDisplayer(
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
                        .legendOff()
                        .width(200).height(200)
                        .refreshOn(2, false)
                        .buildSettings());
        
        addDisplayer(serverDisk);

        // Add the click handler for server details action.
        serverIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                GWT.log("VerticalServerMetrics - Show details for server: " + server);
                metricsDashboard.showServerDetails(server);
            }
        });
        
        // Draw the charts
        displayerCoordinator.drawAll();
    }

    private void addDisplayer(Displayer displayer) {
        displayerCoordinator.addDisplayer(displayer);
        displayer.refreshOn();
        mainPanel.add(displayer);
    }
    
    
}