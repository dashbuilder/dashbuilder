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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.model;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.sort.DataSetSort;

import java.util.LinkedList;
import java.util.List;

/**
 * * Search request.
 */
public class SearchRequest {

    private DataSetMetadata metadata;
    private List<DataColumn> columns = new LinkedList<DataColumn>();
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

    public List<DataColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DataColumn> columns) {
        this.columns = columns;
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
