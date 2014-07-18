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
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DataViewerHelper;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.displayer.DisplayerSettingsFactory;

import static org.dashbuilder.client.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of Displayer instances.</p>
 */
public class SalesDistributionByCountry extends Composite {

    interface SalesDashboardBinder extends UiBinder<Widget, SalesDistributionByCountry>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    Displayer bubbleByCountry;

    @UiField(provided = true)
    Displayer mapByCountry;

    @UiField(provided = true)
    Displayer tableAll;

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
                DisplayerSettingsFactory.newBubbleChartSettings()
                .title("Opportunities distribution by Country ")
                .width(450).height(300)
                .margins(20, 50, 50, 0)
                .column(COUNTRY, "Country")
                .column("opps", "Number of opportunities")
                .column(PROBABILITY, "Average probability")
                .column(COUNTRY, "Country")
                .column(EXPECTED_AMOUNT, "Expected amount")
                .filterOn(false, true, true)
                .buildDisplayerSettings());

        mapByCountry = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .count("opps")
                .sum(EXPECTED_AMOUNT)
                .buildLookup(),
                DisplayerSettingsFactory.newMapChartSettings()
                .title("By Country")
                .width(450).height(290)
                .margins(10, 10, 10, 10)
                .column("Country")
                .column("Number of opportunities")
                .column("Total amount")
                .filterOn(false, true, true)
                .buildDisplayerSettings());

        tableAll = DataViewerHelper.lookup(
                DataSetFactory.newDSLookup()
                .dataset(SALES_OPPS)
                .buildLookup(),
                DisplayerSettingsFactory.newTableSettings()
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
                .buildDisplayerSettings());

        // Make that charts interact among them
        DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();
        displayerCoordinator.addDisplayer(bubbleByCountry);
        displayerCoordinator.addDisplayer(mapByCountry);
        displayerCoordinator.addDisplayer(tableAll);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
    }
}
