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
package org.dashbuilder.client.samples.gallery;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.client.kpi.ClientKPIManager;
import org.dashbuilder.client.samples.sales.SalesOppsData;
import org.dashbuilder.client.samples.sales.SalesOppsDisplayers;
import org.dashbuilder.model.kpi.KPI;

/**
 * The Gallery tree.
 */
@ApplicationScoped
public class GalleryTree {

    private List<GalleryNode> mainNodes = new ArrayList<GalleryNode>();

    @Inject ClientKPIManager kpiManager;

    @PostConstruct
    private void init() {
        initBarChartCategory();
        initPieChartCategory();
        initLineChartCategory();
        initAreaChartCategory();
        initTableReportCategory();
    }

    private void initBarChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Bar Chart");
        KPI kpi = kpiManager.createKPI(SalesOppsData.BY_PRODUCT, SalesOppsDisplayers.BAR_CHART_BY_PRODUCT);
        nodeList.add(new GalleryNodeKPI("Simple", kpi));
        mainNodes.add(nodeList);
    }

    private void initPieChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Pie Chart");
        KPI kpi = kpiManager.createKPI(SalesOppsData.BY_STATUS, SalesOppsDisplayers.PIE_CHART_BY_STATUS);
        nodeList.add(new GalleryNodeKPI("Simple", kpi));
        mainNodes.add(nodeList);
    }

    private void initLineChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Line Chart");
        KPI kpi = kpiManager.createKPI(GalleryData.SALES_PER_YEAR, GalleryDisplayers.MLINE_CHART_SALES_PER_YEAR);
        nodeList.add(new GalleryNodeKPI("Multiple (static)", kpi));
        mainNodes.add(nodeList);
    }

    private void initAreaChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Area Chart");
        KPI kpi = kpiManager.createKPI(SalesOppsData.EXPECTED_PIPELINE, SalesOppsDisplayers.AREA_CHART_EXPECTED_PIPELINE);
        nodeList.add(new GalleryNodeKPI("Simple", kpi));
        mainNodes.add(nodeList);
    }

    private void initTableReportCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Table report");
        KPI kpi = kpiManager.createKPI(SalesOppsData.COUNTRY_SUMMARY, SalesOppsDisplayers.TABLE_COUNTRY_SUMMARY);
        nodeList.add(new GalleryNodeKPI("Grouped table ", kpi));
        mainNodes.add(nodeList);
    }

    public List<GalleryNode> getMainNodes() {
        return mainNodes;
    }
}
