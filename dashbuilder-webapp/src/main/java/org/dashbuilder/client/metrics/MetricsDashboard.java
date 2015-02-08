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
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.backend.ClusterMetricsDataSetGenerator;
import org.dashbuilder.client.metrics.animation.VisibilityAnimation;
import org.dashbuilder.client.metrics.widgets.details.DetailedServerMetrics;
import org.dashbuilder.client.metrics.widgets.summary.SummaryMetrics;
import org.dashbuilder.client.metrics.widgets.vertical.VerticalServerMetrics;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.DataSetHandlerImpl;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;

import java.util.List;

import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;

public class MetricsDashboard extends Composite {

    interface MetricsDashboardBinder extends UiBinder<Widget, MetricsDashboard>{}
    private static final MetricsDashboardBinder uiBinder = GWT.create(MetricsDashboardBinder.class);

    public static final String METRICS_DATASET_UUID = "clusterMetrics";
    public static final String[] METRICS_DATASET_DEFAULT_SERVERS = new String[] {"server1","server2","server3","server4","server5"};
    private static final int ANIMATION_DURATION = 5000;
    private static final int DS_LOOKUP_TIMER_DELAY = 1000;
    private static final int NOTIFICATIONS_TIMER_DELAY = 3000;
    
    // The client bundle for this widget.
    @UiField
    MetricsDashboardClientBundle resources;

    @UiField
    HTML title;

    @UiField
    HorizontalPanel analyticSummaryArea;

    @UiField
    HorizontalPanel verticalSummaryArea;

    @UiField
    HorizontalPanel serverDetailsArea;

    @UiField
    Image backIcon;

    @UiField
    FocusPanel buttonHistory;

    @UiField
    FocusPanel buttonNow;

    @UiField
    Label notificationsLabel;

    @UiField
    FlowPanel notificationsLabelPanel;

    private boolean isViewingSummary;
    private boolean isViewingVerticalSummary;
    private boolean isViewingServerDetails;
    
    private String dataSetUUID;
    private String[] servers;
    private VerticalServerMetrics[] verticalServerMetrics;
    Timer timer;

    public String getTitle() {
        return "System Metrics Dashboard";
    }

    public MetricsDashboard(String dataSetUUID) {
        this(dataSetUUID, null);
    }
    
    public MetricsDashboard(String dataSetUUID, String[] servers) {
        this.dataSetUUID = dataSetUUID;
        this.servers = servers;
        
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Configure user actions.
        backIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                showVerticalServersSummary();
            }
        });
        setPointerCursor(backIcon);
        
        buttonNow.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (!isViewingVerticalSummary) showVerticalServersSummary();
            }
        });
        
        buttonHistory.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (!isViewingSummary) showSummary();
            }
        });
        
        // Initially show vertical servers summary.
        showVerticalServersSummary();

        timer = new Timer() {
            @Override
            public void run() {
                checkServersAlive();
                timer.schedule(DS_LOOKUP_TIMER_DELAY);
            }
        };
        timer.schedule(DS_LOOKUP_TIMER_DELAY);
    }

    private void showSummary() {
        hideAll();
        SummaryMetrics summaryMetrics = new SummaryMetrics(this);
        isViewingSummary = true;
        disableButtonHistory();
        title.setText("History summary (analytic dashboard)");
        analyticSummaryArea.add(summaryMetrics);
        VisibilityAnimation animation = new VisibilityAnimation(analyticSummaryArea);
        animation.run(ANIMATION_DURATION);
    }
    
    private void showVerticalServersSummary() {
        hideAll();
        String[] servers = getAvailableServers();
        if (servers != null && servers.length > 0) {
            this.verticalServerMetrics = new VerticalServerMetrics[servers.length];
            for (int x = 0; x < servers.length; x++) {
                String server = servers[x];
                VerticalServerMetrics verticalServerMetrics = null;
                if (x == 0) verticalServerMetrics = new VerticalServerMetrics(this, server, true);
                else if (x == servers.length - 1) verticalServerMetrics = new VerticalServerMetrics(this, server);
                else verticalServerMetrics = new VerticalServerMetrics(this, server);
                this.verticalServerMetrics[x] = verticalServerMetrics;
                verticalSummaryArea.add(this.verticalServerMetrics[x]);
            }
        }
        isViewingVerticalSummary = true;
        disableButtonNow();
        title.setText("Current system server metrics (real-time dashboard)");
        VisibilityAnimation animation = new VisibilityAnimation(verticalSummaryArea);
        animation.run(ANIMATION_DURATION);

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
        hideAll();
        DetailedServerMetrics detailedServerMetrics = new DetailedServerMetrics(this, server);
        isViewingServerDetails = true;
        disableButtonNow();
        title.setText("Current metrics for " + server);
        backIcon.setVisible(true);
        serverDetailsArea.add(detailedServerMetrics);
        VisibilityAnimation animation = new VisibilityAnimation(serverDetailsArea);
        animation.run(ANIMATION_DURATION);
    }

    private void hideAll() {
        hide(analyticSummaryArea);
        hide(serverDetailsArea);
        hide(verticalSummaryArea);
        backIcon.setVisible(false);
        isViewingSummary = false;
        isViewingVerticalSummary = false;
        isViewingServerDetails = false;
    }

    private void hide(Widget w) {
        if (w.isVisible()) {
            w.setVisible(false);
            if (w instanceof HasWidgets) removeAllChidren((HasWidgets) w);
        }
    }
    
    private void removeAllChidren(HasWidgets w) {
        java.util.Iterator<Widget> itr = w.iterator();
        while(itr.hasNext()) {
            itr.next();
            itr.remove();
        }
    }
    
    private String[] getAvailableServers() {
        // If server list is configured, use it.
        if (servers != null) return servers;
        
        // If not, use servers found in the dataset.
        return getDataSetServers();
    }
    
    private void checkServersAlive() {
        GWT.log("Checking servers alive...");
        
        String[] currentAliveServers = getDataSetServers();
        if (currentAliveServers == null) {
            GWT.log("Cannot find out alive servers.");
            return;
        }
        
        String[] availableServers = getAvailableServers();
        for (String availableServer : availableServers) {
            boolean found = false;
            
            for (String currentAliveServer : currentAliveServers) {
                if (currentAliveServer.equalsIgnoreCase(availableServer)) found = true;
            }
            
            if (found) {
                on(availableServer);
            } else {
                off(availableServer);
            }
        }
        
    }
    
    private void off(String server) {
        getVerticalServerMetrics(server).off();
        showNotification(server + " has been down");
    }

    private void on(String server) {
        getVerticalServerMetrics(server).on();
        showNotification(server + " has been up");
    }
    
    private void showNotification(String message) {
        notificationsLabel.setText(message);
        notificationsLabelPanel.setVisible(true);
        Timer timer1 = new Timer() {
            @Override
            public void run() {
                notificationsLabelPanel.setVisible(false);
            }
        };
        timer1.schedule(NOTIFICATIONS_TIMER_DELAY);
    }
    
    private String[] getDataSetServers() {
        
        DataSetLookup lookup = DataSetFactory.newDataSetLookupBuilder()
            .dataset(getDataSetUUID())
            .filter(ClusterMetricsDataSetGenerator.COLUMN_TIMESTAMP, timeFrame("1second"))
            .group(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
            .column(ClusterMetricsDataSetGenerator.COLUMN_SERVER)
            .buildLookup();

        DataSet dataSet = ClientDataSetManager.get().lookupDataSet(lookup);

        // TODO: DataSet is always null!
        
        if (dataSet != null && dataSet.getRowCount() > 0) {
            List values = dataSet.getColumns().get(0).getValues();
            if (values != null && !values.isEmpty()) {
                String[] result = new String[dataSet.getColumns().size()];
                int x = 0;
                for (Object value : values) {
                    result[x++] = value.toString();
                }
                return result;
            }
            
        }
        
        return null;        
    }
    
    private void setPointerCursor(UIObject object) {
        object.getElement().getStyle().setCursor(Style.Cursor.POINTER);
    }

    private void setAutoCursor(UIObject object) {
        object.getElement().getStyle().setCursor(Style.Cursor.AUTO);
    }
    
    private void disableButtonNow() {
        buttonNow.getElement().getStyle().setOpacity(0.3);
        buttonHistory.getElement().getStyle().setOpacity(1);
        setAutoCursor(buttonNow);
        setPointerCursor(buttonHistory);
    }

    private void disableButtonHistory() {
        buttonNow.getElement().getStyle().setOpacity(1);
        buttonHistory.getElement().getStyle().setOpacity(0.3);
        setAutoCursor(buttonHistory);
        setPointerCursor(buttonNow);
    }

    public String getDataSetUUID() {
        return dataSetUUID;
    }
}