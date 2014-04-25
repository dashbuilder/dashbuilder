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

import static org.dashbuilder.model.displayer.DataDisplayerType.*;

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
        KPI kpi1 = kpiManager.createKPI(SalesOppsData.byProduct(), SalesOppsDisplayers.byProduct(BARCHART));
        KPI kpi2 = kpiManager.createKPI(SalesOppsData.countrySummary(), SalesOppsDisplayers.byCountryMinMaxAvg(BARCHART, 700, 600));

        GalleryNodeList nodeList = new GalleryNodeList("Bar Chart");
        nodeList.add(new GalleryNodeKPI("Simple", kpi1));
        nodeList.add(new GalleryNodeKPI("Multiple", kpi2));
        mainNodes.add(nodeList);
    }

    private void initPieChartCategory() {
        KPI kpi = kpiManager.createKPI(SalesOppsData.byStatus(), SalesOppsDisplayers.byStatus(PIECHART));

        GalleryNodeList nodeList = new GalleryNodeList("Pie Chart");
        nodeList.add(new GalleryNodeKPI("Simple", kpi));
        mainNodes.add(nodeList);
    }

    private void initLineChartCategory() {
        KPI kpi1 = kpiManager.createKPI(GalleryData.salesPerYear(), GalleryDisplayers.salesPerYear(LINECHART));
        KPI kpi2 = kpiManager.createKPI(SalesOppsData.countrySummary(), SalesOppsDisplayers.byCountryMinMaxAvg(LINECHART, 700, 400));

        GalleryNodeList nodeList = new GalleryNodeList("Line Chart");
        nodeList.add(new GalleryNodeKPI("Multiple (static)", kpi1));
        nodeList.add(new GalleryNodeKPI("Multiple (label)", kpi2));
        //nodeList.add(new GalleryNodeKPI("Multiple (date)", kpi3));
        mainNodes.add(nodeList);
    }

    private void initAreaChartCategory() {
        KPI kpi = kpiManager.createKPI(SalesOppsData.expectedPipeline(), SalesOppsDisplayers.expectedPipeline(AREACHART));

        GalleryNodeList nodeList = new GalleryNodeList("Area Chart");
        nodeList.add(new GalleryNodeKPI("Simple", kpi));
        mainNodes.add(nodeList);
    }

    private void initTableReportCategory() {
        KPI kpi1 = kpiManager.createKPI(SalesOppsData.listOfOpportunities(0, 20), SalesOppsDisplayers.opportunitiesListing());
        KPI kpi2 = kpiManager.createKPI(SalesOppsData.countrySummary(), SalesOppsDisplayers.countrySummaryTable());

        GalleryNodeList nodeList = new GalleryNodeList("Table report");
        nodeList.add(new GalleryNodeKPI("Basic", kpi1));
        nodeList.add(new GalleryNodeKPI("Grouped", kpi2));
        mainNodes.add(nodeList);
    }

    public List<GalleryNode> getMainNodes() {
        return mainNodes;
    }
}
