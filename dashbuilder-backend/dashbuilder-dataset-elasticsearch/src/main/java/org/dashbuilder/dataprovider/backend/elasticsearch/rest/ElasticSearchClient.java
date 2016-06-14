/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataprovider.backend.elasticsearch.rest;

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.CountResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.MappingsResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchRequest;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchResponse;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;

import java.io.Closeable;

/**
 * <p>This is the contract for a JBoss Dashbuilder REST client for ElasticSearch servers.</p>
 * <p>Default ElasticSearch native client implementation is provided by classes in package <code>org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl</code></p>
 * <p>You can implement your own client too.</p>
 * 
 */
public interface ElasticSearchClient<T extends ElasticSearchClient> extends Closeable {

    String HEADER_RESPONSE_CODE = "Status-Code";
    int RESPONSE_CODE_NOT_FOUND = 404;
    int RESPONSE_CODE_OK = 200;


    /**
     * Builds the client for a given serer URL.
     * @param serverURL The ElasticSearch server URL.
     * @return The REST client for the given server URL.
     */
    T serverURL(String serverURL);

    /**
     * Builds the client for a given index/es.
     * @param indexes The ElasticSearch index/es.
     * @return The REST client for the given index/es.
     */
    T index(String... indexes);

    /**
     * Builds the client for a given types/es.
     * @param types The ElasticSearch types/es.
     * @return The REST client for the given types/es.
     */
    T type(String... types);

    /**
     * Builds the client for a given cluster.
     * @param clusterName The ElasticSearch cluster name.
     * @return The REST client for the given cluster.
     */
    T clusterName(String clusterName);
    
    /**
     * Sets the timeout value for the HTTP rest client requests. 
     * @param timeout The timeout value in miliseconds.
     */
    T setTimeout(int timeout);
    
    /**
     * Obtain the mappings for a given index/es.
     * 
     * @param index The index/es to obtain the mappings.
     * @return The mappings for the given index/es
     */
    MappingsResponse getMappings(String... index) throws ElasticSearchClientGenericException;

    /**
     * Count documents for a given index/es and type/es
     * @param index The index/es for the document type to count. If value is <code>null</code> returns the count number of all documents if all indexes. 
     * @param type The type/s of the documents to count. If value is <code>null</code> returns the count number of all documents in all indexes or in the index specified by <code>index</code>.
     *
     * @return The number of documents for a given index/es and type/es
     */
    CountResponse count(String[] index, String[] type) throws ElasticSearchClientGenericException;

    /**
     * <p>Obtain documents for a given index/es and type/es</p>
     * <p>If the index/es value set in the <code>request</code> is <code>null</code> returns all documents of all indexes.</p>
     * <p>If the type/s value set in the <code>request</code> is <code>null</code> returns all documents in all indexes or in the index specified by <code>index</code>.</p>
     *
     * @param definition The dataset definition.
     * @param metadata The metadata.
     * @param searchRequest The search request.
     * @return The number of documents for a given index/es and type/es
     */
    SearchResponse search(ElasticSearchDataSetDef definition, DataSetMetadata metadata, SearchRequest searchRequest) throws ElasticSearchClientGenericException;
}
