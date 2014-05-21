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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.client.kpi.ClientKPIManager;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.kpi.KPI;
import org.dashbuilder.model.kpi.KPIFactory;

import static org.dashbuilder.model.samples.SalesConstants.*;

/**
 * A set of KPI definitions built on top of the the Sales Opportunities sample data set.
 */
@ApplicationScoped
public class SalesOppsKPIs {

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

        kpiList.add(KPIFactory.newPieChartKPI()
                .uuid(OPPS_BY_EMPLOYEE)
                .dataset(SALES_OPPS)
                .group(PIPELINE)
                .count("occurrences")
                .title("Pipeline status")
                .titleVisible(false)
                .column("Pipeline")
                .column("Number of opps")
                .buildKPI());

        kpiList.add(KPIFactory.newAreaChartKPI()
                .uuid(OPPS_EXPECTED_PIPELINE)
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 24, DateIntervalType.MONTH)
                .sum(EXPECTED_AMOUNT)
                .title("Expected Pipeline")
                .titleVisible(false)
                .column("Closing date")
                .column("Expected amount")
                .buildKPI());

        kpiList.add(KPIFactory.newPieChartKPI()
                .uuid(OPPS_BY_STATUS)
                .dataset(SALES_OPPS)
                .group(STATUS)
                .sum(AMOUNT)
                .title("By Status")
                .titleVisible(false)
                .column("Status")
                .column("Total amount")
                .buildKPI());

        kpiList.add(KPIFactory.newPieChartKPI()
                .uuid(OPPS_BY_SALESMAN)
                .dataset(SALES_OPPS)
                .group(SALES_PERSON)
                .sum(AMOUNT)
                .title("By Sales Person")
                .titleVisible(false)
                .column("Sales person")
                .column("Total amount")
                .buildKPI());

        kpiList.add(KPIFactory.newBarChartKPI()
                .uuid(OPPS_BY_PRODUCT)
                .dataset(SALES_OPPS)
                .group(PRODUCT)
                .sum(AMOUNT)
                .title("By Product")
                .titleVisible(false)
                .column("Product")
                .column("Total amount")
                .vertical()
                .buildKPI());

        kpiList.add(KPIFactory.newBarChartKPI()
                .uuid(OPPS_BY_COUNTRY)
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .sum(AMOUNT)
                .title("By Country")
                .titleVisible(false)
                .vertical()
                .column("Country")
                .column("Total amount")
                .buildKPI());

        kpiList.add(KPIFactory.newBarChartKPI()
                .uuid(OPPS_BY_PROBABILITY)
                .dataset(SALES_OPPS)
                .group(PROBABILITY)
                .sum(AMOUNT)
                .title("By Probability")
                .titleVisible(false)
                .column("Probability")
                .column("Total amount")
                .vertical()
                .buildKPI());

        kpiList.add(KPIFactory.newTableKPI()
                .uuid(OPPS_COUNTRY_SUMMARY)
                .dataset(SALES_OPPS)
                .group(COUNTRY, "country")
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
                .buildKPI());

        kpiList.add(KPIFactory.newTableKPI()
                .uuid(OPPS_ALL)
                .dataset(SALES_OPPS)
                .rowOffset(0)
                .rowNumber(20)
                .title("List of Opportunities")
                .titleVisible(false)
                .buildKPI());

        for (KPI kpi : kpiList) {
            kpiManager.addKPI(kpi);
        }
    }

    public List<KPI> getAllKPIs() {
        return kpiList;
    }
}
