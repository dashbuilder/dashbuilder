package org.dashbuilder.client.workbench.panels.impl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.workbench.panels.WorkbenchPanelView;
import org.uberfire.client.workbench.panels.impl.AbstractMultiPartWorkbenchPanelPresenter;

@Dependent
public class ListPopoverWorkbenchPanelPresenter extends AbstractMultiPartWorkbenchPanelPresenter<ListPopoverWorkbenchPanelPresenter> {

    @Inject
    public ListPopoverWorkbenchPanelPresenter(@Named("ListPopoverWorkbenchPanelView") final WorkbenchPanelView<ListPopoverWorkbenchPanelPresenter> view,
            final ActivityManager activityManager,
            final PerspectiveManager perspectiveManager) {
        super( view, activityManager, perspectiveManager );
    }

    @Override
    protected ListPopoverWorkbenchPanelPresenter asPresenterType() {
        return this;
    }
}

