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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.client.metrics.MetricsConstants.*;
import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.dashbuilder.dataset.group.DateIntervalType.SECOND;
import static org.dashbuilder.dataset.group.DateIntervalType.YEAR;

public class MetricsDashboard extends Composite {

    interface ExpensesDashboardBinder extends UiBinder<Widget, MetricsDashboard>{}
    private static final ExpensesDashboardBinder uiBinder = GWT.create(ExpensesDashboardBinder.class);

    @UiField(provided = true)
    Displayer usedMemory;


    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "Real-time system metrics";
    }

    public MetricsDashboard() {

        // Create the chart definitions
        usedMemory = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(METRICS_CSV_UUID)
                        .filter(EPOCH, timeFrame("10second"))
                        .group(EPOCH).dynamic(10, SECOND, true)
                        .column(EPOCH)
                        .column(VALUE, MAX, "Max")
                        .title("Used memory")
                        .width(400).height(250)
                        .refreshOn(1)
                        .buildSettings());

        // Make that charts interact among them
        displayerCoordinator.addDisplayer(usedMemory);
        
        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
        usedMemory.refreshOn();
    }
    
    protected void createElasticSearchDisplayer() {
        // Create the chart definitions
        usedMemory = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(METRICS_CSV_UUID)
                        .filter(PLUGIN, equalsTo(MEMORY))
                        .filter(COLLECTD_TYPE, equalsTo(MEMORY))
                        .filter(TYPE_INSTANCE, equalsTo(MEMORY_USED))
                        .filter(TIMESTAMP, timeFrame("1second"))
                        .group(TIMESTAMP).dynamic(10, SECOND, true)
                        .column(TIMESTAMP)
                        .column(VALUE, MAX, "Max")
                        .sort(TIMESTAMP, SortOrder.DESCENDING)
                        .title("Used memory")
                        .width(400).height(250)
                        .refreshOn(1)
                        .buildSettings());
    }

    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }
}
