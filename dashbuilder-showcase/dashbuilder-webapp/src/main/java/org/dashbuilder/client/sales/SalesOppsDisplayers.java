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

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;

import static org.dashbuilder.client.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;

/**
 * A set of displayer definitions for the Sales Dashboard
 */
public class SalesOppsDisplayers {

    public static final DisplayerSettings PIE_PIPELINE = DisplayerSettingsFactory.newPieChartSettings()
            .title("Pipeline status")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Pipeline")
            .column("Number of opps")
            .buildDisplayerSettings();

    public static final DisplayerSettings PIE_STATUS = DisplayerSettingsFactory.newPieChartSettings()
            .title("By Status")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Status")
            .column("Total amount")
            .buildDisplayerSettings();

    public static final DisplayerSettings PIE_SALES_PERSON = DisplayerSettingsFactory.newPieChartSettings()
            .title("By Sales Person")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Sales person")
            .column("Total amount")
            .buildDisplayerSettings();

    public static final DisplayerSettings AREA_EXPECTED_AMOUNT = DisplayerSettingsFactory.newAreaChartSettings()
            .title("Expected Amount")
            .titleVisible(false)
            .margins(20, 50, 100, 100)
            .column("Closing date")
            .column("Expected amount")
            .buildDisplayerSettings();

    public static final DisplayerSettings HBAR_PRODUCT = DisplayerSettingsFactory.newBarChartSettings()
            .title("By Product")
            .titleVisible(false)
            .margins(10, 50, 100, 100)
            .column("Product")
            .column("Total amount")
            .horizontal()
            .buildDisplayerSettings();

    public static final DisplayerSettings HBAR_COUNTRY = DisplayerSettingsFactory.newBarChartSettings()
            .title("By Country")
            .titleVisible(false)
            .margins(10, 80, 100, 100)
            .column(COUNTRY, "Country")
            .column("total", "Total amount")
            .horizontal()
            .buildDisplayerSettings();

    public static final DisplayerSettings TABLE_COUNTRY = DisplayerSettingsFactory.newTableSettings()
            .title("Country Summary")
            .titleVisible(false)
            .column("country", "COUNTRY")
            .column("total", "TOTAL")
            .column("opps", "NUMBER")
            .column("avg", "AVERAGE")
            .column("min", "MIN")
            .column("max", "MAX")
            .tablePageSize(20)
            .buildDisplayerSettings();

    public static final DisplayerSettings TABLE_ALL = DisplayerSettingsFactory.newTableSettings()
            .title("List of Opportunities")
            .titleVisible(false)
            .tablePageSize(20)
            .tableOrderEnabled(true)
            .tableOrderDefault(AMOUNT, DESCENDING)
            .buildDisplayerSettings();
}
