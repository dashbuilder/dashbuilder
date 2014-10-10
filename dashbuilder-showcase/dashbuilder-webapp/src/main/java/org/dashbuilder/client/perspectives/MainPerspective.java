package org.dashbuilder.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.FlowPanel;
import org.uberfire.client.annotations.WorkbenchPanel;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.util.Layouts;

/**
 * A Perspective to show File Explorer
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "MainPerspective", isDefault = true)
public class MainPerspective extends FlowPanel {

    @Inject
    @WorkbenchPanel(parts = "GalleryScreen")
    FlowPanel gallery;

    @PostConstruct
    void doLayout() {
        Layouts.setToFillParent( gallery );
        add( gallery );
    }
}
