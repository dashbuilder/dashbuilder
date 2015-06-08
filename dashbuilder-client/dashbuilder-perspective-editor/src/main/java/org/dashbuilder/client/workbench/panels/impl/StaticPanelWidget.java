package org.dashbuilder.client.workbench.panels.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.DropdownTab;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPane;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.uberfire.client.resources.WorkbenchResources;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.views.bs2.maximize.MaximizeToggleButton;
import org.uberfire.client.workbench.PanelManager;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.WorkbenchPanelPresenter;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.client.workbench.widgets.dnd.WorkbenchDragAndDropManager;
import org.uberfire.client.workbench.widgets.listbar.ResizeFocusPanel;
import org.uberfire.client.workbench.widgets.panel.StaticFocusedResizePanel;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.PartDefinition;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Dependent
public class StaticPanelWidget extends ResizeComposite
        implements HasSelectionHandlers<PartDefinition>, HasFocusHandlers, PanelToolbarWidget.Presenter {

    interface StaticPanelWidgetBinder extends UiBinder<ResizeFocusPanel, StaticPanelWidget> {}
    static StaticPanelWidgetBinder uiBinder = GWT.create(StaticPanelWidgetBinder.class);

    @Inject
    protected PanelManager panelManager;

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Inject
    protected WorkbenchDragAndDropManager dndManager;

    @Inject
    protected PanelToolbarWidget panelToolbarWidget;

    @UiField
    protected Panel panelToolbar;

    @UiField
    protected StaticFocusedResizePanel staticPanel;

    protected WorkbenchPanelPresenter presenter;

    @PostConstruct
    void postConstruct() {
        initWidget(uiBinder.createAndBindUi(this));
        setup();
        Layouts.setToFillParent(this);
    }

    protected void setup() {
        // Init the panel toolbar
        panelToolbarWidget.setPresenter(this);
        panelToolbarWidget.setEditEnabled(perspectiveEditor.isEditOn());
        panelToolbarWidget.setAvailableParts(null);
        panelToolbar.add(panelToolbarWidget);
    }

    public void clear() {
        staticPanel.clear();
    }

    public void setPresenter( final WorkbenchPanelPresenter presenter ) {
        this.presenter = presenter;
    }

    public void setPart( final WorkbenchPartPresenter.View part ) {
        staticPanel.setPart(part);
        panelToolbarWidget.setCurrentPart(part);
        panelToolbarWidget.updateView();
    }

    public void setFocus( boolean hasFocus ) {
        staticPanel.setFocus(hasFocus);
    }

    @Override
    public HandlerRegistration addSelectionHandler( final SelectionHandler<PartDefinition> handler ) {
        return staticPanel.addHandler(handler, SelectionEvent.getType());
    }

    @Override
    public HandlerRegistration addFocusHandler( FocusHandler handler ) {
        return staticPanel.addFocusHandler( handler );
    }

    public WorkbenchPartPresenter.View getPartView() {
        return staticPanel.getPartView();
    }

    /**
     * Returns the toggle button, which is initially hidden, that can be used to trigger maximizing and unmaximizing
     * of the panel containing this list bar. Make the button visible by calling {@link Widget#setVisible(boolean)}
     * and set its maximize and unmaximize actions with {@link MaximizeToggleButton#setMaximizeCommand(Command)} and
     * {@link MaximizeToggleButton#setUnmaximizeCommand(Command)}.
     */
    public MaximizeToggleButtonPresenter getMaximizeButton() {
        return panelToolbarWidget.getMaximizeButton();
    }

    // Panel Toolbar stuff

    @Override
    public void selectPart(WorkbenchPartPresenter.View partView) {

    }

    @Override
    public void closePart(WorkbenchPartPresenter.View partView) {
        perspectiveEditor.closePart(partView.getPresenter().getDefinition());
        perspectiveEditor.saveCurrentPerspective();
    }

    @Override
    public void changePanelType(String panelType) {
        perspectiveEditor.changePanelType(presenter, panelType);
        perspectiveEditor.saveCurrentPerspective();
    }

    @Override
    public String getPanelType() {
        return presenter.getDefinition().getPanelType();
    }

    @Override
    public Map<String,String> getAvailablePanelTypes() {
        Map<String,String> result = new HashMap<String, String>();
        result.put(MultiListWorkbenchPanelPresenterExt.class.getName(), "List");
        result.put(MultiTabWorkbenchPanelPresenterExt.class.getName(), "Tab");
        return result;
    }
}
