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
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.kpi.AreaChartKPIBuilder;
import org.dashbuilder.model.kpi.BarChartKPIBuilder;
import org.dashbuilder.model.kpi.KPIBuilder;
import org.dashbuilder.model.kpi.LineChartKPIBuilder;
import org.dashbuilder.model.kpi.MapChartKPIBuilder;
import org.dashbuilder.model.kpi.MeterChartKPIBuilder;
import org.dashbuilder.model.kpi.PieChartKPIBuilder;
import org.dashbuilder.model.kpi.TableKPIBuilder;

import static org.dashbuilder.model.dataset.group.DateIntervalType.*;
import static org.dashbuilder.model.date.Month.*;
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
        initMeterChartCategory();
        initMapChartCategory();
    }

    private void initBarChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Bar Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Horizontal",
                new BarChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(PRODUCT)
                .sum(AMOUNT)
                .title("By Product")
                .column("Product")
                .column("Total amount")
                .horizontal()
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Vertical (3D)",
                new BarChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(PRODUCT)
                .sum(AMOUNT)
                .title("By Product")
                .column("Product")
                .column("Total amount")
                .vertical().set3d(true)
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple",
                new BarChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .count("#Opps")
                .min(AMOUNT, "Min")
                .max(AMOUNT, "Max")
                .avg(AMOUNT, "Average")
                .sum(AMOUNT, "Total")
                .title("By Country (min/max/avg)")
                .width(700).height(600)
                .column("Country")
                .column("Min", "Min")
                .column("Max", "Max")
                .column("Average", "Avg")
                .horizontal()
                .build()
        ));
    }

    private void initPieChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Pie Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                new PieChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(STATUS)
                .sum(AMOUNT)
                .title("By Status")
                .column("Status")
                .column("Total amount")
                .build()
        ));
    }

    private void initLineChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Line Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                new LineChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE,12, MONTH)
                .sum(AMOUNT)
                .title("Sales opportunities evolution")
                .width(700).height(400)
                .column("Closing date")
                .column("Total amount")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple",
                new LineChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .count("#Opps")
                .min(AMOUNT, "Min")
                .max(AMOUNT, "Max")
                .avg(AMOUNT, "Average")
                .sum(AMOUNT, "Total")
                .title("By Country (min/max/avg)")
                .width(700).height(400)
                .column("Country")
                .column("Min", "Min")
                .column("Max", "Max")
                .column("Average", "Avg")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple (static)",
                new LineChartKPIBuilder()
                .title("Sales Evolution Per Year")
                .column("Month")
                .column("Sales in 2014")
                .column("Sales in 2015")
                .column("Sales in 2016")
                .dataset(new DataSetBuilder()
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
                        .build())
                .build()
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initAreaChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Area Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                new AreaChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 24, MONTH)
                .sum(EXPECTED_AMOUNT)
                .title("Expected Pipeline")
                .column("Closing date")
                .column("Expected amount")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Fixed (per month)",
                new AreaChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE).fixed(MONTH).firstMonth(JANUARY)
                .sum(EXPECTED_AMOUNT)
                .title("Pipeline (best month)")
                .column("Closing date")
                .column("Expected amount per month")
                .build()
        ));
    }

    private void initMeterChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Meter Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                new MeterChartKPIBuilder()
                .title("Sales goal")
                .dataset(SALES_OPPS)
                .sum(AMOUNT, "Total amount")
                .width(100).height(200)
                .meter(0, 5000000, 8000000, 10000000)
                .column("Total amount")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple",
                new MeterChartKPIBuilder()
                .title("Expected amount per year")
                .dataset(SALES_OPPS)
                .group(CREATION_DATE, YEAR)
                .sum(AMOUNT)
                .width(500).height(200)
                .meter(0, 1000000, 3000000, 5000000)
                .column("Year")
                .column("Amount")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Multiple (static)",
                new MeterChartKPIBuilder()
                .title("Heart rate")
                .width(500).height(200)
                .meter(30, 160, 190, 220)
                .column("Person")
                .column("Heart rate")
                .dataset(new DataSetBuilder()
                        .label("person")
                        .number("heartRate")
                        .row("David", 52)
                        .row("Roger", 120)
                        .row("Mark", 74)
                        .row("Michael", 78)
                        .row("Kris", 74)
                        .build())
                .build()
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initMapChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Map");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("GeoMap",
                new MapChartKPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .sum(AMOUNT)
                .title("By Country")
                .width(700).height(500)
                .column("Country")
                .column("Total amount")
                .build()
        ));
    }

    private void initTableReportCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Table report");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeKPI("Basic",
                new TableKPIBuilder()
                .dataset(SALES_OPPS)
                .rowOffset(0)
                .rowNumber(20)
                .title("List of Opportunities")
                .build()
        ));
        nodeList.add(new GalleryNodeKPI("Grouped",
                new TableKPIBuilder()
                .dataset(SALES_OPPS)
                .group(COUNTRY, "Country")
                .count("#Opps")
                .min(AMOUNT, "Min")
                .max(AMOUNT, "Max")
                .avg(AMOUNT, "Average")
                .sum(AMOUNT, "Total")
                .title("Country Summary")
                .build()
        ));
    }
}
