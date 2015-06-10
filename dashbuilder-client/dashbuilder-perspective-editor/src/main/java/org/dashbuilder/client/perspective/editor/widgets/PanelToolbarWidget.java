package org.dashbuilder.client.perspective.editor.widgets;

import java.util.Collection;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.uberfire.client.views.bs2.maximize.MaximizeToggleButton;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.client.workbench.widgets.listbar.ResizeFlowPanel;
import org.uberfire.client.workbench.widgets.listbar.ResizeFocusPanel;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuItem;

/**
 * Panel toolbar
 */
@Dependent
public class PanelToolbarWidget extends ResizeComposite {

    interface Binder extends UiBinder<ResizeFocusPanel, PanelToolbarWidget> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    protected FocusPanel toolbarPanel;

    @UiField
    protected FlowPanel contextMenu;

    @UiField
    protected ButtonGroup changeTypeButtonContainer;

    @UiField
    protected DropdownButton changeTypeButton;

    @UiField
    protected ButtonGroup closeButtonContainer;

    @UiField
    protected Button closeButton;

    @UiField
    protected ButtonGroup maximizeButtonContainer;

    @UiField
    protected MaximizeToggleButton maximizeButton;

    @UiField
    protected ButtonGroup dropdownCaretContainer;

    @UiField
    protected DropdownButton dropdownCaret;

    /** Wraps maximizeButton, which is the view. */
    protected MaximizeToggleButtonPresenter maximizeButtonPresenter;

    protected MenuWidgetFactory menuWidgetFactory;
    protected boolean editEnabled = false;
    protected boolean maximizeEnabled = true;
    protected  Map<String,String> availablePanelTypes = null;
    protected WorkbenchPartPresenter.View currentPartView = null;
    protected Collection<WorkbenchPartPresenter.View> availablePartViews = null;
    protected PartChooserList partChooserList = null;
    protected PanelTypeChooserList panelTypeChooserList = null;

    @Inject
    public PanelToolbarWidget(MenuWidgetFactory menuWidgetFactory) {
        initWidget(uiBinder.createAndBindUi(this));

        this.menuWidgetFactory = menuWidgetFactory;
        maximizeButtonPresenter = new MaximizeToggleButtonPresenter(maximizeButton);

        closeButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                if (currentPartView != null) {
                    PanelToolbarWidget.this.fireEvent(new PartCloseEvent(currentPartView));
                }
            }
        } );
        maximizeButtonPresenter.setMaximizeCommand(new Command() {
            @Override
            public void execute() {
                PanelToolbarWidget.this.fireEvent(new MaximizeClickEvent(maximizeButton.isMaximized()));
            }
        });
        maximizeButtonPresenter.setUnmaximizeCommand(new Command() {
            @Override
            public void execute() {
                PanelToolbarWidget.this.fireEvent(new MaximizeClickEvent(maximizeButton.isMaximized()));
            }
        });

        clear();
    }

    public void setCurrentPart(WorkbenchPartPresenter.View partView) {
        this.currentPartView = partView;
    }

    public void setAvailableParts(Collection<WorkbenchPartPresenter.View> partViews) {
        this.availablePartViews = partViews;
    }

    public void setEditEnabled(boolean enabled) {
        editEnabled = enabled;
    }

    public void setMaximizeEnabled(boolean maximizeEnabled) {
        this.maximizeEnabled = maximizeEnabled;
    }

    public MaximizeToggleButtonPresenter getMaximizeButton() {
        return maximizeButtonPresenter;
    }

    public void setAvailablePanelTypes(Map<String, String> availablePanelTypes) {
        this.availablePanelTypes = availablePanelTypes;
    }

    public void clear() {
        toolbarPanel.setVisible(false);

        currentPartView = null;
        availablePartViews = null;

        contextMenu.setVisible(false);
        contextMenu.clear();

        dropdownCaretContainer.setVisible(false);
        if (partChooserList != null) {
            partChooserList.clear();
        }

        changeTypeButtonContainer.setVisible(false);
        if (panelTypeChooserList != null) {
            panelTypeChooserList.clear();
        }

        maximizeButtonContainer.setVisible(false);
    }

    public void updateView() {
        this.setVisible(currentPartView != null);
        changeTypeButtonContainer.setVisible(currentPartView != null && editEnabled);
        closeButtonContainer.setVisible(currentPartView != null && editEnabled);
        maximizeButtonContainer.setVisible(maximizeEnabled);
        updateContextMenu();
        updateDropdown();
        updateTypeSelector();
    }

    protected void updateContextMenu() {
        contextMenu.clear();
        contextMenu.setVisible(false);

        if (currentPartView != null
                && currentPartView.getPresenter().getMenus() != null
                && currentPartView.getPresenter().getMenus().getItems().size() > 0) {

            contextMenu.setVisible(true);

            for (final MenuItem menuItem : currentPartView.getPresenter().getMenus().getItems()) {
                final Widget result = menuWidgetFactory.makeItem(menuItem, true);
                if (result != null) {
                    contextMenu.add(result);
                }
            }
        }
    }

    protected void updateDropdown() {
        dropdownCaretContainer.setVisible(false);
        dropdownCaret.setRightDropdown(true);
        dropdownCaret.clear();

        if (availablePartViews != null && availablePartViews.size() > 1) {
            dropdownCaretContainer.setVisible(true);
            partChooserList = new PartChooserList();
            dropdownCaret.add(partChooserList);
        }
    }

    protected void updateTypeSelector() {
        changeTypeButtonContainer.setVisible(false);
        changeTypeButton.setRightDropdown(true);
        changeTypeButton.clear();

        if (editEnabled && availablePanelTypes != null) {
            changeTypeButtonContainer.setVisible(true);
            panelTypeChooserList = new PanelTypeChooserList();
            changeTypeButton.add(panelTypeChooserList);
        }
    }

    @Override
    public void onResize() {
        if (!isAttached()) {
            return;
        }

        super.onResize();

        if (partChooserList != null) {
            partChooserList.onResize();
        }
        if (panelTypeChooserList != null) {
            panelTypeChooserList.onResize();
        }
    }

    /**
     * This is the list that appears when you click the down-arrow button in the header (dropdownCaret). It lists all
     * the available parts. Clicking on a list item selects its associated part, making it visible, and hiding all other
     * parts.
     */
    protected class PartChooserList extends ResizeComposite {

        final ResizeFlowPanel panel = new ResizeFlowPanel();

        PartChooserList() {
            initWidget(panel);
            if (currentPartView != null ) {
                for (final WorkbenchPartPresenter.View partView : availablePartViews) {
                    if (partView != currentPartView) {
                        final String title = partView.getPresenter().getTitle();
                        panel.add(new NavLink(title) {{
                            addClickHandler(new ClickHandler() {
                                @Override
                                public void onClick(final ClickEvent event) {
                                    PanelToolbarWidget.this.fireEvent(new PartSelectEvent(partView));
                                }
                            });
                        }});
                    }
                }
            }
            onResize();
        }

        public void clear() {
            panel.clear();
        }
    }

    /**
     * Panel type selection widget
     */
    protected class PanelTypeChooserList extends ResizeComposite {

        final ResizeFlowPanel panel = new ResizeFlowPanel();

        PanelTypeChooserList() {
            initWidget(panel);
            for (final String type : availablePanelTypes.keySet()) {
                String descr = availablePanelTypes.get(type);

                panel.add(new NavLink(descr) {{
                    addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(final ClickEvent event) {
                            PanelToolbarWidget.this.fireEvent(new PanelTypeChangeEvent(currentPartView, type));
                        }
                    });
                }});
            }
            onResize();
        }

        public void clear() {
            panel.clear();
        }
    }

    // Event handlers

    public interface PartSelectHandler extends EventHandler {
        void onPartSelect(PartSelectEvent event);
    }

    public interface PartCloseHandler extends EventHandler {
        void onPartClose(PartCloseEvent event);
    }

    public interface PanelTypeChangeHandler extends EventHandler {
        void onPanelTypeChange(PanelTypeChangeEvent event);
    }

    public interface MaximizeClickHandler extends EventHandler {
        void onMaximize();
        void onMinimize();
    }

    public static class PartSelectEvent extends GwtEvent<PartSelectHandler> {

        public static GwtEvent.Type<PartSelectHandler> TYPE = new GwtEvent.Type<PartSelectHandler>();

        private WorkbenchPartPresenter.View partView;

        public PartSelectEvent(WorkbenchPartPresenter.View partView) {
            this.partView = partView;
        }

        public WorkbenchPartPresenter.View getPartView() {
            return partView;
        }

        @Override
        public Type<PartSelectHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(PartSelectHandler handler) {
            handler.onPartSelect(this);
        }
    }

    public static class PartCloseEvent extends GwtEvent<PartCloseHandler> {

        public static GwtEvent.Type<PartCloseHandler> TYPE = new GwtEvent.Type<PartCloseHandler>();

        private WorkbenchPartPresenter.View partView;

        public PartCloseEvent(WorkbenchPartPresenter.View partView) {
            this.partView = partView;
        }

        public WorkbenchPartPresenter.View getPartView() {
            return partView;
        }

        @Override
        public Type<PartCloseHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(PartCloseHandler handler) {
            handler.onPartClose(this);
        }
    }

    public static class PanelTypeChangeEvent extends GwtEvent<PanelTypeChangeHandler> {

        public static GwtEvent.Type<PanelTypeChangeHandler> TYPE = new GwtEvent.Type<PanelTypeChangeHandler>();

        private WorkbenchPartPresenter.View partView;
        private String newPanelType;

        public PanelTypeChangeEvent(WorkbenchPartPresenter.View partView, String newPanelType) {
            this.partView = partView;
            this.newPanelType = newPanelType;
        }

        public WorkbenchPartPresenter.View getPartView() {
            return partView;
        }

        public String getNewPanelType() {
            return newPanelType;
        }

        @Override
        public Type<PanelTypeChangeHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(PanelTypeChangeHandler handler) {
            handler.onPanelTypeChange(this);
        }
    }
    public static class MaximizeClickEvent extends GwtEvent<MaximizeClickHandler> {

        public static GwtEvent.Type<MaximizeClickHandler> TYPE = new GwtEvent.Type<MaximizeClickHandler>();

        private boolean maximized;

        public MaximizeClickEvent(boolean maximized) {
            this.maximized = maximized;
        }

        public boolean isMaximized() {
            return maximized;
        }

        @Override
        public Type<MaximizeClickHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(MaximizeClickHandler handler) {
            if (maximized) {
                handler.onMaximize();
            } else {
                handler.onMinimize();
            }
        }
    }

    public HandlerRegistration addPartSelectHandler(PartSelectHandler handler) {
        return this.addHandler(handler, PartSelectEvent.TYPE);
    }

    public HandlerRegistration addPartCloseHandler(PartCloseHandler handler) {
        return this.addHandler(handler, PartCloseEvent.TYPE);
    }

    public HandlerRegistration addPanelTypeChangeHandler(PanelTypeChangeHandler handler) {
        return this.addHandler(handler, PanelTypeChangeEvent.TYPE);
    }

    public HandlerRegistration addMaximizeClickHandler(MaximizeClickHandler handler) {
        return this.addHandler(handler, MaximizeClickEvent.TYPE);
    }
}