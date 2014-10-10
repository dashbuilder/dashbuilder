package org.dashbuilder.client.perspectives;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.panels.impl.MultiTabWorkbenchPanelPresenter;
import org.uberfire.client.workbench.panels.impl.StaticWorkbenchPanelPresenter;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

import static org.dashbuilder.client.sales.SalesOppsDisplayers.*;

/**
 * A sample dashboard perspective
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "SalesDashboardPerspective")
public class SalesDashboardPerspective {

    @Inject
    DisplayerSettingsJSONMarshaller jsonHelper;

    @Perspective
    public PerspectiveDefinition buildPerspective() {

        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( MultiTabWorkbenchPanelPresenter.class.getName() );
        perspective.setName("Sales summary");

        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_STATUS)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_SALESMAN)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_PRODUCT)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_COUNTRY)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_COUNTRY_SUMMARY)));

        PanelDefinition east = new PanelDefinitionImpl(StaticWorkbenchPanelPresenter.class.getName());
        east.setMinWidth(400);
        east.setWidth(400);
        east.addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_PIPELINE)));

        PanelDefinition north = new PanelDefinitionImpl(StaticWorkbenchPanelPresenter.class.getName());
        north.setMinWidth(400);
        north.setWidth(500);
        north.addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_EXPECTED_PIPELINE)));
        north.insertChild(CompassPosition.EAST, east);

        perspective.getRoot().insertChild( CompassPosition.NORTH, north);
        return perspective;
    }

    private PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonHelper.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String,String>();
        params.put("json", json);
        params.put("edit", "false");
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }
}
