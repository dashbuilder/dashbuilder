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
package org.dashbuilder.client.lienzo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.renderer.lienzo.client.LienzoRenderer;

import static org.dashbuilder.client.expenses.ExpenseConstants.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.SUM;

/**
 * A composite widget that represents an entire dashboard sample based on a UI binder template.
 * The dashboard itself is composed by a set of Displayer instances.</p>
 * <p>The data set that feeds this dashboard is a CSV file stored into an specific server folder so
 * that is auto-deployed during server start up: <code>dashbuilder-webapp/src/main/webapp/datasets/expenseReports.csv</code></p>
 */
public class LienzoDashboard extends Composite {

    interface LienzoDashboardBinder extends UiBinder<Widget, LienzoDashboard>{}
    private static final LienzoDashboardBinder uiBinder = GWT.create(LienzoDashboardBinder.class);

    @UiField(provided = true)
    Displayer lienzoDisplayer;

    @UiField(provided = true)
    Displayer googleDisplayer;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "Lienzo charts";
    }

    public LienzoDashboard() {

        // Create the chart definitions
        lienzoDisplayer = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(EXPENSES)
                        .group(DEPARTMENT)
                        .column(DEPARTMENT)
                        .column(AMOUNT, SUM, "Total Amount")
                        .title("Expenses by Department (Lienzo)")
                        .width(600).height(400)
                        .margins(10, 50, 50, 20)
                        .filterOn(false, false, false)
                        .renderer(LienzoRenderer.UUID)
                        .buildSettings());

        googleDisplayer = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(EXPENSES)
                        .group(DEPARTMENT)
                        .column(DEPARTMENT)
                        .column(AMOUNT, SUM, "Total Amount")
                        .title("Expenses by Department (Google)")
                        .width(600).height(400)
                        .margins(10, 50, 50, 20)
                        .filterOn(false, false, false)
                        .buildSettings());

        // Make that charts interact among them
        displayerCoordinator.addDisplayer(lienzoDisplayer);
        displayerCoordinator.addDisplayer(googleDisplayer);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
    }

    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }
}
