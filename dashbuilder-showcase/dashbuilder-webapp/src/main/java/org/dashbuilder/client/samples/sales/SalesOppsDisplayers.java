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
package org.dashbuilder.client.samples.sales;

import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;

import static org.dashbuilder.model.displayer.DataDisplayerType.*;
import static org.dashbuilder.model.samples.SalesConstants.*;

/**
 * A set of data displayer definitions built on top of the the Sales Opportunities sample data set.
 */
public class SalesOppsDisplayers {

    public static final DataDisplayer PIE_CHART_PIPELINE_STATUS = new DataDisplayerBuilder()
            .title("Pipeline status")
            .type(PIECHART)
            .x("Pipeline")
            .y("Number of opps")
            .build();

    public static final DataDisplayer AREA_CHART_EXPECTED_PIPELINE = new DataDisplayerBuilder()
            .title("Expected Pipeline")
            .type(AREACHART)
            .x("Closing date")
            .y("Expected amount")
            .build();

    public static final DataDisplayer PIE_CHART_BY_STATUS = new DataDisplayerBuilder()
            .title("By Status")
            .type(PIECHART)
            .x("Status")
            .y("Total amount")
            .build();

    public static final DataDisplayer PIE_CHART_BY_SALES_PERSON = new DataDisplayerBuilder()
            .title("By Sales Person")
            .type(PIECHART)
            .x("Sales person")
            .y("Total amount")
            .build();

    public static final DataDisplayer BAR_CHART_BY_PRODUCT = new DataDisplayerBuilder()
            .title("By Product")
            .type("barchart")
            .x("Product")
            .y("Total amount")
            .build();

    public static final DataDisplayer BAR_CHART_BY_COUNTRY = new DataDisplayerBuilder()
            .title("By Country")
            .type("barchart")
            .x("Country")
            .y("Total amount")
            .build();

    public static final DataDisplayer BAR_CHART_BY_PROBABILITY = new DataDisplayerBuilder()
            .title("By Probability")
            .type(BARCHART)
            .x("Probability")
            .y("Total amount")
            .build();

    public static final DataDisplayer TABLE_COUNTRY_SUMMARY = new DataDisplayerBuilder()
            .title("Country Summary")
            .type(TABLE)
            .build();
}
