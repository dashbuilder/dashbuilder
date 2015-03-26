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
package org.dashbuilder.client.gallery;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.expenses.ExpenseConstants;
import org.dashbuilder.client.expenses.ExpensesDashboard;
import org.dashbuilder.client.metrics.AnalyticMetricsDashboard;
import org.dashbuilder.client.metrics.ClusterMetricsDashboard;
import org.dashbuilder.client.metrics.RealTimeMetricsDashboard;
import org.dashbuilder.client.sales.widgets.SalesDistributionByCountry;
import org.dashbuilder.client.sales.widgets.SalesExpectedByDate;
import org.dashbuilder.client.sales.widgets.SalesGoals;
import org.dashbuilder.client.sales.widgets.SalesTableReports;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.date.TimeAmount;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetModifiedEvent;
import org.dashbuilder.dataset.events.DataSetPushOkEvent;
import org.dashbuilder.shared.sales.SalesConstants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;
import static org.uberfire.workbench.events.NotificationEvent.NotificationType.INFO;

@WorkbenchScreen(identifier = "GalleryWidgetScreen")
@ApplicationScoped
public class GalleryWidgetPresenter {

    private GalleryWidget widget;

    @Inject
    private Event<NotificationEvent> workbenchNotification;

    @WorkbenchPartTitle
    public String getTitle() {
        return widget.getTitle();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return widget;
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest) {
        String widgetId = placeRequest.getParameter("widgetId", "");
        widget = getWidget(widgetId);
    }

    @OnClose
    public void onClose() {
        widget.onClose();
    }

    private GalleryWidget getWidget(String widgetId) {
        if ("salesGoal".equals(widgetId)) return new SalesGoals();
        if ("salesPipeline".equals(widgetId)) return new SalesExpectedByDate();
        if ("salesPerCountry".equals(widgetId)) return new SalesDistributionByCountry();
        if ("salesReports".equals(widgetId)) return new SalesTableReports();
        if ("expenseReports".equals(widgetId)) return new ExpensesDashboard();
        if ("clusterMetrics".equals(widgetId)) return new ClusterMetricsDashboard();
        if ("metrics_realtime".equals(widgetId)) return new RealTimeMetricsDashboard(RealTimeMetricsDashboard.METRICS_DATASET_DEFAULT_SERVERS);
        if ("metrics_analytic".equals(widgetId)) return new AnalyticMetricsDashboard();

        throw new IllegalArgumentException("Unknown gallery widget: " + widgetId);
    }

    // Catch some data set related events and display workbench notifications only and only if:
    // - The data set refresh is enabled and
    // - It's refresh rate is greater than 60 seconds (avoid tons of notifications in "real-time" scenarios)

    private void onDataSetModifiedEvent(@Observes DataSetModifiedEvent event) {
        checkNotNull("event", event);

        DataSetDef def = event.getDataSetDef();
        String targetUUID = event.getDataSetDef().getUUID();
        TimeAmount timeFrame = def.getRefreshTimeAmount();
        boolean noRealTime = timeFrame == null || timeFrame.toMillis() > 60000;

        if ((!def.isRefreshAlways() || noRealTime) && widget.feedsFrom(targetUUID)) {
            workbenchNotification.fire(new NotificationEvent("The data set has been modified. Refreshing the view ...", INFO));
        }
    }

    private void onDataSetPushOkEvent(@Observes DataSetPushOkEvent event) {
        checkNotNull("event", event);
        checkNotNull("event", event.getDataSetMetadata());

        DataSetMetadata metadata = event.getDataSetMetadata();
        DataSetDef def = metadata.getDefinition();
        TimeAmount timeFrame = def.getRefreshTimeAmount();
        if (timeFrame == null || timeFrame.toMillis() > 60000) {
            workbenchNotification.fire(new NotificationEvent("Data set loaded from server [" + def.getProvider() + ", " + event.getDataSetMetadata().getEstimatedSize() + " Kb]", INFO));
        }
    }
}
