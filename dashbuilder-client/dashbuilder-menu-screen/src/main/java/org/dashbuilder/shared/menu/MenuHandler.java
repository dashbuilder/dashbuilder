package org.dashbuilder.shared.menu;

import org.dashbuilder.shared.menu.exception.*;
import org.dashbuilder.shared.mvp.command.GoToPerspectiveCommand;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.menu.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Replace some of this class methods by use of UF MenusFactory methods?
@ApplicationScoped
public class MenuHandler {

    @Inject
    private PlaceManager placeManager;

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;
    
    private Menus menus;
    
    public Menus buildEmptyEditableMenusModel() {
        final List<MenuItem> items = new ArrayList<MenuItem>();
        return buildEditableMenusModel(items);
    }
    
    public static Menus buildEditableMenusModel(final List<MenuItem> items) {
        return new Menus() {

            private List<MenuItem> menuItems = null;

            @Override
            public List<MenuItem> getItems() {
                if (menuItems == null) menuItems = items;
                return menuItems;
            }

            @Override
            public Map<Object, MenuItem> getItemsMap() {
                if (menuItems == null) return null;

                return new HashMap<Object, MenuItem>() {{
                    for ( final MenuItem menuItem : menuItems ) {
                        put( menuItem, menuItem );
                    }
                }};
            }

            @Override
            public void accept(MenuVisitor visitor) {
                if (menuItems != null && visitor.visitEnter( this ) ) {
                    for ( MenuItem item : menuItems ) {
                        item.accept( visitor );
                    }
                    visitor.visitLeave( this );
                }
            }
        };
    }

    public static boolean isMenuGroup(final MenuItem item) {
        if (item == null) return false;

        try {
            final MenuGroup g = (MenuGroup) item;
            return true;
        } catch (final ClassCastException e) {
            return false;
        }
    }

    public MenuItem createMenuItemCommand(final String name, final String activityId) throws MenuSecurityException {
        final MenuItem item = MenuFactory.newSimpleItem(name).respondsWith(new GoToPerspectiveCommand(placeManager, activityId)).endMenu().build().getItems().get(0);
        failIfNotHavePermissionToMakeThis(item);
        return item;        
    }

    public MenuItem createMenuItemGroup(final String name) throws MenuSecurityException {
        final MenuItem item = createMenuItemGroup(name, null);
        failIfNotHavePermissionToMakeThis(item);
        return item;
    }
    
    public MenuItem createMenuItemGroup(final String name, final List<? extends MenuItem> items) throws MenuSecurityException {
        if (name == null || name.trim().length() == 0) return null;
        
        if (items == null || items.isEmpty()) {
            final List<MenuItem> l = new ArrayList<MenuItem>();
            // TODO: Get default perspective activity.
            final MenuItem emptyItem = createMenuItemCommand("Edit me..", "HomePerspective");
            failIfNotHavePermissionToMakeThis(emptyItem);
            l.add(emptyItem);
            return MenuFactory.newSimpleItem(name).withItems(l).endMenu().build().getItems().get(0);
        }
        return MenuFactory.newSimpleItem(name).withItems(items).endMenu().build().getItems().get(0);
    }

    public boolean addItem(final MenuItem item) throws MenuSecurityException {
        failIfNotHavePermissionToMakeThis(item);
        return menus.getItems().add(item);
    }

    public MenuItem replaceItem(final String signatureId, final MenuItem newItem) throws MenuSecurityException {
        failIfNotHavePermissionToMakeThis(newItem);
        if (menus != null && signatureId != null && newItem != null) {
            final MenuItemLocation itemLocation = getItemLocation(menus, signatureId);
            if (itemLocation != null) {
                final MenuGroup parent = itemLocation.getParent();
                final MenuItem item = itemLocation.getItem();
                final int pos = itemLocation.getPosition();
                final List<MenuItem> items  = parent != null ? parent.getItems() : menus.getItems();
                MenuItem itemRemoved = items.remove(pos);
                items.add(pos, newItem);
                return itemRemoved;
            }
        }
        return null;
    }

    public boolean failIfNotHavePermissionToMakeThis(final MenuItem item) throws MenuSecurityException {
        if (notHavePermissionToMakeThis(item)) throw new MenuSecurityException(item);
        return true;
    }

    public boolean notHavePermissionToMakeThis(final MenuItem item) {
        return !authzManager.authorize( item, identity );
    }
    
    public MenuItem removeItem(final String signatureId) throws AbstractMenuException {
        if (menus != null && signatureId != null) {
            final MenuItemLocation itemLocation = getItemLocation(menus, signatureId);
            return removeItem(itemLocation);
        }
        return null;
    }

    public MenuItem removeItem(final MenuItemLocation itemLocation) throws AbstractMenuException {
        if (menus != null && itemLocation != null) {
            final MenuGroup parent = itemLocation.getParent();
            final MenuItem item = itemLocation.getItem();
            final int pos = itemLocation.getPosition();
            final List<MenuItem> items  = parent != null ? parent.getItems() : menus.getItems();
            // Force a minimun of one item existance in a menu group.
            if (parent != null && items.size() == 1) throw new MenuGroupEmptyException(item);

            // Remove the item.
            MenuItem itemRemoved = items.remove(pos);
            return itemRemoved;
        }
        return null;
    }

    public boolean moveItem(final String signatureId, final String targetSignatureId, final boolean before) throws AbstractMenuException {
        final MenuItemLocation sourceItemLocation = getItemLocation(menus, signatureId);
        if (sourceItemLocation == null || sourceItemLocation.getItem() == null) throw new MenuItemNotFoundException(signatureId);
        final MenuItemLocation targetItemLocation = getItemLocation(menus, targetSignatureId);
        if (targetItemLocation == null || targetItemLocation.getItem() == null) throw new MenuItemNotFoundException(targetSignatureId);

        final boolean areSameParent = areSameParent(sourceItemLocation, targetItemLocation);
        final int tPos = before ? targetItemLocation.getPosition() : targetItemLocation.getPosition() + 1;
        if (areSameParent && sourceItemLocation.getPosition() == tPos) {
            return false;
        }
        
        final boolean isSourceMenuGroup = isMenuGroup(sourceItemLocation.getItem());
        final boolean isTargetMenuGroup = isMenuGroup(targetItemLocation.getParent());
        if (isSourceMenuGroup && isTargetMenuGroup) throw new MenuGroupSingleLevelException(sourceItemLocation.getItem());
        final MenuItem removedItem = removeItem(sourceItemLocation);
        if (removedItem != null) {
            final MenuItemLocation itemLocation = getItemLocation(menus, targetSignatureId);
            final MenuGroup parent = itemLocation.getParent();
            final int position = itemLocation.getPosition();
            final List<MenuItem> items  = parent != null ? parent.getItems() : menus.getItems();
            final int targetPosition = before ? position : position + 1;
            items.add(targetPosition, removedItem);
            return true;
        }
        return false;
    }

    
    public interface MenuItemLocation {
        MenuItem getItem();
        MenuGroup getParent();
        int getPosition();
    }
    
    public static boolean areSameParent(final MenuItemLocation location1, final MenuItemLocation location2) {
        return ( (location1.getParent() == null && location2.getParent() == null) ||
                (location1.getParent() != null && location2.getParent() != null && 
                        location1.getParent().getSignatureId().equals(location2.getParent().getSignatureId())) );
    }

    public static MenuItemLocation getItemLocation(final Menus menu, final String signatureId) {
        if (menu != null) {
            final List<MenuItem> items = menu.getItems();
            if (items != null) {
                int pos = 0;
                for (final MenuItem i : items) {
                    final MenuItemLocation result = getItemLocation(null, pos, i, signatureId);
                    if (result != null) {
                        return result;
                    }
                    pos++;
                }
            }
        }

        return null;
    }

    private static MenuItemLocation getItemLocation(final MenuGroup parent, final int position, final MenuItem item, final String signatureId) {
        if (item != null && item.getSignatureId().equals(signatureId)) {
            return new MenuItemLocation() {
                @Override
                public MenuItem getItem() {
                    return item;
                }

                @Override
                public MenuGroup getParent() {
                    return parent;
                }

                @Override
                public int getPosition() {
                    return position;
                }
            };
        } else if (item != null) {
            try {
                final MenuGroup menuGroup = (MenuGroup) item;
                final List<MenuItem> items = menuGroup.getItems();
                if (items != null) {
                    int pos = 0;
                    for (final MenuItem i : items) {
                        final MenuItemLocation result = getItemLocation(menuGroup, pos, i, signatureId);
                        if (result != null) return result;
                        pos++;
                    }
                }
            } catch (ClassCastException e) {
                // Not a menu group.
            }
        }
        
        return null;
    }

    public Menus getMenus() {
        return menus;
    }

    public MenuHandler setMenus(final Menus menus) {
        this.menus = menus;
        return this;
    }
}
