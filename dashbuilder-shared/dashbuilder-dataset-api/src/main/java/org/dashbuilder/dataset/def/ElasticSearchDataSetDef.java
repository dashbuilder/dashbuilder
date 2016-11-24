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

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.validation.groups.ElasticSearchDataSetDefValidation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>DataSet definition class for ElasticSearch provider.</p>
 *
 * <p>This dataset provides these configuration parameters:</p>
 * <ul>
 *     <li>
 *         <code>serverURL</code> - The URL for the ElasticSearch server instance (MANDATORY)
 *     </li>
 *     <li>
 *         <code>clusterName</code> - The name of the cluster in the ElasticSearch server.
 *     </li>
 *     <li>
 *         <code>index</code> - The name of the index. It can be a concrete index name, a collection of index names, comma separated, or the keyword <code>_all</code> for working with all available indexes in the ElasticSearch server (OPTIONAL - Defaults to <code>_all</code>)
 *     </li>
 *     <li>
 *         <code>type</code> - The type name. Only applicable if <code>index</code> parameter is set. It can be a concrete type name, a collection of type names, comma separated, or the keyword <code>_all</code> for working with all available type in the ElasticSearch server (OPTIONAL - Defaults to <code>_all</code>)
 *     </li>
 *     <li>
 *         <code>query</code> - You can perform your custom ElasticSearch DSL query for this data provider. If this parameter exist, the parameters <code>index</code>, <code>type</code> and <code>field</code> are skipped. (OPTIONAL)
 *     </li>
 *     <li>
 *         <code>relevance</code> - The relevance value for search results (OPTIONAL)
 *     </li>
 *     <li>
 *         <code>columns</code> - If not specified, the column definitions for the ElasticSearch dataset are automatically given by querying the index mappings. Otherwise, you can bind a column to another datatype in dashbuilder application using this parameters (OPTIONAL)
 *     </li>
 * </ul>
 *
 * @since 0.3.0
 */
public class ElasticSearchDataSetDef extends DataSetDef {

    // Constants.

    public static enum ElasticSearchKeywords {
        ALL;

        private static final String KEYWORD_ALL = "_all";

        @Override
        public String toString() {
            if (this.equals(ALL)) return KEYWORD_ALL;
            return super.toString();
        }

    }

    // Data Set user parameters.
    @NotNull(groups = {ElasticSearchDataSetDefValidation.class})
    @Size(min = 1, groups = {ElasticSearchDataSetDefValidation.class})
    protected String serverURL;

    protected String clusterName;

    /**
     * Index/es to query. Can handle multiple values, comma separated.
     */
    @NotNull(groups = {ElasticSearchDataSetDefValidation.class})
    @Size(min = 1, groups = {ElasticSearchDataSetDefValidation.class})
    protected String index;

    /**
     * Type/es to query. Can handle multiple values, comma separated. Not mandatory.
     */
    protected String type;

    protected String query;
    protected String relevance;
    protected ColumnSort columnSort;

    public ElasticSearchDataSetDef() {
        super.setProvider(DataSetProviderType.ELASTICSEARCH);
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getRelevance() {
        return relevance;
    }

    public void setRelevance(String relevance) {
        this.relevance = relevance;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public Integer getCacheMaxRows() {
        return cacheMaxRows;
    }

    public void setCacheMaxRows(Integer cacheMaxRows) {
        this.cacheMaxRows = cacheMaxRows;
    }

    public ColumnSort getColumnSort() {
        return columnSort;
    }

    public void setColumnSort(ColumnSort columnSort) {
        this.columnSort = columnSort;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ElasticSearchDataSetDef other = (ElasticSearchDataSetDef) obj;
            if (!super.equals(other)) {
                return false;
            }
            if (serverURL != null && !serverURL.equals(other.serverURL)) {
                return false;
            }
            if (clusterName != null && !clusterName.equals(other.clusterName)) {
                return false;
            }
            if (index != null && !index.equals(other.index)) {
                return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public DataSetDef clone() {
        ElasticSearchDataSetDef def = new ElasticSearchDataSetDef();
        clone(def);
        def.setServerURL(getServerURL());
        def.setClusterName(getClusterName());
        def.setIndex(getIndex());
        def.setType(getType());
        return def;
    }
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("UUID=").append(UUID).append("\n");
        out.append("Provider=").append(provider).append("\n");
        out.append("Public=").append(isPublic).append("\n");
        out.append("Push enabled=").append(pushEnabled).append("\n");
        out.append("Push max size=").append(pushMaxSize).append(" Kb\n");
        out.append("Server URL=").append(serverURL).append("\n");
        out.append("Index=").append(index).append("\n");
        out.append("Type=").append(type).append("\n");
        out.append("Query=").append(query).append("\n");
        out.append("Get all columns=").append(allColumnsEnabled).append("\n");
        out.append("Cache enabled=").append(cacheEnabled).append("\n");
        out.append("Cache max rows=").append(cacheMaxRows).append(" Kb\n");
        return out.toString();
    }
}
