/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dashbuilder.client.perspective.editor.widgets.menu;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.resources.i18n.PerspectiveEditorConstants;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.menu.*;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.util.*;

/**
 * The Menu Bar widget with editable capabilities for menu items.
 * @since 0.3.0
 */
@Alternative
public class EditableWorkbenchMenuBarView extends Composite
        implements
        WorkbenchMenuBarPresenter.View {

    private static final String POINTS = "...";

    interface EditableWorkbenchMenuBarViewBinder
            extends
            UiBinder<Panel, EditableWorkbenchMenuBarView> {

    }

    private static EditableWorkbenchMenuBarViewBinder uiBinder = GWT.create( EditableWorkbenchMenuBarViewBinder.class );

    interface EditableWorkbenchMenuBarViewStyle extends CssResource {
        String mainPanel();
        String editButton();
    }

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Inject
    private PlaceManager placeManager;

    @UiField
    public Nav menuBarLeft;

    @UiField
    public Nav menuBarCenter;

    @UiField
    public Nav menuBarRight;

    @UiField
    EditableWorkbenchMenuBarViewStyle style;
    
    @UiField
    Button editButton;
    
    @UiField
    NavForm barForm;
    
    @UiField
    Button addButton;
    
    @UiField
    Modal menuItemModalPanel;
    
    @UiField
    WellForm menuItemForm;
    
    @UiField
    TextBox menuItemName;
    
    @UiField
    HelpInline menuItemNameHelpInline;
    
    @UiField
    DropdownButton menuItemPerspectives;
    
    @UiField
    Button menuItemOkButton;

    private boolean isEdit;
    private PerspectiveActivity selectedPerspective;
    
    public EditableWorkbenchMenuBarView() {
        initWidget( uiBinder.createAndBindUi( this ) );
        isEdit = false;
        barForm.getTextBox().setVisible(false);
    }
    
    @UiHandler(value = "editButton")
    public void onEdit(final ClickEvent clickEvent) {
        if (isEdit) disableEdit();
        else enableEdit();
    }

    @UiHandler(value = "addButton")
    public void onAdd(final ClickEvent clickEvent) {
        showMenuItemModalPanel();
    }

    @UiHandler(value = "menuItemOkButton")
    public void onMenuItemConfirm(final ClickEvent clickEvent) {
        boolean isValid = true;
        
        // Menu item's name input validations.
        final String name = menuItemName.getText();
        if (isEmpry(name)) {
            menuItemNameHelpInline.setText(PerspectiveEditorConstants.INSTANCE.name_mandatory());
            isValid = false;
        } else {
            menuItemNameHelpInline.setText("");
        }
        if (selectedPerspective == null) {
            // TODO: Show drop down validation error.
            isValid = false;
        }
        
        if (isValid) {
            final String pId = selectedPerspective.getIdentifier();
            createMenuItem(pId, name);
            selectedPerspective = null;
            hideMenuItemModalPanel();
        }
    }
    
    private boolean isEmpry(final String text) {
        return text == null || text.trim().length() == 0;
    }

    private void createMenuItem(final String activityId, final String name) {
        final MenuItem menuItem = MenuFactory.newSimpleItem(name).respondsWith(new Command() {
            public void execute() {
                placeManager.goTo(activityId);
            }
        }).endMenu().build().getItems().get(0);

        addMenuItem(menuItem);
    }
    
    public void enableEdit() {
        isEdit = true;
        resetMenuItemForm();
        barForm.setVisible(true);
    }

    public void disableEdit() {
        isEdit = false;
        selectedPerspective = null;
        resetMenuItemForm();
        hideMenuItemModalPanel();
        barForm.setVisible(false);
    }
    
    private void showMenuItemModalPanel() {
        buildPerspectivesDropDown();
        menuItemModalPanel.show();
    }

    private void hideMenuItemModalPanel() {
        menuItemModalPanel.hide();
    }
    
    private void resetMenuItemForm() {
        menuItemForm.reset();
        selectedPerspective = null;
    }
    
    private void buildPerspectivesDropDown() {

        // Load perspectives and build the drop down.
        final Collection<PerspectiveActivity> perspectives = perspectiveEditor.getPerspectiveActivities();

        menuItemPerspectives.clear();
        menuItemPerspectives.setText(PerspectiveEditorConstants.INSTANCE.perspective_placeholder() + POINTS);
        for (final PerspectiveActivity perspective : perspectives) {
            final String pName = perspective.getDefaultPerspectiveLayout().getName();
            final String name = getSafeHtml(pName);
            final NavLink link = new NavLink(name);
            link.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    selectedPerspective = perspective;
                    menuItemPerspectives.setText(name);
                }
            });
            menuItemPerspectives.add(link);
        }
    }
    
    private String getSafeHtml(final String text) {
        return new SafeHtmlBuilder().appendEscaped(text).toSafeHtml().asString();
    }
    
    /**
     * Add a Presenter Menu item to the view. This simply converts Presenter
     * Menu items to GWT MenuItems. Filtering of menu items for permissions is
     * conducted by the Presenter.
     */
    @Override
    public void addMenuItems( final Menus menus ) {
        for ( final MenuItem activeMenu : menus.getItems() ) {
            addMenuItem(activeMenu);
        }
    }

    private void addMenuItem( final MenuItem menuItem ) {
        final Widget result = makeItem( menuItem );
        if ( result != null ) {
            final Widget gwtItem = makeItem( menuItem );
            if ( menuItem.getPosition().equals( MenuPosition.LEFT ) ) {
                menuBarLeft.add( gwtItem );
            } else if ( menuItem.getPosition().equals( MenuPosition.CENTER ) ) {
                menuBarCenter.add( gwtItem );
            } else if ( menuItem.getPosition().equals( MenuPosition.RIGHT ) ) {
                menuBarRight.add( gwtItem );
            }
        }
    }

    Widget makeItem( final MenuItem item ) {
        if ( notHavePermissionToMakeThis( item ) ) {
            return null;
        }

        if ( item instanceof MenuItemCommand ) {

            return makeMenuItemCommand( item );

        } else if ( item instanceof MenuGroup ) {

            return makeMenuGroup( (MenuGroup) item );

        } else if ( item instanceof MenuCustom ) {

            return makeMenuCustom( (MenuCustom) item );

        }
        return makeNavLink( item );
    }

    Widget makeNavLink( final MenuItem item ) {
        final NavLink gwtItem = new NavLink( item.getCaption() ) {{
            setDisabled( !item.isEnabled() );
        }};
        item.addEnabledStateChangeListener( new EnabledStateChangeListener() {
            @Override
            public void enabledStateChanged( final boolean enabled ) {
                gwtItem.setDisabled( !enabled );
            }
        } );

        return gwtItem;
    }

    Widget makeMenuCustom( MenuCustom item ) {
        final MenuCustom custom = item;

        return ( (IsWidget) item.build() ).asWidget();
    }

    Widget makeMenuGroup( final MenuGroup groups ) {
        final List<Widget> widgetList = new ArrayList<Widget>();
        for ( final MenuItem _item : groups.getItems() ) {
            final Widget result = makeItem( _item );
            if ( result != null ) {
                widgetList.add( result );
            }
        }

        if ( widgetList.isEmpty() ) {
            return null;
        }

        return new Dropdown( groups.getCaption() ) {{
            for ( final Widget widget : widgetList ) {
                add( widget );
            }
        }};
    }

    Widget makeMenuItemCommand( final MenuItem item ) {
        final MenuItemCommand cmdItem = (MenuItemCommand) item;
        final Widget gwtItem;

        gwtItem = new NavLink( cmdItem.getCaption() ) {{
            setDisabled( !item.isEnabled() );
            addClickHandler( new ClickHandler() {
                @Override
                public void onClick( final ClickEvent event ) {
                    cmdItem.getCommand().execute();
                }
            } );
        }};
        item.addEnabledStateChangeListener( new EnabledStateChangeListener() {
            @Override
            public void enabledStateChanged( final boolean enabled ) {
                ( (NavLink) gwtItem ).setDisabled( !enabled );
            }
        } );

        return gwtItem;
    }

    boolean notHavePermissionToMakeThis( MenuItem item ) {
        return !authzManager.authorize( item, identity );
    }

    @Override
    public void clear() {
        menuBarLeft.clear();
        menuBarCenter.clear();
        menuBarRight.clear();
        disableEdit();
        selectedPerspective = null;
    }

}
