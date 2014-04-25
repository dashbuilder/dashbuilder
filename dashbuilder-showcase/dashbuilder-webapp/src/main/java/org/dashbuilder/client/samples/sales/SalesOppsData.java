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

    public static final DataSetRef BY_EMPLOYEE = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(PIPELINE)
            .range(AMOUNT, "occurrences", COUNT)
            .build();

    public static final DataSetRef BY_COUNTRY = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(COUNTRY)
            .range(AMOUNT, SUM)
            .build();

    public static final DataSetRef EXPECTED_PIPELINE = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(CLOSING_DATE, 24, MONTH)
            .range(EXPECTED_AMOUNT, SUM)
            .build();

    public static final DataSetRef BY_PROBABILITY = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(PROBABILITY)
            .range(AMOUNT, SUM)
            .build();

    public static final DataSetRef COUNTRY_SUMMARY = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(COUNTRY, "Country")
            .range(AMOUNT, "#Opps", COUNT)
            .range(AMOUNT, "Min.", MIN)
            .range(AMOUNT, "Max.", MAX)
            .range(AMOUNT, "Average", AVERAGE)
            .range(AMOUNT, "Total", SUM)
            .build();

    public static final DataSetRef BY_STATUS = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(STATUS)
            .range(AMOUNT, SUM)
            .build();

    public static final DataSetRef BY_SALESMAN = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(SALES_PERSON)
            .range(AMOUNT, SUM)
            .build();

    public static final DataSetRef BY_PRODUCT = new DataSetLookupBuilder()
            .uuid(UUID)
            .domain(PRODUCT)
            .range(AMOUNT, SUM)
            .build();
}