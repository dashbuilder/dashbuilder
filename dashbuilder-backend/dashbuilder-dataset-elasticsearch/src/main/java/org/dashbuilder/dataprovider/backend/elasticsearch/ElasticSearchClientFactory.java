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
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.ElasticSearchNativeClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.IntervalBuilderDynamicDate;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;

public class ElasticSearchClientFactory {

    private final ElasticSearchValueTypeMapper valueTypeMapper;
    private final IntervalBuilderDynamicDate intervalBuilderDynamicDate;
    private final ElasticSearchUtils utils;

    public ElasticSearchClientFactory(ElasticSearchValueTypeMapper valueTypeMapper,
                                      IntervalBuilderDynamicDate intervalBuilderDynamicDate, 
                                      ElasticSearchUtils utils) {
        this.valueTypeMapper = valueTypeMapper;
        this.intervalBuilderDynamicDate = intervalBuilderDynamicDate;
        this.utils = utils;
    }

    public ElasticSearchClient newClient(ElasticSearchDataSetDef elasticSearchDataSetDef) {
        ElasticSearchClient client = newClient();
        return configure(client, elasticSearchDataSetDef);
    }

    private ElasticSearchClient newClient() {
        return newNativeClient();
    }
    
    private ElasticSearchClient newNativeClient() {
        return new ElasticSearchNativeClient( this, valueTypeMapper, intervalBuilderDynamicDate, utils );
    }
    
    private static ElasticSearchClient configure( ElasticSearchClient client, 
                                                 ElasticSearchDataSetDef elasticSearchDataSetDef ) {
        String serverURL = elasticSearchDataSetDef.getServerURL();
        String clusterName = elasticSearchDataSetDef.getClusterName();
        if (serverURL == null || serverURL.trim().length() == 0) throw new IllegalArgumentException("Server URL is not set.");
        if (clusterName == null || clusterName.trim().length() == 0) throw new IllegalArgumentException("Cluster name is not set.");

        client.serverURL(serverURL).clusterName(clusterName);

        String[] indexes = ElasticSearchDataSetProvider.fromString(elasticSearchDataSetDef.getIndex());
        if (indexes != null && indexes.length > 0) client.index(indexes);
        String[] types  = ElasticSearchDataSetProvider.fromString(elasticSearchDataSetDef.getType());
        if (types != null && types.length > 0) client.type(types);
        
        return client;
    }
    
}
