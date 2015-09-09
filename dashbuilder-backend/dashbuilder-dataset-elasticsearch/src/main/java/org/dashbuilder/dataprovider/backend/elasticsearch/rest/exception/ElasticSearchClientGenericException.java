package org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception;

/**
 * Generic exception for errors in any ElasticSearchRestClient service.
 */
public class ElasticSearchClientGenericException extends Exception {

    public ElasticSearchClientGenericException() {
    }

    public ElasticSearchClientGenericException(String message) {
        super(message);
    }

    public ElasticSearchClientGenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticSearchClientGenericException(Throwable cause) {
        super(cause);
    }
}
