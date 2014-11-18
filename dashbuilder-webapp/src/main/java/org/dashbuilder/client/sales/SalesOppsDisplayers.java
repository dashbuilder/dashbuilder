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
package org.dashbuilder.client.sales;

import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;

import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

/**
 * A set of displayer definitions for the Sales Dashboard
 */
public class SalesOppsDisplayers {

    public static final DisplayerSettings OPPS_BY_PIPELINE = DisplayerSettingsFactory.newPieChartSettings()
            .uuid("opps-by-pipeline")
            .dataset(SALES_OPPS)
            .group(PIPELINE)
            .column(PIPELINE, "Pipeline")
            .column(COUNT, "Number of opps")
            .title("Pipeline status")
            .titleVisible(false)
            .width(500).height(300)
            .margins(10, 10, 10, 10)
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings OPPS_BY_STATUS = DisplayerSettingsFactory.newPieChartSettings()
            .uuid("opps-by-status")
            .dataset(SALES_OPPS)
            .group(STATUS)
            .column(STATUS, "Status")
            .column(AMOUNT, SUM, "Total amount")
            .title("By Status")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings OPPS_BY_SALESMAN = DisplayerSettingsFactory.newPieChartSettings()
            .uuid("opps-by-salesman")
            .dataset(SALES_OPPS)
            .group(SALES_PERSON)
            .column(SALES_PERSON, "Sales person")
            .column(AMOUNT, SUM, "Total amount")
            .title("By Sales Person")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings OPPS_EXPECTED_PIPELINE = DisplayerSettingsFactory.newAreaChartSettings()
            .uuid("opps-expected-pipeline")
            .dataset(SALES_OPPS)
            .group(CLOSING_DATE).dynamic(24, DateIntervalType.MONTH)
            .column(CLOSING_DATE, "Closing date")
            .column(EXPECTED_AMOUNT, SUM, "Expected amount")
            .title("Expected Amount")
            .titleVisible(false)
            .width(500).height(300)
            .margins(20, 50, 100, 100)
            .filterOn(true, true, true)
            .buildSettings();

    public static final DisplayerSettings OPPS_BY_PRODUCT = DisplayerSettingsFactory.newBarChartSettings()
            .uuid("opps-by-product")
            .dataset(SALES_OPPS)
            .group(PRODUCT)
            .column(PRODUCT, "Product")
            .column(AMOUNT, SUM, "Total amount")
            .title("By Product")
            .titleVisible(false)
            .margins(10, 50, 100, 100)
            .horizontal()
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings OPPS_BY_COUNTRY = DisplayerSettingsFactory.newBarChartSettings()
            .uuid("opps-by-country")
            .dataset(SALES_OPPS)
            .group(COUNTRY)
            .column(COUNTRY, "Country")
            .column(AMOUNT, SUM, "Total amount")
            .title("By Country")
            .titleVisible(false)
            .margins(10, 80, 100, 100)
            .horizontal()
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings OPPS_COUNTRY_SUMMARY = DisplayerSettingsFactory.newTableSettings()
            .uuid("opps-country-summary")
            .dataset(SALES_OPPS)
            .group(COUNTRY)
            .column(COUNTRY, "COUNTRY")
            .column(AMOUNT, SUM, "TOTAL")
            .column(COUNT, "NUMBER")
            .column(AMOUNT, AVERAGE, "AVERAGE")
            .column(AMOUNT, MIN, "MIN")
            .column(AMOUNT, MAX, "MAX")
            .title("Country Summary")
            .titleVisible(false)
            .tablePageSize(20)
            .filterOff(true)
            .buildSettings();

    public static final DisplayerSettings OPPS_ALLOPPS_LISTING = DisplayerSettingsFactory.newTableSettings()
            .uuid("opps-allopps-listing")
            .dataset(SALES_OPPS)
            .title("List of Opportunities")
            .titleVisible(false)
            .tablePageSize(20)
            .tableOrderEnabled(true)
            .tableOrderDefault(AMOUNT, DESCENDING)
            .filterOn(true, true, true)
            .buildSettings();
}
