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
package org.dashbuilder.client.perspectives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.dom.client.Document;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorListener;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorPopup;
import org.dashbuilder.displayer.events.DisplayerUpdatedEvent;
import org.dashbuilder.renderer.table.client.TableRenderer;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.dashbuilder.dataset.group.DateIntervalType.MONTH;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

/**
 * The dashboard composer perspective.
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "DashboardDesignerPerspective")
public class DashboardDesignerPerspective {

    @Inject
    private PlaceManager placeManager;

    @Inject
    DisplayerSettingsJSONMarshaller jsonHelper;

    @Perspective
    public PerspectiveDefinition buildPerspective() {
        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl(MultiListWorkbenchPanelPresenter.class.getName());
        perspective.setName("Designer");
        return perspective;
    }

    @WorkbenchMenu
    public Menus buildMenuBar() {
        return MenuFactory
                .newTopLevelMenu("New displayer")
                .respondsWith(getNewDisplayerCommand())
                .endMenu()
                .newTopLevelMenu("Select prototype")
                .withItems(getSelectPrototypeMenuItems())
                .endMenu().build();
    }

    private Command getNewDisplayerCommand() {
        return new Command() {
            public void execute() {
                /* Displayer settings == null => Create a brand new displayer */
                DisplayerEditorPopup displayerEditor = new DisplayerEditorPopup();
                displayerEditor.init(null, new DisplayerEditorListener() {

                    public void onEditorClosed(DisplayerEditor editor) {
                    }

                    public void onDisplayerSaved(DisplayerEditor editor) {
                        placeManager.goTo(createPlaceRequest(editor.getCurrentSettings()));
                    }
                });
            }
        };
    }

    private PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonHelper.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String,String>();
        params.put("json", json);
        params.put("edit", "true");
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }

    private List<? extends MenuItem> getSelectPrototypeMenuItems() {
        List<MenuItem> results = new ArrayList<MenuItem>();

        results.add(MenuFactory.newSimpleItem("Bar chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newBarChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(PRODUCT)
                                .sum(AMOUNT)
                                .title("Bar chart").titleVisible(false)
                                .column("Product")
                                .column("Total amount")
                                .horizontal()
                                .margins(10, 30, 120, 120)
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Pie chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newPieChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(STATUS)
                                .sum(AMOUNT)
                                .title("Pie chart").titleVisible(false)
                                .margins(10, 10, 10, 10)
                                .column("Status")
                                .column("Total amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }
        }).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Line chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newLineChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(CLOSING_DATE, 12, MONTH)
                                .sum(AMOUNT)
                                .title("Line chart").titleVisible(false)
                                .margins(20, 50, 100, 120)
                                .column("Closing date")
                                .column("Total amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }
        }).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Area chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newAreaChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(CLOSING_DATE, 24, MONTH)
                                .sum(EXPECTED_AMOUNT)
                                .title("Area chart").titleVisible(false)
                                .margins(20, 50, 100, 120)
                                .column("Closing date")
                                .column("Expected amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Bubble chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newBubbleChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(COUNTRY)
                                .count("opps")
                                .avg(PROBABILITY)
                                .sum(EXPECTED_AMOUNT)
                                .title("Bubble chart").titleVisible(false)
                                .width(700).height(400)
                                .margins(20, 50, 50, 0)
                                .column(COUNTRY, "Country")
                                .column("opps", "Number of opportunities")
                                .column(PROBABILITY, "Average probability")
                                .column(COUNTRY, "Country")
                                .column(EXPECTED_AMOUNT, "Expected amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Meter chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newMeterChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .sum(AMOUNT, "Meter chart")
                                .title("Meter chart").titleVisible(false)
                                .width(400).height(200)
                                .meter(0, 5000000, 8000000, 10000000)
                                .column("Total amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Map").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newMapChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(COUNTRY)
                                .sum(AMOUNT)
                                .title("Map").titleVisible(false)
                                .width(700).height(500)
                                .margins(10, 10, 10, 10)
                                .column("Country")
                                .column("Total amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Table").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newTableSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(COUNTRY, "Country")
                                .count("#Opps")
                                .min(AMOUNT, "Min")
                                .max(AMOUNT, "Max")
                                .avg(AMOUNT, "Average")
                                .sum(AMOUNT, "Total")
                                .title("Table").titleVisible(false)
                                .tableWidth(700)
                                .tablePageSize(8)
                                .tableOrderEnabled(true)
                                .tableOrderDefault("Country", ASCENDING)
                                .column("Country")
                                .column("#Opps")
                                .column("Min")
                                .column("Max")
                                .column("Average")
                                .column("Total")
                                .renderer(TableRenderer.UUID)
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        return results;
    }
}
