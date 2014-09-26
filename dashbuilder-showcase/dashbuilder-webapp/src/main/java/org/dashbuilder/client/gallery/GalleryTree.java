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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.events.DataSetModifiedEvent;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.DisplayerSettingsManager;
import org.dashbuilder.client.sales.widgets.SalesExpectedByDate;
import org.dashbuilder.client.sales.widgets.SalesDistributionByCountry;
import org.dashbuilder.client.sales.widgets.SalesGoals;
import org.dashbuilder.client.sales.widgets.SalesTableReports;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.renderer.table.client.TableRenderer;
import org.uberfire.workbench.events.NotificationEvent;

import org.dashbuilder.client.gallery.resources.json.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.date.Month.*;
import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;
import static org.uberfire.workbench.events.NotificationEvent.NotificationType.*;

/**
 * The Gallery tree.
 */
@ApplicationScoped
public class GalleryTree {

    private List<GalleryNode> mainNodes = new ArrayList<GalleryNode>();

    @Inject
    private Event<NotificationEvent> workbenchNotification;

    @Inject DisplayerSettingsManager settingsManager;

    @Inject DisplayerSettingsJSONMarshaller jsonHelper;

    private SalesGoals salesGoalsWidget;
    private SalesExpectedByDate salesByDateWidget;
    private SalesDistributionByCountry salesByCountryWidget;
    private SalesTableReports salesReportsWidget;

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
        initJsonExamples();
    }

    private void onSalesDataSetOutdated(@Observes DataSetModifiedEvent event) {
        checkNotNull("event", event);

        String targetUUID = event.getDataSetMetadata().getUUID();
        if (SALES_OPPS.equals(targetUUID)) {
            workbenchNotification.fire(new NotificationEvent("The sales data set has been modified. Refreshing the dashboard ...", INFO));
            salesGoalsWidget.redrawAll();
            salesByCountryWidget.redrawAll();
            salesByDateWidget.redrawAll();
            salesReportsWidget.redrawAll();
        }
    }

    private void initBarChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Bar Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Horizontal", true,
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PRODUCT)
                        .sum(AMOUNT)
                        .title("By Product")
                        .column("Product")
                        .column("Total amount")
                        .horizontal()
                        .margins(10, 30, 120, 120)
                        .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Vertical (3D)", true,
                DisplayerSettingsFactory.newBarChartSettings()
                        .dataset(SALES_OPPS)
                        .group(PRODUCT)
                        .sum(AMOUNT)
                        .title("By Product")
                        .column("Product")
                        .column("Total amount")
                        .vertical().set3d(true)
                        .margins(10, 80, 120, 120)
                        .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple", true,
                DisplayerSettingsFactory.newBarChartSettings()
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
                        .buildSettings()
        ));
    }

    private void initPieChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Pie Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                DisplayerSettingsFactory.newPieChartSettings()
                .dataset(SALES_OPPS)
                .group(STATUS)
                .sum(AMOUNT)
                .title("By Status")
                .margins(10, 10, 10, 10)
                .column("Status")
                .column("Total amount")
                .buildSettings()
        ));

        nodeList.add(new GalleryNodeDisplayer("Drill-down", true,
                DisplayerSettingsFactory.newPieChartSettings()
                .dataset(SALES_OPPS)
                .group(PIPELINE)
                .sum(AMOUNT)
                .group(STATUS)
                .sum(AMOUNT)
                .group(SALES_PERSON)
                .sum(AMOUNT)
                .title("By Pipeline/Status/Sales person")
                .margins(10, 10, 10, 10)
                .column("Status")
                .column("Total amount")
                .filterOn(true, false, false)
                .buildSettings()
        ));
    }

    private void initLineChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Line Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                DisplayerSettingsFactory.newLineChartSettings()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 12, MONTH)
                .sum(AMOUNT)
                .title("Sales opportunities evolution")
                .margins(20, 50, 100, 120)
                .column("Closing date")
                .column("Total amount")
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple", true,
                DisplayerSettingsFactory.newLineChartSettings()
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
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", true,
                DisplayerSettingsFactory.newLineChartSettings()
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
                .buildSettings()
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initAreaChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Area Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE, 24, MONTH)
                .sum(EXPECTED_AMOUNT)
                .title("Expected Pipeline")
                .margins(20, 50, 100, 120)
                .column("Closing date")
                .column("Expected amount")
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Fixed (per month)", true,
                DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE)
                .fixed(MONTH).firstMonth(JANUARY).asc()
                .sum(EXPECTED_AMOUNT)
                .title("Pipeline (best month)")
                .margins(20, 80, 100, 100)
                .column("Closing date")
                .column("Expected amount per month")
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Drill-down", true,
                DisplayerSettingsFactory.newAreaChartSettings()
                        .dataset(SALES_OPPS)
                        .group(CLOSING_DATE, 12, (String) null)
                        .sum(EXPECTED_AMOUNT)
                        .title("Expected Pipeline")
                        .margins(20, 70, 100, 120)
                        .column("Closing date")
                        .column("Expected amount")
                        .filterOn(true, false, false)
                        .buildSettings()
        ));
    }

    private void initBubbleChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Bubble Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                DisplayerSettingsFactory.newBubbleChartSettings()
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
                .buildSettings()
        ));
    }

    private void initMeterChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Meter Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                DisplayerSettingsFactory.newMeterChartSettings()
                .title("Sales goal")
                .dataset(SALES_OPPS)
                .sum(AMOUNT, "Total amount")
                .width(400).height(200)
                .meter(0, 5000000, 8000000, 10000000)
                .column("Total amount")
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple", true,
                DisplayerSettingsFactory.newMeterChartSettings()
                .title("Expected amount per year")
                .dataset(SALES_OPPS)
                .group(CREATION_DATE, YEAR)
                .sum(AMOUNT)
                .width(600).height(200)
                .meter(0, 1000000, 3000000, 5000000)
                .column("Year")
                .column("Amount")
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", true,
                DisplayerSettingsFactory.newMeterChartSettings()
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
                .buildSettings()
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initMapChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Map");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("GeoMap", true,
                DisplayerSettingsFactory.newMapChartSettings()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .sum(AMOUNT)
                .title("By Country")
                .width(700).height(500)
                .margins(10, 10, 10, 10)
                .column("Country")
                .column("Total amount")
                .buildSettings()
        ));
    }

    private void initTableReportCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Table report");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .title("List of Opportunities")
                .tablePageSize(10)
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
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Filtered", true,
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .filter(COUNTRY, OR(isEqualsTo("United States"), isEqualsTo("Brazil")))
                .title("Opportunities in USA & Brazil")
                .tablePageSize(10)
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
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Grouped", true,
                DisplayerSettingsFactory.newTableSettings()
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
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Default (drill-down)", true,
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .title("List of Opportunities")
                .tablePageSize(10)
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
                .filterOn(true, false, false)
                .renderer(TableRenderer.UUID)
                .buildSettings()
        ));
    }

    private void initDashboardCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Dashboards");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNode("Sales goal") {
            public Widget createWidget() {
                return salesGoalsWidget = new SalesGoals();
            }
        });
        nodeList.add(new GalleryNode("Sales pipeline") {
            public Widget createWidget() {
                return salesByDateWidget = new SalesExpectedByDate();
            }
        });
        nodeList.add(new GalleryNode("Sales per country") {
            public Widget createWidget() {
                return salesByCountryWidget = new SalesDistributionByCountry();
            }
        });
        nodeList.add(new GalleryNode("Sales reports") {
            public Widget createWidget() {
                return salesReportsWidget = new SalesTableReports();
            }
        });
    }

    private void initJsonExamples() {
        GalleryNodeList jsonExamples = new GalleryNodeList( "JSON Examples" );
        mainNodes.add( jsonExamples );

        GalleryNodeList nodeList = new GalleryNodeList("Bar Chart");
        jsonExamples.add(nodeList);

        nodeList.add( new GalleryNodeDisplayer("Horizontal", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.barChart_horizontal() )
        ) );
        nodeList.add(new GalleryNodeDisplayer("Vertical (3D)", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.barChart_vertical() )
        ) );
        nodeList.add(new GalleryNodeDisplayer("Multiple", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.barChart_multiple() )
        ) );

        nodeList = new GalleryNodeList("Pie Chart");
        jsonExamples.add(nodeList);

        nodeList.add( new GalleryNodeDisplayer("Basic", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.pieChart_basic() )
        ) );
        nodeList.add(new GalleryNodeDisplayer("Drill-down", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.pieChart_drillDown() )
        ) );


        nodeList = new GalleryNodeList("Line Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.lineChart_basic() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.lineChart_multiple() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.lineChart_multipleStatic() )
        ));


        nodeList = new GalleryNodeList("Area Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.areaChart_basic() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Fixed (per month)", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.areaChart_fixedMonth() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Drill-down", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.areaChart_drillDown() )
        ));


        nodeList = new GalleryNodeList("Bubble Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.bubbleChart_basic() )
        ));


        nodeList = new GalleryNodeList("Table report");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.tableReport_basic() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Filtered", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.tableReport_filtered() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Grouped", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.tableReport_grouped() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Default (drill-down)", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.tableReport_default() )
        ));


        nodeList = new GalleryNodeList("Meter Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.meterChart_basic() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.meterChart_multiple() )
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.meterChart_multipleStatic() )
        ));


        nodeList = new GalleryNodeList("Map");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("GeoMap", true,
                jsonHelper.fromJson( DisplayerJsonDefinitions.INSTANCE.geoMap() )
        ));
    }
}
