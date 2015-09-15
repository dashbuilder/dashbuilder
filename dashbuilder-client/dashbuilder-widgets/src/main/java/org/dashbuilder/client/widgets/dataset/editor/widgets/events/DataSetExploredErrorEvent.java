package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.def.DataSetDef;
import org.uberfire.workbench.events.UberFireEvent;

public class DataSetExploredErrorEvent implements UberFireEvent {

    private String uuid;
    private String message;
    private String cause;
    private ClientRuntimeError clientRuntimeError;

    public DataSetExploredErrorEvent(ClientRuntimeError clientRuntimeError, String uuid) {
        this.uuid = uuid;
        this.clientRuntimeError = clientRuntimeError;
    }

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

    public ClientRuntimeError getClientRuntimeError() {
        return clientRuntimeError;
    }

    @Override
    public String toString() {
        if (clientRuntimeError != null) return "DataSetErrorEvent [UUID=" + uuid + "] [Client Runtime Error Message=" + clientRuntimeError.getMessage() + "]";
        else return "DataSetErrorEvent [UUID=" + uuid + "] [Message=" + message + "] [Cause=" + cause + "]";
    }

}
