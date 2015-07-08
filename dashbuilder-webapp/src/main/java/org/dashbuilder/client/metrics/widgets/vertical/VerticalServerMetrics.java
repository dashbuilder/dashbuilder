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
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.metrics.RealTimeMetricsDashboard;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;

import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.backend.ClusterMetricsGenerator.*;
import static org.dashbuilder.client.metrics.RealTimeMetricsDashboard.*;

public class VerticalServerMetrics extends Composite {

    interface VerticalServerMetricsBinder extends UiBinder<Widget, VerticalServerMetrics>{}
    private static final VerticalServerMetricsBinder uiBinder = GWT.create(VerticalServerMetricsBinder.class);
    
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
        return AppConstants.INSTANCE.metrics_server_vert_title();
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



/*        Displayer serverMetrics = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMetricSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .group(COLUMN_SERVER)
                        .column(COLUMN_SERVER)
                        .column(COLUMN_CPU0, AVERAGE, "CPU 1 %").format("{value} %", "#,###")
                        .column(COLUMN_CPU1, AVERAGE, "CPU 2 %").format("{value} %", "#,###")
                        .column(COLUMN_DISK_FREE, AVERAGE, "Free Disk").format("{value} Mb", "#,###")
                        .format(COLUMN_CPU0, "")
                        .title("{group} - {column}").titleVisible(true)
                        .metricsLayout(true, 3, 5, 5, 5, 5)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(165).height(70)
                        .margins(5, 5, 30, 5)
                        .buildSettings());   */

        Displayer serverCPU0 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMetricSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .column(COLUMN_CPU0, AVERAGE, "CPU0")
                        .title(AppConstants.INSTANCE.metrics_server_vert_cpu1_title())
                        .titleVisible(true)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(165).height(70)
                        .margins(5, 5, 30, 5)
                        .buildSettings());

        Displayer serverCPU1 = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMetricSettings()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .column(COLUMN_CPU1, AVERAGE, "CPU0")
                        .title(AppConstants.INSTANCE.metrics_server_vert_cpu2_title())
                        .titleVisible(true)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(165).height(70)
                        .margins(5, 5, 30, 5)
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
                        .title(AppConstants.INSTANCE.metrics_server_vert_memconsumption_title())
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .width(165).height(80)
                        .margins(5, 5, 30, 5)
                        .legendOff()
                        .buildSettings());

        // TX/RX
        Displayer serverNetwork = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .subType_Column()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-10second"))
                        .group(COLUMN_TIMESTAMP).dynamic(10, SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_NETWORK_RX, MAX, "Downstream")
                        .column(COLUMN_NETWORK_TX, MAX, "Upstream")
                        .title(AppConstants.INSTANCE.metrics_server_vert_netbw_title())
                        .titleVisible(false)
                        .backgroundColor(BACKGROUND_COLOR)
                        .legendOff()
                        .width(165).height(80)
                        .margins(5, 5, 30, 5)
                        .buildSettings());

        // Processes
        Displayer serverProcessesRunning = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .subType_Bar()
                        .dataset(METRICS_DATASET_UUID)
                        .filter(COLUMN_SERVER, equalsTo(server))
                        .filter(COLUMN_TIMESTAMP, timeFrame("-2second"))
                        .group(COLUMN_TIMESTAMP).dynamic(1, SECOND, true)
                        .column(COLUMN_TIMESTAMP)
                        .column(COLUMN_PROCESSES_RUNNING, MAX, "Running processes")
                        .column(COLUMN_PROCESSES_SLEEPING, MAX, "Sleeping processes")
                        .title(AppConstants.INSTANCE.metrics_server_vert_procs_title())
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
                        .column(COLUMN_DISK_FREE, AppConstants.INSTANCE.metrics_server_vert_du_free())
                        .column(COLUMN_DISK_USED, AppConstants.INSTANCE.metrics_server_vert_du_used())
                        .rowNumber(1)
                        .title(AppConstants.INSTANCE.metrics_server_vert_du_title())
                        .titleVisible(false)
                        .tableWidth(180)
                        .buildSettings());

        addDisplayer(serverCPU0, AppConstants.INSTANCE.metrics_server_vert_cpu_tt());
        addDisplayer(serverCPU1, AppConstants.INSTANCE.metrics_server_vert_cpu_tt());
        addDisplayer(serverMemory, AppConstants.INSTANCE.metrics_server_vert_usedmem_tt());
        addDisplayer(serverNetwork, AppConstants.INSTANCE.metrics_server_vert_netbw_tt());
        addDisplayer(serverProcessesRunning, AppConstants.INSTANCE.metrics_server_vert_procs_tt());
        addDisplayer(serverDisk, AppConstants.INSTANCE.metrics_server_vert_disk_tt());

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
        tooltip.add(displayer.asWidget());
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
        tooltip.setTitle(AppConstants.INSTANCE.metrics_server_vert_serverdown(server));
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
        tooltip.setTitle(AppConstants.INSTANCE.metrics_server_vert_default_tt());
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