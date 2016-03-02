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

import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.Query;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;

import java.util.List;

/**
 * <p>Builds an ElasticSearch query from a collection of DataSetFilter instances and DataSetGroup selection filters.</p>
 * <p>The resulting query structure is very important for searching using best performance as possible. So this interface allows to generate another query structure than the default provided one.</p>
 */
public interface ElasticSearchQueryBuilder<T extends ElasticSearchQueryBuilder> {

    /**
     * Set the dataset metadata.
     * @param metadata The dataset metadata.
     */
    T metadata(DataSetMetadata metadata);

    /**
     * Group operations can contain interval selections to filter that must be added into the resulting query too.
     * 
     * @param groups The group operations.
     */
    T groupInterval(List<DataSetGroup> groups);
    
    /**
     * Add filters to build the query.
     * @param filters The filter operations.
     */
    T filter(List<DataSetFilter> filters);

    /**
     * Build the resuling query and/or filters.
     */
    Query build();
}
