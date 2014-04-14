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
import org.dashbuilder.model.kpi.impl.KPIImpl;

import static org.dashbuilder.model.samples.SalesConstants.*;
import static org.dashbuilder.model.displayer.DataDisplayerType.*;
import static org.dashbuilder.model.displayer.DataDisplayerRenderer.*;

/**
 * A set of KPI definitions related to the Sales Dashboard Sample.
 */
@ApplicationScoped
public class SalesDashboardKPIs {

    private List<KPI> kpiList = new ArrayList<KPI>();

    @Inject KPILocator kpiLocator;

    @PostConstruct
    public void init() {
        kpiList.add(SalesDashboardKPIs.OPPS_EXPECTED_PIPELINE);
        kpiList.add(SalesDashboardKPIs.OPPS_BY_COUNTRY);
        kpiList.add(SalesDashboardKPIs.OPPS_BY_EMPLOYEE);
        kpiList.add(SalesDashboardKPIs.OPPS_BY_PROBABILITY);
        kpiList.add(SalesDashboardKPIs.OPPS_BY_PRODUCT);
        kpiList.add(SalesDashboardKPIs.OPPS_BY_SALESMAN);
        kpiList.add(SalesDashboardKPIs.OPPS_BY_STATUS);
        for (KPI kpi : kpiList) {
            kpiLocator.addKPI(kpi);
        }
    }

    public List<KPI> getAllKPIs() {
        return kpiList;
    }

    public static final KPIImpl OPPS_BY_EMPLOYEE = new KPIImpl("opps-by-pipeline")
        .setDataSetLookup(new DataSetLookupBuilder()
                .uuid(UUID)
                .domain(PIPELINE)
                .range(AMOUNT, "occurrences", "count")
                .build())
        .setDataDisplayer(new DataDisplayerBuilder()
                .title("Pipeline status")
                .type(PIECHART)
                .renderer(GOOGLE)
                .x(PIPELINE, "Pipeline")
                .y("occurrences", "Number of opps")
                .build());

    public static final KPIImpl OPPS_EXPECTED_PIPELINE = new KPIImpl("opps-expected-pipeline")
        .setDataSetLookup(new DataSetLookupBuilder()
                .uuid(UUID)
                .domain(CLOSING_DATE, "dynamic", 24, "month")
                .range(EXPECTED_AMOUNT, "sum")
                .build())
        .setDataDisplayer(new DataDisplayerBuilder()
                .title("Expected Pipeline")
                .type(AREACHART)
                .renderer(GOOGLE)
                .x(CLOSING_DATE, "Closing date")
                .y(EXPECTED_AMOUNT, "Expected amount")
                .build());

    public static final KPIImpl OPPS_BY_STATUS = new KPIImpl("opps-by-status")
        .setDataSetLookup(new DataSetLookupBuilder()
                .uuid(UUID)
                .domain(STATUS)
                .range(AMOUNT, "sum")
                .build())
        .setDataDisplayer(new DataDisplayerBuilder()
                .title("By Status")
                .type(PIECHART)
                .renderer(GOOGLE)
                .x(STATUS, "Status")
                .y(AMOUNT, "Total amount")
                .build());

    public static final KPIImpl OPPS_BY_SALESMAN = new KPIImpl("opps-by-salesman")
        .setDataSetLookup(new DataSetLookupBuilder()
                .uuid(UUID)
                .domain(SALES_PERSON)
                .range(AMOUNT, "sum")
                .build())
        .setDataDisplayer(new DataDisplayerBuilder()
                .title("By Sales Person")
                .type(PIECHART)
                .renderer(GOOGLE)
                .x(SALES_PERSON, "Sales person")
                .y(AMOUNT, "Total amount")
                .build());

    public static final KPIImpl OPPS_BY_PRODUCT = new KPIImpl("opps-by-product")
        .setDataSetLookup(new DataSetLookupBuilder()
                .uuid(UUID)
                .domain(PRODUCT)
                .range(AMOUNT, "sum")
                .build())
        .setDataDisplayer(new DataDisplayerBuilder()
                .title("By Product")
                .type("barchart")
                .renderer(GOOGLE)
                .x(PRODUCT, "Product")
                .y(AMOUNT, "Total amount")
                .build());

    public static final KPIImpl OPPS_BY_COUNTRY = new KPIImpl("opps-by-country")
        .setDataSetLookup(new DataSetLookupBuilder()
                .uuid(UUID)
                .domain(COUNTRY)
                .range(AMOUNT, "sum")
                .build())
        .setDataDisplayer(new DataDisplayerBuilder()
                .title("By Country")
                .type("barchart")
                .renderer(GOOGLE)
                .x(COUNTRY, "Country")
                .y(AMOUNT, "Total amount")
                .build());

    public static final KPIImpl OPPS_BY_PROBABILITY = new KPIImpl("opps-by-prob")
        .setDataSetLookup(new DataSetLookupBuilder()
                .uuid(UUID)
                .domain(PROBABILITY)
                .range(AMOUNT, "sum")
                .build())
        .setDataDisplayer(new DataDisplayerBuilder()
                .title("By Probability")
                .type(BARCHART)
                .renderer(GOOGLE)
                .x(PROBABILITY, "Probability")
                .y(AMOUNT, "Total amount")
                .build());
}
