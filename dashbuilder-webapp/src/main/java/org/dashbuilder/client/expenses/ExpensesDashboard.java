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
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

/**
 * A composite widget that represents an entire dashboard sample based on a UI binder template.
 * The dashboard itself is composed by a set of Displayer instances.</p>
 * <p>The data set that feeds this dashboard is a CSV file stored into an specific server folder so
 * that is auto-deployed during server start up: <code>dashbuilder-webapp/src/main/webapp/datasets/expenseReports.csv</code></p>
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

    public String getTitle() {
        return "Expense reports";
    }

    public ExpensesDashboard() {

        // Create the chart definitions

        pieByOffice = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                        .dataset(EXPENSES)
                        .group(OFFICE)
                        .column(OFFICE)
                        .column(AMOUNT, SUM, "sum1")
                        .format("Office", "$ #,##0.00")
                        .group(DEPARTMENT)
                        .column(DEPARTMENT)
                        .column(AMOUNT, SUM, "sum2")
                        .format("Department", "$ #,##0.00")
                        .group(EMPLOYEE)
                        .column(EMPLOYEE)
                        .column(AMOUNT, SUM, "sum3")
                        .format("Employee", "$ #,##0.00")
                        .title("Expenses by Office")
                        .width(400).height(250)
                        .margins(10, 10, 10, 0)
                        .filterOn(true, true, true)
                        .buildSettings());

        barByDepartment = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(EXPENSES)
                        .group(DEPARTMENT)
                        .column(DEPARTMENT)
                        .column(AMOUNT, SUM).format("Total Amount", "$ #,##0.00")
                        .title("Expenses by Department")
                        .width(400).height(250)
                        .margins(10, 50, 50, 20)
                        .filterOn(false, true, true)
                        .buildSettings());

        bubbleByEmployee = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBubbleChartSettings()
                        .dataset(EXPENSES)
                        .group(EMPLOYEE)
                        .column(EMPLOYEE)
                        .column(AMOUNT, SUM).format("Total", "$ #,##0.00")
                        .column(AMOUNT, AVERAGE).format("Average", "$ #,##0.00")
                        .column(EMPLOYEE, "Employee")
                        .column(COUNT, "Number of expense reports")
                        .title("Expenses by Employee")
                        .titleVisible(false)
                        .width(600).height(280)
                        .margins(10, 50, 80, 0)
                        .filterOn(false, true, true)
                        .buildSettings());

        lineByDate = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(EXPENSES)
                        .group(DATE).dynamic(8, DAY_OF_WEEK, true)
                        .column(DATE)
                        .column(AMOUNT, SUM)
                        .format("Total Amount", "$ #,##0.00")
                        .title("Expenses evolution")
                        .titleVisible(false)
                        .width(500).height(250)
                        .margins(10, 50, 50, 50)
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
                        .column(OFFICE).format("Office")
                        .column(DEPARTMENT).format("Department")
                        .column(EMPLOYEE).format("Employee")
                        .column(AMOUNT).format("Amount", "$ #,##0.00")
                        .column(DATE).format("Date", "MMM E dd, yyyy")
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
