package org.dashbuilder.dataset.backend.exception;

import org.jboss.errai.config.rebind.EnvUtil;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * <p>Handles backend exceptions.</p>
 * 
 * @since 0.3.0 
 */
@ApplicationScoped
public class ExceptionManager {

    @Inject
    private Logger log;
    
    /**
     * <p>Return a <code>@Portable RuntimeException</code> that can be captured by client side widgets.</p>
     *  
     * @param e The exception that caused the error.
     * @return The portable exception to send to the client side.
     */
    public RuntimeException handleException(final Exception e) {
        log.error("An Exception has occurred and has been handled and sent to the client.", e);
        if (e instanceof RuntimeException && EnvUtil.isPortableType(e.getClass()) ) {
            return (RuntimeException) e;
        }
        return new GenericPortableException( e.getMessage(), e );
    }
}
