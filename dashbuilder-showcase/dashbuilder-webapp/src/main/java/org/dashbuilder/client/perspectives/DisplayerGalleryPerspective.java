package org.dashbuilder.client.perspectives;

import javax.enterprise.context.ApplicationScoped;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.panels.impl.MultiTabWorkbenchPanelPresenter;
import org.uberfire.client.workbench.panels.impl.StaticWorkbenchPanelPresenter;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

/**
 * The gallery perspective.
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "DisplayerGalleryPerspective", isDefault = true)
public class DisplayerGalleryPerspective {

    @Perspective
    public PerspectiveDefinition buildPerspective() {

        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( MultiTabWorkbenchPanelPresenter.class.getName());
        perspective.setName("Gallery");

        PanelDefinition west = new PanelDefinitionImpl( StaticWorkbenchPanelPresenter.class.getName() );
        west.setWidth(200);
        west.setMinWidth(150);
        west.addPart(new PartDefinitionImpl(new DefaultPlaceRequest("org.dashbuilder.gallery.GalleryTreeScreen")));

        perspective.getRoot().insertChild( CompassPosition.WEST, west);
        return perspective;
    }
}
