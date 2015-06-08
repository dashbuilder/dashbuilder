package org.dashbuilder.client.workbench.panels.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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

    public interface Presenter {
        void selectPart(WorkbenchPartPresenter.View partView);
        void closePart(WorkbenchPartPresenter.View partView);
        void changePanelType(String panelType);
        String getPanelType();
        Map<String,String> getAvailablePanelTypes();
    }

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

    protected Presenter presenter = null;
    protected MenuWidgetFactory menuWidgetFactory;
    protected boolean editEnabled = false;
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
                if (currentPartView != null && presenter != null) {
                    presenter.closePart(currentPartView);
                }
            }
        } );


        clear();
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
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

    public MaximizeToggleButtonPresenter getMaximizeButton() {
        return maximizeButtonPresenter;
    }

    protected void onPerspectiveEditOn(@Observes PerspectiveEditOnEvent event) {
        setEditEnabled(true);
        updateView();
    }

    protected void onPerspectiveEditOff(@Observes PerspectiveEditOffEvent event) {
        setEditEnabled(false);
        updateView();
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
    }

    public void updateView() {
        toolbarPanel.setVisible(true);
        changeTypeButtonContainer.setVisible(editEnabled);
        closeButtonContainer.setVisible(editEnabled);
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

        if (editEnabled && !presenter.getAvailablePanelTypes().isEmpty()) {
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
                                    if (presenter != null) {
                                        presenter.selectPart(partView);
                                    }
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
            Map<String,String> types = presenter.getAvailablePanelTypes();
            for (final String type : types.keySet()) {
                String descr = types.get(type);

                panel.add(new NavLink(descr) {{
                    addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(final ClickEvent event) {
                            if (presenter != null) {
                                presenter.changePanelType(type);
                            }
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
}