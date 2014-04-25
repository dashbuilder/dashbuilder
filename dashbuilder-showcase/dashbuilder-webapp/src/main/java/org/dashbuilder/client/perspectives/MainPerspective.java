package org.dashbuilder.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

/**
 * A Perspective to show File Explorer
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "MainPerspective", isDefault = true)
public class MainPerspective {

    private PerspectiveDefinition perspective;

    @PostConstruct
    public void init() {
        buildPerspective();
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        return this.perspective;
    }

    public PerspectiveDefinition buildPerspective() {
        perspective = new PerspectiveDefinitionImpl( PanelType.ROOT_STATIC);
        perspective.setTransient(true);
        perspective.setName("MainPerspective");
        //perspective.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest("HomeScreen")));
        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "GalleryScreen" ) ) );
        //perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "StaticChartScreen" ) ) );
        //perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "KPIScreen" ).addParameter("kpi", "opps-country-summary") ) );
        return perspective;
    }
}
