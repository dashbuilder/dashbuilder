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
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.metrics.animation.VisibilityAnimation;
import org.dashbuilder.client.metrics.widgets.details.DetailedServerMetrics;
import org.dashbuilder.client.metrics.widgets.summary.SummaryMetrics;
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

    @UiField
    FocusPanel buttonHistory;

    @UiField
    FocusPanel buttonNow;
    
    private boolean isViewingSummary;
    private boolean isViewingVerticalSummary;
    private boolean isViewingServerDetails;

    public String getTitle() {
        return "System Metrics Dashboard";
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
    }

    private void showSummary() {
        hideAll();
        SummaryMetrics summaryMetrics = new SummaryMetrics();
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
            for (int x = 0; x < servers.length; x++) {
                String server = servers[x];
                VerticalServerMetrics verticalServerMetrics = null;
                if (x == 0) verticalServerMetrics = new VerticalServerMetrics(this, server, true);
                else verticalServerMetrics = new VerticalServerMetrics(this, server);
                verticalSummaryArea.add(verticalServerMetrics);
            }
        }
        isViewingVerticalSummary = true;
        disableButtonNow();
        title.setText("Current system server metrics (real-time dashboard)");
        VisibilityAnimation animation = new VisibilityAnimation(verticalSummaryArea);
        animation.run(ANIMATION_DURATION);

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
        // TODO: Perfom a real dataset lookup.
        return new String[] {"server1","server2","server3","server4","server5"};
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

}