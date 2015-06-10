package org.dashbuilder.client.workbench.panels.impl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.workbench.panels.WorkbenchPanelView;
import org.uberfire.client.workbench.panels.impl.AbstractMultiPartWorkbenchPanelPresenter;

/**
 * A panel with a title bar and drop-down list that allows selecting among the parts it contains, and drag-and-drop
 * for moving parts to and from other drag-and-drop enabled panels. Only one part at a time is visible, and it fills
 * the entire available space not used up by the title bar.
 */
@Dependent
public class TabListWorkbenchPanelPresenter extends AbstractMultiPartWorkbenchPanelPresenter<TabListWorkbenchPanelPresenter> {

    @Inject
    public TabListWorkbenchPanelPresenter(@Named("TabListWorkbenchPanelView") final WorkbenchPanelView<TabListWorkbenchPanelPresenter> view,
            final ActivityManager activityManager,
            final PerspectiveManager perspectiveManager) {
        super( view, activityManager, perspectiveManager );
    }

    @Override
    protected TabListWorkbenchPanelPresenter asPresenterType() {
        return this;
    }

}
