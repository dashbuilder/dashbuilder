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
import org.dashbuilder.displayer.client.DataViewerCoordinator;
import org.dashbuilder.displayer.client.DataViewerHelper;
import org.dashbuilder.dataset.DataSetFactory;

import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.sort.SortOrder.DESCENDING;
import static org.dashbuilder.dataset.date.DayOfWeek.*;
import static org.dashbuilder.client.sales.SalesConstants.*;

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

    public SalesExpectedByDate() {

        // Create the chart definitions

        areaChartByDate = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE, 80, MONTH)
                .sum(EXPECTED_AMOUNT)
                .buildLookup(),
                DisplayerSettingsFactory.newAreaChartSettings()
                .title("Expected pipeline")
                .titleVisible(true)
                .width(850).height(200)
                .margins(10, 80, 80, 100)
                .column("Creation date")
                .column("Amount")
                .filterOn(false, true, true)
                .buildDisplayerSettings());

        pieChartYears = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE, YEAR)
                .count("occurrences")
                .buildLookup(),
                DisplayerSettingsFactory.newPieChartSettings()
                .title("Year")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 0, 0, 0)
                .filterOn(false, true, false)
                .buildDisplayerSettings());

        pieChartQuarters = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE).fixed(QUARTER)
                .count("occurrences")
                .buildLookup(),
                DisplayerSettingsFactory.newPieChartSettings()
                .title("Quarter")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 0, 0, 0)
                .filterOn(false, true, false)
                .buildDisplayerSettings());

        barChartDayOfWeek = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE).fixed(DAY_OF_WEEK).firstDay(SUNDAY)
                .count("occurrences")
                .buildLookup(),
                DisplayerSettingsFactory.newBarChartSettings()
                .title("Day of week")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 20, 80, 0)
                .horizontal()
                .filterOn(false, true, true)
                .buildDisplayerSettings());


        pieChartByPipeline = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(PIPELINE)
                .count("occurrences")
                .buildLookup(),
                DisplayerSettingsFactory.newPieChartSettings()
                .title("Pipeline")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 0, 0, 0)
                .column("Pipeline")
                .column("Number of opps")
                .filterOn(false, true, true)
                .buildDisplayerSettings());

        tableAll = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .buildLookup(),
                DisplayerSettingsFactory.newTableSettings()
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
                .column(AMOUNT, "Amount")
                .column(EXPECTED_AMOUNT, "Expected")
                .column(CREATION_DATE, "Creation")
                .column(CLOSING_DATE, "Closing")
                .filterOn(false, true, true)
                .buildDisplayerSettings());

        // Make that charts interact among them
        DataViewerCoordinator viewerCoordinator = new DataViewerCoordinator();
        viewerCoordinator.addViewer(areaChartByDate);
        viewerCoordinator.addViewer(pieChartYears);
        viewerCoordinator.addViewer(pieChartQuarters);
        viewerCoordinator.addViewer(barChartDayOfWeek);
        viewerCoordinator.addViewer(pieChartByPipeline);
        viewerCoordinator.addViewer(tableAll);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        viewerCoordinator.drawAll();
    }


}
