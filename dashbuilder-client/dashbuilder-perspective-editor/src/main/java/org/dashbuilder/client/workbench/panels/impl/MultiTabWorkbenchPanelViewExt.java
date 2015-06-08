package org.dashbuilder.client.workbench.panels.impl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.views.bs2.maximize.MaximizeToggleButton;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.impl.AbstractMultiPartWorkbenchPanelView;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.PartDefinition;
import org.uberfire.workbench.model.menu.MenuItem;

/**
 * A Workbench panel that can contain WorkbenchParts.
 */
@Dependent
@Named("MultiTabWorkbenchPanelViewExt")
public class MultiTabWorkbenchPanelViewExt
        extends AbstractMultiPartWorkbenchPanelView<MultiTabWorkbenchPanelPresenterExt> {

    @Inject
    protected TabPanelWidget tabPanel;

    @Override
    protected MultiPartWidget setupWidget() {
        addOnFocusHandler(tabPanel);
        addSelectionHandler(tabPanel);

        final MaximizeToggleButtonPresenter maximizeButton = tabPanel.getMaximizeButton();
        maximizeButton.setVisible( true );
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

        return tabPanel;
    }

    @Override
    public void maximize() {
        super.maximize();
        tabPanel.getMaximizeButton().setMaximized( true );
    }

    @Override
    public void unmaximize() {
        super.unmaximize();
        tabPanel.getMaximizeButton().setMaximized( false );
    }

    @Override
    public void setElementId( String elementId ) {
        super.setElementId( elementId );
        tabPanel.getMaximizeButton().getView().asWidget().ensureDebugId( elementId + "-maximizeButton" );
    }


    protected void addSelectionHandler( HasSelectionHandlers<PartDefinition> widget ) {
        widget.addSelectionHandler(new SelectionHandler<PartDefinition>() {
            @Override
            public void onSelection(final SelectionEvent<PartDefinition> event) {
                panelManager.onPartLostFocus();
                panelManager.onPartFocus(event.getSelectedItem());
            }
        });
    }
}