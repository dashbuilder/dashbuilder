package org.dashbuilder.dataset.backend.exception;

import java.io.Serializable;

/**
 * <p>Root of all portable Exceptions resulting from server-side errors that need to be sent to the client.</p> 
 * @since 0.3.0
 */
public class GenericPortableException extends RuntimeException implements Serializable {

    public GenericPortableException() {
    }

    public GenericPortableException( final String message ) {
        super( message );
    }

    public GenericPortableException( final String message, Exception e ) {
        super( message, e );
    }

}
