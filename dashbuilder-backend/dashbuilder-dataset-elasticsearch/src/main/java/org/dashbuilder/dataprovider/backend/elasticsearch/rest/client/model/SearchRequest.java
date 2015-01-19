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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model;

import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * * Search request.
 */
public class SearchRequest {

    private DataSetMetadata metadata;
    private String[] indexes;
    private String[] types;
    private String[] fields;
    private List<DataSetGroup> aggregations = new LinkedList<DataSetGroup>();
    /* The query is the filter representation for ElasticSearch queries and filters from a collection of DataSetFilter instances, including aggregations. */
    private Query query;
    private List<DataSetSort> sorting = new LinkedList<DataSetSort>();
    private int start = 0;
    private int size = 50;

    public SearchRequest(DataSetMetadata metadata) {
        this.metadata = metadata;
    }

    public DataSetMetadata getMetadata() {
        return metadata;
    }

    public String[] getIndexes() {
        return indexes;
    }

    public void setIndexes(String[] indexes) {
        this.indexes = indexes;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public List<DataSetGroup> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<DataSetGroup> aggregations) {
        this.aggregations = aggregations;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public List<DataSetSort> getSorting() {
        return sorting;
    }

    public void setSorting(List<DataSetSort> sorting) {
        this.sorting = sorting;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
