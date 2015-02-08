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
package org.dashbuilder.client.metrics.widgets.summary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.backend.ClusterMetricsDataSetGenerator;
import org.dashbuilder.client.metrics.MetricsDashboard;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.dataset.group.AggregateFunctionType.MAX;

public class SummaryMetrics extends Composite {

    interface SummaryMetricsBinder extends UiBinder<Widget, SummaryMetrics>{}
    private static final SummaryMetricsBinder uiBinder = GWT.create(SummaryMetricsBinder.class);

    @UiField(provided = true)
    Displayer maxCPUxServer;

    @UiField(provided = true)
    Displayer maxMemxServerSettings;

    @UiField(provided = true)
    Displayer maxProcessesxServerSettings;

    @UiField(provided = true)
    Displayer maxNetworkxServerSettings;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "Metrics summary (Analytic)";
    }

    public SummaryMetrics(MetricsDashboard metricsDashboard) {

        buildSummary(metricsDashboard);
        
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
        
        // Draw the charts
        displayerCoordinator.drawAll();
    }

    protected void buildSummary(MetricsDashboard metricsDashboard) {
        maxCPUxServer = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(metricsDashboard.getDataSetUUID())
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_CPU0, MAX, "CPU0 Max")
                .column(ClusterMetricsDataSetGenerator.COLUMN_CPU1, MAX, "CPU1 Max")
                .title("Max CPU usage")
                .titleVisible(true)
                .vertical()
                .buildSettings());
        
        maxMemxServerSettings = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(metricsDashboard.getDataSetUUID())
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_USED, MAX, "Max used memory")
                .column(ClusterMetricsDataSetGenerator.COLUMN_MEMORY_FREE, MAX, "Max free memory")
                .title("Max Memory usage")
                .titleVisible(true)
                .vertical()
                .buildSettings());
        
        maxProcessesxServerSettings = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(metricsDashboard.getDataSetUUID())
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_RUNNING, MAX, "Max running processes")
                .column(ClusterMetricsDataSetGenerator.COLUMN_PROCESSES_SLEEPING, MAX, "Max sleeping processes")
                .title("Max processes usage")
                .titleVisible(true)
                .vertical()
                .buildSettings());
        
        maxNetworkxServerSettings = DisplayerHelper.lookupDisplayer(DisplayerSettingsFactory.newBarChartSettings()
                .dataset(metricsDashboard.getDataSetUUID())
                .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
                .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER, "Server")
                .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_TX, MAX, "Max upstream speed")
                .column(ClusterMetricsDataSetGenerator.COLUMN_NETWORK_RX, MAX, "Max downstream speed")
                .title("Max network speed")
                .titleVisible(true)
                .vertical()
                .buildSettings());

        // Make that charts interact among them
        displayerCoordinator.addDisplayer(maxCPUxServer);
        displayerCoordinator.addDisplayer(maxMemxServerSettings);
        displayerCoordinator.addDisplayer(maxProcessesxServerSettings);
        displayerCoordinator.addDisplayer(maxNetworkxServerSettings);
    }
    
}