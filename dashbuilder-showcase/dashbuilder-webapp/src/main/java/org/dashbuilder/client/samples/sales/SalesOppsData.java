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

import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.dataset.DataSetRef;

import static org.dashbuilder.model.dataset.group.DateIntervalType.*;
import static org.dashbuilder.model.dataset.group.ScalarFunctionType.*;
import static org.dashbuilder.model.displayer.DataDisplayerType.*;
import static org.dashbuilder.model.samples.SalesConstants.*;

/**
 * Multiple data set definitions created on top of the Sales Opportunities sample data set.
 */
public class SalesOppsData {

    public static DataSetRef byEmployee() {
        return new DataSetLookupBuilder()
            .uuid(SALES_OPPS)
            .domain(PIPELINE)
            .range(AMOUNT, "occurrences", COUNT)
            .build();
    }

    public static DataSetRef byCountry() {
        return new DataSetLookupBuilder()
            .uuid(SALES_OPPS)
            .domain(COUNTRY)
            .range(AMOUNT, SUM)
            .build();
    }

    public static DataSetRef expectedPipeline() {
        return new DataSetLookupBuilder()
                .uuid(SALES_OPPS)
                .domain(CLOSING_DATE, 24, MONTH)
                .range(EXPECTED_AMOUNT, SUM)
                .build();
    }

    public static DataSetRef byProbability() {
        return new DataSetLookupBuilder()
                .uuid(SALES_OPPS)
                .domain(PROBABILITY)
                .range(AMOUNT, SUM)
                .build();
    }

    public static DataSetRef byStatus() {
        return new DataSetLookupBuilder()
                .uuid(SALES_OPPS)
                .domain(STATUS)
                .range(AMOUNT, SUM)
                .build();
    }

    public static DataSetRef bySalesman() {
        return new DataSetLookupBuilder()
                .uuid(SALES_OPPS)
                .domain(SALES_PERSON)
                .range(AMOUNT, SUM)
                .build();
    }

    public static DataSetRef byProduct() {
        return new DataSetLookupBuilder()
            .uuid(SALES_OPPS)
            .domain(PRODUCT)
            .range(AMOUNT, SUM)
            .build();
    }

    public static DataSetRef countrySummary() {
        return new DataSetLookupBuilder()
                .uuid(SALES_OPPS)
                .domain(COUNTRY, "Country")
                .range(AMOUNT, "#Opps", COUNT)
                .range(AMOUNT, "Min", MIN)
                .range(AMOUNT, "Max", MAX)
                .range(AMOUNT, "Average", AVERAGE)
                .range(AMOUNT, "Total", SUM)
                .build();
    }

    public static DataSetRef listOfOpportunities(int offset, int rows) {
        return new DataSetLookupBuilder()
                .uuid(SALES_OPPS)
                .rowOffset(offset)
                .rowNumber(rows)
                .build();
    }
}