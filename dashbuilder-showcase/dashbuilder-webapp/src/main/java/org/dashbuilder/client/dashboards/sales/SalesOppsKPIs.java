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
package org.dashbuilder.client.dashboards.sales;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.client.kpi.ClientKPIManager;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DisplayerFactory;
import org.dashbuilder.model.kpi.KPI;
import org.dashbuilder.model.kpi.KPIFactory;

import static org.dashbuilder.model.dataset.sort.SortOrder.*;
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
    public static final String OPPS_COUNTRY_SUMMARY = "opps-country-summary";
    public static final String OPPS_ALL = "opps-allopps-listing";

    private List<KPI> kpiList = new ArrayList<KPI>();

    @Inject ClientKPIManager kpiManager;

    public static final DataSetRef DATA_ALL_OPPS = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .buildLookup();

    public static final DataSetRef GROUP_PIPELINE = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(PIPELINE)
            .count("occurrences")
            .buildLookup();

    public static final DataSetRef GROUP_STATUS = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(STATUS)
            .sum(AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_CLOSING_DATE = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(CLOSING_DATE, 24, DateIntervalType.MONTH)
            .sum(EXPECTED_AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_SALES_PERSON = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(SALES_PERSON)
            .sum(AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_PRODUCT = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(PRODUCT)
            .sum(AMOUNT)
            .buildLookup();

    public static final DataSetRef GROUP_COUNTRY = DataSetFactory.newDSLookup()
            .dataset(SALES_OPPS)
            .group(COUNTRY)
            .count("opps")
            .min(AMOUNT, "min")
            .max(AMOUNT, "max")
            .avg(AMOUNT, "avg")
            .sum(AMOUNT, "total")
            .buildLookup();

    public static final DataDisplayer PIE_PIPELINE = DisplayerFactory.newPieChartDisplayer()
            .title("Pipeline status")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Pipeline")
            .column("Number of opps")
            .buildDisplayer();

    public static final DataDisplayer PIE_STATUS = DisplayerFactory.newPieChartDisplayer()
            .title("By Status")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Status")
            .column("Total amount")
            .buildDisplayer();

    public static final DataDisplayer PIE_SALES_PERSON = DisplayerFactory.newPieChartDisplayer()
            .title("By Sales Person")
            .titleVisible(false)
            .margins(10, 10, 10, 10)
            .column("Sales person")
            .column("Total amount")
            .buildDisplayer();

    public static final DataDisplayer AREA_EXPECTED_AMOUNT = DisplayerFactory.newAreaChartDisplayer()
            .title("Expected Amount")
            .titleVisible(false)
            .margins(20, 50, 100, 100)
            .column("Closing date")
            .column("Expected amount")
            .buildDisplayer();

    public static final DataDisplayer HBAR_PRODUCT = DisplayerFactory.newBarChartDisplayer()
            .title("By Product")
            .titleVisible(false)
            .margins(10, 50, 100, 100)
            .column("Product")
            .column("Total amount")
            .horizontal()
            .buildDisplayer();

    public static final DataDisplayer HBAR_COUNTRY = DisplayerFactory.newBarChartDisplayer()
            .title("By Country")
            .titleVisible(false)
            .margins(10, 80, 100, 100)
            .column(COUNTRY, "Country")
            .column("total", "Total amount")
            .horizontal()
            .buildDisplayer();

    public static final DataDisplayer TABLE_COUNTRY = DisplayerFactory.newTableDisplayer()
            .title("Country Summary")
            .titleVisible(false)
            .column("country", "COUNTRY")
            .column("total", "TOTAL")
            .column("opps", "NUMBER")
            .column("avg", "AVERAGE")
            .column("min", "MIN")
            .column("max", "MAX")
            .tablePageSize(20)
            .buildDisplayer();

    public static final DataDisplayer TABLE_ALL = DisplayerFactory.newTableDisplayer()
            .title("List of Opportunities")
            .titleVisible(false)
            .tablePageSize(20)
            .tableOrderEnabled(true)
            .tableOrderDefault(AMOUNT, DESCENDING)
            .buildDisplayer();

    @PostConstruct
    public void init() {

        kpiList.add(KPIFactory.newKPI(OPPS_BY_EMPLOYEE, GROUP_PIPELINE, PIE_PIPELINE));
        kpiList.add(KPIFactory.newKPI(OPPS_EXPECTED_PIPELINE, GROUP_CLOSING_DATE, AREA_EXPECTED_AMOUNT));
        kpiList.add(KPIFactory.newKPI(OPPS_BY_STATUS, GROUP_STATUS, PIE_STATUS));
        kpiList.add(KPIFactory.newKPI(OPPS_BY_SALESMAN, GROUP_SALES_PERSON, PIE_SALES_PERSON));
        kpiList.add(KPIFactory.newKPI(OPPS_BY_PRODUCT, GROUP_PRODUCT, HBAR_PRODUCT));
        kpiList.add(KPIFactory.newKPI(OPPS_BY_COUNTRY, GROUP_COUNTRY, HBAR_COUNTRY));
        kpiList.add(KPIFactory.newKPI(OPPS_COUNTRY_SUMMARY, GROUP_COUNTRY, TABLE_COUNTRY));
        kpiList.add(KPIFactory.newKPI(OPPS_ALL, DATA_ALL_OPPS, TABLE_ALL));

        for (KPI kpi : kpiList) {
            kpiManager.addKPI(kpi);
        }
    }

    public List<KPI> getAllKPIs() {
        return kpiList;
    }
}
