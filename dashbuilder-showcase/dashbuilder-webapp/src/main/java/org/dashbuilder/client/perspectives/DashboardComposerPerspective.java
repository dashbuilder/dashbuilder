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
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.panels.impl.MultiTabWorkbenchPanelPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.shared.sales.SalesConstants.*;

/**
 * The dashboard composer perspective.
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "DashboardComposerPerspective", isDefault = true)
public class DashboardComposerPerspective {

    @Inject
    private PlaceManager placeManager;

    @Inject
    DisplayerSettingsJSONMarshaller jsonHelper;

    @Perspective
    public PerspectiveDefinition buildPerspective() {
        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( MultiTabWorkbenchPanelPresenter.class.getName() );
        perspective.setName("Composer");
        return perspective;
    }

    @WorkbenchMenu
    public Menus buildMenuBar() {
        return MenuFactory
                .newTopLevelMenu("New displayer")
                .withItems(getNewDisplayerMenuItems())
                .endMenu().build();
    }

    private PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonHelper.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String,String>();
        params.put("json", json);
        params.put("edit", "true");
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }

    private List<? extends MenuItem> getNewDisplayerMenuItems() {
        List<MenuItem> results = new ArrayList<MenuItem>();

        results.add(MenuFactory.newSimpleItem("Bar chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newBarChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(PRODUCT)
                                .sum(AMOUNT)
                                .title("Bar chart")
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
                                .title("Pie chart")
                                .margins(10, 10, 10, 10)
                                .column("Status")
                                .column("Total amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Line chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newLineChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(CLOSING_DATE, 12, MONTH)
                                .sum(AMOUNT)
                                .title("Line chart")
                                .margins(20, 50, 100, 120)
                                .column("Closing date")
                                .column("Total amount")
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        results.add(MenuFactory.newSimpleItem("Area chart").respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(createPlaceRequest(
                        DisplayerSettingsFactory.newAreaChartSettings()
                                .uuid(Document.get().createUniqueId())
                                .dataset(SALES_OPPS)
                                .group(CLOSING_DATE, 24, MONTH)
                                .sum(EXPECTED_AMOUNT)
                                .title("Area chart")
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
                                .title("Bubble chart")
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
                                .title("Map")
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
                                .title("Country Summary")
                                .tablePageSize(10)
                                .tableOrderEnabled(true)
                                .tableOrderDefault("Country", DESCENDING)
                                .filterOn(false, true, true)
                                .buildSettings()));
            }}).endMenu().build().getItems().get(0));

        return results;
    }
}
