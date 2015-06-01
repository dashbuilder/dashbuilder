package org.dashbuilder.client.perspective.editor;

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
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.views.bs2.maximize.MaximizeToggleButton;
import org.uberfire.client.workbench.events.PerspectiveChange;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.impl.AbstractMultiPartWorkbenchPanelView;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.ForcedPlaceRequest;
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
    private MenuWidgetFactory menuWidgetFactory;

    @Inject
    protected MaximizeToggleButton maximizeButton;

    @Inject
    protected UberTabPanelExt tabPanel;

    @Inject
    protected PlaceManager placeManager;

    @Inject
    protected PerspectiveManager perspectiveManager;

    @Inject
    protected PerspectiveEditorSettings perspectiveEditorSettings;

    protected MaximizeToggleButtonPresenter maximizeButtonPresenter;
    protected ButtonGroup changeTypeButtonGroup;
    protected ButtonGroup closeButtonGroup;
    protected FlowPanel headerMenu = new FlowPanel();
    protected FlowPanel contextMenu = new FlowPanel();

    @PostConstruct
    protected void init() {
        tabPanel.setView(this);
    }

    @Override
    protected MultiPartWidget setupWidget() {
        Layouts.setToFillParent(tabPanel);
        addOnFocusHandler(tabPanel);
        addSelectionHandler(tabPanel);

        return tabPanel;
    }

    @Override
    protected void populatePartViewContainer() {

        setupMaximizeButton();
        setupCloseButton();
        setupChangeTypeButton();
        setupContextMenu();

        Style headerMenuStyle = headerMenu.getElement().getStyle();
        headerMenuStyle.setMarginRight(10, Style.Unit.PX);
        headerMenuStyle.setMarginTop(5, Style.Unit.PX);
        headerMenuStyle.setZIndex(2); // otherwise, clicks don't make it through the tab area
        headerMenuStyle.setPosition(Style.Position.ABSOLUTE);
        headerMenuStyle.setRight(0, Style.Unit.PX);

        Style contextMenuStyle = contextMenu.getElement().getStyle();
        contextMenuStyle.setDisplay(Style.Display.INLINE);
        contextMenuStyle.setMarginRight(5, Style.Unit.PX);

        headerMenu.add(changeTypeButtonGroup);
        headerMenu.add(closeButtonGroup);

        ButtonGroup maximizeButtonGroup = new ButtonGroup();
        maximizeButtonGroup.add(maximizeButton);
        headerMenu.add(maximizeButtonGroup);

        headerMenu.add(contextMenu);
        headerMenu.add(changeTypeButtonGroup);
        headerMenu.add(closeButtonGroup);
        headerMenu.add(maximizeButtonGroup);
        getPartViewContainer().add( headerMenu );

        super.populatePartViewContainer();
    }

    protected void setupMaximizeButton() {
        maximizeButtonPresenter = new MaximizeToggleButtonPresenter(maximizeButton);
        maximizeButtonPresenter.setMaximizeCommand(new Command() {
            @Override
            public void execute() {
                maximize();
            }
        });
        maximizeButtonPresenter.setUnmaximizeCommand(new Command() {
            @Override
            public void execute() {
                unmaximize();
            }
        });
    }

    protected void setupChangeTypeButton() {
        Button changeTypeButton = new Button();
        changeTypeButton.setTitle("Show as list");
        changeTypeButton.setIcon(IconType.ASTERISK);
        changeTypeButton.setSize(ButtonSize.MINI);
        changeTypeButton.addClickHandler(new ClickHandler() {
            @Override public void onClick(ClickEvent clickEvent) {
                // TODO: move this logic to a more appropriate place
                getPresenter().getDefinition().setPanelType(MultiListWorkbenchPanelPresenterExt.class.getName());
                PerspectiveActivity currentPerspective = perspectiveManager.getCurrentPerspective();
                perspectiveManager.savePerspectiveState(new Command() {
                    public void execute() {
                    }
                });
                placeManager.goTo(new ForcedPlaceRequest(currentPerspective.getIdentifier()));
            }
        });
        changeTypeButtonGroup = new ButtonGroup();
        changeTypeButtonGroup.add(changeTypeButton);
    }

    protected void setupCloseButton() {
        Button closeButton = new Button();
        closeButton.setTitle("Close");
        closeButton.setIcon(IconType.REMOVE);
        closeButton.setSize(ButtonSize.MINI);
        closeButton.addClickHandler(new ClickHandler() {
            @Override public void onClick(ClickEvent clickEvent) {
                final WorkbenchPartPresenter.View partToDeselect = tabPanel.getSelectedPart();
                panelManager.closePart(partToDeselect.getPresenter().getDefinition());
            }
        });
        closeButtonGroup = new ButtonGroup();
        closeButtonGroup.add(closeButton);
    }

    protected void setupContextMenu() {
        contextMenu.clear();
        final WorkbenchPartPresenter.View part = tabPanel.getSelectedPart();
        if ( part != null && part.getPresenter().getMenus() != null && part.getPresenter().getMenus().getItems().size() > 0 ) {
            for ( final MenuItem menuItem : part.getPresenter().getMenus().getItems() ) {
                final Widget result = menuWidgetFactory.makeItem( menuItem, true );
                if ( result != null ) {
                    contextMenu.add(result);
                }
            }
        }
    }

    @Override
    public void maximize() {
        super.maximize();
        maximizeButton.setMaximized(true);
    }

    @Override
    public void unmaximize() {
        super.unmaximize();
        maximizeButton.setMaximized(false);
    }

    @Override
    public void setElementId( String elementId ) {
        super.setElementId(elementId);
        maximizeButton.ensureDebugId(elementId + "-maximizeButton");
    }

    protected void addSelectionHandler( HasSelectionHandlers<PartDefinition> widget ) {
        widget.addSelectionHandler(new SelectionHandler<PartDefinition>() {
            @Override
            public void onSelection(final SelectionEvent<PartDefinition> event) {
                panelManager.onPartLostFocus();
                panelManager.onPartFocus(event.getSelectedItem());
                updateHeaderStatus();
            }
        });
    }

    protected void onPerspectiveEditOn(@Observes PerspectiveEditOnEvent event) {
        updateHeaderStatus();
    }

    protected void onPerspectiveEditOff(@Observes PerspectiveEditOffEvent event) {
        updateHeaderStatus();
    }

    protected void updateHeaderStatus() {
        changeTypeButtonGroup.setVisible(perspectiveEditorSettings.isEditOn());
        closeButtonGroup.setVisible(perspectiveEditorSettings.isEditOn());
        setupContextMenu();
    }
}