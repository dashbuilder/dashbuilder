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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;

import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;

/**
 * A set of displayer definitions for the Sales Dashboard
 */
@ApplicationScoped
public class SalesOppsDisplayers {

    public static final String OPPS_BY_EMPLOYEE = "opps-by-pipeline";
    public static final String OPPS_EXPECTED_PIPELINE = "opps-expected-pipeline";
    public static final String OPPS_SALES_PER_YEAR = "opps-sales-per-year";
    public static final String OPPS_BY_STATUS = "opps-by-status";
    public static final String OPPS_BY_SALESMAN = "opps-by-salesman";
    public static final String OPPS_BY_PRODUCT = "opps-by-product";
    public static final String OPPS_BY_COUNTRY = "opps-by-country";
    public static final String OPPS_COUNTRY_SUMMARY = "opps-country-summary";
    public static final String OPPS_ALL = "opps-allopps-listing";

    public static final DisplayerSettings PIE_PIPELINE = DisplayerSettingsFactory.newPieChartSettings()
            .uuid(OPPS_BY_EMPLOYEE)
            .dataset(SALES_OPPS)
            .group(PIPELINE)
            .count("occurrences")
            .title("Pipeline status")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Pipeline")
            .column("Number of opps")
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings PIE_STATUS = DisplayerSettingsFactory.newPieChartSettings()
            .uuid(OPPS_BY_STATUS)
            .dataset(SALES_OPPS)
            .group(STATUS)
            .sum(AMOUNT)
            .title("By Status")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Status")
            .column("Total amount")
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings PIE_SALES_PERSON = DisplayerSettingsFactory.newPieChartSettings()
            .uuid(OPPS_BY_SALESMAN)
            .dataset(SALES_OPPS)
            .group(SALES_PERSON)
            .sum(AMOUNT)
            .title("By Sales Person")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Sales person")
            .column("Total amount")
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings AREA_EXPECTED_AMOUNT = DisplayerSettingsFactory.newAreaChartSettings()
            .uuid(OPPS_EXPECTED_PIPELINE)
            .dataset(SALES_OPPS)
            .group(CLOSING_DATE, 24, DateIntervalType.MONTH)
            .sum(EXPECTED_AMOUNT)
            .title("Expected Amount")
            .titleVisible(false)
            .margins(20, 50, 100, 100)
            .column("Closing date")
            .column("Expected amount")
            .filterOn(true, true, true)
            .buildSettings();

    public static final DisplayerSettings HBAR_PRODUCT = DisplayerSettingsFactory.newBarChartSettings()
            .uuid(OPPS_BY_PRODUCT)
            .dataset(SALES_OPPS)
            .group(PRODUCT)
            .sum(AMOUNT)
            .title("By Product")
            .titleVisible(false)
            .margins(10, 50, 100, 100)
            .column("Product")
            .column("Total amount")
            .horizontal()
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings HBAR_COUNTRY = DisplayerSettingsFactory.newBarChartSettings()
            .uuid(OPPS_BY_COUNTRY)
            .dataset(SALES_OPPS)
            .group(COUNTRY)
            .count("opps")
            .min(AMOUNT, "min")
            .max(AMOUNT, "max")
            .avg(AMOUNT, "avg")
            .sum(AMOUNT, "total")
            .title("By Country")
            .titleVisible(false)
            .margins(10, 80, 100, 100)
            .column(COUNTRY, "Country")
            .column("total", "Total amount")
            .horizontal()
            .filterOn(false, true, true)
            .buildSettings();

    public static final DisplayerSettings TABLE_COUNTRY = DisplayerSettingsFactory.newTableSettings()
            .uuid(OPPS_COUNTRY_SUMMARY)
            .dataset(SALES_OPPS)
            .group(COUNTRY)
            .count("opps")
            .min(AMOUNT, "min")
            .max(AMOUNT, "max")
            .avg(AMOUNT, "avg")
            .sum(AMOUNT, "total")
            .title("Country Summary")
            .titleVisible(false)
            .column("country", "COUNTRY")
            .column("total", "TOTAL")
            .column("opps", "NUMBER")
            .column("avg", "AVERAGE")
            .column("min", "MIN")
            .column("max", "MAX")
            .tablePageSize(20)
            .filterOff(true)
            .buildSettings();

    public static final DisplayerSettings TABLE_ALL = DisplayerSettingsFactory.newTableSettings()
            .uuid(OPPS_ALL)
            .dataset(SALES_OPPS)
            .title("List of Opportunities")
            .titleVisible(false)
            .tablePageSize(20)
            .tableOrderEnabled(true)
            .tableOrderDefault(AMOUNT, DESCENDING)
            .filterOn(true, true, true)
            .buildSettings();
}
