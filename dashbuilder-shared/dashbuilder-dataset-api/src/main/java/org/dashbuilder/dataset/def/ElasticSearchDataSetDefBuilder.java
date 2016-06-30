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
package org.dashbuilder.dataset.def;

/**
 * A builder for defining ElasticSearch data sets
 *
 * <pre>
 *    DataSetDef dataSetDef = DataSetDefFactory.newElasticSearchDataSetDef()
 *     .uuid("all_employees")
 *     .serverURL("localhost:9300")
 *     .index("index1")
 *     .type("type1")
 *     .query("DSL query here")
 *     .relevance("10")
 *     .buildDef();
 * </pre>
 */
public interface ElasticSearchDataSetDefBuilder<T extends DataSetDefBuilder> extends DataSetDefBuilder<T> {

    /**
     * <p>Set the ElasticSearch server instance URL.</p>
     *
     * @param serverURL The URL for the ElasticSearch server instance.
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T serverURL(String serverURL);

    /**
     * <p>Set the ElasticSearch cluster name.</p>
     *
     * @param clusterName The cluster name.
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T clusterName(String clusterName);

    /**
     * <p>Set the name of the index that this dataset will handle.</p>
     * <p>This DataSet definition supports working with multiple indexes, so this method can be invoked several times.</p>
     * 
     * @param index The index name
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T index(String index);

    /**
     * <p>Set the type of the given index/es that this dataset will handle.</p>
     * <p>This DataSet definition supports working with multiple types, so this method can be invoked several times.</p>
     * <p>If there is no <code>index</code> configured, the type is skipped.</p>
     * 
     * @param type The type of the given index/es
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T type(String type);

    /**
     * <p>Set a custom query to extract the data from ElasticSearch server, instead of manually setting <code>index</code> and <code>type</code> parameters.</p>
     * <p>This parameter is only used if there are no <code>index</code> and <code>type</code> parameters already configured.</p>
     *
     * @param query The DSL query to perform for data extraction.
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T query(String query);

    /**
     * <p>Set the minimum relevance value for resulting search documents.</p>
     * <p>If a document relevance value is less than the configured using this method, the document will be present in the dataset.</p>
     *
     * @param relevance The minimum relevance value
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T relevance(String relevance);
    
    /**
     * Enables the static cache
     *
     * @param synced For keeping the cache synced with the remote database content. If false, the cached content will get never updated once read.
     * @param maxRowsInCache Max. rows the cache is able to handle. For higher values the cache is automatically disabled.
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T cacheOn(boolean synced, int maxRowsInCache);
}
