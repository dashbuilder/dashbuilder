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
package org.dashbuilder.client.expenses;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.client.expenses.ExpenseConstants.*;

/**
 * A composite widget that represents an entire dashboard sample based on a UI binder template.
 * The dashboard itself is composed by a set of Displayer instances.</p>
 * <p>The data set that feeds this dashboard is a CSV file stored into an specific server folder so
 * that is auto-deployed during server start up: <code>dashbuilder-showcase/dashbuilder-webapp/src/main/webapp/datasets/expenseReports.csv</code></p>
 */
public class ExpensesDashboard extends Composite {

    interface ExpensesDashboardBinder extends UiBinder<Widget, ExpensesDashboard>{}
    private static final ExpensesDashboardBinder uiBinder = GWT.create(ExpensesDashboardBinder.class);

    @UiField(provided = true)
    Displayer pieByOffice;

    @UiField(provided = true)
    Displayer barByDepartment;

    @UiField(provided = true)
    Displayer lineByDate;

    @UiField(provided = true)
    Displayer bubbleByEmployee;

    @UiField(provided = true)
    Displayer tableAll;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public ExpensesDashboard() {

        // Create the chart definitions

        pieByOffice = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                        .dataset(EXPENSES)
                        .group(OFFICE)
                        .sum(AMOUNT)
                        .group(DEPARTMENT)
                        .sum(AMOUNT)
                        .group(EMPLOYEE)
                        .sum(AMOUNT)
                        .title("Expenses by Office")
                        .width(400).height(250)
                        .margins(10, 10, 10, 0)
                        .column("Office")
                        .column("Total Amount")
                        .filterOn(true, true, true)
                        .buildSettings());

        barByDepartment = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(EXPENSES)
                        .group(DEPARTMENT)
                        .sum(AMOUNT)
                        .title("Expenses by Department")
                        .width(400).height(250)
                        .margins(10, 50, 50, 20)
                        .column(DEPARTMENT, "Department")
                        .column(AMOUNT, "Total Amount")
                        .filterOn(false, true, true)
                        .buildSettings());

        bubbleByEmployee = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBubbleChartSettings()
                        .dataset(EXPENSES)
                        .group(EMPLOYEE)
                        .count("expenses")
                        .avg(AMOUNT, "average")
                        .sum(AMOUNT)
                        .title("Expenses by Employee")
                        .titleVisible(false)
                        .width(700).height(250)
                        .margins(10, 50, 80, 130)
                        .column(EMPLOYEE, "Employee")
                        .column(AMOUNT, "Total amount")
                        .column("average", "Average amount")
                        .column(EMPLOYEE, "Employee")
                        .column("expenses", "Number of expense reports")
                        .filterOn(false, true, true)
                        .buildSettings());

        lineByDate = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(EXPENSES)
                        .group(DATE, 8, DAY_OF_WEEK)
                        .sum(AMOUNT)
                        .title("Expenses evolution")
                        .titleVisible(false)
                        .width(500).height(250)
                        .margins(10, 50, 50, 50)
                        .column(DATE, "Date")
                        .column(AMOUNT, "Total Amount")
                        .filterOn(true, true, true)
                        .buildSettings());

        tableAll = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(EXPENSES)
                        .title("List of expense reports")
                        .titleVisible(false)
                        .tablePageSize(8)
                        .tableOrderEnabled(true)
                        .tableOrderDefault(AMOUNT, DESCENDING)
                        .column(OFFICE, "Office")
                        .column(DEPARTMENT, "Department")
                        .column(EMPLOYEE, "Employee")
                        .column(AMOUNT, "Amount")
                        .column(DATE, "Date")
                        .filterOn(true, true, true)
                        .tableWidth(600)
                        .buildSettings());

        // Make that charts interact among them
        displayerCoordinator.addDisplayer(pieByOffice);
        displayerCoordinator.addDisplayer(barByDepartment);
        displayerCoordinator.addDisplayer(bubbleByEmployee);
        displayerCoordinator.addDisplayer(lineByDate);
        displayerCoordinator.addDisplayer(tableAll);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
    }

    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }
}
