package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import org.dashbuilder.dataset.def.DataSetDef;
import org.uberfire.workbench.events.UberFireEvent;

public class DataSetExploredErrorEvent implements UberFireEvent {

    private final String uuid;
    private final String message;
    private final String cause;

    public DataSetExploredErrorEvent(String cause, String message, String uuid) {
        this.cause = cause;
        this.message = message;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMessage() {
        return message;
    }

    public String getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "DataSetErrorEvent [UUID=" + uuid + "] [Message=" + message + "] [Cause=" + cause + "]";
    }

}
