package org.dashbuilder.client.workbench.panels.impl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.impl.AbstractMultiPartWorkbenchPanelView;
import org.uberfire.mvp.Command;

/**
 * A Workbench panel that can contain WorkbenchParts.
 */
@Dependent
@Named("MultiListWorkbenchPanelViewExt")
public class MultiListWorkbenchPanelViewExt
        extends AbstractMultiPartWorkbenchPanelView<MultiListWorkbenchPanelPresenterExt> {

    @Inject
    protected ListBarWidgetExt listBar;

    @Override
    protected MultiPartWidget setupWidget() {
        if ( contextWidget != null ) {
            listBar.setExpanderCommand( new Command() {
                @Override
                public void execute() {
                    contextWidget.toogleDisplay();
                }
            } );
        }
        addOnFocusHandler( listBar );
        addSelectionHandler( listBar );

        final MaximizeToggleButtonPresenter maximizeButton = listBar.getMaximizeButton();
        maximizeButton.setVisible(true);
        maximizeButton.setMaximizeCommand( new Command() {
            @Override
            public void execute() {
                maximize();
            }
        } );
        maximizeButton.setUnmaximizeCommand( new Command() {
            @Override
            public void execute() {
                unmaximize();
            }
        } );

        return listBar;
    }

    @Override
    public void maximize() {
        super.maximize();
        listBar.getMaximizeButton().setMaximized( true );
    }

    @Override
    public void unmaximize() {
        super.unmaximize();
        listBar.getMaximizeButton().setMaximized( false );
    }

    @Override
    public void setElementId( String elementId ) {
        super.setElementId( elementId );
        listBar.getMaximizeButton().getView().asWidget().ensureDebugId( elementId + "-maximizeButton" );
    }
}