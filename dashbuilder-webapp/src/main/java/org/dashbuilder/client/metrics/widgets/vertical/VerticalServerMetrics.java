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

import com.github.gwtbootstrap.client.ui.Tooltip;
import com.github.gwtbootstrap.client.ui.constants.Placement;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.backend.ClusterMetricsDataSetGenerator;
import org.dashbuilder.client.metrics.RealTimeMetricsDashboard;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.MAX;
import static org.dashbuilder.dataset.group.DateIntervalType.SECOND;

public class VerticalServerMetrics extends Composite {

    interface VerticalServerMetricsBinder extends UiBinder<Widget, VerticalServerMetrics>{}
    private static final VerticalServerMetricsBinder uiBinder = GWT.create(VerticalServerMetricsBinder.class);
    
    private static final String TOOLTIP_CPU = "CPU usage (%)";
    private static final String TOOLTIP_USED_MEMORY = "Used memory (Gb)";
    private static final String TOOLTIP_NET_BW = "Network BW (kbps)";
    private static final String TOOLTIP_PROCESSES = "Running processes";
    private static final String TOOLTIP_DISK = "Disk usage (Mb)";
    
    @UiField
    VerticalPanel mainPanel;

    @UiField
    FocusPanel serverIcon;
    
    @UiField
    HTML serverName;
    
    @UiField
    Tooltip tooltip;
    
    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    private String server;
    private boolean isOff = false;
    
    public String getTitle() {
        return "Server metrics (Vertical)";
    }

    public VerticalServerMetrics(final RealTimeMetricsDashboard metricsDashboard, final String server) {
        this.server = server;
        
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // The server name.
        serverName.setText(server);
        toolTipDefaultText();

        Displayer serverCPU = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newLineChartSettings()
                        .dataset(RealTimeMetricsDashboard.METRICS_DATASET_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("10second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_CPU0, MAX, "CPU0")
                        .column(ClusterMetricsDataSetGenerator.COLUMN_CPU1, MAX, "CPU1")
                        .title("CPU usage")
                        .titleVisible(false)
                        .backgroundColor(RealTimeMetricsDashboard.BACKGROUND_COLOR)
                        .width(200).height(200)
                        .legendOff()
                        .refreshOn(1, false)
                        .buildSettings());

        addDisplayer(serverCPU, TOOLTIP_CPU);
        
        // Used memory
        Displayer serverMemory = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(RealTimeMetricsDashboard.METRICS_DATASET_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("10second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_USED, MAX, "Used memory")
                        .title("Memory consumption")
                        .titleVisible(false)
                        .backgroundColor(RealTimeMetricsDashboard.BACKGROUND_COLOR)
                        .width(200).height(200)
                        .legendOff()
                        .refreshOn(1, false)
                        .buildSettings());

        addDisplayer(serverMemory, TOOLTIP_USED_MEMORY);

        // TX/RX
        Displayer serverNetwork = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(RealTimeMetricsDashboard.METRICS_DATASET_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("10second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_RX, MAX, "Downstream")
                        .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_TX, MAX, "Upstream")
                        .title("Network bandwidth")
                        .titleVisible(false)
                        .backgroundColor(RealTimeMetricsDashboard.BACKGROUND_COLOR)
                        .legendOff()
                        .width(200).height(200)
                        .refreshOn(1, false)
                        .horizontal()
                        .buildSettings());

        addDisplayer(serverNetwork, TOOLTIP_NET_BW);

        // Processes
        Displayer serverProcessesRunning = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(RealTimeMetricsDashboard.METRICS_DATASET_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .group(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP).dynamic(1, SECOND, true)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP)
                        .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_RUNNING, MAX, "Running processes")
                        .title("Running processes")
                        .titleVisible(false)
                        .backgroundColor(RealTimeMetricsDashboard.BACKGROUND_COLOR)
                        .legendOff()
                        .width(200).height(200)
                        .refreshOn(1, false)
                        .buildSettings());
        
        addDisplayer(serverProcessesRunning, TOOLTIP_PROCESSES);

        // Disk
        Displayer serverDisk = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(RealTimeMetricsDashboard.METRICS_DATASET_UUID)
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_SERVER, equalsTo(server))
                        .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
                        .column(ClusterMetricsDataSetGenerator.COLUMN_DISK_FREE, "Free disk space (Mb)")
                        .column(ClusterMetricsDataSetGenerator.COLUMN_DISK_USED, "Used disk space (Mb)")
                        .title("Disk usage")
                        .titleVisible(false)
                        .tableWidth(200)
                        .refreshOn(1, false)
                        .buildSettings());
        
        addDisplayer(serverDisk, TOOLTIP_DISK);

        // Add the click handler for server details action.
        serverIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                metricsDashboard.showServerDetails(server);
            }
        });
        setPointerCursor(serverIcon);
        
        // Draw the charts and enable automatic refresh.
        displayerCoordinator.drawAll();
        displayerCoordinator.refreshOnAll();
    }

    private void addDisplayer(Displayer displayer, String toolTipText) {
        displayerCoordinator.addDisplayer(displayer);
        Tooltip tooltip = new Tooltip(toolTipText);
        tooltip.setPlacement(Placement.LEFT);
        tooltip.add(displayer);
        mainPanel.add(tooltip);
    }

    private void setPointerCursor(UIObject object) {
        object.getElement().getStyle().setCursor(Style.Cursor.POINTER);
    }

    /**
     * Disable this server metrics (server probably down) 
     */
    public VerticalServerMetrics off() {
        serverIcon.getElement().getStyle().setOpacity(0.3);
        mainPanel.getElement().getStyle().setOpacity(0.3);
        tooltip.setText(server + " is down");
        isOff = true;
        return this;
    }

    /**
     * Enable this server metrics (server already up) 
     */
    public VerticalServerMetrics on() {
        serverIcon.getElement().getStyle().setOpacity(1);
        mainPanel.getElement().getStyle().setOpacity(1);
        toolTipDefaultText();
        isOff = false;
        return this;
    }
    
    private void toolTipDefaultText() {
        tooltip.setText("Show real-time metric details for server " + server);
    }

    public String getServer() {
        return server;
    }

    public boolean isOn() {
        return !isOff;
    }

    public boolean isOff() {
        return isOff;
    }
}