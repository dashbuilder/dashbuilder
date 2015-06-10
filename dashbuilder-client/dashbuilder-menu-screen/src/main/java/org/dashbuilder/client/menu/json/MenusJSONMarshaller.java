/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.menu.json;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.*;
import org.dashbuilder.client.menu.MenuUtils;
import org.dashbuilder.client.menu.widgets.MenuComponent;
import org.dashbuilder.client.mvp.command.GoToPerspectiveCommand;
import org.uberfire.workbench.model.menu.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO: Roles and other MenuItem attributes deserialization.
public class MenusJSONMarshaller {

    public static final String MENU_ITEMS = "items";
    public static final String MENU_ITEM_TYPE = "itemType";
    public static final String MENU_ITEM_CAPTION = "caption";
    public static final String MENU_ITEM_CONTRIBUTION_POINT = "contributionPoint";
    public static final String MENU_ITEM_ORDER = "order";
    public static final String MENU_ITEM_SIGNATURE_ID = "signatureId";
    public static final String MENU_ITEM_POSITION = "position";
    public static final String MENU_ITEM_ROLES = "roles";
    public static final String MENU_ITEM_NAME = "name";
    public static final String MENU_ITEM_COMMAND_ACTIVITYID = "activityId";
    
    public MenusJSONMarshaller() {
    }

    public Menus fromJsonString(final String jsonString) {
        if (!isEmpty(jsonString )) {
            final JSONObject menusObject = JSONParser.parseStrict(jsonString).isObject();
            if ( menusObject != null ) {
                final JSONArray items = menusObject.get(MENU_ITEMS).isArray();

                if (items != null) {
                    final List<MenuItem> itemList = new ArrayList<MenuItem>();
                    for (int x = 0; x < items.size(); x++) {
                        final JSONObject menuItemObject = items.get(x).isObject();
                        final MenuItem item = deserializeMenuItem(menuItemObject);
                        if (item != null) itemList.add(item);
                    }

                    return MenuUtils.buildEditableMenusModel(itemList);
                }
            }
        }
        return null;
    }

    private MenuItem deserializeMenuItem(final JSONObject itemObject) {
        if (itemObject != null) {
            final String typeStr = itemObject.get(MENU_ITEM_TYPE).isString().stringValue();
            final MenuComponent.MenuItemTypes type = MenuComponent.MenuItemTypes.valueOf(typeStr);
            switch (type) {
                case COMMAND:
                    return deserializeMenuItemCommand(itemObject);
                case GROUP:
                    return deserializeMenuGroup(itemObject);
                default:
                    GWT.log("Menu item with type '" + typeStr + "' not supported.");
            }
        }
        return null;
    }

    private MenuItem deserializeMenuItemCommand(final JSONObject itemObject) {
        if (itemObject != null) {
            final String caption = itemObject.get(MENU_ITEM_CAPTION).isString().stringValue();
            final String activityId = itemObject.get(MENU_ITEM_COMMAND_ACTIVITYID).isString().stringValue();
            return MenuUtils.createMenuItemCommand(caption, activityId);
        }
        return null;
    }

    private MenuItem deserializeMenuGroup(final JSONObject itemObject) {
        if (itemObject != null) {
            final String caption = itemObject.get(MENU_ITEM_CAPTION).isString().stringValue();
            final JSONArray items = itemObject.get( MENU_ITEMS ).isArray();
            if (items != null) {
                final List<MenuItem> result = new ArrayList<MenuItem>();
                for (int x = 0; x < items.size(); x++) {
                    JSONObject columnJson = items.get(x).isObject();
                    final MenuItem i = deserializeMenuItem(columnJson.isObject());
                    if (i != null) result.add(i);
                }
                return MenuUtils.createMenuItemGroup(caption, result);
            }
        }
        return null;
    }

    public String toJsonString(final Menus menus) {
        final JSONValue menusObject = toJson(menus);
        return menusObject != null ? menusObject.toString() : null;
    }
    
    private JSONValue toJson(final Menus menus) {
        if (menus != null) {
            final List<MenuItem> items = menus.getItems();
            if (items != null && !items.isEmpty()) {
                JSONArray itemsArray = new JSONArray();
                for (int i=0; i<items.size(); i++) {
                    final MenuItem item = items.get(i);                    
                    final JSONObject itemSerialized = serialize(item);
                    if (itemSerialized != null) {
                        itemsArray.set(i, itemSerialized);
                    }
                }
                JSONObject result = new JSONObject();
                result.put(MENU_ITEMS, itemsArray);
                return result;
            }
        }
        return null;
    }

    private JSONObject serialize(final MenuItem menuItem) {
        if (menuItem != null) {
            try  {
                final MenuItemCommand itemCommand = (MenuItemCommand) menuItem;
                return serializeMenuItemCommand(itemCommand);
            } catch (final ClassCastException e) {

            }
            try  {
                final MenuGroup itemGroup = (MenuGroup) menuItem;
                return serializeMenuGroup(itemGroup);
            } catch (final ClassCastException e) {

            }
            GWT.log("ERROR: Unsupported serialization for menu item type: " + menuItem.getClass().getName());
        }
        return null;
    }

    private JSONObject serializeMenuItemCommand(final MenuItemCommand menuItemCommand) {
        if (menuItemCommand != null) {
            try  {
                final GoToPerspectiveCommand goToPerspectiveCommand = (GoToPerspectiveCommand) menuItemCommand.getCommand();
                final String activityId = goToPerspectiveCommand.getActivityId();
                final JSONObject itemObject = serializeItemBasicAttributes(menuItemCommand);
                if (itemObject != null && activityId != null) {
                    itemObject.put(MENU_ITEM_TYPE, new JSONString(MenuComponent.MenuItemTypes.COMMAND.name()));
                    itemObject.put(MENU_ITEM_TYPE, new JSONString(MenuComponent.MenuItemTypes.COMMAND.name()));
                    itemObject.put(MENU_ITEM_COMMAND_ACTIVITYID, new JSONString(activityId));
                    return itemObject;
                }
            } catch (final ClassCastException e) {
                GWT.log("Only menu items with command [GoToPerspectiveCommand] are supported.");
            }
        }
        return null;
    }

    private JSONObject serializeMenuGroup(final MenuGroup menuGroup) {
        if (menuGroup != null) {
            final JSONObject itemObject = serializeItemBasicAttributes(menuGroup);
            if (itemObject != null) {
                itemObject.put(MENU_ITEM_TYPE, new JSONString(MenuComponent.MenuItemTypes.GROUP.name()));
                final List<MenuItem> items = menuGroup.getItems();
                if (items != null && !items.isEmpty()) {
                    JSONArray itemsArray = new JSONArray();
                    int x = 0;
                    for (final MenuItem item : items) {
                        final JSONObject itemElement = serialize(item);
                        if (itemElement != null) {
                            itemsArray.set(x, itemElement);
                        }
                        x++;
                    }
                    itemObject.put(MENU_ITEMS, itemsArray);
                }
                return itemObject;
            }
        }
        return null;
    }

    private JSONObject serializeItemBasicAttributes(final MenuItem item) {
        if (item != null) {
            final String caption = item.getCaption();
            final String contributionPoint = item.getContributionPoint();
            final String signatureId = item.getSignatureId();
            final int order = item.getOrder();
            final MenuPosition menuPosition = item.getPosition();
            final Collection<String> roles = item.getRoles();
            final JSONObject result = new JSONObject();
            if (caption != null) result.put(MENU_ITEM_CAPTION, new JSONString(caption));
            if (contributionPoint != null) result.put(MENU_ITEM_CONTRIBUTION_POINT, new JSONString(contributionPoint));
            if (signatureId != null) result.put(MENU_ITEM_SIGNATURE_ID, new JSONString(signatureId));
            result.put(MENU_ITEM_ORDER, new JSONNumber(order));
            if (menuPosition != null) result.put(MENU_ITEM_POSITION, new JSONString(menuPosition.name()));
            if (roles != null && !roles.isEmpty()) {
                final JSONArray rolesArray = new JSONArray();
                int x = 0;
                for (final String role : roles) {
                    final JSONObject roleObject = serializeRole(role);
                    if (roleObject != null) {
                        rolesArray.set(x, roleObject);
                    }
                    x++;
                }
                result.put(MENU_ITEM_ROLES, rolesArray);
            }
            return result;
        }
        return null;
    }

    private JSONObject serializeRole(final String role) {
        if (!isEmpty(role)) {
            JSONObject roleObject = new JSONObject();
            if (role != null) roleObject.put(MENU_ITEM_NAME, new JSONString(role));
            return roleObject;
        }
        return null;
    }
    
    public static boolean isEmpty(final String str) {
        return str == null || str.trim().length() == 0;
    }

}
