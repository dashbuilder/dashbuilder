package org.dashbuilder.client.workbench.panels.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.resources.WorkbenchResources;
import org.dashbuilder.client.perspective.editor.widgets.PanelToolbarWidget;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.views.bs2.maximize.MaximizeToggleButton;
import org.uberfire.client.workbench.panels.MaximizeToggleButtonPresenter;
import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.WorkbenchPanelPresenter;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.client.workbench.widgets.dnd.DragArea;
import org.uberfire.client.workbench.widgets.dnd.WorkbenchDragAndDropManager;
import org.uberfire.client.workbench.widgets.listbar.ListbarPreferences;
import org.uberfire.client.workbench.widgets.listbar.ResizeFocusPanel;
import org.uberfire.commons.data.Pair;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.PartDefinition;

import static com.google.gwt.dom.client.Style.Display.*;

/**
 * Implementation of ListBarWidget based on GWTBootstrap 2 components.
 */
@Dependent
public class ListPanelWidget extends ResizeComposite implements MultiPartWidget {

    /**
     * When a part is added to the list bar, a special title widget is created for it. This title widget is draggable.
     * To promote testability, the draggable title widget is given a predictable debug ID of the form
     * {@code DEBUG_ID_PREFIX + DEBUG_TITLE_PREFIX + partName}.
     * <p>
     * Note that debug IDs are only assigned when the app inherits the GWT Debug module. See
     * {@link Widget#ensureDebugId(com.google.gwt.dom.client.Element, String)} for details.
     */
    public static final String DEBUG_TITLE_PREFIX = "ListBar-title-";

    interface ListBarWidgetBinder extends UiBinder<ResizeFocusPanel, ListPanelWidget> {}
    static ListBarWidgetBinder uiBinder = GWT.create(ListBarWidgetBinder.class);

    /**
     * Preferences bean that applications can optionally provide. If this injection is unsatisfied, default settings are used.
     */
    @Inject
    protected Instance<ListbarPreferences> optionalListBarPrefs;

    @Inject
    protected PanelToolbarWidget panelToolbarWidget;

    @UiField
    protected Panel toolbarContainer;

    @UiField
    protected FocusPanel container;

    @UiField
    protected SimplePanel title;

    @UiField
    protected Button contextDisplay;

    @UiField
    protected FlowPanel header;

    @UiField
    protected FlowPanel content;

    protected WorkbenchPanelPresenter presenter;
    protected WorkbenchDragAndDropManager dndManager;
    protected final Map<PartDefinition, FlowPanel> partContentView = new HashMap<PartDefinition, FlowPanel>();
    protected final Map<PartDefinition, Widget> partTitle = new HashMap<PartDefinition, Widget>();
    protected LinkedHashSet<PartDefinition> parts = new LinkedHashSet<PartDefinition>();
    protected Pair<PartDefinition, FlowPanel> currentPart;

    protected boolean editEnabled = false;
    protected boolean popoverEnabled = false;

    @PostConstruct
    void postConstruct() {
        initWidget( uiBinder.createAndBindUi( this ) );
        setup();
        Layouts.setToFillParent(this);
        scheduleResize();
    }

    public void setup() {

        container.addFocusHandler( new FocusHandler() {
            @Override
            public void onFocus( FocusEvent event ) {
                if ( currentPart != null && currentPart.getK1() != null ) {
                    selectPart( currentPart.getK1() );
                }
            }
        } );

        // Show when moving the mouse over
        container.addDomHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                showHeader();
            }
        }, MouseOverEvent.getType());

        container.addDomHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent mouseOverEvent) {
                hideHeader();
            }
        }, MouseOutEvent.getType());

        if ( isPropertyListbarContextDisable() ) {
            contextDisplay.removeFromParent();
        }

        content.getElement().getStyle().setPosition(Style.Position.RELATIVE );
        content.getElement().getStyle().setTop(0.0, Style.Unit.PX );
        content.getElement().getStyle().setLeft(0.0, Style.Unit.PX );
        content.getElement().getStyle().setWidth( 100.0, Style.Unit.PCT );
        // height is calculated and set in onResize()

        // Init the panel toolbar
        updatePanelToolbar();
        updateHeaderStyle();
        toolbarContainer.add(panelToolbarWidget);
    }

    public void showHeader() {
        if (popoverEnabled) {
            header.setVisible(true);
        }
    }

    public void hideHeader() {
        if (popoverEnabled) {
            header.setVisible(false);
        }
    }

    public PanelToolbarWidget getPanelToolbarWidget() {
        return panelToolbarWidget;
    }

    boolean isPropertyListbarContextDisable() {
        if ( optionalListBarPrefs.isUnsatisfied() ) {
            return true;
        }

        // as of Errai 3.0.4.Final, Instance.isUnsatisfied() always returns false. The try-catch is a necessary safety net.
        try {
            return optionalListBarPrefs.get().isContextEnabled();
        } catch ( IOCResolutionException e ) {
            return true;
        }
    }

    public void setExpanderCommand( final Command command ) {
        if ( !isPropertyListbarContextDisable() ) {
            contextDisplay.addClickHandler( new ClickHandler() {
                @Override
                public void onClick( ClickEvent event ) {
                    command.execute();
                }
            } );
        }
    }

    public boolean isEditEnabled() {
        return editEnabled;
    }

    public void setEditEnabled(boolean editEnabled) {
        this.editEnabled = editEnabled;
        panelToolbarWidget.setEditEnabled(editEnabled);
        panelToolbarWidget.updateView();
        updateHeaderStyle();
    }

    public boolean isPopoverEnabled() {
        return popoverEnabled;
    }

    public void setPopoverEnabled(boolean popoverEnabled) {
        this.popoverEnabled = popoverEnabled;
        updateHeaderStyle();
    }

    protected void updateHeaderStyle() {
        header.getElement().removeClassName(WorkbenchResources.INSTANCE.CSS().listBar());
        header.getElement().removeClassName(WorkbenchResources.INSTANCE.CSS().listPopover());
        header.getElement().removeClassName(WorkbenchResources.INSTANCE.CSS().listPopoverEdit());
        header.setVisible(!popoverEnabled);
        title.setVisible(true);

        if (!isPopoverEnabled()) {
            header.getElement().addClassName(WorkbenchResources.INSTANCE.CSS().listBar());
        }
        else {
            if (isEditEnabled()) {
                header.getElement().addClassName(WorkbenchResources.INSTANCE.CSS().listPopoverEdit());
            }
            else {
                header.getElement().addClassName(WorkbenchResources.INSTANCE.CSS().listPopover());
                title.setVisible(false);
            }
        }
    }

    @Override
    public void setPresenter( final WorkbenchPanelPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setDndManager( final WorkbenchDragAndDropManager dndManager ) {
        this.dndManager = dndManager;
    }

    @Override
    public void clear() {
        title.clear();
        content.clear();
        panelToolbarWidget.clear();

        parts.clear();
        partContentView.clear();
        partTitle.clear();
        currentPart = null;
    }

    @Override
    public void addPart( final WorkbenchPartPresenter.View view ) {
        final PartDefinition partDefinition = view.getPresenter().getDefinition();
        if ( parts.contains( partDefinition ) ) {
            selectPart( partDefinition );
            return;
        }

        parts.add( partDefinition );

        final FlowPanel panel = new FlowPanel();
        Layouts.setToFillParent( panel );
        panel.add( view );
        content.add( panel );

        // IMPORTANT! if you change what goes in this map, update the remove(PartDefinition) method
        partContentView.put( partDefinition, panel );

        final Widget title = buildTitle( view.getPresenter().getTitle(), view.getPresenter().getTitleDecoration() );
        partTitle.put( partDefinition, title );
        title.ensureDebugId( DEBUG_TITLE_PREFIX + view.getPresenter().getTitle() );

        if (isEditEnabled()) {
            dndManager.makeDraggable( view, title );
        }

        scheduleResize();
    }

    private void updateBreadcrumb( final PartDefinition partDefinition ) {
        this.title.clear();

        final Widget title = partTitle.get( partDefinition );
        this.title.add(title);
    }

    private Widget buildTitle( final String title, final IsWidget titleDecoration ) {
        final SpanElement spanElement = Document.get().createSpanElement();
        spanElement.getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        spanElement.getStyle().setOverflow( Style.Overflow.HIDDEN );
        spanElement.getStyle().setTextOverflow(Style.TextOverflow.ELLIPSIS );
        spanElement.getStyle().setDisplay( BLOCK );
        final String titleWidget = (titleDecoration instanceof Image) ? titleDecoration.toString() : "";
        spanElement.setInnerHTML(titleWidget + " " + title.replaceAll( " ", "\u00a0" ) );

        return new DragArea() {{
            add( spanElement );
        }};
    }

    @Override
    public void changeTitle( final PartDefinition part,
            final String title,
            final IsWidget titleDecoration ) {
        final Widget _title = buildTitle( title, titleDecoration );
        partTitle.put( part, _title );
        if (isEditEnabled()) {
            dndManager.makeDraggable(partContentView.get(part), _title);
        }
        if ( currentPart != null && currentPart.getK1().equals( part ) ) {
            updateBreadcrumb( part );
        }
    }

    @Override
    public boolean selectPart( final PartDefinition part ) {
        if ( !parts.contains( part ) ) {
            //not necessary to check if current is part
            return false;
        }

        if ( currentPart != null ) {
            if ( currentPart.getK1().equals( part ) ) {
                return true;
            }
            parts.add( currentPart.getK1() );
            currentPart.getK2().getElement().getStyle().setDisplay( NONE );
        }

        currentPart = Pair.newPair( part, partContentView.get( part ) );
        currentPart.getK2().getElement().getStyle().setDisplay(BLOCK );
        updateBreadcrumb( part );
        parts.remove( currentPart.getK1() );

        container.setVisible(true);

        updatePanelToolbar();

        scheduleResize();

        SelectionEvent.fire(ListPanelWidget.this, part);

        return true;
    }

    @Override
    public boolean remove( final PartDefinition part ) {
        if ( currentPart.getK1().equals( part ) ) {
            if ( parts.size() > 0 ) {
                presenter.selectPart( parts.iterator().next() );
            } else {
                clear();
                container.setVisible(false);
            }
        }

        boolean removed = parts.remove( part );
        FlowPanel view = partContentView.remove( part );
        if ( view != null ) {
            // FIXME null check should not be necessary, but sometimes the entry in partContentView is missing!
            content.remove( view );
        }
        partTitle.remove( part );

        updatePanelToolbar();

        scheduleResize();

        return removed;
    }

    @Override
    public void setFocus( final boolean hasFocus ) {
    }

    @Override
    public void addOnFocusHandler( final Command command ) {
    }

    @Override
    public int getPartsSize() {
        if ( currentPart == null ) {
            return 0;
        }
        return parts.size() + 1;
    }

    @Override
    public HandlerRegistration addBeforeSelectionHandler( final BeforeSelectionHandler<PartDefinition> handler ) {
        return addHandler( handler, BeforeSelectionEvent.getType() );
    }

    @Override
    public HandlerRegistration addSelectionHandler( final SelectionHandler<PartDefinition> handler ) {
        return addHandler( handler, SelectionEvent.getType() );
    }

    @Override
    public void onResize() {
        if ( !isAttached() ) {
            return;
        }

        // need explicit resize here because height: 100% in CSS makes the panel too tall
        int contentHeight = getOffsetHeight() - header.getOffsetHeight();

        if ( contentHeight < 0 ) {
            // occasionally (like 1 in 20 times) the panel has 0px height when we get the onResize() call
            // this is a temporary workaround until we figure it out
            content.getElement().getStyle().setHeight( 100, Style.Unit.PCT );
        } else {
            content.getElement().getStyle().setHeight( contentHeight, Style.Unit.PX );
        }

        super.onResize();

        // FIXME only need to do this for the one visible part .. need to call onResize() when switching parts anyway
        for ( int i = 0; i < content.getWidgetCount(); i++ ) {
            final FlowPanel container = (FlowPanel) content.getWidget( i );
            final Widget containedWidget = container.getWidget( 0 );
            if ( containedWidget instanceof RequiresResize) {
                ( (RequiresResize) containedWidget ).onResize();
            }
        }
        panelToolbarWidget.onResize();
    }

    protected void scheduleResize() {
        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onResize();
            }
        } );
    }

    protected List<WorkbenchPartPresenter.View> getAvailablePartViews() {
        List<WorkbenchPartPresenter.View> availableParts = new ArrayList<WorkbenchPartPresenter.View>();
        for (FlowPanel flowPanel : partContentView.values()) {
            availableParts.add((WorkbenchPartPresenter.View) flowPanel.getWidget(0));
        }
        return availableParts;
    }

    protected void updatePanelToolbar() {
        panelToolbarWidget.setCurrentPart(currentPart != null ? (WorkbenchPartPresenter.View) currentPart.getK2().getWidget(0) : null);
        panelToolbarWidget.setAvailableParts(getAvailablePartViews());
        panelToolbarWidget.updateView();
    }
}