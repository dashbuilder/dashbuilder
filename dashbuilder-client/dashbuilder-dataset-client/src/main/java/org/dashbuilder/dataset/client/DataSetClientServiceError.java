package org.dashbuilder.dataset.client;

import org.jboss.errai.bus.client.api.messaging.Message;

/**
 * <p>Default error produced by data set backend services. It encapsulates the underlying Errai technology.</p>
 * 
 * @since 0.3.0 
 */
public class DataSetClientServiceError {
    private Message errorMessage;
    private Throwable throwable;

    public DataSetClientServiceError(Message errorMessage, Throwable throwable) {
        this.errorMessage = errorMessage;
        this.throwable = throwable;
    }

    public Object getMessage() {
        return errorMessage;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
