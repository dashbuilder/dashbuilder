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
import org.dashbuilder.client.gallery.GalleryWidget;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.dataset.group.DateIntervalType;

import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of Displayer instances.</p>
 */
public class SalesTableReports extends Composite implements GalleryWidget {

    interface SalesDashboardBinder extends UiBinder<Widget, SalesTableReports>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    Displayer tableByProduct;

    @UiField(provided = true)
    Displayer tableBySalesman;

    @UiField(provided = true)
    Displayer tableByCountry;

    @UiField(provided = true)
    Displayer tableByYear;

    @UiField(provided = true)
    Displayer tableAll;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    @Override
    public String getTitle() {
        return "Sales reports";
    }

    @Override
    public void onClose() {
        displayerCoordinator.closeAll();
    }

    @Override
    public boolean feedsFrom(String dataSetId) {
        return SALES_OPPS.equals(dataSetId);
    }

    @Override
    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }

    public SalesTableReports() {

        // Create the chart definitions

        tableAll = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
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
                .buildSettings());

        tableByCountry = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .column(COUNTRY, "Country")
                .column(COUNT, "#Opps")
                .column(AMOUNT, MIN, "Min")
                .column(AMOUNT, MAX, "Max")
                .column(AMOUNT, AVERAGE, "Average")
                .column(AMOUNT, SUM, "Total")
                .title("Country summary")
                .titleVisible(false)
                .tablePageSize(8)
                .tableOrderEnabled(true)
                .tableOrderDefault("Total", DESCENDING)
                .filterOn(false, true, true)
                .buildSettings());

        tableByProduct = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .group(PRODUCT)
                .column(PRODUCT, "Product")
                .column(COUNT, "#Opps")
                .column(AMOUNT, MIN, "Min")
                .column(AMOUNT, MAX, "Max")
                .column(AMOUNT, AVERAGE, "Average")
                .column(AMOUNT, SUM, "Total")
                .title("Product summary")
                .titleVisible(false)
                .tablePageSize(8)
                .tableOrderEnabled(true)
                .tableOrderDefault("Total", DESCENDING)
                .filterOn(false, true, true)
                .buildSettings());

        tableBySalesman = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .group(SALES_PERSON)
                .column(SALES_PERSON, "Sales person")
                .column(COUNT, "#Opps")
                .column(AMOUNT, MIN, "Min")
                .column(AMOUNT, MAX, "Max")
                .column(AMOUNT, AVERAGE, "Average")
                .column(AMOUNT, SUM, "Total")
                .title("Sales by person")
                .titleVisible(false)
                .tablePageSize(8)
                .tableOrderEnabled(true)
                .tableOrderDefault("Total", DESCENDING)
                .filterOn(false, true, true)
                .buildSettings());

        tableByYear = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .group(CREATION_DATE).dynamic(DateIntervalType.YEAR, true)
                .column(CREATION_DATE, "Creation date")
                .column(COUNT, "#Opps")
                .column(AMOUNT, MIN, "Min")
                .column(AMOUNT, MAX, "Max")
                .column(AMOUNT, AVERAGE, "Average")
                .column(AMOUNT, SUM, "Total")
                .title("Year summary")
                .titleVisible(false)
                .tablePageSize(8)
                .tableOrderEnabled(true)
                .tableOrderDefault("Total", DESCENDING)
                .filterOn(false, true, true)
                .buildSettings());

        // Make that charts interact among them
        displayerCoordinator.addDisplayer(tableByCountry);
        displayerCoordinator.addDisplayer(tableByProduct);
        displayerCoordinator.addDisplayer(tableBySalesman);
        displayerCoordinator.addDisplayer(tableByYear);
        displayerCoordinator.addDisplayer(tableAll);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
    }
}
