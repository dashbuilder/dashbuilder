package org.dashbuilder.client.workbench.panels.impl;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.dashbuilder.client.perspective.editor.widgets.PanelToolbarWidget;
import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.impl.AbstractMultiPartWorkbenchPanelView;
import org.uberfire.mvp.Command;

@Dependent
@Named("ListPopoverWorkbenchPanelView")
public class ListPopoverWorkbenchPanelView
        extends AbstractMultiPartWorkbenchPanelView<ListPopoverWorkbenchPanelPresenter> {

    @Inject
    protected ListPanelWidget listBar;

    @Inject
    protected PerspectiveEditor perspectiveEditor;

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
        addOnFocusHandler(listBar);
        addSelectionHandler(listBar);
        setupPanelToolbar();

        listBar.setEditEnabled(perspectiveEditor.isEditOn());
        listBar.setPopoverEnabled(true);
        return listBar;
    }

    protected void setupPanelToolbar() {
        PanelToolbarWidget panelToolbarWidget = listBar.getPanelToolbarWidget();
        Map< String, String > panelTypes = new HashMap<String, String>();
        panelTypes.put(TabListWorkbenchPanelPresenter.class.getName(), "Tabs");
        panelTypes.put(ListBarWorkbenchPanelPresenter.class.getName(), "List");
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
        listBar.setEditEnabled(true);
    }

    protected void onPerspectiveEditOff(@Observes PerspectiveEditOffEvent event) {
        listBar.setEditEnabled(false);
    }
}