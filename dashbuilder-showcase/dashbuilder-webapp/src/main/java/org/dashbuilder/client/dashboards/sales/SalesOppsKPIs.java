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
import org.dashbuilder.model.kpi.KPI;
import org.dashbuilder.model.kpi.KPIFactory;

import static org.dashbuilder.client.dashboards.sales.SalesOppsData.*;
import static org.dashbuilder.client.dashboards.sales.SalesOppsDisplayers.*;

/**
 * A set of KPI definitions built on top of the the Sales Opportunities data set.
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
