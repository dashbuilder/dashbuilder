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
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.backend.ClusterMetricsDataSetGenerator.*;
import static org.dashbuilder.client.metrics.RealTimeMetricsDashboard.*;

public class VerticalServerMetrics extends Composite {

    interface VerticalServerMetricsBinder extends UiBinder<Widget, VerticalServerMetrics>{}
    private static final VerticalServerMetricsBinder uiBinder = GWT.create(VerticalServerMetricsBinder.class);
    
    private static final String TOOLTIP_CPU = "CPU usage (%)";
    private static final String TOOLTIP_USED_MEMORY = "Used memory (Gb)";
    private static final String TOOLTIP_NET_BW = "Network BW (kbps)";
    private static final String TOOLTIP_PROCESSES = "Running/Sleeping processes";
    private static final String TOOLTIP_DISK = "Disk usage (Mb)";
    
    @UiField
    Panel mainPanel;

    @UiField
    FocusPanel serverIcon;
    
    @UiField
    HTML serverName;
    
    @UiField
    Tooltip tooltip;
    
    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    private int refreshInterval;
    private String server;
    private boolean isOff = false;
    
    public String getTitle() {
        return "Server metrics (Vertical)";
    }

    public VerticalServerMetrics(final RealTimeMetricsDashboard metricsDashboard, final String server) {
        this(metricsDashboard, server, 2);
    }
    
    public VerticalServerMetrics(final RealTimeMetricsDashboard metricsDashboard, final String server, int refreshInterval) {
        this.server = server;
        this.refreshInterval = refreshInterval;
        
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // The server name.
        serverName.setText(server);
        toolTipDefaultText();

        Displayer serverCPU = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newLineChartSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("begin[minute] till now"))
                        .group(COLUMN_TIMESTAMP).fixed(SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_CPU0, MAX, "CPU0")
                        .column(COLUMN_CPU1, MAX, "CPU1")
                        .title("CPU usage")
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(165).height(80)
                        .margins(5, 5, 30, 5)
                        .legendOff()
                        .buildSettings());

        // Used memory
        Displayer serverMemory = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("begin[minute] till now"))
                        .group(COLUMN_TIMESTAMP).fixed(SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_MEMORY_USED, MAX, "Used memory")
                        .title("Memory consumption")
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(165).height(80)
                        .margins(5, 5, 30, 5)
                        .legendOff()
                        .buildSettings());

        // TX/RX
        Displayer serverNetwork = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-10second"))
                        .group(COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_NETWORK_RX, MAX, "Downstream")
                        .column(COLUMN_NETWORK_TX, MAX, "Upstream")
                        .title("Network bandwidth")
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .legendOff()
                        .width(165).height(80)
                        .margins(5, 5, 30, 5)
                        .vertical()
                        .buildSettings());

        // Processes
        Displayer serverProcessesRunning = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .group(COLUMN_TIMESTAMP).dynamic(1, SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_PROCESSES_RUNNING, MAX, "Running processes")
                        .column(COLUMN_PROCESSES_SLEEPING, MAX, "Slepping processes")
                        .title("Running/Sleepping processes")
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .legendOff()
                        .width(165).height(80)
                        .margins(5, 5, 30, 5)
                        .buildSettings());

        // Disk
        Displayer serverDisk = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .column(COLUMN_DISK_FREE, "Free disk (Mb)")
                        .column(COLUMN_DISK_USED, "Used disk (Mb)")
                        .rowNumber(1)
                        .title("Disk usage")
                        .titleVisible(false)
                        .tableWidth(180)
                        .buildSettings());

        addDisplayer(serverCPU, TOOLTIP_CPU);
        addDisplayer(serverMemory, TOOLTIP_USED_MEMORY);
        addDisplayer(serverNetwork, TOOLTIP_NET_BW);
        addDisplayer(serverProcessesRunning, TOOLTIP_PROCESSES);
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
    }

    private void addDisplayer(Displayer displayer, String toolTipText) {
        displayerCoordinator.addDisplayer(displayer);
        FlowPanel panel = new FlowPanel();
        Tooltip tooltip = new Tooltip(toolTipText);
        tooltip.setPlacement(Placement.TOP);
        tooltip.add(displayer);
        panel.add(tooltip);
        panel.getElement().getStyle().setPadding(10, Style.Unit.PX);
        mainPanel.add(panel);
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

    /**
     * redraw
     */
    public VerticalServerMetrics redraw() {
        displayerCoordinator.redrawAll();
        return this;
    }

    public VerticalServerMetrics dispose() {
        this.displayerCoordinator.closeAll();
        return this;
    }

    private void toolTipDefaultText() {
        tooltip.setText("Click to get more details");
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