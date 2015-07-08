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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.backend.ClusterMetricsGenerator;
import org.dashbuilder.client.gallery.GalleryWidget;
import org.dashbuilder.client.metrics.widgets.details.DetailedServerMetrics;
import org.dashbuilder.client.metrics.widgets.vertical.VerticalServerMetrics;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.gwtbootstrap3.client.ui.Label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;

public class RealTimeMetricsDashboard extends Composite implements GalleryWidget {

    interface RealTimeMetricsDashboardBinder extends UiBinder<Widget, RealTimeMetricsDashboard>{}
    private static final RealTimeMetricsDashboardBinder uiBinder = GWT.create(RealTimeMetricsDashboardBinder.class);

    public static final String METRICS_DATASET_UUID = "clusterMetrics";
    public static final String[] METRICS_DATASET_DEFAULT_SERVERS = new String[] {"server1","server2","server3","server4"};
    public static final String BACKGROUND_COLOR = "#F8F8FF";
    private static final int DS_LOOKUP_TIMER_DELAY = 1000;
    private static final int REFRESH_TIMER_DELAY = 3000;
    private static final int NOTIFICATIONS_TIMER_DELAY = 3000;
    private static final String NOTIFICATIONS_SERVER_OFF_COLOR = "#CD5C5C";
    private static final String NOTIFICATIONS_SERVER_ON_COLOR = "#90EE90";
    
    
    // The client bundle for this widget.
    @UiField
    MetricsDashboardClientBundle resources;

    @UiField
    HorizontalPanel summaryArea;

    @UiField
    HorizontalPanel serverDetailsArea;

    @UiField
    Label notificationsLabel;

    @UiField
    FlowPanel notificationsLabelPanel;

    private String[] servers;
    final List<String> dataSetServers = new ArrayList<String>();
    private VerticalServerMetrics[] verticalServerMetrics;
    Timer dataSetLookupTimer;
    Timer refreshTimer;

    @Override
    public String getTitle() {
        return AppConstants.INSTANCE.metrics_rt_title();
    }

    @Override
    public void onClose() {
        dispose();
    }

    @Override
    public boolean feedsFrom(String dataSetId) {
        return METRICS_DATASET_UUID.equals(dataSetId);
    }

    @Override
    public void redrawAll() {
    }

    public RealTimeMetricsDashboard() {
        this(null);
    }

    public RealTimeMetricsDashboard(String[] servers) {
        this.servers = servers;

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void onLoad() {
        init();
    }

    @Override
    protected void onUnload() {
        dispose();
    }

    public void init() {
        refreshTimer = new Timer() {
            public void run() {
                refreshServerWidgets();
                refreshTimer.schedule(REFRESH_TIMER_DELAY);
            }
        };
        dataSetLookupTimer = new Timer() {
            @Override
            public void run() {
                updateDataSetServers(new Runnable() {
                    public void run() {
                        checkServersAlive();
                    }
                });
                dataSetLookupTimer.schedule(DS_LOOKUP_TIMER_DELAY);
            }
        };

        // If server list is configured, use it.
        if (servers != null) {
            dataSetServers.clear();
            for (String server : servers) {
                dataSetServers.add(server);
            }
        }

        // Initially show vertical servers summary.
        showVerticalServersSummary();
        dataSetLookupTimer.schedule(DS_LOOKUP_TIMER_DELAY);
        refreshTimer.schedule(REFRESH_TIMER_DELAY);
    }

    public void dispose() {
        dataSetLookupTimer.cancel();
        refreshTimer.cancel();
        if (verticalServerMetrics != null && verticalServerMetrics.length > 0) {
            for (VerticalServerMetrics verticalServerMetric : verticalServerMetrics) {
                if (verticalServerMetric != null && verticalServerMetric.isOn()) {
                    verticalServerMetric.off();
                    verticalServerMetric.dispose();
                }
            }
        }
    }

    public void showVerticalServersSummary() {
        hideAll();
        if (servers != null && servers.length > 0) {
            this.verticalServerMetrics = new VerticalServerMetrics[servers.length];
            for (int x = 0; x < servers.length; x++) {
                String server = servers[x];
                VerticalServerMetrics verticalServerMetrics = new VerticalServerMetrics(this, server);
                if (!dataSetServers.contains(server)) verticalServerMetrics = verticalServerMetrics.off();
                this.verticalServerMetrics[x] = verticalServerMetrics;
                summaryArea.add(this.verticalServerMetrics[x]);
            }
        }
        summaryArea.setVisible(true);
    }
    
    private VerticalServerMetrics getVerticalServerMetrics(String server) {
        if (verticalServerMetrics != null && verticalServerMetrics.length > 0) {
            for (VerticalServerMetrics verticalServerMetric : verticalServerMetrics) {
                if (verticalServerMetric.getServer().equalsIgnoreCase(server)) return verticalServerMetric;
            }
        }
        return null;
    }

    public void showServerDetails(String server) {
        dispose();
        hideAll();
        DetailedServerMetrics detailedServerMetrics = new DetailedServerMetrics(this, server, 3);
        serverDetailsArea.add(detailedServerMetrics);
        serverDetailsArea.setVisible(true);
    }

    private void hideAll() {
        hide(serverDetailsArea);
        hide(summaryArea);
    }

    private void hide(Widget w) {
        if (w.isVisible()) {
            if (w instanceof HasWidgets) removeAllChidren((HasWidgets) w);
            w.setVisible(false);
        }
    }
    
    private void removeAllChidren(HasWidgets w) {
        java.util.Iterator<Widget> itr = w.iterator();
        while(itr.hasNext()) {
            itr.next();
            itr.remove();
        }
    }
    
    private void checkServersAlive() {
        GWT.log("Checking servers alive...");
        
        if (servers != null) {
            for (String availableServer : servers) {
                boolean found = false;

                for (String currentAliveServer : dataSetServers) {
                    if (currentAliveServer.equalsIgnoreCase(availableServer)) found = true;
                }

                if (found) {
                    on(availableServer);
                } else {
                    off(availableServer);
                }
            }
        }
    }
    
    private void off(String server) {
        VerticalServerMetrics vm = getVerticalServerMetrics(server);
        if (vm.isOn()) {
            vm.off();
            showNotification(server + " has been down", NOTIFICATIONS_SERVER_OFF_COLOR);
        }
    }

    private void on(String server) {
        VerticalServerMetrics vm = getVerticalServerMetrics(server);
        if (vm.isOff()) {
            vm.on();
            showNotification(server + " has been up", NOTIFICATIONS_SERVER_ON_COLOR);
        }
    }
    
    private void showNotification(String message, String bgColor) {
        notificationsLabel.setText(message);
        notificationsLabelPanel.setVisible(true);
        notificationsLabelPanel.getElement().getStyle().setBackgroundColor(bgColor);
        Timer timer1 = new Timer() {
            @Override
            public void run() {
                notificationsLabelPanel.setVisible(false);
            }
        };
        timer1.schedule(NOTIFICATIONS_TIMER_DELAY);
    }
    
    private synchronized void refreshServerWidgets() {
        if (verticalServerMetrics != null && verticalServerMetrics.length > 0) {
            for (VerticalServerMetrics verticalServerMetric : verticalServerMetrics) {
                if (verticalServerMetric != null && verticalServerMetric.isOn()) {
                    verticalServerMetric.redraw();
                }
            }
        }
    }

    private synchronized void updateDataSetServers(final Runnable listener) {

        final DataSetLookup lookup = DataSetFactory.newDataSetLookupBuilder()
            .dataset(METRICS_DATASET_UUID)
            .filter(ClusterMetricsGenerator.COLUMN_TIMESTAMP, timeFrame("-1second"))
            .group(ClusterMetricsGenerator.COLUMN_SERVER)
            .column(ClusterMetricsGenerator.COLUMN_SERVER)
            .buildLookup();

        try {
            DataSetClientServices.get().lookupDataSet(lookup, new DataSetReadyCallback() {
                @Override
                public void callback(DataSet dataSet) {
                    if (dataSet != null && dataSet.getRowCount() > 0) {
                        List values = dataSet.getColumns().get(0).getValues();
                        if (values != null && !values.isEmpty()) {
                            boolean fireListener = false;
                            for (Object value : values) {
                                if (!dataSetServers.contains(value.toString())) {
                                    dataSetServers.add(value.toString());
                                    fireListener = true;
                                }
                                Iterator<String> existingServersIt = dataSetServers.iterator();
                                while (existingServersIt.hasNext()) {
                                    String s = existingServersIt.next();
                                    if (!values.contains(s)) {
                                        existingServersIt.remove();
                                        fireListener = true;
                                    }
                                    
                                }
                            }
                            if (fireListener) listener.run();
                        }
                    }
                }
    
                @Override
                public void notFound() {
                    GWT.log("DataSet with UUID [" + METRICS_DATASET_UUID + "] not found.");
                }

                @Override
                public boolean onError(final ClientRuntimeError error) {
                    // TODO
                    GWT.log("Error occurred in RealTimeMetricsDashboard#updateDataSetServers!");
                    return false;
                }
            });
            
        } catch (Exception e) {
            GWT.log("Error looking up dataset with UUID [" + METRICS_DATASET_UUID + "]");
        }
    }
}