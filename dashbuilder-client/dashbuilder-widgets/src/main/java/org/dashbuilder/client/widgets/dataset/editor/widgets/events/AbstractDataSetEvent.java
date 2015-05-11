package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @param <H> handler type
 * @since 0.3.0
 */
public abstract class AbstractDataSetEvent<H extends EventHandler> extends GwtEvent<H> {
    private String uuid;

    public AbstractDataSetEvent(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
