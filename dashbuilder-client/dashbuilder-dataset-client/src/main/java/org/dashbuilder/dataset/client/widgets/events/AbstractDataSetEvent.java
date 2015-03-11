package org.dashbuilder.dataset.client.widgets.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public abstract class AbstractDataSetEvent<H extends EventHandler> extends GwtEvent<H> {
    private String uuid;

    public AbstractDataSetEvent(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
