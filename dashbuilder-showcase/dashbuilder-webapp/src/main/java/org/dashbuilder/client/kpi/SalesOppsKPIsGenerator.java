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
package org.dashbuilder.client.kpi;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.kpi.KPI;

import static org.dashbuilder.model.samples.SalesConstants.*;
import static org.dashbuilder.model.displayer.DataDisplayerType.*;
import static org.dashbuilder.model.dataset.group.ScalarFunctionType.*;
import static org.dashbuilder.model.dataset.group.DomainStrategy.*;
import static org.dashbuilder.model.dataset.group.DateIntervalType.*;

/**
 * A set of KPI definitions built on top of the the Sales Opportunities data set.
 */
@ApplicationScoped
public class SalesOppsKPIsGenerator {

    public static final String OPPS_BY_EMPLOYEE = "opps-by-pipeline";
    public static final String OPPS_EXPECTED_PIPELINE = "opps-expected-pipeline";
    public static final String OPPS_SALES_PER_YEAR = "opps-sales-per-year";
    public static final String OPPS_BY_STATUS = "opps-by-status";
    public static final String OPPS_BY_SALESMAN = "opps-by-salesman";
    public static final String OPPS_BY_PRODUCT = "opps-by-product";
    public static final String OPPS_BY_COUNTRY = "opps-by-country";
    public static final String OPPS_BY_PROBABILITY = "opps-by-prob";
    public static final String OPPS_COUNTRY_SUMMARY = "opps-country-summary";
    public static final String OPPS_ALL = "opps-allopps-listing";

    private List<KPI> kpiList = new ArrayList<KPI>();

    @Inject ClientKPIManager kpiManager;

    @PostConstruct
    public void init() {
        kpiList.add(kpiManager.createKPI(
                OPPS_BY_EMPLOYEE,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(PIPELINE)
                        .range(AMOUNT, "occurrences", COUNT)
                        .build(),
                new DataDisplayerBuilder()
                        .title("Pipeline status")
                        .type(PIECHART)
                        .x(PIPELINE, "Pipeline")
                        .y("occurrences", "Number of opps")
                        .build()));

        kpiList.add(kpiManager.createKPI(
                OPPS_EXPECTED_PIPELINE,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(CLOSING_DATE, 24, MONTH)
                        .range(EXPECTED_AMOUNT, SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("Expected Pipeline")
                        .type(AREACHART)
                        .x(CLOSING_DATE, "Closing date")
                        .y(EXPECTED_AMOUNT, "Expected amount")
                        .build()));

        /* TODO: MULTIPLE strategy
        kpiList.add(kpiManager.createKPI(
                OPPS_SALES_PER_YEAR,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(CREATION_DATE, FIXED, MONTH)
                        .range(AMOUNT, SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("Sales per year")
                        .type(LINECHART)
                        .x(CREATION_DATE, "Opportunity date")
                        .y(AMOUNT, "Total amount")
                        .build()));*/

        kpiList.add(kpiManager.createKPI(
                OPPS_BY_STATUS,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(STATUS)
                        .range(AMOUNT, SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("By Status")
                        .type(PIECHART)
                        .x(STATUS, "Status")
                        .y(AMOUNT, "Total amount")
                        .build()));

        kpiList.add(kpiManager.createKPI(
                OPPS_BY_SALESMAN,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(SALES_PERSON)
                        .range(AMOUNT, SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("By Sales Person")
                        .type(PIECHART)
                        .x(SALES_PERSON, "Sales person")
                        .y(AMOUNT, "Total amount")
                        .build()));

        kpiList.add(kpiManager.createKPI(
                OPPS_BY_PRODUCT,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(PRODUCT)
                        .range(AMOUNT, SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("By Product")
                        .type("barchart")
                        .x(PRODUCT, "Product")
                        .y(AMOUNT, "Total amount")
                        .build()));

        kpiList.add(kpiManager.createKPI(
                OPPS_BY_COUNTRY,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(COUNTRY)
                        .range(AMOUNT, SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("By Country")
                        .type("barchart")
                        .x(COUNTRY, "Country")
                        .y(AMOUNT, "Total amount")
                        .build()));

        kpiList.add(kpiManager.createKPI(
                OPPS_BY_PROBABILITY,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(PROBABILITY)
                        .range(AMOUNT, SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("By Probability")
                        .type(BARCHART)
                        .x(PROBABILITY, "Probability")
                        .y(AMOUNT, "Total amount")
                        .build()));

        kpiList.add(kpiManager.createKPI(
                OPPS_COUNTRY_SUMMARY,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .domain(COUNTRY, "Country")
                        .range(AMOUNT, "#Opps", COUNT)
                        .range(AMOUNT, "Min.", MIN)
                        .range(AMOUNT, "Max.", MAX)
                        .range(AMOUNT, "Average", AVERAGE)
                        .range(AMOUNT, "Total", SUM)
                        .build(),
                new DataDisplayerBuilder()
                        .title("Country Summary")
                        .type(TABLE)
                        .build()));

        kpiList.add(kpiManager.createKPI(
                OPPS_ALL,
                new DataSetLookupBuilder()
                        .uuid(UUID)
                        .rowOffset(0)
                        .rowNumber(100)
                        .build(),
                new DataDisplayerBuilder()
                        .title("List of Opportunities")
                        .type(TABLE)
                        .build()));

        for (KPI kpi : kpiList) {
            kpiManager.addKPI(kpi);
        }
    }

    public List<KPI> getAllKPIs() {
        return kpiList;
    }
}
