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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.renderer.table.client.TableRenderer;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.date.Month.*;
import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

/**
 * The Gallery tree.
 */
@ApplicationScoped
public class GalleryTree {

    private List<GalleryTreeNode> mainNodes = new ArrayList<GalleryTreeNode>();

    @Inject DisplayerSettingsJSONMarshaller jsonHelper;

    public List<GalleryTreeNode> getMainNodes() {
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

    private PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonHelper.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String,String>();
        params.put("json", json);
        params.put("edit", "false");
        params.put("showRendererSelector", "true");
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }

    private PlaceRequest createPlaceRequest(String widgetId) {
        Map<String,String> params = new HashMap<String,String>();
        params.put("widgetId", widgetId);
        return new DefaultPlaceRequest("GalleryWidgetScreen", params);
    }

    private void initBarChartCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Bar Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Horizontal", createPlaceRequest(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PRODUCT)
                        .column(PRODUCT, "Product")
                        .column(AMOUNT, SUM, "Total amount")
                        .title("By Product")
                        .horizontal()
                        .width(600).height(400)
                        .margins(50, 80, 120, 120)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Vertical (Drill-down)", createPlaceRequest(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PIPELINE)
                        .column(PIPELINE, "Pipeline")
                        .column(AMOUNT, SUM, "Total amount")
                        .group(STATUS)
                        .column(STATUS, "Status")
                        .column(AMOUNT, SUM, "Total amount")
                        .group(SALES_PERSON)
                        .column(SALES_PERSON, "Sales person")
                        .column(AMOUNT, SUM, "Total amount")
                        .title("By Pipeline/Status/Sales person")
                        .width(600).height(400)
                        .margins(50, 80, 120, 120)
                        .vertical()
                        .filterOn(true, false, false)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Horizontal (Drill-down)", createPlaceRequest(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PIPELINE)
                        .column(PIPELINE, "Pipeline")
                        .column(AMOUNT, SUM, "Total amount")
                        .group(STATUS)
                        .column(STATUS, "Status")
                        .column(AMOUNT, SUM, "Total amount")
                        .group(SALES_PERSON)
                        .column(SALES_PERSON, "Sales person")
                        .column(AMOUNT, SUM, "Total amount")
                        .title("By Pipeline/Status/Sales person")
                        .width(600).height(400)
                        .margins(50, 80, 120, 120)
                        .horizontal()
                        .filterOn(true, false, false)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Vertical", createPlaceRequest(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PRODUCT)
                        .column(PRODUCT, "Product")
                        .column(AMOUNT, SUM, "Total amount")
                        .title("By Product")
                        .vertical().set3d(true)
                        .width(600).height(400)
                        .margins(50, 80, 120, 120)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Multiple", createPlaceRequest(
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(SALES_OPPS)
                        .group(COUNTRY)
                        .column(COUNTRY, "Country")
                        .column(AMOUNT, MIN, "Min")
                        .column(AMOUNT, MAX, "Max")
                        .column(AMOUNT, AVERAGE,  "Avg")
                        .title("By Country (min/max/avg)")
                        .width(700).height(600)
                        .margins(50, 80, 120, 120)
                        .horizontal()
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
    }

    private void initPieChartCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Pie Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Basic", createPlaceRequest(
                DisplayerSettingsFactory.newPieChartSettings()
                        .dataset(SALES_OPPS)
                        .group(STATUS)
                        .column(STATUS)
                        .column(AMOUNT, SUM)
                        .title("By Status")
                        .margins(10, 10, 10, 10)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));

        nodeList.add(new GalleryPlaceRequest("Drill-down", createPlaceRequest(
                DisplayerSettingsFactory.newPieChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PIPELINE)
                        .column(PIPELINE, "Pipeline")
                        .column(AMOUNT, SUM, "Total amount")
                        .group(STATUS)
                        .column(STATUS, "Status")
                        .column(AMOUNT, SUM, "Total amount")
                        .group(SALES_PERSON)
                        .column(SALES_PERSON, "Sales person")
                        .column(AMOUNT, SUM, "Total amount")
                        .title("By Pipeline/Status/Sales person")
                        .margins(10, 10, 10, 10)
                        .filterOn(true, false, false)
                        .buildSettings()
        )));
    }

    private void initLineChartCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Line Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Basic", createPlaceRequest(
                DisplayerSettingsFactory.newLineChartSettings()
                        .dataset(SALES_OPPS)
                        .group(CLOSING_DATE).dynamic(12, MONTH, true)
                        .column(CLOSING_DATE, "Closing date")
                        .column(AMOUNT, SUM, "Total amount")
                        .title("Sales opportunities evolution")
                        .margins(20, 50, 100, 120)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Multiple", createPlaceRequest(
                DisplayerSettingsFactory.newLineChartSettings()
                        .dataset(SALES_OPPS)
                        .group(COUNTRY)
                        .column(COUNTRY, "Country")
                        .column(AMOUNT, MIN, "Min")
                        .column(AMOUNT, MAX, "Max")
                        .column(AMOUNT, AVERAGE, "Avg")
                        .title("By Country (min/max/avg)")
                        .margins(30, 100, 80, 80)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Multiple (static)", createPlaceRequest(
                DisplayerSettingsFactory.newLineChartSettings()
                        .title("Sales Evolution Per Year")
                        .margins(20, 80, 50, 120)
                        .column("month", "Month")
                        .column("2014", "Sales in 2014")
                        .column("2015", "Sales in 2015")
                        .column("2016", "Sales in 2016")
                        .dataset(DataSetFactory.newDataSetBuilder()
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
                        .buildSettings()
        )));

        // nodeList.add(new GalleryNodeDisplayer("Multiple (date)", ...)));
    }

    private void initAreaChartCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Area Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Basic", createPlaceRequest(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(SALES_OPPS)
                        .group(CLOSING_DATE).dynamic(24, MONTH, true)
                        .column(CLOSING_DATE, "Closing date")
                        .column(EXPECTED_AMOUNT, SUM, "Expected amount")
                        .title("Expected Pipeline")
                        .margins(20, 50, 100, 120)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Fixed (per month)", createPlaceRequest(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(SALES_OPPS)
                        .group(CLOSING_DATE).fixed(MONTH, true).firstMonth(JANUARY).asc()
                        .column(CLOSING_DATE, "Closing date")
                        .column(EXPECTED_AMOUNT, SUM, "Expected amount per month")
                        .title("Pipeline (best month)")
                        .margins(20, 80, 100, 100)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Drill-down", createPlaceRequest(
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(SALES_OPPS)
                        .group(CLOSING_DATE).dynamic(12, true)
                        .column(CLOSING_DATE, "Closing date")
                        .column(EXPECTED_AMOUNT, SUM, "Expected amount")
                        .title("Expected Pipeline")
                        .margins(20, 70, 100, 120)
                        .filterOn(true, true, true)
                        .buildSettings()
        )));
    }

    private void initBubbleChartCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Bubble Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Basic", createPlaceRequest(
                DisplayerSettingsFactory.newBubbleChartSettings()
                        .dataset(SALES_OPPS)
                        .group(COUNTRY)
                        .column(COUNTRY, "Country")
                        .column(COUNT, "Number of opportunities")
                        .column(PROBABILITY, AVERAGE, "Average probability")
                        .column(COUNTRY, "Country")
                        .column(EXPECTED_AMOUNT, SUM, "Expected amount")
                        .title("Opportunities distribution by Country ")
                        .width(700).height(400)
                        .margins(20, 50, 50, 0)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
    }

    private void initMeterChartCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Meter Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Basic", createPlaceRequest(
                DisplayerSettingsFactory.newMeterChartSettings()
                        .title("Sales goal")
                        .dataset(SALES_OPPS)
                        .column(AMOUNT, SUM, "Total amount")
                        .width(400).height(200)
                        .meter(0, 5000000, 8000000, 10000000)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Multiple", createPlaceRequest(
                DisplayerSettingsFactory.newMeterChartSettings()
                        .title("Expected amount per year")
                        .dataset(SALES_OPPS)
                        .group(CREATION_DATE).dynamic(12, YEAR, true)
                        .column(CREATION_DATE, "Year")
                        .column(AMOUNT, SUM, "Amount")
                        .width(600).height(200)
                        .meter(0, 1000000, 3000000, 5000000)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Multiple (static)", createPlaceRequest(
                DisplayerSettingsFactory.newMeterChartSettings()
                        .title("Heart rate")
                        .width(500).height(200)
                        .meter(30, 160, 190, 220)
                        .column("person", "Person")
                        .column("heartRate", "Heart rate")
                        .dataset(DataSetFactory.newDataSetBuilder()
                                .label("person")
                                .number("heartRate")
                                .row("David", 52)
                                .row("Roger", 120)
                                .row("Mark", 74)
                                .row("Michael", 78)
                                .row("Kris", 74)
                                .buildDataSet())
                        .buildSettings()
        )));

        // nodeList.add(new GalleryNodeDisplayer("Multiple (date)", ...)));
    }

    private void initMapChartCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Map");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("GeoMap", createPlaceRequest(
                DisplayerSettingsFactory.newMapChartSettings()
                        .dataset(SALES_OPPS)
                        .group(COUNTRY)
                        .column(COUNTRY, "Country")
                        .column(AMOUNT, SUM, "Total amount")
                        .title("By Country")
                        .width(700).height(500)
                        .margins(10, 10, 10, 10)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
    }

    private void initTableReportCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Table report");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Basic", createPlaceRequest(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(SALES_OPPS)
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
                        .title("List of Opportunities")
                        .tablePageSize(10)
                        .tableOrderEnabled(true)
                        .tableOrderDefault(AMOUNT, DESCENDING)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Filtered", createPlaceRequest(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(SALES_OPPS)
                        .column(CUSTOMER, "Customer")
                        .column(PRODUCT, "Product")
                        .column(STATUS, "Status")
                        .column(SOURCE, "Source")
                        .column(CREATION_DATE, "Creation")
                        .column(EXPECTED_AMOUNT, "Expected")
                        .column(CLOSING_DATE, "Closing")
                        .column(AMOUNT, "Amount")
                        .filter(COUNTRY, OR(equalsTo("United States"), equalsTo("Brazil")))
                        .title("Opportunities in USA & Brazil")
                        .tablePageSize(10)
                        .tableOrderEnabled(true)
                        .tableOrderDefault(AMOUNT, DESCENDING)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Grouped", createPlaceRequest(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(SALES_OPPS)
                        .group(COUNTRY)
                        .column(COUNTRY, "Country")
                        .column(COUNT, "#Opps")
                        .column(AMOUNT, MIN, "Min")
                        .column(AMOUNT, MAX, "Max")
                        .column(AMOUNT, AVERAGE, "Average")
                        .column(AMOUNT, SUM, "Total")
                        .sort("Total", DESCENDING)
                        .title("Country Summary")
                        .tablePageSize(10)
                        .tableOrderEnabled(true)
                        .tableOrderDefault("Country", DESCENDING)
                        .filterOn(false, true, true)
                        .buildSettings()
        )));
        nodeList.add(new GalleryPlaceRequest("Default (drill-down)", createPlaceRequest(
                DisplayerSettingsFactory.newTableSettings()
                        .dataset(SALES_OPPS)
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
                        .title("List of Opportunities")
                        .tablePageSize(10)
                        .tableOrderEnabled(true)
                        .tableOrderDefault(AMOUNT, DESCENDING)
                        .filterOn(true, true, true)
                        .renderer(TableRenderer.UUID)
                        .buildSettings()
        )));
    }

    private void initDashboardCategory() {
        GalleryTreeNodeList nodeList = new GalleryTreeNodeList("Dashboards");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryPlaceRequest("Sales goals", createPlaceRequest("salesGoal")));
        nodeList.add(new GalleryPlaceRequest("Sales pipeline", createPlaceRequest("salesPipeline")));
        nodeList.add(new GalleryPlaceRequest("Sales per country", createPlaceRequest("salesPerCountry")));
        nodeList.add(new GalleryPlaceRequest("Sales reports", createPlaceRequest("salesReports")));
        nodeList.add(new GalleryPlaceRequest("Expense reports", createPlaceRequest("expenseReports")));
        nodeList.add(new GalleryPlaceRequest("System metrics (real-time)", createPlaceRequest("metrics_realtime")));
        nodeList.add(new GalleryPlaceRequest("System metrics (historic)", createPlaceRequest("metrics_analytic")));
    }
}
