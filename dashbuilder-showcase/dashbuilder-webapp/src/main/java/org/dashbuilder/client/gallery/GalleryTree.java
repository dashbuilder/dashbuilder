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
package org.dashbuilder.client.gallery;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.kpi.ClientKPIManager;
import org.dashbuilder.client.sales.widgets.SalesExpectedByDate;
import org.dashbuilder.client.sales.widgets.SalesDistributionByCountry;
import org.dashbuilder.client.sales.widgets.SalesGoals;
import org.dashbuilder.client.sales.widgets.SalesTableReports;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.kpi.KPIFactory;

import static org.dashbuilder.model.dataset.group.DateIntervalType.*;
import static org.dashbuilder.model.dataset.filter.FilterFactory.*;
import static org.dashbuilder.model.dataset.sort.SortOrder.*;
import static org.dashbuilder.model.date.Month.*;
import static org.dashbuilder.client.sales.SalesConstants.*;

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
        initBubbleChartCategory();
        initTableReportCategory();
        initMeterChartCategory();
        initMapChartCategory();
        initDashboardCategory();
    }

    private void initBarChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Bar Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Horizontal",
                KPIFactory.newBarChartKPI()
                        .dataset(SALES_OPPS)
                        .group(PRODUCT)
                        .sum(AMOUNT)
                        .title("By Product")
                        .column("Product")
                        .column("Total amount")
                        .horizontal()
                        .margins(10, 30, 120, 120)
                        .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Vertical (3D)",
                KPIFactory.newBarChartKPI()
                        .dataset(SALES_OPPS)
                        .group(PRODUCT)
                        .sum(AMOUNT)
                        .title("By Product")
                        .column("Product")
                        .column("Total amount")
                        .vertical().set3d(true)
                        .margins(10, 80, 120, 120)
                        .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple",
                KPIFactory.newBarChartKPI()
                        .dataset(SALES_OPPS)
                        .group(COUNTRY, "Country")
                        .count("#Opps")
                        .min(AMOUNT, "Min")
                        .max(AMOUNT, "Max")
                        .avg(AMOUNT, "Average")
                        .sum(AMOUNT, "Total")
                        .title("By Country (min/max/avg)")
                        .width(700).height(600)
                        .margins(10, 50, 120, 100)
                        .column("Country")
                        .column("Min", "Min")
                        .column("Max", "Max")
                        .column("Average", "Avg")
                        .horizontal()
                        .buildKPI()
        ));
    }

    private void initPieChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Pie Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                KPIFactory.newPieChartKPI()
                .dataset(SALES_OPPS)
                .group(STATUS)
                .sum(AMOUNT)
                .title("By Status")
                .margins(10, 10, 10, 10)
                .column("Status")
                .column("Total amount")
                .buildKPI()
        ));
    }

    private void initLineChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Line Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                KPIFactory.newLineChartKPI()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 12, MONTH)
                .sum(AMOUNT)
                .title("Sales opportunities evolution")
                .margins(20, 50, 100, 120)
                .column("Closing date")
                .column("Total amount")
                .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple",
                KPIFactory.newLineChartKPI()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .count("#Opps")
                .min(AMOUNT, "Min")
                .max(AMOUNT, "Max")
                .avg(AMOUNT, "Average")
                .sum(AMOUNT, "Total")
                .title("By Country (min/max/avg)")
                .margins(30, 100, 80, 80)
                .column("Country")
                .column("Min", "Min")
                .column("Max", "Max")
                .column("Average", "Avg")
                .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple (static)",
                KPIFactory.newLineChartKPI()
                .title("Sales Evolution Per Year")
                .margins(20, 80, 50, 120)
                .column("Month")
                .column("Sales in 2014")
                .column("Sales in 2015")
                .column("Sales in 2016")
                .dataset(DataSetFactory.newDSBuilder()
                        .label("month")
                        .number("2014")
                        .number("2015")
                        .number("2016")
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
                        .buildDataSet())
                .buildKPI()
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initAreaChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Area Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                KPIFactory.newAreaChartKPI()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 24, MONTH)
                .sum(EXPECTED_AMOUNT)
                .title("Expected Pipeline")
                .margins(20, 50, 100, 120)
                .column("Closing date")
                .column("Expected amount")
                .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Fixed (per month)",
                KPIFactory.newAreaChartKPI()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE)
                .fixed(MONTH).firstMonth(JANUARY).asc()
                .sum(EXPECTED_AMOUNT)
                .title("Pipeline (best month)")
                .margins(20, 80, 100, 100)
                .column("Closing date")
                .column("Expected amount per month")
                .buildKPI()
        ));
    }

    private void initBubbleChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Bubble Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                KPIFactory.newBubbleChartKPI()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .count("opps")
                .avg(PROBABILITY)
                .sum(EXPECTED_AMOUNT)
                .title("Opportunities distribution by Country ")
                .width(700).height(400)
                .margins(20, 50, 50, 0)
                .column(COUNTRY, "Country")
                .column("opps", "Number of opportunities")
                .column(PROBABILITY, "Average probability")
                .column(COUNTRY, "Country")
                .column(EXPECTED_AMOUNT, "Expected amount")
                .buildKPI()
        ));
    }

    private void initMeterChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Meter Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                KPIFactory.newMeterChartKPI()
                .title("Sales goal")
                .dataset(SALES_OPPS)
                .sum(AMOUNT, "Total amount")
                .width(100).height(200)
                .meter(0, 5000000, 8000000, 10000000)
                .column("Total amount")
                .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple",
                KPIFactory.newMeterChartKPI()
                .title("Expected amount per year")
                .dataset(SALES_OPPS)
                .group(CREATION_DATE, YEAR)
                .sum(AMOUNT)
                .width(600).height(200)
                .meter(0, 1000000, 3000000, 5000000)
                .column("Year")
                .column("Amount")
                .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple (static)",
                KPIFactory.newMeterChartKPI()
                .title("Heart rate")
                .width(500).height(200)
                .meter(30, 160, 190, 220)
                .column("Person")
                .column("Heart rate")
                .dataset(DataSetFactory.newDSBuilder()
                        .label("person")
                        .number("heartRate")
                        .row("David", 52)
                        .row("Roger", 120)
                        .row("Mark", 74)
                        .row("Michael", 78)
                        .row("Kris", 74)
                        .buildDataSet())
                .buildKPI()
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initMapChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Map");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("GeoMap",
                KPIFactory.newMapChartKPI()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .sum(AMOUNT)
                .title("By Country")
                .width(700).height(500)
                .margins(10, 10, 10, 10)
                .column("Country")
                .column("Total amount")
                .buildKPI()
        ));
    }

    private void initTableReportCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Table report");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                KPIFactory.newTableKPI()
                .dataset(SALES_OPPS)
                .title("List of Opportunities")
                .tablePageSize(20)
                .tableOrderEnabled(true)
                .tableOrderDefault(AMOUNT, DESCENDING)
                .column(COUNTRY, "Country")
                .column(CUSTOMER, "Customer")
                .column(PRODUCT, "Product")
                .column(SALES_PERSON, "Salesman")
                .column(STATUS, "Status")
                .column(SOURCE, "Source")
                .column(CREATION_DATE, "Creation")
                .column(EXPECTED_AMOUNT, "Expected")
                .column(CLOSING_DATE, "Closing")
                .column(AMOUNT, "Amount")
                .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Filtered",
                KPIFactory.newTableKPI()
                .dataset(SALES_OPPS)
                .filter(COUNTRY, OR(isEqualsTo("United States"), isEqualsTo("Brazil")))
                .title("Opportunities in USA & Brazil")
                .tablePageSize(20)
                .tableOrderEnabled(true)
                .tableOrderDefault(AMOUNT, DESCENDING)
                .column(CUSTOMER, "Customer")
                .column(PRODUCT, "Product")
                .column(STATUS, "Status")
                .column(SOURCE, "Source")
                .column(CREATION_DATE, "Creation")
                .column(EXPECTED_AMOUNT, "Expected")
                .column(CLOSING_DATE, "Closing")
                .column(AMOUNT, "Amount")
                .buildKPI()
        ));
        nodeList.add(new GalleryNodeKPI("Grouped",
                KPIFactory.newTableKPI()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .count("#Opps")
                .min(AMOUNT, "Min")
                .max(AMOUNT, "Max")
                .avg(AMOUNT, "Average")
                .sum(AMOUNT, "Total")
                .title("Country Summary")
                .tablePageSize(10)
                .tableOrderEnabled(true)
                .tableOrderDefault("Country", DESCENDING)
                .buildKPI()
        ));

    }

    private void initDashboardCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Dashboards");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNode("Sales goal") {
            public Widget createWidget() {
                return new SalesGoals();
            }
        });
        nodeList.add(new GalleryNode("Sales pipeline") {
            public Widget createWidget() {
                return new SalesExpectedByDate();
            }
        });
        nodeList.add(new GalleryNode("Sales per country") {
            public Widget createWidget() {
                return new SalesDistributionByCountry();
            }
        });
        nodeList.add(new GalleryNode("Sales reports") {
            public Widget createWidget() {
                return new SalesTableReports();
            }
        });
    }
}
