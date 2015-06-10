package org.dashbuilder.client.workbench.panels.impl;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.dashbuilder.client.perspective.editor.widgets.PanelToolbarWidget;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.impl.AbstractMultiPartWorkbenchPanelView;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.PartDefinition;

/**
 * A Workbench panel that can contain WorkbenchParts.
 */
@Dependent
@Named("TabListWorkbenchPanelView")
public class TabListWorkbenchPanelView
        extends AbstractMultiPartWorkbenchPanelView<TabListWorkbenchPanelPresenter> {

    @Inject
    protected TabPanelWidget tabPanel;

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Override
    protected MultiPartWidget setupWidget() {
        addOnFocusHandler(tabPanel);
        addSelectionHandler(tabPanel);
        setupPanelToolbar();

        tabPanel.setEditEnabled(perspectiveEditor.isEditOn());
        return tabPanel;
    }

    protected void setupPanelToolbar() {
        PanelToolbarWidget panelToolbarWidget = tabPanel.getPanelToolbarWidget();
        Map< String, String > panelTypes = new HashMap<String, String>();
        panelTypes.put(ListBarWorkbenchPanelPresenter.class.getName(), "List");
        panelTypes.put(ListPopoverWorkbenchPanelPresenter.class.getName(), "List (Auto-hide)");
        panelToolbarWidget.setAvailablePanelTypes(panelTypes);

        panelToolbarWidget.addPartSelectHandler(new PanelToolbarWidget.PartSelectHandler() {
            @Override public void onPartSelect(PanelToolbarWidget.PartSelectEvent event) {
                selectPart(event.getPartView().getPresenter().getDefinition());
            }
        });
        panelToolbarWidget.addPartCloseHandler(new PanelToolbarWidget.PartCloseHandler() {
            @Override public void onPartClose(PanelToolbarWidget.PartCloseEvent event) {
                perspectiveEditor.closePart(event.getPartView().getPresenter().getDefinition());
                perspectiveEditor.saveCurrentPerspective();
            }
        });
        panelToolbarWidget.addPanelTypeChangeHandler(new PanelToolbarWidget.PanelTypeChangeHandler() {
            @Override public void onPanelTypeChange(PanelToolbarWidget.PanelTypeChangeEvent event) {
                perspectiveEditor.changePanelTypeAndSave(presenter, event.getNewPanelType());
            }
        });
        panelToolbarWidget.addMaximizeClickHandler(new PanelToolbarWidget.MaximizeClickHandler() {
            @Override public void onMaximize() {
                maximize();
            }
            @Override public void onMinimize() {
                unmaximize();
            }
        });
    }

    protected void onPerspectiveEditOn(@Observes PerspectiveEditOnEvent event) {
        tabPanel.setEditEnabled(true);
    }

    protected void onPerspectiveEditOff(@Observes PerspectiveEditOffEvent event) {
        tabPanel.setEditEnabled(false);
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