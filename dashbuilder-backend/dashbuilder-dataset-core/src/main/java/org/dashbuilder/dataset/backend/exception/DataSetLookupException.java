package org.dashbuilder.dataset.backend.exception;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.io.Serializable;

/**
 * <p>Default exception when a data set lookup fails.</p>
 * 
 * @since 0.3.0 
 */
@Portable
public class DataSetLookupException extends RuntimeException implements Serializable {

    private String uuid;

    public DataSetLookupException() {
        
    }
    
    public DataSetLookupException(final String uuid) {
        this.uuid = uuid;
    }

    public DataSetLookupException(final String uuid, final String message ) {
        super( message );
        this.uuid = uuid;
    }

    public DataSetLookupException(final String uuid, final String message, Exception e ) {
        super( message, e );
        this.uuid = uuid;
    }

}
