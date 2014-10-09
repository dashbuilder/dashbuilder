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
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

import static org.dashbuilder.client.sales.SalesOppsDisplayers.*;

/**
 * A sample dashboard perspective
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "SalesReportsPerspective")
public class SalesReportsPerspective {

    @Inject
    DisplayerSettingsJSONMarshaller jsonHelper;

    @Perspective
    public PerspectiveDefinition buildPerspective() {

        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( PanelType.ROOT_TAB);
        perspective.setTransient(true);
        perspective.setName("Sales reports");

        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_ALLOPPS_LISTING)));
        perspective.getRoot().addPart(new PartDefinitionImpl(createPlaceRequest(OPPS_COUNTRY_SUMMARY)));
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
