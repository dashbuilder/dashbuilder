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
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataSetBuilder;
import org.dashbuilder.model.kpi.KPIBuilder;

import static org.dashbuilder.model.date.Month.*;
import static org.dashbuilder.model.dataset.group.DateIntervalType.MONTH;
import static org.dashbuilder.model.dataset.group.ScalarFunctionType.*;
import static org.dashbuilder.model.displayer.DataDisplayerType.*;
import static org.dashbuilder.model.samples.SalesConstants.*;

/**
 * The Gallery tree.
 */
@ApplicationScoped
public class GalleryTree {

    private List<GalleryNode> mainNodes = new ArrayList<GalleryNode>();

    @Inject ClientKPIManager kpiManager;

    public List<GalleryNode> getMainNodes() {
        return mainNodes;
    }

    @PostConstruct
    private void init() {
        initBarChartCategory();
        initPieChartCategory();
        initLineChartCategory();
        initAreaChartCategory();
        initTableReportCategory();
        initMapChartCategory();
    }

    private void initBarChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Bar Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Simple",
                    new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(PRODUCT)
                .function(AMOUNT, SUM)
                .title("By Product")
                .type(BARCHART)
                .column("Product")
                .column("Total amount")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .function(AMOUNT, "#Opps", COUNT)
                .function(AMOUNT, "Min", MIN)
                .function(AMOUNT, "Max", MAX)
                .function(AMOUNT, "Average", AVERAGE)
                .function(AMOUNT, "Total", SUM)
                .title("By Country (min/max/avg)")
                .type(BARCHART).width(700).height(600)
                .column("Country")
                .column("Min", "Min")
                .column("Max", "Max")
                .column("Average", "Avg")
                .build()
        ));
    }

    private void initPieChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Pie Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Simple",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(STATUS)
                .function(AMOUNT, SUM)
                .title("By Status")
                .type(PIECHART)
                .column("Status")
                .column("Total amount")
                .build()
        ));
    }

    private void initLineChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Line Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Multiple",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .function(AMOUNT, "#Opps", COUNT)
                .function(AMOUNT, "Min", MIN)
                .function(AMOUNT, "Max", MAX)
                .function(AMOUNT, "Average", AVERAGE)
                .function(AMOUNT, "Total", SUM)
                .title("By Country (min/max/avg)")
                .type(LINECHART).width(700).height(400)
                .column("Country")
                .column("Min", "Min")
                .column("Max", "Max")
                .column("Average", "Avg")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple (static)",
                new KPIBuilder()
                .title("Sales Evolution Per Year")
                .type(LINECHART)
                .column("Month")
                .column("Sales in 2014")
                .column("Sales in 2015")
                .column("Sales in 2016")
                .dataset(new DataSetBuilder()
                        .column("month", ColumnType.LABEL)
                        .column("2014", ColumnType.NUMBER)
                        .column("2015", ColumnType.NUMBER)
                        .column("2016", ColumnType.NUMBER)
                        .row(JANUARY, 1000d, 2000d, 3000d)
                        .row(FEBRUARY, 1400d, 2300d, 2000d)
                        .row(MARCH, 1300d, 2000d, 1400d)
                        .row(APRIL, 900d, 2100d, 1500d)
                        .row(MAY, 1300d, 2300d, 1600d)
                        .row(JUNE, 1010d, 2000d, 1500d)
                        .row(JULY, 1050d, 2400d, 3000d)
                        .row(AUGUST, 2300d, 2000d, 3200d)
                        .row(SEPTEMBER, 1900d, 2700d, 3000d)
                        .row(OCTOBER, 1200d, 2200d, 3100d)
                        .row(NOVEMBER, 1400d, 2100d, 3100d)
                        .row(DECEMBER, 1100d, 2100d, 4200d)
                        .build())
                .build()
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initAreaChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Area Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Simple",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 24, MONTH)
                .function(EXPECTED_AMOUNT, SUM)
                .title("Expected Pipeline")
                .type(AREACHART)
                .column("Closing date")
                .column("Expected amount")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Fixed (per month)",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE).fixed(MONTH).firstMonth(JANUARY)
                .function(EXPECTED_AMOUNT, SUM)
                .title("Pipeline (best month)")
                .type(AREACHART)
                .column("Closing date")
                .column("Expected amount per month")
                .build()
        ));
    }

    private void initMapChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Map");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("GeoMap",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .function(AMOUNT, SUM)
                .title("By Country")
                .type(MAP).width(700).height(500)
                .column("Country")
                .column("Total amount")
                .build()
        ));
    }

    private void initTableReportCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Table report");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .rowOffset(0)
                .rowNumber(20)
                .title("List of Opportunities")
                .type(TABLE)
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Grouped",
                new KPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .function(AMOUNT, "#Opps", COUNT)
                .function(AMOUNT, "Min", MIN)
                .function(AMOUNT, "Max", MAX)
                .function(AMOUNT, "Average", AVERAGE)
                .function(AMOUNT, "Total", SUM)
                .title("Country Summary")
                .type(TABLE)
                .build()
        ));
    }
}
