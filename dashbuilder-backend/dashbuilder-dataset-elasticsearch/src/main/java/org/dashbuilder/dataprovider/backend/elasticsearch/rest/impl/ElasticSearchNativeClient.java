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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl;

import com.google.common.collect.UnmodifiableIterator;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchClientFactory;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.IntervalBuilderDynamicDate;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsAction;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;

/**
 * The Dashbuilder's client implementation for the ElasticSearch data provider.
 * It uses the native Java admin client from ElasticSearch.
 *
 * @since 0.5.0 
 */
public class ElasticSearchNativeClient implements ElasticSearchClient<ElasticSearchNativeClient> {

    protected static final String EL_CLUTER_NAME = "cluster.name";
    protected static final String EL_CLIENT_TIMEOUT = "client.transport.ping_timeout";
    
    protected String serverURL;
    protected String clusterName;
    protected String[] index;
    protected String[] type;
    protected long timeout = 30000;
    
    private Client client;

    private final ElasticSearchClientFactory clientFactory;
    private final ElasticSearchValueTypeMapper valueTypeMapper;
    private final IntervalBuilderDynamicDate intervalBuilderDynamicDate;
    private final ElasticSearchUtils utils;
    
    public ElasticSearchNativeClient(ElasticSearchClientFactory clientFactory, 
                                     ElasticSearchValueTypeMapper valueTypeMapper,
                                     IntervalBuilderDynamicDate intervalBuilderDynamicDate,
                                     ElasticSearchUtils utils) {
        this.clientFactory = clientFactory;
        this.valueTypeMapper = valueTypeMapper;
        this.intervalBuilderDynamicDate = intervalBuilderDynamicDate;
        this.utils = utils;
    }

    @Override
    public ElasticSearchNativeClient serverURL(String serverURL) {
        this.serverURL = serverURL;
        return this;
    }

    @Override
    public ElasticSearchNativeClient index(String... indexes) {
        this.index = indexes;
        return this;
    }

    @Override
    public ElasticSearchNativeClient type(String... types) {
        this.type = types;
        return this;
    }

    @Override
    public ElasticSearchNativeClient clusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    @Override
    public ElasticSearchNativeClient setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MappingsResponse getMappings( String... index ) throws ElasticSearchClientGenericException {
        checkClient();

        Collection<IndexMappingResponse> indexMappingResponse = null;
        int responseCode = RESPONSE_CODE_OK;
        try {
            indexMappingResponse = new LinkedList<IndexMappingResponse>();

            // Obtain the mappings.
            GetMappingsResponse _mappingsResponse = getMappings();
            responseCode = ElasticSearchUtils.getResponseCode(_mappingsResponse);
            
            if ( RESPONSE_CODE_OK == responseCode ) {

                ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappingsResponse = _mappingsResponse.getMappings();
                if (mappingsResponse == null || mappingsResponse.isEmpty()) throw new RuntimeException("There are no index mappings on the server.");
                Iterator<String> mappingsResponseIt =  mappingsResponse.keysIt();
                while (mappingsResponseIt.hasNext()) {
                    String mappingsResponseKey = mappingsResponseIt.next();

                    Collection<TypeMappingResponse> typeMappingResponse = new LinkedList<TypeMappingResponse>();

                    UnmodifiableIterator<String> typeNames= mappingsResponse.get(mappingsResponseKey).keysIt();
                    if (!typeNames.hasNext()) throw new RuntimeException("There index '" + mappingsResponseKey + "' has not types.");

                    while (typeNames.hasNext()) {
                        
                        String typeName = typeNames.next();
                        Map<String, Object> mappingsMap = mappingsResponse.get(mappingsResponseKey).get(typeName).getSourceAsMap();

                        FieldMappingResponse[] fieldMappingResponses = parseMappings( mappingsMap );
                        
                        if ( null != fieldMappingResponses ) {

                            TypeMappingResponse resultTypeMapping = new TypeMappingResponse( typeName, fieldMappingResponses );
                            typeMappingResponse.add( resultTypeMapping );
                            
                        }
                       
                    }

                    indexMappingResponse.add(
                            new IndexMappingResponse(mappingsResponseKey, 
                                    typeMappingResponse.toArray(new TypeMappingResponse[typeMappingResponse.size()])));
                }
                
            }
            
        } catch (Exception e) {
            
            throw new ElasticSearchClientGenericException(e);
            
        }

        return new MappingsResponse(responseCode, indexMappingResponse.toArray(new IndexMappingResponse[indexMappingResponse.size()]));
    }

    @Override
    public CountResponse count( String[] index, 
                                String... type ) throws ElasticSearchClientGenericException {
        checkClient();

        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder( client, SearchAction.INSTANCE )
                .setSize( 0 );
        
        if ( null != index ) {
            searchRequestBuilder.setIndices(index);
        }
        if ( null != type ) {
            searchRequestBuilder.setTypes(type);
        }
        
        
        ActionFuture<org.elasticsearch.action.search.SearchResponse> response = client.search( searchRequestBuilder.request() );
        org.elasticsearch.action.search.SearchResponse searchResponse = response.actionGet();
        long total = searchResponse.getHits().totalHits();

        return new CountResponse( total, searchResponse.getTotalShards() );
    }

    @Override
    public SearchResponse search( ElasticSearchDataSetDef definition, 
                                  DataSetMetadata metadata, 
                                  SearchRequest request ) throws ElasticSearchClientGenericException {
        checkClient();

        int start = request.getStart();
        int size = request.getSize();
        List<DataSetGroup> aggregations = request.getAggregations();
        List<DataSetSort> sorting = request.getSorting();

        // The order for column ids in the resulting data set is already given by the provider (based on the lookup definition).
        List<DataColumn> columns = Collections.unmodifiableList( request.getColumns()) ;
        
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder( client, SearchAction.INSTANCE )
                .setFetchSource(true);
        
        if ( null != index ) {
            
            searchRequestBuilder.setIndices( index );
            
            if ( null != type ) {
                searchRequestBuilder.setTypes( type );
            }
            
        }

        // AGGREGATIONS.
        List<AbstractAggregationBuilder> aggregationsBuilders = null;
        
        if ( null != aggregations && !aggregations.isEmpty() ) {

            // TODO: Use all group operations, not just first one.
            aggregationsBuilders = new NativeClientAggregationsBuilder( clientFactory,
                    intervalBuilderDynamicDate, utils, metadata, columns, request )
                    .build( aggregations.get( 0 ) );

        }

        boolean existAggregations = aggregationsBuilders != null && !aggregations.isEmpty();
        if ( existAggregations ) {

            for ( AbstractAggregationBuilder b : aggregationsBuilders ) {

                searchRequestBuilder.addAggregation( b );
                
            }

        }
        
        // SEARCH QUERY.
        QueryBuilder queryBuilder = new NativeClientQueryBuilder().build( request.getQuery() );

        boolean existQuery = queryBuilder != null;
        if ( existQuery ) {

            searchRequestBuilder.setQuery( queryBuilder );
          
        }

        // If aggregations exist, we care about the aggregation results, not document results.
        int sizeToPull =  existAggregations ? 0 : size;
        int startToPull  = existAggregations ? 0 : start;
        
        // Trim.
        searchRequestBuilder.setFrom( startToPull );
        
        // Size.
        if ( sizeToPull > -1 ) {
            
            searchRequestBuilder.setSize( sizeToPull );
        }

        // If neither query or aggregations exists (just retrieving all element with optinal sort operation), perform a "match_all" query to EL server.
        if ( !existQuery && !existAggregations ) {
            
            searchRequestBuilder.setQuery( new MatchAllQueryBuilder() );
            
        }

        // Add the fields to retrieve, if apply.
        if ( !existAggregations && !columns.isEmpty() ) {

            String[] fields = getColumnIds( columns );
            for ( String field : fields ) {
                
                searchRequestBuilder.addField( field );
                
            }

        }

        // SORTING.
        if ( sorting != null && !sorting.isEmpty() ) {
            
            for (DataSetSort sortOp : sorting) {
                List<ColumnSort> columnSorts = sortOp.getColumnSortList();
                
                if (columnSorts != null && !columnSorts.isEmpty()) {
                    for (ColumnSort columnSort : columnSorts) {
                        
                        searchRequestBuilder.addSort( columnSort.getColumnId(),
                                columnSort.getOrder().asInt() == 1 ? 
                                        org.elasticsearch.search.sort.SortOrder.ASC : 
                                        org.elasticsearch.search.sort.SortOrder.DESC);
                    }
                }
                
            }
            
        }

        // Perform the query to the EL server instance.
        org.elasticsearch.action.search.SearchResponse response =  client.search(searchRequestBuilder.request()).actionGet();
        
        try {

            // Parse and create the search response for the data provider.
            return new NativeClientResponseParser( valueTypeMapper )
                    .parse( metadata, response, columns );

        } catch (ParseException e) {
            throw new ElasticSearchClientGenericException( "Error parsing response from server." , e );
        }
        
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @SuppressWarnings("unchecked")
    private FieldMappingResponse[] parseMappings(  Map<String, Object> mappingsMap ) {

        if ( null != mappingsMap && !mappingsMap.isEmpty() ) {

            Map<String, Object> propertiesMap = (Map<String, Object>) mappingsMap.get( "properties" );

            if ( null != propertiesMap && !propertiesMap.isEmpty() ) {

                List<FieldMappingResponse> fieldMappingResponses = new LinkedList<FieldMappingResponse>();

                for ( Map.Entry<String, Object> entry : propertiesMap.entrySet() ) {

                    FieldMappingResponse fieldMapping = parsePropertyMappings( entry.getKey(), (Map<String, Object>) entry.getValue());

                    if ( null != fieldMapping ) {

                        fieldMappingResponses.add( fieldMapping );

                    }
                }

                return fieldMappingResponses.toArray( new FieldMappingResponse[ fieldMappingResponses.size() ] );

            }

        }


        return null;
    }

    @SuppressWarnings("unchecked")
    private FieldMappingResponse parsePropertyMappings(  String pId, Map<String, Object> propertyMapping ) {

        if ( null != propertyMapping && !propertyMapping.isEmpty() ) {

            
            String format = null;
            final List<MultiFieldMappingResponse> multiFieldMappings = new LinkedList<>();

            Object[] parseIndexMappingsFieldAndType = parseIndexMappingsFieldAndType( propertyMapping );

            if ( null != parseIndexMappingsFieldAndType && parseIndexMappingsFieldAndType.length == 2 ) {

                FieldMappingResponse.FieldType fieldType = (FieldMappingResponse.FieldType) parseIndexMappingsFieldAndType[0];
                FieldMappingResponse.IndexType indexType = (FieldMappingResponse.IndexType) parseIndexMappingsFieldAndType[1];

                // Field format.
                Object f = propertyMapping.get( "format" );
                if ( null != f ) {
                    
                    format = f.toString();
                }

                // Multi-fields.
                Object mf = propertyMapping.get( "fields" );
                if ( null != mf ) {

                    Map<String, Map<String, String>> mFields = (Map<String, Map<String, String>>) mf;

                    if ( !mFields.isEmpty() ) {

                        for ( Map.Entry<String, Map<String, String>> entry1 : mFields.entrySet() ) {

                            String mFieldId = entry1.getKey();

                            Map<String, String> mFieldMappings = entry1.getValue();

                            Object[] mFieldMapTypeIndex = parseIndexMappingsFieldAndType( mFieldMappings );

                            if ( null != mFieldMapTypeIndex && mFieldMapTypeIndex.length == 2 ) {

                                FieldMappingResponse.FieldType fieldType1 = (FieldMappingResponse.FieldType) mFieldMapTypeIndex[0];
                                FieldMappingResponse.IndexType indexType1 = (FieldMappingResponse.IndexType) mFieldMapTypeIndex[1];

                                MultiFieldMappingResponse multiFieldMapping = new MultiFieldMappingResponse( mFieldId, fieldType1, indexType1 );
                                multiFieldMappings.add( multiFieldMapping );

                            }
                        }

                    }
                    
                }
                
                return new FieldMappingResponse( pId,
                        fieldType,
                        indexType,
                        format,
                        multiFieldMappings.isEmpty() ?
                                null :
                                multiFieldMappings.toArray( new MultiFieldMappingResponse[ multiFieldMappings.size() ] ) );

                
            }
            
        }

        return null;
    }
    
    private Object[] parseIndexMappingsFieldAndType( Map<String, ?> propertyMapping ) {

        if ( null != propertyMapping && !propertyMapping.isEmpty() ) {

            FieldMappingResponse.FieldType fieldType = null;
            FieldMappingResponse.IndexType indexType = null;

            // Field index type.
            Object i = propertyMapping.get( "index" );
            
            if ( null != i ) {

                indexType = FieldMappingResponse.IndexType.valueOf( i.toString().toUpperCase() );
                
            }

            // Field data type.
            Object f = propertyMapping.get( "type" );

            if ( null != f ) {

                fieldType = FieldMappingResponse.FieldType.valueOf( f.toString().toUpperCase() );

            }
            
            return new Object[] { fieldType, indexType };
        }
        
        return null;
    }

    private void checkClient() throws ElasticSearchClientGenericException {

        if ( null == client ) {

            try {

                buildClient();

            } catch (Exception e) {
                throw new ElasticSearchClientGenericException( "Error while building the elastic search client.", e );
            }

        }

    }

    private String[] getColumnIds( List<DataColumn> columns ) {
        if ( columns == null || columns.isEmpty() ) {
            return null;
        }
        
        String[] result = new String[ columns.size() ];
        
        for ( int x = 0; x < columns.size(); x++ ) {
            DataColumn column = columns.get( x );
            result[x] = column.getId();
        }
        
        return result;
    }
    

    private Client buildClient() throws Exception {
        if ( null == client ) {
            client = NativeClientFactory.getInstance().newClient( serverURL, clusterName, timeout );
        }
        
        return client;
    }

    

    private GetMappingsResponse getMappings() {
        GetMappingsRequestBuilder builder = new GetMappingsRequestBuilder(client, GetMappingsAction.INSTANCE, index);
        return client.admin().indices().getMappings(builder.request()).actionGet();
    }

    
}
