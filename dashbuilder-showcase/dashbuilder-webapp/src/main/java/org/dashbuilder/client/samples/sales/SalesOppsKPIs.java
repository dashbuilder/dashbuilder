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
import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.kpi.KPI;

import static org.dashbuilder.model.samples.SalesConstants.*;
import static org.dashbuilder.model.displayer.DataDisplayerType.*;
import static org.dashbuilder.client.samples.sales.SalesOppsDisplayers.*;
import static org.dashbuilder.client.samples.sales.SalesOppsData.*;

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
        kpiList.add(kpiManager.createKPI(OPPS_BY_EMPLOYEE, BY_EMPLOYEE, PIE_CHART_PIPELINE_STATUS));
        kpiList.add(kpiManager.createKPI(OPPS_EXPECTED_PIPELINE, EXPECTED_PIPELINE, AREA_CHART_EXPECTED_PIPELINE));
        kpiList.add(kpiManager.createKPI(OPPS_BY_STATUS, BY_STATUS, PIE_CHART_BY_STATUS));
        kpiList.add(kpiManager.createKPI(OPPS_BY_SALESMAN, BY_SALESMAN, PIE_CHART_BY_SALES_PERSON));
        kpiList.add(kpiManager.createKPI(OPPS_BY_PRODUCT, BY_PRODUCT, BAR_CHART_BY_PRODUCT));
        kpiList.add(kpiManager.createKPI(OPPS_BY_COUNTRY, BY_COUNTRY, BAR_CHART_BY_COUNTRY));
        kpiList.add(kpiManager.createKPI(OPPS_BY_PROBABILITY, BY_PROBABILITY, BAR_CHART_BY_PROBABILITY));
        kpiList.add(kpiManager.createKPI(OPPS_COUNTRY_SUMMARY, COUNTRY_SUMMARY, TABLE_COUNTRY_SUMMARY));
        kpiList.add(kpiManager.createKPI(OPPS_ALL,
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
