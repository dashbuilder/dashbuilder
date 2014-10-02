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

        nodeList.add(new GalleryNodeDisplayer("Horizontal", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Vertical (3D)", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Multiple", GalleryEditorType.FORM,
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

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.FORM,
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

        nodeList.add(new GalleryNodeDisplayer("Drill-down", GalleryEditorType.FORM,
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

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Multiple", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", GalleryEditorType.FORM,
                DisplayerSettingsFactory.newLineChartSettings()
                .title("Sales Evolution Per Year")
                .margins(20, 80, 50, 120)
                .column("Month")
                .column("Sales in 2014")
                .column("Sales in 2015")
                .column("Sales in 2016")
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
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initAreaChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Area Chart");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Fixed (per month)", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Drill-down", GalleryEditorType.FORM,
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

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.FORM,
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

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.FORM,
                DisplayerSettingsFactory.newMeterChartSettings()
                .title("Sales goal")
                .dataset(SALES_OPPS)
                .sum(AMOUNT, "Total amount")
                .width(400).height(200)
                .meter(0, 5000000, 8000000, 10000000)
                .column("Total amount")
                .buildSettings()
        ));
        nodeList.add(new GalleryNodeDisplayer("Multiple", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", GalleryEditorType.FORM,
                DisplayerSettingsFactory.newMeterChartSettings()
                .title("Heart rate")
                .width(500).height(200)
                .meter(30, 160, 190, 220)
                .column("Person")
                .column("Heart rate")
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
        ));

        // nodeList.add(new GalleryNodeKPI("Multiple (date)", ...));
    }

    private void initMapChartCategory() {
        GalleryNodeList nodeList = new GalleryNodeList("Map");
        mainNodes.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("GeoMap", GalleryEditorType.FORM,
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

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Filtered", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Grouped", GalleryEditorType.FORM,
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
        nodeList.add(new GalleryNodeDisplayer("Default (drill-down)", GalleryEditorType.FORM,
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

        nodeList.add( new GalleryNodeDisplayer("Horizontal", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Product\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Total amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"barChart\": {\n" +
                                "        \"bar_horizontal\": \"true\"\n" +
                                "    },\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"30\",\n" +
                                "            \"top\": \"10\",\n" +
                                "            \"left\": \"120\",\n" +
                                "            \"right\": \"120\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"BARCHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"By Product\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"product\",\n" +
                                "                    \"columnId\": \"product\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ) );

        nodeList.add(new GalleryNodeDisplayer("Vertical (3D)", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Product\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Total amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"barChart\": {\n" +
                                "        \"bar_horizontal\": \"false\"\n" +
                                "    },\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"80\",\n" +
                                "            \"top\": \"10\",\n" +
                                "            \"left\": \"120\",\n" +
                                "            \"right\": \"120\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"BARCHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"By Product\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"product\",\n" +
                                "                    \"columnId\": \"product\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ) );

        nodeList.add(new GalleryNodeDisplayer("Multiple", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Country\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"Min\",\n" +
                                "            \"columnDisplayName\": \"Min\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"Max\",\n" +
                                "            \"columnDisplayName\": \"Max\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"Average\",\n" +
                                "            \"columnDisplayName\": \"Avg\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"barChart\": {\n" +
                                "        \"bar_horizontal\": \"true\"\n" +
                                "    },\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"50\",\n" +
                                "            \"top\": \"10\",\n" +
                                "            \"left\": \"120\",\n" +
                                "            \"right\": \"100\"\n" +
                                "        },\n" +
                                "        \"width\": \"700\",\n" +
                                "        \"height\": \"600\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"BARCHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"By Country (min/max/avg)\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"country\",\n" +
                                "                    \"columnId\": \"Country\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"columnId\": \"#Opps\",\n" +
                                "                        \"function\": \"COUNT\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Min\",\n" +
                                "                        \"function\": \"MIN\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Max\",\n" +
                                "                        \"function\": \"MAX\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Average\",\n" +
                                "                        \"function\": \"AVERAGE\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Total\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ) );


        nodeList = new GalleryNodeList("Pie Chart");
        jsonExamples.add(nodeList);

        nodeList.add( new GalleryNodeDisplayer("Basic", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Status\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Total amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"10\",\n" +
                                "            \"top\": \"10\",\n" +
                                "            \"left\": \"10\",\n" +
                                "            \"right\": \"10\"\n" +
                                "        },\n" +
                                "        \"3d\": \"true\",\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"PIECHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"By Status\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"status\",\n" +
                                "                    \"columnId\": \"status\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ) );

        nodeList.add(new GalleryNodeDisplayer("Drill-down", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Status\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Total amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"10\",\n" +
                                "            \"top\": \"10\",\n" +
                                "            \"left\": \"10\",\n" +
                                "            \"right\": \"10\"\n" +
                                "        },\n" +
                                "        \"3d\": \"true\",\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"PIECHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"true\",\n" +
                                "        \"enabled\": \"true\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"By Pipeline/Status/Sales person\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"pipeline\",\n" +
                                "                    \"columnId\": \"pipeline\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            },\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"status\",\n" +
                                "                    \"columnId\": \"status\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            },\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"salesPerson\",\n" +
                                "                    \"columnId\": \"salesPerson\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ) );


        nodeList = new GalleryNodeList("Line Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Closing date\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Total amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"50\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"100\",\n" +
                                "            \"right\": \"120\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"LINECHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Sales opportunities evolution\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"closingDate\",\n" +
                                "                    \"columnId\": \"closingDate\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"12\",\n" +
                                "                    \"intervalSize\": \"MONTH\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Multiple", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Country\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"Min\",\n" +
                                "            \"columnDisplayName\": \"Min\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"Max\",\n" +
                                "            \"columnDisplayName\": \"Max\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"Average\",\n" +
                                "            \"columnDisplayName\": \"Avg\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"100\",\n" +
                                "            \"top\": \"30\",\n" +
                                "            \"left\": \"80\",\n" +
                                "            \"right\": \"80\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"LINECHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"By Country (min/max/avg)\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"country\",\n" +
                                "                    \"columnId\": \"Country\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"columnId\": \"#Opps\",\n" +
                                "                        \"function\": \"COUNT\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Min\",\n" +
                                "                        \"function\": \"MIN\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Max\",\n" +
                                "                        \"function\": \"MAX\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Average\",\n" +
                                "                        \"function\": \"AVERAGE\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Total\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Month\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Sales in 2014\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Sales in 2015\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Sales in 2016\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"80\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"50\",\n" +
                                "            \"right\": \"120\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"LINECHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Sales Evolution Per Year\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSet\": {\n" +
                                "        \"column.0\": {\n" +
                                "            \"id\": \"month\",\n" +
                                "            \"type\": \"LABEL\",\n" +
                                "            \"values\": [\"JANUARY\", \"FEBRUARY\", \"MARCH\", \"APRIL\", \"MAY\", \"JUNE\", \"JULY\", \"AUGUST\", \"SEPTEMBER\", \"OCTOBER\", \"NOVEMBER\", \"DECEMBER\"]\n" +
                                "        },\n" +
                                "        \"column.1\": {\n" +
                                "            \"id\": \"2014\",\n" +
                                "            \"type\": \"NUMBER\",\n" +
                                "            \"values\": [\"1000.0\", \"1400.0\", \"1300.0\", \"900.0\", \"1300.0\", \"1010.0\", \"1050.0\", \"2300.0\", \"1900.0\", \"1200.0\", \"1400.0\", \"1100.0\"]\n" +
                                "        },\n" +
                                "        \"column.2\": {\n" +
                                "            \"id\": \"2015\",\n" +
                                "            \"type\": \"NUMBER\",\n" +
                                "            \"values\": [\"2000.0\", \"2300.0\", \"2000.0\", \"2100.0\", \"2300.0\", \"2000.0\", \"2400.0\", \"2000.0\", \"2700.0\", \"2200.0\", \"2100.0\", \"2100.0\"]\n" +
                                "        },\n" +
                                "        \"column.3\": {\n" +
                                "            \"id\": \"2016\",\n" +
                                "            \"type\": \"NUMBER\",\n" +
                                "            \"values\": [\"3000.0\", \"2000.0\", \"1400.0\", \"1500.0\", \"1600.0\", \"1500.0\", \"3000.0\", \"3200.0\", \"3000.0\", \"3100.0\", \"3100.0\", \"4200.0\"]\n" +
                                "        }\n" +
                                "    }\n" +
                                "}"
                )
        ));


        nodeList = new GalleryNodeList("Area Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Closing date\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Expected amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"50\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"100\",\n" +
                                "            \"right\": \"120\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"AREACHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Expected Pipeline\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"closingDate\",\n" +
                                "                    \"columnId\": \"closingDate\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"24\",\n" +
                                "                    \"intervalSize\": \"MONTH\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"expectedAmount\",\n" +
                                "                        \"columnId\": \"expectedAmount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Fixed (per month)", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Closing date\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Expected amount per month\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"80\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"100\",\n" +
                                "            \"right\": \"100\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"AREACHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Pipeline (best month)\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"closingDate\",\n" +
                                "                    \"columnId\": \"closingDate\",\n" +
                                "                    \"groupStrategy\": \"FIXED\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"intervalSize\": \"MONTH\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"expectedAmount\",\n" +
                                "                        \"columnId\": \"expectedAmount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Drill-down", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Closing date\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Expected amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"70\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"100\",\n" +
                                "            \"right\": \"120\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"300\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"AREACHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"true\",\n" +
                                "        \"enabled\": \"true\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Expected Pipeline\"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"closingDate\",\n" +
                                "                    \"columnId\": \"closingDate\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"12\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"expectedAmount\",\n" +
                                "                        \"columnId\": \"expectedAmount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));


        nodeList = new GalleryNodeList("Bubble Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnId\": \"country\",\n" +
                                "            \"columnDisplayName\": \"Country\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"opps\",\n" +
                                "            \"columnDisplayName\": \"Number of opportunities\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"probability\",\n" +
                                "            \"columnDisplayName\": \"Average probability\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"country\",\n" +
                                "            \"columnDisplayName\": \"Country\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"expectedAmount\",\n" +
                                "            \"columnDisplayName\": \"Expected amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"50\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"50\",\n" +
                                "            \"right\": \"0\"\n" +
                                "        },\n" +
                                "        \"width\": \"700\",\n" +
                                "        \"height\": \"400\",\n" +
                                "        \"legend\": {\n" +
                                "            \"show\": \"true\",\n" +
                                "            \"position\": \"POSITION_RIGHT\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"type\": \"BUBBLECHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Opportunities distribution by Country \"\n" +
                                "    },\n" +
                                "    \"axis\": {\n" +
                                "        \"x\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        },\n" +
                                "        \"y\": {\n" +
                                "            \"labels_show\": \"false\"\n" +
                                "        }\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"country\",\n" +
                                "                    \"columnId\": \"country\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"columnId\": \"opps\",\n" +
                                "                        \"function\": \"COUNT\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"probability\",\n" +
                                "                        \"columnId\": \"probability\",\n" +
                                "                        \"function\": \"AVERAGE\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"expectedAmount\",\n" +
                                "                        \"columnId\": \"expectedAmount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));


        nodeList = new GalleryNodeList("Table report");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnId\": \"country\",\n" +
                                "            \"columnDisplayName\": \"Country\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"customer\",\n" +
                                "            \"columnDisplayName\": \"Customer\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"product\",\n" +
                                "            \"columnDisplayName\": \"Product\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"salesPerson\",\n" +
                                "            \"columnDisplayName\": \"Salesman\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"status\",\n" +
                                "            \"columnDisplayName\": \"Status\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"source\",\n" +
                                "            \"columnDisplayName\": \"Source\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"creationDate\",\n" +
                                "            \"columnDisplayName\": \"Creation\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"expectedAmount\",\n" +
                                "            \"columnDisplayName\": \"Expected\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"closingDate\",\n" +
                                "            \"columnDisplayName\": \"Closing\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"amount\",\n" +
                                "            \"columnDisplayName\": \"Amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"table\": {\n" +
                                "        \"sort\": {\n" +
                                "            \"order\": \"DESCENDING\",\n" +
                                "            \"enabled\": \"true\",\n" +
                                "            \"columnId\": \"amount\"\n" +
                                "        },\n" +
                                "        \"pageSize\": \"10\",\n" +
                                "        \"width\": \"0\"\n" +
                                "    },\n" +
                                "    \"type\": \"TABLE\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"List of Opportunities\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\"\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Filtered", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnId\": \"customer\",\n" +
                                "            \"columnDisplayName\": \"Customer\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"product\",\n" +
                                "            \"columnDisplayName\": \"Product\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"status\",\n" +
                                "            \"columnDisplayName\": \"Status\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"source\",\n" +
                                "            \"columnDisplayName\": \"Source\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"creationDate\",\n" +
                                "            \"columnDisplayName\": \"Creation\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"expectedAmount\",\n" +
                                "            \"columnDisplayName\": \"Expected\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"closingDate\",\n" +
                                "            \"columnDisplayName\": \"Closing\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"amount\",\n" +
                                "            \"columnDisplayName\": \"Amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"table\": {\n" +
                                "        \"sort\": {\n" +
                                "            \"order\": \"DESCENDING\",\n" +
                                "            \"enabled\": \"true\",\n" +
                                "            \"columnId\": \"amount\"\n" +
                                "        },\n" +
                                "        \"pageSize\": \"10\",\n" +
                                "        \"width\": \"0\"\n" +
                                "    },\n" +
                                "    \"type\": \"TABLE\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Opportunities in USA & Brazil\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"filterOps\": [\n" +
                                "            {\n" +
                                "                \"columnId\": \"country\",\n" +
                                "                \"functionType\": \"OR\",\n" +
                                "                \"terms\": [\n" +
                                "                    {\n" +
                                "                        \"columnId\": \"country\",\n" +
                                "                        \"functionType\": \"IS_EQUALS_TO\",\n" +
                                "                        \"terms\": [\"United States\"]\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"columnId\": \"country\",\n" +
                                "                        \"functionType\": \"IS_EQUALS_TO\",\n" +
                                "                        \"terms\": [\"Brazil\"]\n" +
                                "                    }\n" +
                                "                ]\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Grouped", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"table\": {\n" +
                                "        \"sort\": {\n" +
                                "            \"order\": \"DESCENDING\",\n" +
                                "            \"enabled\": \"true\",\n" +
                                "            \"columnId\": \"Country\"\n" +
                                "        },\n" +
                                "        \"pageSize\": \"10\",\n" +
                                "        \"width\": \"0\"\n" +
                                "    },\n" +
                                "    \"type\": \"TABLE\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Country Summary\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"country\",\n" +
                                "                    \"columnId\": \"Country\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"columnId\": \"#Opps\",\n" +
                                "                        \"function\": \"COUNT\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Min\",\n" +
                                "                        \"function\": \"MIN\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Max\",\n" +
                                "                        \"function\": \"MAX\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Average\",\n" +
                                "                        \"function\": \"AVERAGE\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Total\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Default (drill-down)", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnId\": \"country\",\n" +
                                "            \"columnDisplayName\": \"Country\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"customer\",\n" +
                                "            \"columnDisplayName\": \"Customer\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"product\",\n" +
                                "            \"columnDisplayName\": \"Product\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"salesPerson\",\n" +
                                "            \"columnDisplayName\": \"Salesman\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"status\",\n" +
                                "            \"columnDisplayName\": \"Status\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"source\",\n" +
                                "            \"columnDisplayName\": \"Source\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"creationDate\",\n" +
                                "            \"columnDisplayName\": \"Creation\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"expectedAmount\",\n" +
                                "            \"columnDisplayName\": \"Expected\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"closingDate\",\n" +
                                "            \"columnDisplayName\": \"Closing\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnId\": \"amount\",\n" +
                                "            \"columnDisplayName\": \"Amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"table\": {\n" +
                                "        \"sort\": {\n" +
                                "            \"order\": \"DESCENDING\",\n" +
                                "            \"enabled\": \"true\",\n" +
                                "            \"columnId\": \"amount\"\n" +
                                "        },\n" +
                                "        \"pageSize\": \"10\",\n" +
                                "        \"width\": \"0\"\n" +
                                "    },\n" +
                                "    \"type\": \"TABLE\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"true\",\n" +
                                "        \"enabled\": \"true\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"List of Opportunities\"\n" +
                                "    },\n" +
                                "    \"renderer\": \"table\",\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\"\n" +
                                "    }\n" +
                                "}"
                )
        ));


        nodeList = new GalleryNodeList("Meter Chart");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("Basic", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Total amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"50\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"80\",\n" +
                                "            \"right\": \"80\"\n" +
                                "        },\n" +
                                "        \"width\": \"400\",\n" +
                                "        \"height\": \"200\"\n" +
                                "    },\n" +
                                "    \"meter\": {\n" +
                                "        \"critical\": \"8000000\",\n" +
                                "        \"warning\": \"5000000\",\n" +
                                "        \"start\": \"0\",\n" +
                                "        \"end\": \"10000000\"\n" +
                                "    },\n" +
                                "    \"type\": \"METERCHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Sales goal\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"Total amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Multiple", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Year\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"50\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"80\",\n" +
                                "            \"right\": \"80\"\n" +
                                "        },\n" +
                                "        \"width\": \"600\",\n" +
                                "        \"height\": \"200\"\n" +
                                "    },\n" +
                                "    \"meter\": {\n" +
                                "        \"critical\": \"3000000\",\n" +
                                "        \"warning\": \"1000000\",\n" +
                                "        \"start\": \"0\",\n" +
                                "        \"end\": \"5000000\"\n" +
                                "    },\n" +
                                "    \"type\": \"METERCHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Expected amount per year\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"creationDate\",\n" +
                                "                    \"columnId\": \"creationDate\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"-1\",\n" +
                                "                    \"intervalSize\": \"YEAR\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));

        nodeList.add(new GalleryNodeDisplayer("Multiple (static)", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Person\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Heart rate\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"50\",\n" +
                                "            \"top\": \"20\",\n" +
                                "            \"left\": \"80\",\n" +
                                "            \"right\": \"80\"\n" +
                                "        },\n" +
                                "        \"width\": \"500\",\n" +
                                "        \"height\": \"200\"\n" +
                                "    },\n" +
                                "    \"meter\": {\n" +
                                "        \"critical\": \"190\",\n" +
                                "        \"warning\": \"160\",\n" +
                                "        \"start\": \"30\",\n" +
                                "        \"end\": \"220\"\n" +
                                "    },\n" +
                                "    \"type\": \"METERCHART\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"Heart rate\"\n" +
                                "    },\n" +
                                "    \"dataSet\": {\n" +
                                "        \"column.0\": {\n" +
                                "            \"id\": \"person\",\n" +
                                "            \"type\": \"LABEL\",\n" +
                                "            \"values\": [\"David\", \"Roger\", \"Mark\", \"Michael\", \"Kris\"]\n" +
                                "        },\n" +
                                "        \"column.1\": {\n" +
                                "            \"id\": \"heartRate\",\n" +
                                "            \"type\": \"NUMBER\",\n" +
                                "            \"values\": [\"52\", \"120\", \"74\", \"78\", \"74\"]\n" +
                                "        }\n" +
                                "    }\n" +
                                "}"
                )
        ));


        nodeList = new GalleryNodeList("Map");
        jsonExamples.add(nodeList);

        nodeList.add(new GalleryNodeDisplayer("GeoMap", GalleryEditorType.JSON,
                jsonHelper.fromJsonString(
                                "{\n" +
                                "    \"columns\": [\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Country\"\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"columnDisplayName\": \"Total amount\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chart\": {\n" +
                                "        \"margin\": {\n" +
                                "            \"bottom\": \"10\",\n" +
                                "            \"top\": \"10\",\n" +
                                "            \"left\": \"10\",\n" +
                                "            \"right\": \"10\"\n" +
                                "        },\n" +
                                "        \"width\": \"700\",\n" +
                                "        \"height\": \"500\"\n" +
                                "    },\n" +
                                "    \"type\": \"MAP\",\n" +
                                "    \"filter\": {\n" +
                                "        \"listening\": \"false\",\n" +
                                "        \"selfapply\": \"false\",\n" +
                                "        \"enabled\": \"false\",\n" +
                                "        \"notification\": \"false\"\n" +
                                "    },\n" +
                                "    \"title\": {\n" +
                                "        \"visible\": \"true\",\n" +
                                "        \"title\": \"By Country\"\n" +
                                "    },\n" +
                                "    \"dataSetLookup\": {\n" +
                                "        \"dataSetUuid\": \"dataset-sales-opportunities\",\n" +
                                "        \"rowCount\": \"-1\",\n" +
                                "        \"rowOffset\": \"0\",\n" +
                                "        \"groupOps\": [\n" +
                                "            {\n" +
                                "                \"columnGroup\": {\n" +
                                "                    \"sourceId\": \"country\",\n" +
                                "                    \"columnId\": \"country\",\n" +
                                "                    \"groupStrategy\": \"DYNAMIC\",\n" +
                                "                    \"maxIntervals\": \"15\",\n" +
                                "                    \"asc\": \"true\",\n" +
                                "                    \"firstMonthOfYear\": \"JANUARY\",\n" +
                                "                    \"firstDayOfWeek\": \"MONDAY\"\n" +
                                "                },\n" +
                                "                \"groupFunctions\": [\n" +
                                "                    {\n" +
                                "                        \"sourceId\": \"amount\",\n" +
                                "                        \"columnId\": \"amount\",\n" +
                                "                        \"function\": \"SUM\"\n" +
                                "                    }\n" +
                                "                ],\n" +
                                "                \"selectedIntervals\": []\n" +
                                "            }\n" +
                                "        ]\n" +
                                "    }\n" +
                                "}"
                )
        ));
    }
}
