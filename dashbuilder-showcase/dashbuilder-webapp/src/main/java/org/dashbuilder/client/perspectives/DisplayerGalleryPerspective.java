package org.dashbuilder.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.Position;
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

        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( PanelType.ROOT_TAB);
        perspective.setTransient(true);
        perspective.setName("Gallery");

        PanelDefinition west = new PanelDefinitionImpl(PanelType.STATIC);
        west.setWidth(200);
        west.setMinWidth(150);
        west.addPart(new PartDefinitionImpl(new DefaultPlaceRequest("org.dashbuilder.gallery.GalleryTreeScreen")));

        perspective.getRoot().insertChild(Position.WEST, west);
        return perspective;
    }
}
