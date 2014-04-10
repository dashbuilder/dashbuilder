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
        perspective = new PerspectiveDefinitionImpl( PanelType.ROOT_TAB);
        perspective.setTransient(true);
        perspective.setName("MainPerspective");

        //p.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "HelloWorldScreen" ) ) );
        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "KPIPresenter" ).addParameter("kpi", "sample0") ) );
        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "KPIPresenter" ).addParameter("kpi", "sample1") ) );
        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "KPIPresenter" ).addParameter("kpi", "test-group") ) );

        return perspective;
    }
}
