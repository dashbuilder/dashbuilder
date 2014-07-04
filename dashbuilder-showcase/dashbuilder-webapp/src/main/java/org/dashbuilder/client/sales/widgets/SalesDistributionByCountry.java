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
import org.dashbuilder.displayer.client.DataViewer;
import org.dashbuilder.displayer.client.DataViewerCoordinator;
import org.dashbuilder.displayer.client.DataViewerHelper;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.displayer.DisplayerFactory;

import static org.dashbuilder.client.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of DataViewer instances.</p>
 */
public class SalesDistributionByCountry extends Composite {

    interface SalesDashboardBinder extends UiBinder<Widget, SalesDistributionByCountry>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    DataViewer bubbleByCountry;

    @UiField(provided = true)
    DataViewer mapByCountry;

    @UiField(provided = true)
    DataViewer tableAll;

    public SalesDistributionByCountry() {

        // Create the chart definitions

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
                .width(450).height(300)
                .margins(20, 50, 50, 0)
                .column(COUNTRY, "Country")
                .column("opps", "Number of opportunities")
                .column(PROBABILITY, "Average probability")
                .column(COUNTRY, "Country")
                .column(EXPECTED_AMOUNT, "Expected amount")
                .filterOn(false, true, true)
                .buildDisplayer());

        mapByCountry = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .count("opps")
                .sum(EXPECTED_AMOUNT)
                .buildLookup(),
                DisplayerFactory.newMapChart()
                .title("By Country")
                .width(450).height(290)
                .margins(10, 10, 10, 10)
                .column("Country")
                .column("Number of opportunities")
                .column("Total amount")
                .filterOn(false, true, true)
                .buildDisplayer());

        tableAll = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .buildLookup(),
                DisplayerFactory.newTable()
                .title("List of Opportunities")
                .titleVisible(true)
                .tablePageSize(8)
                .tableOrderEnabled(true)
                .tableOrderDefault(AMOUNT, DESCENDING)
                .column(COUNTRY, "Country")
                .column(CUSTOMER, "Customer")
                .column(PRODUCT, "Product")
                .column(SALES_PERSON, "Salesman")
                .column(STATUS, "Status")
                .column(CREATION_DATE, "Creation")
                .column(EXPECTED_AMOUNT, "Expected")
                .column(CLOSING_DATE, "Closing")
                .column(AMOUNT, "Amount")
                .filterOn(false, true, true)
                .buildDisplayer());

        // Make that charts interact among them
        DataViewerCoordinator viewerCoordinator = new DataViewerCoordinator();
        viewerCoordinator.addViewer(bubbleByCountry);
        viewerCoordinator.addViewer(mapByCountry);
        viewerCoordinator.addViewer(tableAll);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        viewerCoordinator.drawAll();
    }
}
