/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.util;

import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.Query;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchRequest;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchResponse;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.elasticsearch.action.ActionResponse;

import java.util.*;

/**
 * @since 0.3.0
 */
public class ElasticSearchUtils {

    protected ElasticSearchValueTypeMapper valueTypeMapper;

    public ElasticSearchUtils(ElasticSearchValueTypeMapper valueTypeMapper) {
        this.valueTypeMapper = valueTypeMapper;
    }

    public static int getResponseCode(ActionResponse response) {
        if (response == null) return ElasticSearchClient.RESPONSE_CODE_NOT_FOUND;
        String responseCode = response.getHeader( ElasticSearchClient.HEADER_RESPONSE_CODE );

        if (responseCode == null) return ElasticSearchClient.RESPONSE_CODE_OK;
        return Integer.decode(responseCode);
    }
    
    /**
     * <p>Obtain the minimum date and maximum date values for the given column with identifier <code>dateColumnId</code>.</p>
     *
     * @param  client The client for performing the query to ELS
     * @param metadata The data set metadata
     * @param dateColumnId The column identifier for the date type column
     * @param query The query model, if any, for filtering the results
     * @return The minimum and maximum dates.
     */
    public Date[] calculateDateLimits(ElasticSearchClient client, DataSetMetadata metadata, String dateColumnId, Query query) throws ElasticSearchClientGenericException {

        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) metadata.getDefinition();

        // The resulting data set columns.
        String minDateColumnId = dateColumnId + "_min";
        String maxDateColumnId = dateColumnId + "_max";
        DataColumn minDateColumn = new DataColumnImpl(minDateColumnId, ColumnType.NUMBER);
        DataColumn maxDateColumn = new DataColumnImpl(maxDateColumnId, ColumnType.NUMBER);
        List<DataColumn> columns = new ArrayList<DataColumn>(2);
        columns.add(minDateColumn);
        columns.add(maxDateColumn);

        // Create the aggregation model to bulid the query to EL server.
        DataSetGroup aggregation = new DataSetGroup();
        GroupFunction minFunction = new GroupFunction(dateColumnId, minDateColumnId, AggregateFunctionType.MIN);
        GroupFunction maxFunction = new GroupFunction(dateColumnId, maxDateColumnId, AggregateFunctionType.MAX);
        aggregation.addGroupFunction(minFunction, maxFunction);

        SearchRequest request = new SearchRequest(metadata);
        request.setColumns(columns);
        request.setAggregations(Arrays.asList(aggregation));

        // Append the filter clauses
        if (query != null) request.setQuery(query);

        // Perform the query.
        SearchResponse searchResult = client.search(def, metadata, request);
        if (searchResult != null) {
            SearchHitResponse[] hits = searchResult.getHits();
            if (hits != null && hits.length == 1) {
                SearchHitResponse hit0 = hits[0];
                Map<String, Object> fields = hit0.getFields();
                if (fields != null && !fields.isEmpty()) {
                    Double min = (Double) fields.get(minDateColumnId);
                    Double max = (Double) fields.get(maxDateColumnId);
                    Date minValue = valueTypeMapper.parseDate(def, dateColumnId, min.longValue());
                    Date maxValue = valueTypeMapper.parseDate(def, dateColumnId, max.longValue());
                    return new Date[] {minValue, maxValue};
                }
            }
        }

        return null;
    }

    /**
     * ELS wildcard query characters replacement for Dashbuilder the LIKE core function ones:
     * -------------------------------------------------------
     * | ELS | Dashbuilder | Description                     |
     * | ?   |     _       | Matches any character           |
     * | *   |     %       | Matches zero or more characters |
     * -------------------------------------------------------
     *
     */
    public String transformPattern(String pattern) {
        if (pattern == null) {
            return null;
        }
        
        if (pattern.trim().length() > 0) {
            // Replace Dashbuilder wildcard characters by the ones used in ELS wildcard query.
            pattern = pattern.replace("%", "*");
            pattern = pattern.replace("_", "?");
        }
        
        return pattern;
    }
    
}
