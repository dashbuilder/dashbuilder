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
package org.dashbuilder.client.menu.widgets;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.client.menu.ClientMenuUtils;
import org.dashbuilder.client.resources.i18n.MenusConstants;
import org.dashbuilder.client.widgets.animations.AlphaAnimation;
import org.dashbuilder.shared.menu.MenuHandler;
import org.dashbuilder.shared.mvp.command.GoToPerspectiveCommand;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.errors.ErrorPopup;
import org.uberfire.workbench.model.menu.*;
import org.uberfire.workbench.model.menu.MenuItem;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Menu Bar widget with editable capabilities for menu items.
 * @since 0.3.0
 */
@ApplicationScoped
public class MenuView extends Composite
        implements
        MenuScreen.View {

    private static final int ALPHA_ANIMATION_DURATION = 500;
    private static final String POINTS = "...";
    private static final String MENU_ITEM_TYPE_GROUP = "MENU_ITEM_TYPE_GROUP";
    private static final String DROPDOWN_OPEN_STYLE = "open";
    public static final String DRAG_DATA_TEXT = "text";
    final MenuComponent.MenuItemTypes ITEM_TYPE_COMMAND = MenuComponent.MenuItemTypes.COMMAND;

    interface MenuViewBinder
            extends
            UiBinder<Panel, MenuView> {

    }

    private static MenuViewBinder uiBinder = GWT.create( MenuViewBinder.class );

    interface MenuViewStyle extends CssResource {
        String mainPanel();
        String labelError();
        String removeButtonPanelDragOver();
        String menuItemDropOver_b();
        String menuItemDropOver_l();
        String menuItemDropOver_r();
        String menuItemTypeRadioButton();
        String menuItemModalPanel();
    }

    @UiField
    public Nav menuBarLeft;

    @UiField
    public Nav menuBarCenter;

    @UiField
    public Nav menuBarRight;

    @UiField
    MenuViewStyle style;
    
    @UiField
    NavForm barForm;
    
    @UiField
    Button addButton;
    
    @UiField
    FlowPanel removeButtonPanel;
    
    @UiField
    Button removeButton;
    
    @UiField(provided = true)
    BaseModal menuItemModalPanel;
    
    @UiField
    WellForm menuItemForm;
    
    @UiField
    InlineLabel nameLabel;
    
    @UiField
    TextBox menuItemName;

    @UiField
    FlowPanel perspectivesPanel;
    
    @UiField
    InlineLabel perspectiveLabel;

    @UiField
    FlowPanel menuItemTypesWrapperPanel;
    
    @UiField
    InlineLabel menuItemTypeLabel;
    
    @UiField
    FlowPanel menuItemTypesPanel;
    
    @UiField
    DropdownButton menuItemPerspectives;
    
    @UiField
    Button menuItemCreateButton;

    @UiField
    Button menuItemEditButton;
    
    private boolean isEdit;
    private String selectedItemUUID;
    private PerspectiveActivity selectedPerspective;
    private MenuComponent.MenuItemTypes selectedItemType;
    private MenuScreen.ViewCallback viewCallback;
    private Timer menuGroupTimer;
    private HandlerRegistration editMenuItemButtonHandlerRegistration;
    
    public MenuView() {

        // Modal panel creation.
        menuItemModalPanel = new BaseModal() {
            @Override
            protected boolean handleDefaultAction() {
                final ClickEvent e = new ClickEvent() {};
                boolean result = false;
                if (selectedItemUUID == null) {
                    menuItemCreateButton.fireEvent(e);
                    result = true;
                } else {
                    menuItemEditButton.fireEvent(e);
                    result = true;
                }
                return result;
            }
        };

        // GWT UI Binding.
        initWidget( uiBinder.createAndBindUi( this ) );

        // Modal panel settings.
        menuItemModalPanel.setTitle(MenusConstants.INSTANCE.newMenuItem());
        menuItemModalPanel.addStyleName(style.menuItemModalPanel());
        menuItemModalPanel.setCloseVisible(true);
        menuItemModalPanel.setKeyboard(true);
        menuItemModalPanel.setAnimation(true);
        menuItemModalPanel.setBackdrop(BackdropType.STATIC);
        
        // Edition settings, disabled by default.
        isEdit = false;
        barForm.getTextBox().setEnabled(false);
        barForm.getTextBox().setVisible(false);
        
        // Enable remove button - drop item feature.
        disableRemove();
        removeButtonPanel.addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(final DragOverEvent event) {
                event.stopPropagation();
                if (isEdit) removeButtonPanel.addStyleName(style.removeButtonPanelDragOver());
            }
        }, DragOverEvent.getType());
        removeButtonPanel.addDomHandler(new DragLeaveHandler() {
            @Override
            public void onDragLeave(final DragLeaveEvent event) {
                event.stopPropagation();
                if (isEdit) removeButtonPanel.removeStyleName(style.removeButtonPanelDragOver());
            }
        }, DragLeaveEvent.getType());
        removeButtonPanel.addDomHandler(new DropHandler() {
            @Override
            public void onDrop(final DropEvent event) {
                event.stopPropagation();
                if (isEdit) {
                    removeButtonPanel.removeStyleName(style.removeButtonPanelDragOver());
                    final String id = event.getData(DRAG_DATA_TEXT);
                    if (id != null) {
                        viewCallback.removeItem(id);
                    }    
                }
            }
        }, DropEvent.getType());

    }
    
    @Override
    public void build(final Menus menus, final MenuScreen.ViewCallback callback) {
        this.viewCallback = callback;
        clearMenuBars();
        buildMenuItems(menus);
        disableRemove();
    }

    @Override
    public void showError(String msg) {
        ErrorPopup.showMessage("MenuView error: " + msg);
    }

    @UiHandler(value = "addButton")
    public void onAdd(final ClickEvent clickEvent) {
        createMenuItem();
    }

    @UiHandler(value = "menuItemEditButton")
    public void onMenuItemUpdate(final ClickEvent clickEvent) {
        final boolean isValid = isMenuItemValid();
        if (isValid) {
            final String name = menuItemName.getText();
            switch (selectedItemType) {
                case COMMAND:
                    final String pId = selectedPerspective.getIdentifier();
                    editMenuItemCommand(selectedItemUUID, pId, name);
                    break;
                case GROUP:
                    editMenuItemGroup(selectedItemUUID, name);
                    break;
            }
            selectedItemUUID = null;
            selectedItemType = null;
            selectedPerspective = null;
            hideMenuItemModalPanel();
        }
    }
    
    @UiHandler(value = "menuItemCreateButton")
    public void onMenuItemConfirm(final ClickEvent clickEvent) {
        final boolean isValid = isMenuItemValid();
        if (isValid) {
            final String name = menuItemName.getText();
            switch (selectedItemType) {
                case COMMAND:
                    final String pId = selectedPerspective.getIdentifier();
                    createMenuItemCommand(pId, name);
                    break;
                case GROUP:
                    createMenuItemGroup(name);
                    break;
            }
            selectedItemUUID = null;
            selectedItemType = null;
            selectedPerspective = null;
            hideMenuItemModalPanel();
        }
    }
    
    private boolean isMenuItemValid() {
        boolean isValid = true;

        // Menu item's name input validations.
        final String name = menuItemName.getText();
        if (isEmpty(name)) {
            nameLabel.addStyleName(style.labelError());
            isValid = false;
        } else {
            nameLabel.removeStyleName(style.labelError());
        }

        if (selectedItemType == null) {
            menuItemTypeLabel.addStyleName(style.labelError());
            isValid = false;
        } else {
            menuItemTypeLabel.removeStyleName(style.labelError());
            if (ITEM_TYPE_COMMAND.equals(selectedItemType) && selectedPerspective == null) {
                perspectiveLabel.addStyleName(style.labelError());
                isValid = false;
            } else {
                perspectiveLabel.removeStyleName(style.labelError());
            }
        }
        
        return isValid;
    }
    private boolean isEmpty(final String text) {
        return text == null || text.trim().length() == 0;
    }

    private void createMenuItemCommand(final String activityId, final String name) {
        viewCallback.createItemCommand(name, activityId);
    }

    private void createMenuItemGroup(final String name) {
        viewCallback.createItemGroup(name);
    }


    private void editMenuItemCommand(final String itemUUID, final String activityId, final String name) {
        viewCallback.editItemCommand(itemUUID, name, activityId);
    }

    private void editMenuItemGroup(final String itemUUID, final String name) {
        viewCallback.editeItemGroup(itemUUID, name);
    }

    public void enableEdition() {
        isEdit = true;
        barForm.setVisible(true);
    }

    public void disableEdition() {
        isEdit = false;
        selectedItemUUID = null;
        selectedItemType = null;
        selectedPerspective = null;
        resetMenuItemForm();
        hideMenuItemModalPanel();
        barForm.setVisible(false);
    }
    
    private void createMenuItem() {
        resetMenuItemForm();
        buildItemTypes();
        enablePerspectivesDropDown();
        buildPerspectivesDropDown(null);
        menuItemModalPanel.setTitle(MenusConstants.INSTANCE.newMenuItem());
        menuItemCreateButton.setVisible(true);
        menuItemEditButton.setVisible(false);
        menuItemModalPanel.show();
        menuItemName.setFocus(true);
    }

    private void editMenuItem(final MenuItem item) {
        resetMenuItemForm();
        final boolean isItemCommand = item instanceof MenuItemCommand;
        final String itemCaption = new SafeHtmlBuilder().appendEscaped(item.getCaption()).toSafeHtml().asString(); 
        menuItemName.setText(itemCaption);
        selectedItemUUID = item.getSignatureId();
        selectedItemType = !isItemCommand ? MenuComponent.MenuItemTypes.GROUP : MenuComponent.MenuItemTypes.COMMAND;
        menuItemTypesWrapperPanel.setVisible(false);
        boolean showPerspectiveSelector = false;
        if (isItemCommand) {
            final MenuItemCommand itemCommand = (MenuItemCommand) item;
            try {
                final GoToPerspectiveCommand goToPerspectiveCommand = (GoToPerspectiveCommand) itemCommand.getCommand();
                buildPerspectivesDropDown(goToPerspectiveCommand.getActivityId());
                showPerspectiveSelector = true;
            } catch (ClassCastException e) {
                // Perspective goTo command not supported.
            }
        } 
        if (showPerspectiveSelector) enablePerspectivesDropDown();
        else disablePerspectivesDropDown();;
        menuItemModalPanel.setTitle(MenusConstants.INSTANCE.editMenuItem() + " " + itemCaption);
        menuItemCreateButton.setVisible(false);
        menuItemEditButton.setVisible(true);
        menuItemModalPanel.show();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                menuItemName.setFocus(true);
            }
        });
    }

    private void hideMenuItemModalPanel() {
        menuItemModalPanel.hide();
    }
    
    private void resetMenuItemForm() {
        nameLabel.removeStyleName(style.labelError());
        perspectiveLabel.removeStyleName(style.labelError());
        menuItemName.setText("");
        selectedItemUUID = null;
        selectedItemType = null;
        selectedPerspective = null;
        menuItemPerspectives.clear();
        menuItemTypesPanel.clear();
        menuItemForm.reset();
    }
    
    private void enableRemove() {
        removeButtonPanel.getElement().setDraggable(Element.DRAGGABLE_TRUE);
        final AlphaAnimation alphaAnimation = new AlphaAnimation(removeButtonPanel);
        alphaAnimation.show(ALPHA_ANIMATION_DURATION);
    }

    private void disableRemove() {
        removeButtonPanel.getElement().setDraggable(Element.DRAGGABLE_FALSE);
        final AlphaAnimation alphaAnimation = new AlphaAnimation(removeButtonPanel);
        alphaAnimation.hide(ALPHA_ANIMATION_DURATION);
    }

    private void buildItemTypes() {
        final MenuComponent.MenuItemTypes[] itemTypes = MenuComponent.MenuItemTypes.values();
        
        menuItemTypesPanel.clear();
        selectedItemType = ITEM_TYPE_COMMAND;
        for (final MenuComponent.MenuItemTypes type: itemTypes) {
            final String name = ClientMenuUtils.getItemTypeName(type).asString();
            final RadioButton button = new RadioButton(MENU_ITEM_TYPE_GROUP, name);
            button.addStyleName(style.menuItemTypeRadioButton());
            if (ITEM_TYPE_COMMAND.equals(type)) {
                button.setValue(true);
            } else {
                button.setValue(false);
            }
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    if (ITEM_TYPE_COMMAND.equals(type)) {
                        enablePerspectivesDropDown();
                    } else {
                        disablePerspectivesDropDown();
                    }
                    selectedItemType = type;
                }
            });
            menuItemTypesPanel.add(button);
            menuItemTypesWrapperPanel.setVisible(true);
        }
    }
    
    private void buildPerspectivesDropDown(final String activityId) {
        // Load perspectives and build the drop down.
        final Collection<PerspectiveActivity> perspectives = viewCallback.getPerspectiveActivities();

        menuItemPerspectives.clear();
        // TODO: Check selected value when editing.
        if (activityId == null) menuItemPerspectives.setText(MenusConstants.INSTANCE.perspective_placeholder() + POINTS);
        else menuItemPerspectives.setText(activityId);
        
        // Create the drop down entries for each perspective.
        for (final PerspectiveActivity perspective : perspectives) {
            final String pId = perspective.getIdentifier();
            
            // Editing an existing menu item type command.
            if (selectedPerspective == null && activityId != null &&
                    activityId.equals(pId)) {
                selectedPerspective = perspective;
            }
            
            // Build the drop down entry.
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

    private void enablePerspectivesDropDown() {
        final AlphaAnimation alphaAnimation = new AlphaAnimation(perspectivesPanel);
        alphaAnimation.show(ALPHA_ANIMATION_DURATION);
    }

    private void disablePerspectivesDropDown() {
        final AlphaAnimation alphaAnimation = new AlphaAnimation(perspectivesPanel);
        alphaAnimation.hide(ALPHA_ANIMATION_DURATION);
    }
    
    private String getSafeHtml(final String text) {
        return new SafeHtmlBuilder().appendEscaped(text).toSafeHtml().asString();
    }
    
    private void buildMenuItems(final Menus menus) {
        ClientMenuUtils.doDebugLog("buildMenuItems");
        if (menus != null) {
            for ( final MenuItem activeMenu : menus.getItems() ) {
                buildMenuItem(activeMenu);
            }
        }
    }

    private void buildMenuItem(final MenuItem menuItem) {
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

    private void configureWidgetDnd(final Widget gwtItem, final MenuComponent.MenuItemTypes type, final MenuItem item) {
        if (item != null && gwtItem != null) {
            final String uuid = item.getSignatureId();
            
            // Enable GWT native Dnd.
            gwtItem.getElement().setDraggable(Element.DRAGGABLE_TRUE);

            // Drag start handler.
            gwtItem.addDomHandler(new DragStartHandler() {
                @Override
                public void onDragStart(final DragStartEvent event) {
                    event.stopPropagation();
                    onMenuItemDragStart(event, type, uuid, gwtItem);
                    
                }
            }, DragStartEvent.getType());

            // Drag over handler.
            gwtItem.addDomHandler(new DragOverHandler() {
                @Override
                public void onDragOver(final DragOverEvent event) {
                    event.stopPropagation();
                    onMenuItemDragOver(event, type, uuid, gwtItem);
                }
            }, DragOverEvent.getType());

            // Drag leave handler.
            gwtItem.addDomHandler(new DragLeaveHandler() {
                @Override
                public void onDragLeave(final DragLeaveEvent event) {
                    event.stopPropagation();
                    onMenuItemDragLeave(event, type, uuid, gwtItem);
                }
            }, DragLeaveEvent.getType());

            // Drag end handler.
            gwtItem.addDomHandler(new DragEndHandler() {
                @Override
                public void onDragEnd(final DragEndEvent event) {
                    event.stopPropagation();
                    onMenuItemDragEnd(event, type, uuid, gwtItem);
                }
            }, DragEndEvent.getType());
            
            // Drop handler.
            gwtItem.addDomHandler(new DropHandler() {
                @Override
                public void onDrop(final DropEvent event) {
                    event.stopPropagation();
                    onMenuItemDrop(event, type, uuid, gwtItem);
                }
            }, DropEvent.getType());
        }
    }

    private enum MenuViewDragPosition {
        BOTTOM, LEFT, RIGHT;
    }
    
    private void onMenuItemDragStart(final DragStartEvent event, final MenuComponent.MenuItemTypes type, final String uuid, final Widget gwtItem) {
        if (isEdit) {
            event.setData(DRAG_DATA_TEXT, uuid);
            ClientMenuUtils.doDebugLog("onMenuItemDragStart - uuid: " + uuid);
            enableRemove();
        }
    }
    
    private void onMenuItemDragOver(final DragOverEvent event, final MenuComponent.MenuItemTypes type, final String uuid, final Widget gwtItem) {
        if (isEdit) {
            removeMenuItemDragStyles(gwtItem);
            final boolean isMenuGroup = MenuComponent.MenuItemTypes.GROUP.equals(type); 
            if (isMenuGroup) {
                openMenuGroup(gwtItem);
            }
            final MenuHandler.MenuItemLocation location = viewCallback.getItemLocation(uuid);
            if (location != null) {
                final boolean isItemHorizontal = location.getParent() == null;
                final MenuViewDragPosition dragPosition = getDragPosition(event, type, isItemHorizontal, gwtItem);
                applyDragPositionStyles(gwtItem, dragPosition, isMenuGroup, isItemHorizontal);
            }
        }
    }
    
    private boolean isMouseOnBottomOfWidget(final DomEvent event, final Widget w) {
        final int mouseY = event.getNativeEvent().getClientY();
        final int itemY0 = w.getAbsoluteTop();
        final int itemY1 = itemY0 + w.getElement().getOffsetHeight();
        final int itemY = itemY0 + ( (itemY1 - itemY0) / 2 );
        final boolean isMouseOnBottom = mouseY > itemY;
        return isMouseOnBottom;
    }

    private boolean isMouseOnRightOfWidget(final DomEvent event, final Widget w) {
        final int mouseX = event.getNativeEvent().getClientX();
        final int itemX0 = w.getAbsoluteLeft();
        final int itemX1 = itemX0 + w.getElement().getOffsetWidth();
        final int itemX = itemX0 + ( (itemX1 - itemX0) / 2 );
        final boolean isMouseOnRight = mouseX > itemX;
        return isMouseOnRight;
    }

    private void onMenuItemDragLeave(final DragLeaveEvent event, final MenuComponent.MenuItemTypes type, final String id, final Widget gwtItem) {
        if (isEdit) {
            removeMenuItemDragStyles(gwtItem);
        }
    }

    private void onMenuItemDragEnd(final DragEndEvent event, final MenuComponent.MenuItemTypes type, final String id, final Widget gwtItem) {
        if (isEdit) {
            removeMenuItemDragStyles(gwtItem);
            disableRemove();
        }
    }

    private void onMenuItemDrop(final DropEvent event, final MenuComponent.MenuItemTypes type, final String targetUUID, final Widget gwtItem) {
        if (isEdit) {
            removeMenuItemDragStyles(gwtItem);
            disableRemove();

            final String sourceUUID = event.getData("text");
            if (sourceUUID != null && targetUUID != null && !sourceUUID.equals(targetUUID)) {
                final MenuHandler.MenuItemLocation location = viewCallback.getItemLocation(targetUUID);
                if (location != null) {
                    final boolean isItemHorizontal = location.getParent() == null;
                    final MenuViewDragPosition dragPosition = getDragPosition(event, type, isItemHorizontal, gwtItem);
                    if (dragPosition != null) {
                        final boolean isItemVertical = !isItemHorizontal;
                        final boolean isMenuGroup = MenuComponent.MenuItemTypes.GROUP.equals(type);
                        final boolean isBottom = MenuViewDragPosition.BOTTOM.equals(dragPosition);
                        final boolean isLeft = MenuViewDragPosition.LEFT.equals(dragPosition);
                        final boolean before = (!isBottom && isItemVertical) || isLeft;
                        applyDragPositionStyles(gwtItem, dragPosition, isMenuGroup, isItemHorizontal);
                        ClientMenuUtils.doDebugLog("Drop from '" + sourceUUID + "' on  '" + targetUUID + "' using before=" + before);
                        viewCallback.moveItem(sourceUUID, targetUUID, before);
                    }
                }
            }
        }
    }
    
    private MenuViewDragPosition getDragPosition(final DomEvent event, final MenuComponent.MenuItemTypes type, boolean isItemHorizontal, final Widget gwtItem) {
        if (gwtItem != null) {
            final boolean isMenuGroup = MenuComponent.MenuItemTypes.GROUP.equals(type);
            final boolean isMouseOnBottom = isMouseOnBottomOfWidget(event, gwtItem);
            final boolean isMouseOnRight = isMouseOnRightOfWidget(event, gwtItem);
            if (isMouseOnBottom && !isMenuGroup && !isItemHorizontal) {
                return MenuViewDragPosition.BOTTOM;
            } else if (isMouseOnRight && isItemHorizontal) {
                return MenuViewDragPosition.RIGHT;
            } else if (isItemHorizontal){
                return MenuViewDragPosition.LEFT;
            }
        }
        return null;
    }
    
    private void applyDragPositionStyles(final Widget widget, final MenuViewDragPosition dragPosition,
                                         final boolean isMenuGroup, final boolean isItemHorizontal) {
        if (widget != null && dragPosition != null) {
            final boolean isBottom = MenuViewDragPosition.BOTTOM.equals(dragPosition);
            final boolean isLeft = MenuViewDragPosition.LEFT.equals(dragPosition);
            if (isBottom && !isMenuGroup && !isItemHorizontal) {
                widget.addStyleName(style.menuItemDropOver_b());
            } else if (!isLeft && isItemHorizontal) {
                widget.addStyleName(style.menuItemDropOver_r());
            } else if (isItemHorizontal){
                widget.addStyleName(style.menuItemDropOver_l());
            }
        }
    }
    
    private void openMenuGroup(final Widget gwtItem) {
        startTimer(gwtItem);
    }

    private void closeMenuGroup(final Widget gwtItem) {
        gwtItem.getElement().removeClassName(DROPDOWN_OPEN_STYLE);
    }
    
    private void startTimer(final Widget menuGroup) {
        if (menuGroupTimer == null) {
            menuGroupTimer = new Timer() {
                @Override
                public void run() {
                    removeMenuItemDragStyles(menuGroup);
                    closeMenuGroup(menuGroup);
                    menuGroupTimer = null;
                }
            };
            menuGroup.getElement().addClassName(DROPDOWN_OPEN_STYLE);
            menuGroupTimer.schedule(2000);
        }
    }

    private void endTimer() {
        if (menuGroupTimer != null) {
            menuGroupTimer.run();
            menuGroupTimer.cancel();
        }
    }
    
    private void removeMenuItemDragStyles(final Widget gwtItem) {
        gwtItem.removeStyleName(style.menuItemDropOver_b());
        gwtItem.removeStyleName(style.menuItemDropOver_l());
        gwtItem.removeStyleName(style.menuItemDropOver_r());
    }
    
    private Button buildMenuItemEditWidget(final MenuItem item) {
        final boolean isMenuGroup = item instanceof MenuGroup;
        final Button i = new Button();
        i.setTitle(MenusConstants.INSTANCE.editMenuItem());
        i.setIcon(IconType.PENCIL);
        i.setType(ButtonType.LINK);
        i.setSize(ButtonSize.SMALL);
        final Style style = i.getElement().getStyle();
        style.setColor("#FFFFFF");   
        style.setCursor(Style.Cursor.POINTER);
        style.setMarginTop(0, Style.Unit.PX);
        if (isMenuGroup) {
            style.setPosition(Style.Position.ABSOLUTE);
            style.setTop(0, Style.Unit.PX);
            style.setLeft(0, Style.Unit.PX);
            style.setHeight(1, Style.Unit.PX);
        }
        i.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                editMenuItem(item);
                
            }
        });
        return i;
    }
    
    private void configureEditIconMouseEvents(final Widget widget, final AlphaAnimation showAnimation, final Button editIcon) {
        widget.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                event.stopPropagation();
                showAnimation.show(1000);
            }
        }, MouseOverEvent.getType());

        widget.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                event.stopPropagation();
                if (showAnimation.isRunning()) showAnimation.cancel();
                editIcon.setVisible(false);
            }
        }, MouseOutEvent.getType());
    }

    Widget makeItem( final MenuItem item ) {
        if ( notHavePermissionToMakeThis( item ) ) {
            return null;
        }

        if ( item instanceof MenuItemCommand) {

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
        configureWidgetDnd(gwtItem, MenuComponent.MenuItemTypes.COMMAND, item);

        // Handlers for edit, move and remove features.
        final Button editIcon = buildMenuItemEditWidget(item);
        final String editIconStyle = editIcon.getElement().getAttribute("style");
        editIcon.setVisible(false);
        final AlphaAnimation showAnimation = new AlphaAnimation(editIcon, editIconStyle);
        gwtItem.add(editIcon);
        configureEditIconMouseEvents(gwtItem, showAnimation, editIcon);
        configureWidgetDnd(gwtItem, MenuComponent.MenuItemTypes.COMMAND, item);
        
        return gwtItem;
    }

    Widget makeMenuCustom( MenuCustom item ) {
        final MenuCustom custom = item;
        final Widget w =((IsWidget) item.build()).asWidget();
        // TODO: Not supported for edition neither moving or removing.
        return w;
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

        // Add children and configure handlers for edit, move and remove features.
        final Button editIcon = buildMenuItemEditWidget(groups);
        final String editIconStyle = editIcon.getElement().getAttribute("style");
        editIcon.setVisible(false);
        final AlphaAnimation showAnimation = new AlphaAnimation(editIcon, editIconStyle);
        final Dropdown dropdown = new Dropdown( groups.getCaption() ) {{
            addWidget(editIcon);
            for ( final Widget widget : widgetList ) {
                add( widget );
            }
        }};
        configureEditIconMouseEvents(dropdown, showAnimation, editIcon);
        configureWidgetDnd(dropdown, MenuComponent.MenuItemTypes.GROUP, groups);

        return dropdown;
    }

    Widget makeMenuItemCommand( final MenuItem item ) {
        final MenuItemCommand cmdItem = (MenuItemCommand) item;
        final NavLink gwtItem;

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

        // Handlers for edit, move and remove features.
        final Button editIcon = buildMenuItemEditWidget(item);
        final String editIconStyle = editIcon.getElement().getAttribute("style");
        editIcon.setVisible(false);
        final AlphaAnimation showAnimation = new AlphaAnimation(editIcon, editIconStyle);
        gwtItem.add(editIcon);
        configureEditIconMouseEvents(gwtItem, showAnimation, editIcon);
        configureWidgetDnd(gwtItem, MenuComponent.MenuItemTypes.COMMAND, item);

        return gwtItem;
    }

    boolean notHavePermissionToMakeThis( MenuItem item ) {
        return viewCallback.notHavePermissionToMakeThis(item);
    }

    private void clearMenuBars() {
        ClientMenuUtils.doDebugLog("clearMenuBars");
        menuBarLeft.clear();
        menuBarCenter.clear();
        menuBarRight.clear();
    }
    
    @Override
    public void clear() {
        clearMenuBars();
        resetMenuItemForm();
        isEdit = false;
        endTimer();
        viewCallback = null;
        editMenuItemButtonHandlerRegistration = null;
    }

}
