/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataset.json;

import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.json.JsonObject;

import static org.dashbuilder.dataset.json.DataSetDefJSONMarshaller.*;

public class ELSDefJSONMarshaller implements DataSetDefJSONMarshallerExt<ElasticSearchDataSetDef> {

    public static ELSDefJSONMarshaller INSTANCE = new ELSDefJSONMarshaller();

    public static final String SERVER_URL = "serverURL";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String INDEX = "index";
    public static final String TYPE = "type";
    public static final String QUERY = "query";
    public static final String RELEVANCE = "relevance";
    public static final String CACHE_SYNCED = "cacheSynced";

    @Override
    public void fromJson(ElasticSearchDataSetDef dataSetDef, JsonObject json) {
        String serverURL = json.getString(SERVER_URL);
        String clusterName = json.getString(CLUSTER_NAME);
        String index = json.getString(INDEX);
        String type = json.getString(TYPE);
        String query = json.getString(QUERY);
        String relevance = json.getString(RELEVANCE);
        String cacheEnabled = json.getString(CACHE_ENABLED);
        String cacheMaxRows = json.getString(CACHE_MAXROWS);
        String cacheSynced = json.getString(CACHE_SYNCED);

        // ServerURL parameter.
        if (isBlank(serverURL)) {
            throw new IllegalArgumentException("The serverURL property is missing.");
        } else {
            dataSetDef.setServerURL(serverURL);
        }

        // Cluster name parameter.
        if (isBlank(clusterName)) {
            throw new IllegalArgumentException("The clusterName property is missing.");
        } else {
            dataSetDef.setClusterName(clusterName);
        }

        // Index parameter
        if (isBlank(index)) {
            throw new IllegalArgumentException("The index property is missing.");
        } else {
            dataSetDef.setIndex(index);
        }

        // Type parameter.
        if (!isBlank(type)) {
            dataSetDef.setType(type);
        }

        // Query parameter.
        if (!isBlank(query)) {
            dataSetDef.setQuery(query);
        }

        // Relevance parameter.
        if (!isBlank(relevance)) {
            dataSetDef.setRelevance(relevance);
        }

        // Cache enabled parameter.
        if (!isBlank(cacheEnabled)) {
            dataSetDef.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));
        }

        // Cache max rows parameter.
        if (!isBlank(cacheMaxRows)) {
            dataSetDef.setCacheMaxRows(Integer.parseInt(cacheMaxRows));
        }
    }

    @Override
    public void toJson(ElasticSearchDataSetDef dataSetDef, JsonObject json) {
        // Server URL.
        json.put(SERVER_URL, dataSetDef.getServerURL());

        // Cluster name.
        json.put(CLUSTER_NAME, dataSetDef.getClusterName());

        // Index.
        json.put(INDEX, dataSetDef.getIndex());

        // Type.
        json.put(TYPE, dataSetDef.getType());

        // All columns flag.
        json.put(ALL_COLUMNS, dataSetDef.isAllColumnsEnabled());
    }
}
