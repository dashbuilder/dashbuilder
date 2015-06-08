package org.dashbuilder.client.workbench.panels.impl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.workbench.panels.impl.AbstractWorkbenchPanelPresenter;
import org.uberfire.client.workbench.panels.impl.StaticWorkbenchPanelView;

/**
 * An undecorated panel that can contain one part at a time and does not support child panels. The part's view fills
 * the entire panel. Adding a new part replaces the existing part. Does not support drag-and-drop rearrangement of
 * parts.
 */
@Dependent
public class StaticWorkbenchPanelPresenterExt extends AbstractWorkbenchPanelPresenter<StaticWorkbenchPanelPresenterExt> {

    @Inject
    public StaticWorkbenchPanelPresenterExt( @Named("StaticWorkbenchPanelViewExt") final StaticWorkbenchPanelViewExt view,
            final PerspectiveManager perspectiveManager ) {
        super( view, perspectiveManager );
    }

    @Override
    protected StaticWorkbenchPanelPresenterExt asPresenterType() {
        return this;
    }

    /**
     * Returns null (static panels don't support child panels).
     */
    @Override
    public String getDefaultChildType() {
        return null;
    }
}
