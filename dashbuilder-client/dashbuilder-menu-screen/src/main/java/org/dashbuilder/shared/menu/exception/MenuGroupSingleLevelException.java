package org.dashbuilder.shared.menu.exception;

import org.uberfire.workbench.model.menu.MenuItem;

public class MenuGroupSingleLevelException extends AbstractMenuException {

    public MenuGroupSingleLevelException(MenuItem item) {
        super(item);
    }

}
