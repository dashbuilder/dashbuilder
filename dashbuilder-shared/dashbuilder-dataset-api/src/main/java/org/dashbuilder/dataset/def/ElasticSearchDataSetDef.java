/**
 * Copyright (C) 2014 JBoss Inc
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
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.errai.common.client.api.annotations.Portable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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
@Portable
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
    @NotNull(message = "{dataSetApi_elDataSetDef_serverURL_notNull}")
    @NotEmpty(message = "{dataSetApi_elDataSetDef_serverURL_notNull}")
    protected String serverURL;

    protected String clusterName;

    @NotNull(message = "{dataSetApi_elDataSetDef_index_notNull}")
    protected List<String> index;

    @NotNull(message = "{dataSetApi_elDataSetDef_type_notNull}")
    protected List<String> type;

    protected String query;
    protected String relevance;
    protected ColumnSort columnSort;
    protected boolean cacheEnabled = false;
    protected Integer cacheMaxRows = 1000;
    protected boolean cacheSynced = false;
    protected boolean allColumnsEnabled = true;

    public ElasticSearchDataSetDef() {
        super.setProvider(DataSetProviderType.ELASTICSEARCH);
        index = new ArrayList<String>();
        type = new ArrayList<String>();
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

    public boolean addIndex(String index) {
        return this.index.add(index);
    }

    public boolean addType(String type) {
        return this.type.add(type);
    }

    /**
     * <p>Returns the index/es specified by dataset user parameters.</p>
     * <p>If not specified, returns <code>ElasticSearchKeywords.ALL</code></p>
     *
     * @return The index/es to use. If not specified, returns <code>ElasticSearchKeywords.ALL</code>
     */
    public String[] getIndex() {
        return index.size() > 0 ? index.toArray(new String[index.size()]) : new String[] { ElasticSearchKeywords.ALL.toString() };
    }

    public void setIndex(List<String> index) {
        this.index = index;
    }

    /**
     * <p>Returns the type/s specified by dataset user parameters.</p>
     * <p>If not specified, returns <code>null</code></p>
     *
     * @return The type/s to use. If not specified, returns <code>null</code>
     */
    public String[] getType() {
        return type.size() > 0 ? type.toArray(new String[type.size()]) : null;
    }

    public void setType(List<String> type) {
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

    public boolean isCacheSynced() {
        return cacheSynced;
    }

    public void setCacheSynced(boolean cacheSynced) {
        this.cacheSynced = cacheSynced;
    }

    public ColumnSort getColumnSort() {
        return columnSort;
    }

    public void setColumnSort(ColumnSort columnSort) {
        this.columnSort = columnSort;
    }

    public boolean isAllColumnsEnabled() {
        return allColumnsEnabled;
    }

    public void setAllColumnsEnabled(boolean allColumnsEnabled) {
        this.allColumnsEnabled = allColumnsEnabled;
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
        out.append("Cache synced=").append(cacheSynced).append("\n");
        return out.toString();
    }
}
