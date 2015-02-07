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
import org.dashbuilder.client.metrics.animation.VisibilityAnimation;
import org.dashbuilder.client.metrics.widgets.details.DetailedServerMetrics;
import org.dashbuilder.client.metrics.widgets.vertical.VerticalServerMetrics;

public class MetricsDashboard extends Composite {

    interface MetricsDashboardBinder extends UiBinder<Widget, MetricsDashboard>{}
    private static final MetricsDashboardBinder uiBinder = GWT.create(MetricsDashboardBinder.class);

    private static final int ANIMATION_DURATION = 5000;
    
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
    

    public String getTitle() {
        return "System Metrics Dashboard Widget";
    }

    public MetricsDashboard() {

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Configure user actions.
        backIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                showVerticalServersSummary();
            }
        });
        
        // Initially show vertical servers summary.
        showVerticalServersSummary();
    }
    
    private void showVerticalServersSummary() {
        hideAll();
        String[] servers = getAvailableServers();
        if (servers != null && servers.length > 0) {
            for (int x = 0; x < servers.length; x++) {
                String server = servers[x];
                VerticalServerMetrics verticalServerMetrics = null;
                if (x == 0) verticalServerMetrics = new VerticalServerMetrics(this, server, true);
                else verticalServerMetrics = new VerticalServerMetrics(this, server);
                verticalSummaryArea.add(verticalServerMetrics);
            }
        }
        title.setText("System metrics summary");
        backIcon.setVisible(false);
        VisibilityAnimation animation = new VisibilityAnimation(verticalSummaryArea);
        animation.run(ANIMATION_DURATION);

    }

    public void showServerDetails(String server) {
        hideAll();
        DetailedServerMetrics detailedServerMetrics = new DetailedServerMetrics(this, server);
        title.setText("Details for " + server);
        backIcon.setVisible(true);
        serverDetailsArea.add(detailedServerMetrics);
        VisibilityAnimation animation = new VisibilityAnimation(serverDetailsArea);
        animation.run(ANIMATION_DURATION);
    }

    private void hideAll() {
        hide(analyticSummaryArea);
        hide(serverDetailsArea);
        hide(verticalSummaryArea);
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
        // TODO: Perfom a real dataset lookup.
        return new String[] {"server1","server2","server3","server4","server5"};
    }

}