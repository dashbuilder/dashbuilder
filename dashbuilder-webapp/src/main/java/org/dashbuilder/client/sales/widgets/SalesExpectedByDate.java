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
package org.dashbuilder.client.sales.widgets;

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
import static org.dashbuilder.dataset.date.DayOfWeek.*;
import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of Displayer instances.</p>
 */
public class SalesExpectedByDate extends Composite {

    interface SalesDashboardBinder extends UiBinder<Widget, SalesExpectedByDate>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    Displayer areaChartByDate;

    @UiField(provided = true)
    Displayer pieChartYears;

    @UiField(provided = true)
    Displayer pieChartQuarters;

    @UiField(provided = true)
    Displayer barChartDayOfWeek;

    @UiField(provided = true)
    Displayer pieChartByPipeline;

    @UiField(provided = true)
    Displayer tableAll;

    @UiField(provided = true)
    Displayer countrySelector;

    @UiField(provided = true)
    Displayer customerSelector;

    @UiField(provided = true)
    Displayer salesmanSelector;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "Sales pipeline";
    }

    public SalesExpectedByDate() {

        // Create the chart definitions

        areaChartByDate = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE).dynamic(80, DAY, true)
                .column(CREATION_DATE, "Creation date")
                .column(EXPECTED_AMOUNT, SUM).format("Amount", "$ #,###")
                .title("Expected pipeline")
                .titleVisible(true)
                .width(600).height(200)
                .margins(10, 80, 80, 100)
                .filterOn(true, true, true)
                .buildSettings());

        pieChartYears = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE).dynamic(YEAR, true)
                .column(CREATION_DATE, "Year")
                .column(COUNT, "#occs").format("Occurrences", "#,###")
                .title("Year")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 0, 0, 0)
                .filterOn(false, true, false)
                .buildSettings());

        pieChartQuarters = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE).fixed(QUARTER, true)
                .column(CREATION_DATE, "Creation date")
                .column(COUNT, "#occs").format("Occurrences", "#,###")
                .title("Quarter")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 0, 0, 0)
                .filterOn(false, true, false)
                .buildSettings());

        barChartDayOfWeek = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE).fixed(DAY_OF_WEEK, true).firstDay(SUNDAY)
                .column(CREATION_DATE, "Creation date")
                .column(COUNT, "#occs").format("Occurrences", "#,###")
                .title("Day of week")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 20, 80, 0)
                .horizontal()
                .filterOn(false, true, true)
                .buildSettings());


        pieChartByPipeline = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PIPELINE)
                        .column(PIPELINE, "Pipeline")
                        .column(COUNT, "#opps").format("Number of opps", "#,###")
                        .title("Pipeline")
                .titleVisible(true)
                        .width(200).height(150)
                        .margins(0, 0, 0, 0)
                        .filterOn(false, true, true)
                .buildSettings());

        tableAll = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(SALES_OPPS)
                        .title("List of Opportunities")
                        .titleVisible(true)
                        .tablePageSize(5)
                        .tableOrderEnabled(true)
                        .tableOrderDefault(AMOUNT, DESCENDING)
                        .column(COUNTRY, "Country")
                        .column(CUSTOMER, "Customer")
                        .column(PRODUCT, "Product")
                        .column(SALES_PERSON, "Salesman")
                        .column(STATUS, "Status")
                        .column(AMOUNT).format("Amount", "$ #,###")
                        .column(EXPECTED_AMOUNT).format("Expected", "$ #,###")
                        .column(CREATION_DATE).format("Creation", "MMM dd, yyyy")
                        .column(CLOSING_DATE).format("Closing", "MMM dd, yyyy")
                        .filterOn(false, true, true)
                        .buildSettings());

        // Create the selectors

        countrySelector = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newSelectorSettings()
                        .dataset(SALES_OPPS)
                        .group(COUNTRY)
                        .column(COUNTRY, "Country")
                        .column(COUNT, "#Opps").format("#,###")
                        .column(AMOUNT, SUM).format("Total", "$ #,##0.00")
                        .sort(COUNTRY, ASCENDING)
                        .filterOn(false, true, true)
                        .buildSettings());

        salesmanSelector = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newSelectorSettings()
                        .dataset(SALES_OPPS)
                        .group(SALES_PERSON)
                        .column(SALES_PERSON, "Employee")
                        .column(COUNT, "#Opps").format("#,###")
                        .column(AMOUNT, SUM).format("Total", "$ #,##0.00")
                        .sort(SALES_PERSON, ASCENDING)
                        .filterOn(false, true, true)
                        .buildSettings());

        customerSelector = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newSelectorSettings()
                        .dataset(SALES_OPPS)
                        .group(CUSTOMER)
                        .column(CUSTOMER, "Customer")
                        .column(COUNT, "#Opps").format("#,###")
                        .column(AMOUNT, SUM).format("Total", "$ #,##0.00")
                        .sort(CUSTOMER, ASCENDING)
                        .filterOn(false, true, true)
                        .buildSettings());

        // Make the displayers interact among them
        displayerCoordinator.addDisplayer(areaChartByDate);
        displayerCoordinator.addDisplayer(pieChartYears);
        displayerCoordinator.addDisplayer(pieChartQuarters);
        displayerCoordinator.addDisplayer(barChartDayOfWeek);
        displayerCoordinator.addDisplayer(pieChartByPipeline);
        displayerCoordinator.addDisplayer(tableAll);
        displayerCoordinator.addDisplayer(countrySelector);
        displayerCoordinator.addDisplayer(salesmanSelector);
        displayerCoordinator.addDisplayer(customerSelector);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
    }

    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }
}
