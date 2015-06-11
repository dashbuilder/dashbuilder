package org.dashbuilder.shared.menu.exception;

import org.uberfire.workbench.model.menu.MenuItem;

public abstract class AbstractMenuException extends Exception {
    private MenuItem item;

    public AbstractMenuException(final MenuItem item) {
        super();
        this.item = item;
    }

    public MenuItem getItem() {
        return item;
    }
    
    public String getItemUUID() {
        return item != null ? item.getSignatureId() : "";
    }

    public String getCaption() {
        return item != null ? item.getCaption() : "";
    }

}
