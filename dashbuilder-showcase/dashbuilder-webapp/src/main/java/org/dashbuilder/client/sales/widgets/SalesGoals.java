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
import org.dashbuilder.client.displayer.DataViewer;
import org.dashbuilder.client.displayer.DataViewerCoordinator;
import org.dashbuilder.client.displayer.DataViewerHelper;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.displayer.DisplayerFactory;

import static org.dashbuilder.client.sales.SalesConstants.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of DataViewer instances.</p>
 */
public class SalesGoals extends Composite {

    interface SalesDashboardBinder extends UiBinder<Widget, SalesGoals>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    DataViewer meterChartAmount;

    @UiField(provided = true)
    DataViewer lineChartByDate;

    @UiField(provided = true)
    DataViewer barChartByProduct;

    @UiField(provided = true)
    DataViewer barChartByEmployee;

    @UiField(provided = true)
    DataViewer bubbleByCountry;

    public SalesGoals() {

        // Create the chart definitions

        meterChartAmount = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .sum(AMOUNT)
                .buildLookup(),
                DisplayerFactory.newMeterChart()
                .title("Sales goal")
                .titleVisible(true)
                .width(200).height(200)
                .meter(0, 15000000, 25000000, 35000000)
                .column("Total amount")
                .buildDisplayer());

        lineChartByDate = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 80, MONTH)
                .sum(AMOUNT)
                .sum(EXPECTED_AMOUNT)
                .buildLookup(),
                DisplayerFactory.newLineChart()
                .title("Expected pipeline")
                .titleVisible(true)
                .width(800).height(200)
                .margins(10, 80, 80, 100)
                .column("Closing date")
                .column("Total amount")
                .column("Expected amount")
                .buildDisplayer());

        barChartByProduct = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(PRODUCT)
                .sum(AMOUNT)
                .sum(EXPECTED_AMOUNT)
                .buildLookup(),
                DisplayerFactory.newBarChart()
                .title("By product")
                .titleVisible(true)
                .column("Product")
                .column("Amount")
                .column("Expected")
                .width(400).height(150)
                .margins(10, 80, 80, 10)
                .vertical()
                .buildDisplayer());

        barChartByEmployee = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(SALES_PERSON)
                .sum(AMOUNT)
                .sort(AMOUNT, DESCENDING)
                .buildLookup(),
                DisplayerFactory.newBarChart()
                .title("By employee")
                .titleVisible(true)
                .column("Employee")
                .column("Amount")
                .width(400).height(150)
                .margins(10, 80, 80, 10)
                .vertical()
                .buildDisplayer());

        bubbleByCountry = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .count("opps")
                .avg(PROBABILITY)
                .sum(EXPECTED_AMOUNT)
                .buildLookup(),
                DisplayerFactory.newBubbleChart()
                .title("Opportunities distribution by Country ")
                .width(550).height(250)
                .margins(10, 30, 50, 0)
                .column(COUNTRY, "Country")
                .column("opps", "Number of opportunities")
                .column(PROBABILITY, "Average probability")
                .column(COUNTRY, "Country")
                .column(EXPECTED_AMOUNT, "Expected amount")
                .buildDisplayer());

        // Make that charts interact among them
        DataViewerCoordinator viewerCoordinator = new DataViewerCoordinator();
        viewerCoordinator.addViewer(meterChartAmount);
        viewerCoordinator.addViewer(lineChartByDate);
        viewerCoordinator.addViewer(barChartByProduct);
        viewerCoordinator.addViewer(barChartByEmployee);
        viewerCoordinator.addViewer(bubbleByCountry);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        viewerCoordinator.drawAll();
    }

}
