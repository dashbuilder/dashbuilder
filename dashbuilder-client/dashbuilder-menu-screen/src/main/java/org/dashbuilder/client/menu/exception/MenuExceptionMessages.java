package org.dashbuilder.client.menu.exception;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.dashbuilder.client.resources.i18n.MenusConstants;
import org.dashbuilder.shared.menu.exception.AbstractMenuException;
import org.dashbuilder.shared.menu.exception.MenuGroupEmptyException;
import org.dashbuilder.shared.menu.exception.MenuGroupSingleLevelException;
import org.dashbuilder.shared.menu.exception.MenuItemNotFoundException;
import org.uberfire.workbench.model.menu.MenuItem;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MenuExceptionMessages {

    public String getMessage(final AbstractMenuException exception) {
        final MenuItem item = exception.getItem();
        final SafeHtmlBuilder m = new SafeHtmlBuilder()
                .appendEscaped(MenusConstants.INSTANCE.opFailed()).appendEscaped(" - ")
                .appendEscaped(getMenuMessage(exception));
        if (item != null ) {
            m.appendEscaped(" --").appendEscaped(exception.getCaption()).appendEscaped("--");
        }
        return m.toSafeHtml().asString();
    }
    
    protected String getMenuMessage(final AbstractMenuException exception) {
        if (exception instanceof MenuGroupEmptyException) {
            return MenusConstants.INSTANCE.menuGroupEmptyError();
        } else if (exception instanceof MenuGroupSingleLevelException) {
            return MenusConstants.INSTANCE.menuGroupSingleLevelError();
        } else if (exception instanceof MenuItemNotFoundException) {
            return MenusConstants.INSTANCE.menuGroupEmptyError() + " --" + exception.getItemUUID() + "--";
        }
        return MenusConstants.INSTANCE.opFailed();
    }
    
}
