package org.dashbuilder.client.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.dashbuilder.client.menu.widgets.MenuComponent;
import org.dashbuilder.client.mvp.command.GoToPerspectiveCommand;
import org.dashbuilder.client.resources.i18n.MenusConstants;
import org.uberfire.workbench.model.menu.*;

import java.util.*;

// TODO: Replace some of this class methods by use of UF MenusFactory methods?
public class MenuUtils {

    public static Menus buildEmptyEditableMenusModel() {
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
    
    public static MenuItem createMenuItemCommand(final String name, final String activityId) {
        return MenuFactory.newSimpleItem(name).respondsWith(new GoToPerspectiveCommand(activityId)).endMenu().build().getItems().get(0);
    }

    public static MenuItem createMenuItemGroup(final String name) {
        return createMenuItemGroup(name, null);
    }
    
    public static MenuItem createMenuItemGroup(final String name, final List<? extends MenuItem> items) {
        if (name == null || name.trim().length() == 0) return null;
        
        if (items == null || items.isEmpty()) {
            final List<MenuItem> l = new ArrayList<MenuItem>();
            // TODO: Get default perspective activity.
            final MenuItem emptyItem = createMenuItemCommand(MenusConstants.INSTANCE.editMe(), "HomePerspective");
            l.add(emptyItem);
            return MenuFactory.newSimpleItem(name).withItems(l).endMenu().build().getItems().get(0);
        }
        return MenuFactory.newSimpleItem(name).withItems(items).endMenu().build().getItems().get(0);
    }

    public static boolean addItem(final Menus menu, final MenuItem item) {
        return menu.getItems().add(item);
    }
    
    public static MenuItem removeItem(final Menus menu, final String signatureId) {
        if (menu != null && signatureId != null) {
            final MenuItemLocation itemLocation = getItemLocation(menu, signatureId);
            if (itemLocation != null) {
                final MenuGroup parent = itemLocation.getParent();
                final MenuItem item = itemLocation.getItem();
                final int pos = itemLocation.getPosition();
                final List<MenuItem> items  = parent != null ? parent.getItems() : menu.getItems();
                MenuItem itemRemoved = items.remove(pos);
                return itemRemoved;
            }
        }
        return null;
    }

    public static boolean moveItem(final Menus menu, final String signatureId, final String targetSignatureId, final boolean before) {
        GWT.log("Removing '" + signatureId + "' from parent");
        final MenuItem removedItem = removeItem(menu, signatureId);
        if (removedItem != null && targetSignatureId != null) {
            final MenuItemLocation itemLocation = getItemLocation(menu, targetSignatureId);
            if (itemLocation != null) {
                final MenuGroup parent = itemLocation.getParent();
                final String parentSignatureId = parent != null ? parent.getSignatureId() : "root";
                final int position = itemLocation.getPosition();
                final List<MenuItem> items  = parent != null ? parent.getItems() : menu.getItems();
                final int targetPosition = before ? position : position + 1;
                GWT.log("Adding '" + signatureId + "' to '" + parentSignatureId + "' / position: '" + targetPosition + "' / before '" + before + "'");
                items.add(targetPosition, removedItem);
                return true;
            }
        }
        return false;
    }

    public interface MenuItemLocation {
        MenuItem getItem();
        MenuGroup getParent();
        int getPosition();
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
    
    public static SafeHtml getItemTypeName(final MenuComponent.MenuItemTypes type) {
        if (type != null) {
            switch (type) {
                case COMMAND:
                    return new SafeHtmlBuilder().appendEscaped(MenusConstants.INSTANCE.menuItemTypeCommand()).toSafeHtml();
                case GROUP:
                    return new SafeHtmlBuilder().appendEscaped(MenusConstants.INSTANCE.menuItemTypeGroup()).toSafeHtml();
            }
        }
        return null;
    }
    
    public static void logMenus(final Menus menu) {
        if (menu != null) {
            for (final MenuItem item : menu.getItems()) {
                log(item, "");
            }
        }
    }

    public static void log(final MenuItem item, final String separator) {
        if (item != null) {
            final String caption = item.getCaption();
            final String contributionPoint = item.getContributionPoint();
            final int order = item.getOrder();
            final String signatureId = item.getSignatureId();
            Collection<String> traits = item.getTraits();
            GWT.log(separator + "Menu Item");
            GWT.log(separator + "*********");
            GWT.log(separator + " Caption: " + caption);
            GWT.log(separator + " Contrib poing: " + contributionPoint);
            GWT.log(separator + " Order: " + order);
            GWT.log(separator + " signatureId: " + signatureId);
            try {
                final MenuItemCommand itemCommand = (MenuItemCommand) item;
                final GoToPerspectiveCommand goToPerspectiveCommand = (GoToPerspectiveCommand) itemCommand.getCommand();
                final String activityId = goToPerspectiveCommand.getActivityId();
                GWT.log(separator + " activityId: " + activityId);
            } catch (ClassCastException e) {
            }
            if (traits != null) {
                final StringBuilder t = new StringBuilder();
                for (final String trait : traits) {
                    t.append(trait).append(", ");
                }
                GWT.log(separator + " Traits: " + t.toString());
            }
            if (item instanceof MenuGroup) {
                final MenuGroup group = (MenuGroup) item;
                if (group.getItems() != null) {
                    for (final MenuItem i : group.getItems()) {
                        log(i, separator + "--");
                    }
                }
            }
        }
    }
}
