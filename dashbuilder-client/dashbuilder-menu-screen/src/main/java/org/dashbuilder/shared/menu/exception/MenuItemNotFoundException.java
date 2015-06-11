package org.dashbuilder.shared.menu.exception;

import org.uberfire.workbench.model.menu.MenuItem;

public class MenuItemNotFoundException extends AbstractMenuException {

    private String itemUUID;
    
    public MenuItemNotFoundException(MenuItem item) {
        super(item);
        this.itemUUID = getItemUUID();
    }

    public MenuItemNotFoundException(final String itemUUID) {
        super(null);
        this.itemUUID = itemUUID;
    }

}
