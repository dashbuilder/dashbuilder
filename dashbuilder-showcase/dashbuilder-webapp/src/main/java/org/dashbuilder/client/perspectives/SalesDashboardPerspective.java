package org.dashbuilder.client.perspectives;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.Position;
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

        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( PanelType.ROOT_TAB);
        perspective.setTransient(true);
        perspective.setName("Sales summary");

        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_STATUS)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_SALESMAN)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_PRODUCT)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_COUNTRY)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_COUNTRY_SUMMARY)));

        PanelDefinition east = new PanelDefinitionImpl(PanelType.SIMPLE_DND);
        east.setMinWidth(500);
        east.setWidth(550);
        east.setMinHeight(350);
        east.setHeight(350);
        east.addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_BY_PIPELINE)));

        PanelDefinition north = new PanelDefinitionImpl(PanelType.SIMPLE_DND);
        north.setMinWidth(500);
        north.setWidth(550);
        north.setMinHeight(350);
        north.setHeight(350);
        north.addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_EXPECTED_PIPELINE)));
        north.insertChild(Position.EAST, east);

        perspective.getRoot().insertChild(Position.NORTH, north);
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
