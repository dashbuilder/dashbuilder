package org.dashbuilder.client.workbench.panels.impl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.impl.AbstractWorkbenchPanelView;
import org.uberfire.client.workbench.panels.impl.StaticWorkbenchPanelPresenter;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.client.workbench.widgets.panel.StaticFocusedResizePanel;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.PartDefinition;

/**
 * The view component of {@link StaticWorkbenchPanelPresenter}.
 */
@Dependent
@Named("StaticWorkbenchPanelViewExt")
public class StaticWorkbenchPanelViewExt
        extends AbstractWorkbenchPanelView<StaticWorkbenchPanelPresenterExt> {

    @Inject
    PlaceManager placeManager;

    @Inject
    StaticPanelWidget staticPanel;

    @PostConstruct
    void postConstruct() {
        initWidget(staticPanel);

        staticPanel.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(final FocusEvent event) {
                panelManager.onPanelFocus(presenter.getDefinition());
            }
        });

        //When a tab is selected ensure content is resized and set focus
        staticPanel.addSelectionHandler(new SelectionHandler<PartDefinition>() {
            @Override
            public void onSelection(final SelectionEvent<PartDefinition> event) {
                panelManager.onPartLostFocus();
                panelManager.onPartFocus(event.getSelectedItem());
            }
        });

        Layouts.setToFillParent(staticPanel);

        final MaximizeToggleButtonPresenter maximizeButton = staticPanel.getMaximizeButton();
        maximizeButton.setVisible(true);
        maximizeButton.setMaximizeCommand(new Command() {
            @Override
            public void execute() {
                maximize();
            }
        });
        maximizeButton.setUnmaximizeCommand(new Command() {
            @Override
            public void execute() {
                unmaximize();
            }
        } );
    }

    @Override
    public void init( final StaticWorkbenchPanelPresenterExt presenter ) {
        staticPanel.setPresenter(presenter);
    }

    @Override
    public void maximize() {
        super.maximize();
        staticPanel.getMaximizeButton().setMaximized( true );
    }

    @Override
    public void unmaximize() {
        super.unmaximize();
        staticPanel.getMaximizeButton().setMaximized( false );
    }

    @Override
    public void setElementId( String elementId ) {
        super.setElementId( elementId );
        staticPanel.getMaximizeButton().getView().asWidget().ensureDebugId( elementId + "-maximizeButton" );
    }

    // override is for unit test: super.getWidget() returns a new mock every time
    @Override
    public Widget getWidget() {
        return staticPanel;
    }

    @Override
    public void addPart( final WorkbenchPartPresenter.View view ) {
        if ( staticPanel.getPartView() != null ) {
            placeManager.tryClosePlace( getCurrentPartDefinition().getPlace(), new Command() {
                @Override
                public void execute() {
                    staticPanel.setPart( view );
                    onResize();
                }
            } );
        } else {
            staticPanel.setPart( view );
            onResize();
        }
    }

    @Override
    public void changeTitle( final PartDefinition part,
            final String title,
            final IsWidget titleDecoration ) {
    }

    @Override
    public boolean selectPart( final PartDefinition part ) {
        PartDefinition currentPartDefinition = getCurrentPartDefinition();
        if ( currentPartDefinition != null && currentPartDefinition.equals( part ) ) {
            return true;
        }
        return false;
    }

    @Override
    public boolean removePart( final PartDefinition part ) {
        PartDefinition currentPartDefinition = getCurrentPartDefinition();
        if ( currentPartDefinition != null && currentPartDefinition.equals( part ) ) {
            staticPanel.clear();
            return true;
        }
        return false;
    }

    @Override
    public void setFocus( boolean hasFocus ) {
        staticPanel.setFocus(hasFocus);
    }

    @Override
    public void onResize() {
        presenter.onResize( getOffsetWidth(), getOffsetHeight() );
        super.onResize();
    }

    private PartDefinition getCurrentPartDefinition() {
        WorkbenchPartPresenter.View partView = staticPanel.getPartView();
        if ( partView == null ) {
            return null;
        }

        WorkbenchPartPresenter presenter = partView.getPresenter();
        if ( presenter == null ) {
            return null;
        }

        return presenter.getDefinition();
    }
}
