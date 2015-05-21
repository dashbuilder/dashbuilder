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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.ElasticSearchRestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.exception.ElasticSearchRestClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.CountResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.util.ElasticSearchJSONParser;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.filter.*;
import org.dashbuilder.dataset.group.AggregateFunction;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.count.*;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.collect.UnmodifiableIterator;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.filters.InternalFilters;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.InternalMin;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.InternalValueCount;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.*;

/**
 * The REST client for ElasticSearch server using ElasticSearch native Java client.
 * 
 */
@ApplicationScoped
@Named("elasticsearchNativeClient")
public class ElasticSearchNativeRESTClient implements ElasticSearchRestClient<ElasticSearchNativeRESTClient> {

    protected static final String EL_CLUTER_NAME = "cluster.name";
    protected static final String EL_CLIENT_TIMEOUT = "client.transport.ping_timeout";
    protected static final String HEADER_RESPONSE_CODE = "Status-Code";
    protected static final String AGGREGATION_PREFFIX = "agg_";
    protected static final String SYMBOL_UNDERSCORE = "_";

    protected static final int RESPONSE_CODE_NOT_FOUND = 404;
    protected static final int RESPONSE_CODE_OK = 200;
    // TODO: @Inject -> Not working
    protected ElasticSearchJSONParser jsonParser;
    
    protected String serverURL;
    protected String clusterName;
    protected String[] index;
    protected String[] type;
    // Defaults to 30sec.
    protected long timeout = 30000;
    
    private Client client;

    public ElasticSearchNativeRESTClient() {
        // TODO: Remove when cdi injection works.
        jsonParser = new ElasticSearchJSONParser();
    }

    @Override
    public ElasticSearchNativeRESTClient serverURL(String serverURL) {
        this.serverURL = serverURL;
        if (clusterName != null) buildClient();
        return this;
    }

    @Override
    public ElasticSearchNativeRESTClient index(String... indexes) {
        this.index = indexes;
        if (serverURL != null && clusterName != null) buildClient();
        return this;
    }

    @Override
    public ElasticSearchNativeRESTClient type(String... types) {
        this.type = types;
        if (serverURL != null && clusterName != null) {
            if (index == null) throw new IllegalArgumentException("You cannot call ElasticSearchRestClient#type before calling ElasticSearchRestClient#index."); 
            buildClient();
        }
        return this;
    }

    @Override
    public ElasticSearchNativeRESTClient clusterName(String clusterName) {
        this.clusterName = clusterName;
        if (serverURL != null) buildClient();
        return this;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public IndexResponse getAllIndexes() throws ElasticSearchRestClientGenericException {
        if (client == null) throw new IllegalArgumentException("ElasticSearchRestClient instance is not build.");
        // TODO
        return null;
    }

    @Override
    public MappingsResponse getMappings(String... index) throws ElasticSearchRestClientGenericException {
        if (client == null) throw new IllegalArgumentException("ElasticSearchRestClient instance is not build.");

        Collection<IndexMappingResponse> indexMappingResponse = null;
        int responseCode = RESPONSE_CODE_OK;
        try {
            indexMappingResponse = new LinkedList<IndexMappingResponse>();

            // Obtain the mappings.
            GetMappingsResponse _mappingsResponse = getMappings();
            responseCode = getResponseCode(_mappingsResponse);
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappingsResponse = _mappingsResponse.getMappings();
            if (mappingsResponse == null || mappingsResponse.isEmpty()) throw new RuntimeException("There are no index mappings on the server.");
            Iterator<String> mappingsResponseIt =  mappingsResponse.keysIt();
            while (mappingsResponseIt.hasNext()) {
                String mappingsResponseKey = mappingsResponseIt.next();
    
                Collection<TypeMappingResponse> typeMappingResponse = new LinkedList<TypeMappingResponse>();
    
                UnmodifiableIterator<String> typeNames= mappingsResponse.get(mappingsResponseKey).keysIt();
                if (typeNames != null && !typeNames.hasNext()) throw new RuntimeException("There index '" + mappingsResponseKey + "' has not types.");
                while (typeNames.hasNext()) {
                    String typeName = typeNames.next();
                    FieldMappingResponse[] fieldMappingResponse = jsonParser.parseFieldMappings(mappingsResponse.get(mappingsResponseKey).get(typeName).source().string());
                    TypeMappingResponse resultTypeMapping = new TypeMappingResponse(typeName, fieldMappingResponse);
                    typeMappingResponse.add(resultTypeMapping);
                }
                
                
                indexMappingResponse.add(new IndexMappingResponse(mappingsResponseKey, typeMappingResponse.toArray(new TypeMappingResponse[typeMappingResponse.size()])));
            }
        } catch (Exception e) {
            throw new ElasticSearchRestClientGenericException(e);
        }

        return new MappingsResponse(responseCode, indexMappingResponse.toArray(new IndexMappingResponse[indexMappingResponse.size()]));
    }

    @Override
    public CountResponse count(String[] index, String... type) throws ElasticSearchRestClientGenericException {
        if (client == null) throw new IllegalArgumentException("ElasticSearchRestClient instance is not build.");

        CountRequestBuilder builder = new CountRequestBuilder(client);
        if (index != null) builder = builder.setIndices(index);
        if (type != null) builder = builder.setTypes(type);
        
        org.elasticsearch.action.count.CountResponse countResponse = builder.execute().actionGet();
        // TODO: Shards.
        return new CountResponse(countResponse.getCount(), null);
    }

    /**
     * @see <a>http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/search.html</a>
     * 
     * TODO: Improve using search types - http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-search-type.html
     */
    @Override
    public SearchResponse search(ElasticSearchRestClientSearchRequest request) throws ElasticSearchRestClientGenericException {
        if (client == null) throw new IllegalArgumentException("ElasticSearchRestClient instance is not build.");

        // Perform the search query.
        DataSetMetadata metadata = request.getMetadata();
        String[] index = request.getIndexes();
        String[] type = request.getTypes();
        String[] fields = request.getFields();
        int start = request.getStart();
        int size = request.getSize();
        ColumnGroup groupByColumn = request.getGroupByColumn();
        List<GroupFunction> groupFunctions = request.getGroupFunctions();
        List<ColumnSort> sorting = request.getSorting();
        List<DataSetFilter> filters = request.getFilters();

        SearchRequestBuilder builder = new SearchRequestBuilder(client);
        builder.setFetchSource(true);
        if (index != null) builder = builder.setIndices(index);
        if (index != null && type != null) builder = builder.setTypes(type);

        boolean existAggregations = false;

        List<String> columnIds = new ArrayList<String>();
        List<ColumnType> columnTypes = new ArrayList<ColumnType>();

        TermsBuilder groupByAggregation = null;
        // Group by.
        if (groupByColumn != null) {
            groupByAggregation = translateGroupByColumn(groupByColumn, groupFunctions, metadata);
            builder.addAggregation(groupByAggregation);
            String colId = groupByAggregation.getName();
            ColumnType colType = metadata.getColumnType(colId);
            columnIds.add(colId);
            columnTypes.add(colType);
            existAggregations = true;
        }

        // Aggregations.
        if (groupFunctions != null && !groupFunctions.isEmpty()) {
            for (GroupFunction groupFunction : groupFunctions) {
                AbstractAggregationBuilder aggr = translateAggregation(metadata, groupFunction); 
                if (groupByAggregation == null && aggr != null) builder.addAggregation(aggr);
                else if (groupByAggregation!= null && aggr != null) groupByAggregation.subAggregation(aggr);
                if (aggr != null) {
                    String colId = aggr.getName();
                    columnIds.add(colId);
                    columnTypes.add(ColumnType.NUMBER);
                }
            }
            existAggregations = true;
        } 
        
        // If there are no aggregations. Use original dataset columns.
        if (!existAggregations && fields != null) {
            if (fields != null ) {
                builder = builder.addFields(fields);
                for (String field : fields) {
                    if (!existColumnInMetadataDef(field, metadata)) throw new RuntimeException("Aggregation by column [" + field + "] failed. No column with the given id.");
                    ColumnType colType = metadata.getColumnType(field);
                    columnIds.add(field);
                    columnTypes.add(colType);
                }
            }
        }
        
        // Sorting.
        if (sorting != null && !sorting.isEmpty()) {
            for (ColumnSort columnSort : sorting) {
                builder.addSort(columnSort.getColumnId(), translateSort(columnSort.getOrder()));
            }
        }

        // Filtering.
        if (filters != null && !filters.isEmpty()) {
            addFilters(builder, filters, metadata); 
        }
        
        // if aggregations exist, we care about the aggregation results, not document results.
        int sizeToPull = existAggregations ? 0 : size;
        int startToPull = existAggregations ? 0 : start;
        
        // Trimming.
        builder = builder.setFrom(startToPull).setSize(sizeToPull);

        // Perform the query to the EL server instance.
        org.elasticsearch.action.search.SearchResponse response =  client.search(builder.request()).actionGet();
        return buildSearchResponse(response, columnIds, columnTypes);
    }

    /**
     * TODO: Sort resuling columns (fields Map)
     * @param response
     * @return
     */
    protected SearchResponse buildSearchResponse(org.elasticsearch.action.search.SearchResponse response, List<String> columnIds, List<ColumnType> columnTypes) {
        // Convert to rest client model.
        long tookInMilis = response.getTookInMillis();
        int responseCode = getResponseCode(response);
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
        
        // Build the response using the aggregated results.
        if (existAggregations) {
            createResponsetHits(hits, null, aggregations, columnIds, columnTypes);
        }
        // Build the response using original dataset columns, as no aggregation is present.
        else {
            for (int i = 0; i < hitCount; i++) {
                SearchHit searchHit = response.getHits().getAt(i);
                float score = searchHit.getScore();
                String id = searchHit.getId();
                String type = searchHit.getType();
                String index = searchHit.getIndex();
                long version = searchHit.getVersion();
                Map<String, Object> sourceAsMap = searchHit.getSource();
                Map<String, SearchHitField> sourceFields = searchHit.getFields();

                Map<String, Object> fields = new HashMap<String, Object>();

                // If there are some "fields" defined by user in the data set provider, use sourceFields to obtain the values.
                if (sourceFields != null && !sourceFields.isEmpty()) {
                    for (Map.Entry<String, SearchHitField> entry : sourceFields.entrySet()) {
                        String fieldName = entry.getKey();
                        SearchHitField hitValue = entry.getValue();
                        if (hitValue != null) {
                            Object fieldValue = hitValue.getValue();

                            // Fill the values map.
                            fields.put(fieldName, fieldValue);
                        }
                    }
                }
                // If there are no "fields" defined by user in the data set provider, obtain all fields, use sourceAsMap to obtain the values.
                else if (sourceAsMap != null && !sourceAsMap.isEmpty()) {
                    for (Map.Entry<String, Object> entry : sourceAsMap.entrySet()) {
                        String fieldName = entry.getKey();
                        Object fieldValue = entry.getValue();

                        // Fill the values map.
                        fields.put(fieldName, fieldValue);
                    }
                }

                SearchHitResponse hit = new SearchHitResponse(score, index, id, type, version, fields);
                hits.add(hit);
            }
        }
        
        return new SearchResponse(tookInMilis, responseCode, totalHits, maxScore, totalShards, successfulShards, shardFailures, columnIds, columnTypes, hits.toArray(new SearchHitResponse[hits.size()]));
    }

    protected void createResponsetHits(List<SearchHitResponse> hits, SearchHitResponse sourceHit, Aggregations aggregations, List<String> columnIds, List<ColumnType> columnTypes) {
        Map<String, Object> fields = new HashMap<String, Object>();
        
        if (sourceHit != null && sourceHit.getFields() != null) {
            fields = sourceHit.getFields();
        }
        
        for (Aggregation aggregation : aggregations.asList()) {
            String aggName = aggregation.getName();
            Object value = null;

            // MultiBucketsAggregation
            if (aggregation instanceof StringTerms) {
                StringTerms agg = (StringTerms) aggregation;
                Collection<Terms.Bucket> buckets = agg.getBuckets();
                if (buckets != null && !buckets.isEmpty()) {
                    // Each bucket will become a dataset row.
                    for (Terms.Bucket bucket : buckets) {
                        Map<String, Object> bucketFields = new HashMap<String, Object>();
                        String aggValue = bucket.getKey();
                        bucketFields.put(aggName, aggValue);
                        SearchHitResponse hit = new SearchHitResponse(bucketFields);

                        Aggregations bucketAggregations = bucket.getAggregations();
                        if (bucketAggregations != null && !bucketAggregations.asList().isEmpty()) {
                            createResponsetHits(hits, hit, bucketAggregations, columnIds, columnTypes);
                        }
                    }
                }
            } else if (aggregation instanceof ValueCount) {
                if (sourceHit == null) sourceHit = new SearchHitResponse(fields);
                ValueCount agg = (ValueCount) aggregation;
                value = agg.getValue();
            } else if (aggregation instanceof Sum) {
                if (sourceHit == null) sourceHit = new SearchHitResponse(fields);
                Sum agg = (Sum) aggregation;
                value = agg.getValue();
            } else if (aggregation instanceof Min) {
                if (sourceHit == null) sourceHit = new SearchHitResponse(fields);
                Min agg = (Min) aggregation;
                value = agg.getValue();
            } else if (aggregation instanceof Max) {
                if (sourceHit == null) sourceHit = new SearchHitResponse(fields);
                Max agg = (Max) aggregation;
                value = agg.getValue();
            } else if (aggregation instanceof Avg) {
                if (sourceHit == null) sourceHit = new SearchHitResponse(fields);
                Avg agg = (Avg) aggregation;
                value = agg.getValue();
            } else if (aggregation instanceof Cardinality) {
                if (sourceHit == null) sourceHit = new SearchHitResponse(fields);
                Cardinality agg = (Cardinality) aggregation;
                value = agg.getValue();
            }

            // Fill the response hit instance. 
            fields.put(aggName, value);

        }
        
        if (sourceHit != null) hits.add(sourceHit);
        
    }
    
    /**
     * <p>Translates the ColumnGroup definition into a TermsBuilder one in oder to perform field collapsing.</p>
     * <p>It found for the GroupFunction that does not have any operation associated, as it's the group column name definition.</p>
     */
    protected TermsBuilder translateGroupByColumn(ColumnGroup columnGroup, List<GroupFunction> groupFunctions, DataSetMetadata metadata) {
        if (columnGroup == null) return null;
        
        // TODO: Support for GroupStrategy.
        String columnId = columnGroup.getColumnId();
        String sourceId = columnGroup.getSourceId();
        boolean asc = columnGroup.isAscendingOrder();
        
        if (groupFunctions != null && !groupFunctions.isEmpty()) {
            for (GroupFunction groupFunction : groupFunctions) {
                if (groupFunction.getFunction() == null) {
                    columnId = groupFunction.getColumnId();
                    if (!sourceId.equals(groupFunction.getSourceId())) throw new RuntimeException("Grouping by this source property [" + sourceId + "] not possible.");
                    if (!existColumnInMetadataDef(sourceId, metadata)) throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
                }
            }
        }
        return new TermsBuilder(columnId).field(sourceId).order(Terms.Order.term(asc));
    }
    
    protected boolean existColumnInMetadataDef(String name, DataSetMetadata metadata) {
        if (name == null || metadata == null) return false;
        
        int cols = metadata.getNumberOfColumns();
        for (int x = 0; x < cols; x++) {
            String colName = metadata.getColumnId(x);
            if (name.equals(colName)) return true;
        }
        return false;
    }
    
    /*
     *********************************************************************
       * Helper methods.
     *********************************************************************
     */

    protected Client buildClient() throws IllegalArgumentException{
        if (client == null) {
            if (serverURL == null || serverURL.trim().length() == 0) throw new IllegalArgumentException("Parameter serverURL is missing.");
            if (clusterName == null || clusterName.trim().length() == 0) throw new IllegalArgumentException("Parameter clusterName is missing.");
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put(EL_CLUTER_NAME, clusterName)
                    .put(EL_CLIENT_TIMEOUT, timeout)
                    .build();
            // TODO: Use serverURL for connection.
            client = new TransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        }
        return client;
    }
    
    protected int getResponseCode(ActionResponse response) {
        if (response == null) return RESPONSE_CODE_NOT_FOUND;
        String responseCode = response.getHeader(HEADER_RESPONSE_CODE);
        
        if (responseCode == null) return RESPONSE_CODE_OK;
        return Integer.decode(responseCode);
    }
    
    /*
     *********************************************************************
       * EL Java native client methods.
     *********************************************************************
     */

    private GetMappingsResponse getMappings() {
        GetMappingsRequestBuilder builder = new GetMappingsRequestBuilder(client.admin().indices(), index);
        return client.admin().indices().getMappings(builder.request()).actionGet();
    }

    /**
     * @see <a>http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/java-aggs.html</a>
     */
    private AbstractAggregationBuilder translateAggregation(DataSetMetadata metadata, GroupFunction groupFunction) {
        if (groupFunction == null) return null;

        // If there is no function in groupfunction means that it's a grouped column definition. 
        // Aggregation by this groupfunction is handled by groupby aggregation done before.
        AggregateFunctionType ft = groupFunction.getFunction();
        if (ft == null) return null;

        AbstractAggregationBuilder result = null;

        String sourceId = groupFunction.getSourceId();
        if (sourceId != null && !existColumnInMetadataDef(sourceId, metadata)) throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
        if (sourceId == null) sourceId = metadata.getColumnId(0);
        if (sourceId == null) throw new IllegalArgumentException("Aggregation from unknown column id.");

        
        String columnId = groupFunction.getColumnId();
        if (columnId == null) throw new IllegalArgumentException("Aggregation to unknown column id.");
        
        if (AggregateFunctionType.SUM.equals(ft)) result = AggregationBuilders.sum(columnId).field(sourceId);
        else if (AggregateFunctionType.MAX.equals(ft)) result = AggregationBuilders.max(columnId).field(sourceId);
        else if (AggregateFunctionType.MIN.equals(ft)) result = AggregationBuilders.min(columnId).field(sourceId);
        else if (AggregateFunctionType.AVERAGE.equals(ft)) result = AggregationBuilders.avg(columnId).field(sourceId);
        else if (AggregateFunctionType.DISTINCT.equals(ft)) result = AggregationBuilders.cardinality(columnId).field(sourceId);
        else if (AggregateFunctionType.COUNT.equals(ft)) result = AggregationBuilders.count(columnId).field(sourceId);
        
        return result;
    }
    
    /**
     * @see <a>http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/query-dsl-filters.html</a>
     * 
     * TODO: Improve filters query implementation?
     */
    private void addFilters(SearchRequestBuilder builder, List<DataSetFilter> filters, DataSetMetadata metadata) {
        FilterBuilder resultFilter = null;
        QueryBuilder resultQuery = null;
        
        // TODO: Iterate by all the filter operations.
        DataSetFilter filterOp = filters.get(0);
        List<ColumnFilter> filterList = filterOp.getColumnFilterList();
        for (ColumnFilter filter : filterList) {
            String columnId = filter.getColumnId();
            ColumnType columnType = metadata.getColumnType(columnId);
            
            if (filter instanceof CoreFunctionFilter) {
                CoreFunctionFilter f = (CoreFunctionFilter) filter;
                CoreFunctionType type = f.getType();
                List<Comparable> params = f.getParameters();

                if (CoreFunctionType.IS_NULL.equals(type)) {
                    resultFilter = FilterBuilders.notFilter(FilterBuilders.existsFilter(columnId));
                }
                else if (CoreFunctionType.IS_NOT_NULL.equals(type)) {
                    resultFilter = FilterBuilders.existsFilter(columnId);
                }
                else if (CoreFunctionType.IS_EQUALS_TO.equals(type)) {
                    if (ColumnType.LABEL.equals(columnType)) resultFilter = FilterBuilders.termFilter(columnId, params.get(0));
                    else resultQuery = QueryBuilders.matchQuery(columnId, params.get(0));
                }
                else if (CoreFunctionType.IS_NOT_EQUALS_TO.equals(type)) {
                    if (ColumnType.LABEL.equals(columnType)) resultFilter = FilterBuilders.notFilter(FilterBuilders.termFilter(columnId, params.get(0)));
                    else resultQuery = QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery(columnId, params.get(0)));
                    
                    resultFilter = FilterBuilders.notFilter(FilterBuilders.termFilter(columnId, params.get(0)));
                }
                else if (CoreFunctionType.IS_LOWER_THAN.equals(type)) {
                    resultFilter = FilterBuilders.rangeFilter(columnId).lt(params.get(0));
                }
                else if (CoreFunctionType.IS_LOWER_OR_EQUALS_TO.equals(type)) {
                    resultFilter = FilterBuilders.rangeFilter(columnId).lte(params.get(0));
                }
                else if (CoreFunctionType.IS_GREATER_THAN.equals(type)) {
                    resultFilter = FilterBuilders.rangeFilter(columnId).gt(params.get(0));
                }
                else if (CoreFunctionType.IS_GREATER_OR_EQUALS_TO.equals(type)) {
                    resultFilter = FilterBuilders.rangeFilter(columnId).gte(params.get(0));
                }
                else if (CoreFunctionType.IS_BETWEEN.equals(type)) {
                    resultFilter = FilterBuilders.rangeFilter(columnId).gt(params.get(0)).lt(params.get(1));
                }
                else {
                    throw new IllegalArgumentException("Core function type not supported: " + type);
                }
            }
            if (filter instanceof LogicalExprFilter) {
                // TODO: logical expr filters
                LogicalExprFilter f = (LogicalExprFilter) filter;
                LogicalExprType type = f.getLogicalOperator();
                if (LogicalExprType.AND.equals(type)) {
                }
                if (LogicalExprType.OR.equals(type)) {

                }
                if (LogicalExprType.NOT.equals(type)) {

                }
            }

            if (resultFilter != null) builder.setPostFilter(resultFilter);
            if (resultQuery != null) builder.setQuery(resultQuery);
        }
    }
    
    private org.elasticsearch.search.sort.SortOrder translateSort(SortOrder sortOrder) {
        if (sortOrder.equals(SortOrder.ASCENDING)) return org.elasticsearch.search.sort.SortOrder.ASC;
        return org.elasticsearch.search.sort.SortOrder.DESC;
    }
   

}
