package org.dashbuilder.shared.menu.exception;

import org.uberfire.workbench.model.menu.MenuItem;

public class MenuSecurityException extends AbstractMenuException {

    public MenuSecurityException(MenuItem item) {
        super(item);
    }

}
