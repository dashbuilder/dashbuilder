package org.dashbuilder.client.menu;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.dashbuilder.client.menu.widgets.MenuComponent;
import org.dashbuilder.client.resources.i18n.MenusConstants;
import org.dashbuilder.shared.mvp.command.GoToPerspectiveCommand;
import org.uberfire.workbench.model.menu.MenuGroup;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.MenuItemCommand;
import org.uberfire.workbench.model.menu.Menus;

import java.util.Collection;

public class ClientMenuUtils {

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
    
    public static void doDebugLog(final String log) {
        // GWT.log(log);
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
            doDebugLog(separator + "Menu Item");
            doDebugLog(separator + "*********");
            doDebugLog(separator + " Caption: " + caption);
            doDebugLog(separator + " Contrib poing: " + contributionPoint);
            doDebugLog(separator + " Order: " + order);
            doDebugLog(separator + " signatureId: " + signatureId);
            try {
                final MenuItemCommand itemCommand = (MenuItemCommand) item;
                final GoToPerspectiveCommand goToPerspectiveCommand = (GoToPerspectiveCommand) itemCommand.getCommand();
                final String activityId = goToPerspectiveCommand.getActivityId();
                doDebugLog(separator + " activityId: " + activityId);
            } catch (ClassCastException e) {
            }
            if (traits != null) {
                final StringBuilder t = new StringBuilder();
                for (final String trait : traits) {
                    t.append(trait).append(", ");
                }
                doDebugLog(separator + " Traits: " + t.toString());
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
