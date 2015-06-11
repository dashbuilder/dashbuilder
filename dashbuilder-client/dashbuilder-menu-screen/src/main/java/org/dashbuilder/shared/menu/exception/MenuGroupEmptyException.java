package org.dashbuilder.shared.menu.exception;

import org.uberfire.workbench.model.menu.MenuItem;

public class MenuGroupEmptyException extends AbstractMenuException {

    public MenuGroupEmptyException(MenuItem item) {
        super(item);
    }

}
