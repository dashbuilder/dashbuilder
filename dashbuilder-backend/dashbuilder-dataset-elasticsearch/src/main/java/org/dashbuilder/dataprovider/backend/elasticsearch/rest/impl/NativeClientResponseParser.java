/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl;

import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.EmptySearchResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;

import java.text.ParseException;
import java.util.*;


/**
 * Helper class for the ELS native client that parses the response from the ELS server and builds the resulting data set.
 *
 * @since 0.5.0
 */
public class NativeClientResponseParser {

    private final ElasticSearchValueTypeMapper valueTypeMapper;

    public NativeClientResponseParser(ElasticSearchValueTypeMapper valueTypeMapper) {
        this.valueTypeMapper = valueTypeMapper;
    }

    public SearchResponse parse(DataSetMetadata metadata,
                                               org.elasticsearch.action.search.SearchResponse response,
                                               List<DataColumn> columns ) throws ParseException {
        // Convert to rest client model.
        long tookInMilis = response.getTookInMillis();
        int responseCode = ElasticSearchUtils.getResponseCode(response);
        long totalHits = response.getHits().getTotalHits();
        float maxScore = response.getHits().getMaxScore();
        int totalShards = response.getTotalShards();
        int successfulShards = response.getSuccessfulShards();
        int shardFailures = response.getFailedShards();
        int hitCount = response.getHits().getHits().length;
        Aggregations aggregations = response.getAggregations();
        boolean existAggregations = aggregations != null && !aggregations.asList().isEmpty();

        // No results.
        if (hitCount == 0 && !existAggregations) return new EmptySearchResponse(tookInMilis, responseCode, totalHits, maxScore, totalShards, successfulShards, shardFailures);

        // There are results. Build the resulting dataset columns & values.
        List<SearchHitResponse> hits = new LinkedList<SearchHitResponse>();

        if (existAggregations) {

            // Build the response using the aggregated results.
            parseAggregationsResponse( metadata, hits, aggregations, columns );

        } else {

            // Build the response using original dataset columns, as no aggregation is present.
            parseHitsResponse( metadata, hits, response.getHits(), columns );

        }

        // Build the response model object.
        if ( hits.isEmpty() ) {

            return  new EmptySearchResponse(tookInMilis, responseCode, totalHits, maxScore, totalShards, successfulShards, shardFailures);

        } else {

            return new SearchResponse(tookInMilis, responseCode, totalHits, maxScore, totalShards, successfulShards, shardFailures, hits.toArray(new SearchHitResponse[hits.size()]));

        }

    }

    private void parseHitsResponse( DataSetMetadata metadata,
                                    List<SearchHitResponse> hits,
                                    SearchHits responseHits,
                                    List<DataColumn> columns ) throws ParseException {

        final SearchHit[] resultHits = responseHits.getHits();

        if ( null != resultHits ) {

            for (SearchHit searchHit : resultHits) {

                float score = searchHit.getScore();
                String id = searchHit.getId();
                String type = searchHit.getType();
                String index = searchHit.getIndex();
                long version = searchHit.getVersion();
                Map<String, Object> sourceAsMap = searchHit.getSource();
                Map<String, SearchHitField> sourceFields = searchHit.getFields();

                Map<String, Object> fields = new HashMap<String, Object>();

                if (null != sourceFields && !sourceFields.isEmpty()) {

                    // If there are some "fields" defined by user in the data set provider, use sourceFields to obtain the values.
                    for (Map.Entry<String, SearchHitField> entry : sourceFields.entrySet()) {
                        String fieldName = entry.getKey();
                        SearchHitField hitValue = entry.getValue();
                        if (hitValue != null) {
                            Object fieldValue = hitValue.getValue();

                            // Fill the values map.
                            fields.put(fieldName, fieldValue);
                        }
                    }

                } else if (null != sourceAsMap && !sourceAsMap.isEmpty()) {

                    // If there are no "fields" defined by user in the data set provider, obtain all fields, use sourceAsMap to obtain the values.
                    for (Map.Entry<String, Object> entry : sourceAsMap.entrySet()) {
                        String fieldName = entry.getKey();
                        Object fieldValue = entry.getValue();

                        // Fill the values map.
                        fields.put(fieldName, fieldValue);
                    }
                }

                SearchHitResponse hit = new SearchHitResponse(score, index, id, type, version, orderAndParseFields(metadata, fields, columns));
                hits.add(hit);

            }

        }

    }

    private void parseAggregationsResponse(DataSetMetadata metadata,
                                           List<SearchHitResponse> hits,
                                           Aggregations aggregations,
                                           List<DataColumn> columns) throws ParseException {

        parseAggregationsResponse( metadata, hits, aggregations, null, columns );

    }

    private void parseAggregationsResponse(DataSetMetadata metadata,
                                           List<SearchHitResponse> hits,
                                           Aggregations aggregations,
                                           SearchHitResponse sourceHit,
                                           List<DataColumn> columns) throws ParseException {


        Map<String, Aggregation> aggregationMap = aggregations.asMap();

        if ( null != aggregationMap && !aggregationMap.isEmpty() ) {

            Map<String, Object> fields = new HashMap<String, Object>();

            if ( null != sourceHit && null != sourceHit.getFields() ) {


                fields = sourceHit.getFields();
            }

            for (Aggregation aggregation : aggregations.asList()) {

                Object value = null;

                // MultiBucketsAggregation
                if (aggregation instanceof StringTerms) {

                    StringTerms agg = (StringTerms) aggregation;

                    Collection<Terms.Bucket> buckets = agg.getBuckets();
                    if ( buckets != null && !buckets.isEmpty() ) {

                        // Each bucket becomes a dataset's row.
                        for ( Terms.Bucket bucket : buckets ) {

                            String aggValue = bucket.getKeyAsString();
                            Map<String, Object> bucketFields = new HashMap<String, Object>();
                            bucketFields.put( agg.getName() , aggValue );

                            SearchHitResponse hit = new SearchHitResponse(bucketFields);

                            Aggregations bucketAggregations = bucket.getAggregations();

                            if ( null != bucketAggregations && !bucketAggregations.asList().isEmpty() ) {

                                parseAggregationsResponse( metadata, hits, bucketAggregations, hit, columns );

                            }

                        }


                    }

                } else {

                    if ( null == sourceHit ) {

                        sourceHit = new SearchHitResponse(fields);

                    }

                    if (aggregation instanceof ValueCount) {

                        ValueCount agg = (ValueCount) aggregation;
                        value = agg.getValue();

                    } else if (aggregation instanceof Sum) {

                        Sum agg = (Sum) aggregation;
                        value = agg.getValue();

                    } else if (aggregation instanceof Min) {

                        Min agg = (Min) aggregation;
                        value = agg.getValue();

                    } else if (aggregation instanceof Max) {

                        Max agg = (Max) aggregation;
                        value = agg.getValue();

                    } else if (aggregation instanceof Avg) {

                        Avg agg = (Avg) aggregation;
                        value = agg.getValue();

                    } else if (aggregation instanceof Cardinality) {

                        Cardinality agg = (Cardinality) aggregation;
                        value = agg.getValue();

                    } else if ( aggregation instanceof InternalHistogram) {

                        InternalHistogram agg = (InternalHistogram) aggregation;

                        List buckets = agg.getBuckets();

                        if ( null != buckets && !buckets.isEmpty() ) {

                            for ( Object oBucket : buckets ) {

                                if ( oBucket instanceof InternalHistogram.Bucket ) {

                                    InternalHistogram.Bucket bucket = (InternalHistogram.Bucket) oBucket;

                                    String aggValue = bucket.getKeyAsString();
                                    Map<String, Object> bucketFields = new HashMap<String, Object>();
                                    bucketFields.put( agg.getName() , aggValue );

                                    SearchHitResponse hit = new SearchHitResponse(bucketFields);

                                    Aggregations bucketAggregations = bucket.getAggregations();

                                    if ( null != bucketAggregations && !bucketAggregations.asList().isEmpty() ) {

                                        parseAggregationsResponse( metadata, hits, bucketAggregations, hit, columns );

                                    }
                                }
                            }

                        }

                    }

                }

                String aggName = aggregation.getName();

                if ( null != value ) {

                    fields.put( aggName, value );

                }

            }

        }

        if ( null != sourceHit &&
                null != sourceHit.getFields() &&
                !sourceHit.getFields().isEmpty() ) {


            SearchHitResponse result = new SearchHitResponse( sourceHit.getScore(),
                    sourceHit.getIndex(), sourceHit.getId(), sourceHit.getType(),
                    sourceHit.getVersion(), orderAndParseFields( metadata, sourceHit.getFields(), columns ) );

            hits.add( result );

        }

    }

    private Map<String, Object> orderAndParseFields(DataSetMetadata metadata,
                                                    Map<String, Object> fields,
                                                    List<DataColumn> columns ) throws ParseException {
        if (fields == null) return null;
        if (columns == null) return new LinkedHashMap<String, Object>(fields);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for ( DataColumn column : columns ) {

            String columnId = column.getId();

            if ( fields.containsKey( columnId ) ) {

                Object value = fields.get( columnId );
                Object parsedValue = parseValue( metadata, column, value );

                result.put(columnId, parsedValue);

            }

        }

        return result;
    }

    /**
     * Parses a given value returned by the JSON response from EL server.
     *
     * @param column       The data column definition.
     * @param value        The value to parse.
     * @return             The parsed value for the given column type.
     */
    private Object parseValue( DataSetMetadata metadata,
                              DataColumn column,
                              Object value ) throws ParseException {

        if ( null != metadata && null != column && null != value ) {

            String valueStr = value.toString();
            ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) metadata.getDefinition();
            ColumnType columnType = column.getColumnType();

            if ( ColumnType.TEXT.equals( columnType ) ) {

                return valueTypeMapper.parseText(def, column.getId(), valueStr );

            } else if ( ColumnType.LABEL.equals( columnType ) ) {

                boolean isColumnGroup = column.getColumnGroup() != null && column.getColumnGroup().getStrategy().equals(GroupStrategy.FIXED);

                return valueTypeMapper.parseLabel( def, column.getId(), valueStr, isColumnGroup );

            } else if ( ColumnType.NUMBER.equals( columnType ) ) {

                return valueTypeMapper.parseNumeric( def, column.getId(), valueStr );

            } else if (ColumnType.DATE.equals(columnType)) {

                // We can expect two return core types from EL server when handling dates:
                // 1.- String type, using the field pattern defined in the index' mappings, when it's result of a query without aggregations.
                // 2.- Numeric type, when it's result from a scalar function or a value pickup.

                if ( value instanceof Number ) {

                    Number number = (Number) value;
                    return valueTypeMapper.parseDate(def, column.getId(), number.longValue() );


                } else {

                    return valueTypeMapper.parseDate( def, column.getId(), valueStr );

                }

            }

            throw new UnsupportedOperationException("Cannot parse value for column with id [" + column.getId() + "] (Data Set UUID [" + def.getUUID() + "]). Value core type not supported. Expecting string or number or date core field types.");

        }

        return null;
    }

   
}
