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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest;

import com.google.gson.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.Search;
import io.searchbox.core.search.sort.Sort;
import io.searchbox.indices.mapping.GetMapping;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchClientFactory;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchDataSetProvider;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * <p>The Jest/GSON client for ElasticSearch server.</p>
 *
 * @see <a href="https://github.com/searchbox-io/Jest">https://github.com/searchbox-io/Jest</a>
 */
public class ElasticSearchJestClient implements ElasticSearchClient<ElasticSearchJestClient> {

    protected String serverURL;
    protected String clusterName;
    protected String[] index;
    protected String[] type;

    protected ElasticSearchClientFactory clientFactory;
    protected ElasticSearchValueTypeMapper typeMapper;
    protected ElasticSearchUtils utils;
    
    // JestClient is designed to be singleton, don't construct it for each request.
    private JestClient client;
    private ElasticSearchJestClient anotherClient;
    protected int timeout = DEFAULT_TIMEOUT;

    public ElasticSearchJestClient(ElasticSearchClientFactory clientFactory,
                                   ElasticSearchValueTypeMapper typeMapper,
                                   ElasticSearchUtils utils) {

        this.clientFactory = clientFactory;
        this.typeMapper = typeMapper;
        this.utils = utils;
    }

    @Override
    public ElasticSearchJestClient serverURL(String serverURL) {
        this.serverURL = serverURL;
        if (clusterName != null) buildClient();
        return this;
    }

    @Override
    public ElasticSearchJestClient index(String... indexes) {
        this.index = indexes;
        if (serverURL != null && clusterName != null) buildClient();
        return this;
    }

    @Override
    public ElasticSearchJestClient type(String... types) {
        this.type = types;
        if (serverURL != null && clusterName != null) {
            if (index == null)
                throw new IllegalArgumentException("You cannot call elasticsearchRESTEasyClient#type before calling elasticsearchRESTEasyClient#index.");
            buildClient();
        }
        return this;
    }

    @Override
    public ElasticSearchJestClient clusterName(String clusterName) {
        this.clusterName = clusterName;
        if (serverURL != null) buildClient();
        return this;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public MappingsResponse getMappings(String... index) throws ElasticSearchClientGenericException {
        if (client == null) throw new IllegalArgumentException("elasticsearchRESTEasyClient instance is not build.");

        try {
            IndexMappingResponse[] result = new IndexMappingResponse[index.length];
            int x = 0;
            for (String _index : index) {
                IndexMappingResponse indexMappings = getMappings(_index, null);
                result[x++] = indexMappings;
            }
            return new MappingsResponse(ElasticSearchDataSetProvider.RESPONSE_CODE_OK, result);
        } catch (Exception e) {
            throw new ElasticSearchClientGenericException("Cannot obtain mappings.", e);
        }
    }

    protected IndexMappingResponse getMappings(String index, String type) throws Exception {
        GetMapping.Builder builder = new GetMapping.Builder().addIndex(index);
        if (type != null) builder = builder.addType(type);

        GetMapping getMapping = builder.build();
        JestResult result = client.execute(getMapping);
        Set<Map.Entry<String, JsonElement>> mappings = result.getJsonObject().get(index).getAsJsonObject().get("mappings").getAsJsonObject().entrySet();
        TypeMappingResponse[] types = new TypeMappingResponse[mappings.size()];
        int x = 0;
        for (Map.Entry<String, JsonElement> entry : mappings) {
            String typeName = entry.getKey();
            JsonElement typeMappings = entry.getValue();
            JsonElement properties = typeMappings.getAsJsonObject().get("properties");
            Set<Map.Entry<String, JsonElement>> propertyMappings = properties.getAsJsonObject().entrySet();
            FieldMappingResponse[] fields = new FieldMappingResponse[propertyMappings.size()];
            int y = 0;
            for (Map.Entry<String, JsonElement> propertyMapping : propertyMappings) {
                String field = propertyMapping.getKey();
                JsonElement element = propertyMapping.getValue();
                FieldMapping fieldMappings = new Gson().fromJson(element, FieldMapping.class);
                
                // Field data type.
                FieldMappingResponse.FieldType fieldType = null;
                if (fieldMappings.getType() != null)
                    fieldType = FieldMappingResponse.FieldType.valueOf(fieldMappings.getType().toUpperCase());
                
                // Field index type.
                FieldMappingResponse.IndexType indexType = null;
                if (fieldMappings.getIndex() != null)
                    indexType = FieldMappingResponse.IndexType.valueOf(fieldMappings.getIndex().toUpperCase());
                String format = fieldMappings.getFormat();

                // Multi-fields.
                List<MultiFieldMappingResponse> multiFieldsResult = null;
                JsonElement multiFieldsElemement = element.getAsJsonObject().get("fields");
                if (multiFieldsElemement != null) {
                    Set<Map.Entry<String, JsonElement>> multiFields = multiFieldsElemement.getAsJsonObject().entrySet();
                    if (multiFields != null && !multiFields.isEmpty()) {
                        multiFieldsResult = new ArrayList<MultiFieldMappingResponse>(multiFields.size());
                        for (Map.Entry<String, JsonElement> multiFieldEntry : multiFields) {
                            String multiFieldId = multiFieldEntry.getKey();
                            JsonElement multiFieldElement = multiFieldEntry.getValue();
                            if (multiFieldElement != null) {
                                JsonElement _dtElement = multiFieldElement.getAsJsonObject().get("type");
                                String _dt = _dtElement != null ? _dtElement.getAsString() : null;
                                JsonElement _itElement = multiFieldElement.getAsJsonObject().get("index");
                                String _it = _itElement != null ? _itElement.getAsString() : null;
                                FieldMappingResponse.FieldType multiFieldDataType = _dt != null ? FieldMappingResponse.FieldType.valueOf(_dt.toUpperCase()) : fieldType; 
                                FieldMappingResponse.IndexType multiFieldIndexType = _it != null ? FieldMappingResponse.IndexType.valueOf(_it.toUpperCase()) : indexType;
                                MultiFieldMappingResponse multiFieldMappingResponse = new MultiFieldMappingResponse(multiFieldId, multiFieldDataType, multiFieldIndexType);
                                multiFieldsResult.add(multiFieldMappingResponse);
                            }
                        }
                    }
                }
                MultiFieldMappingResponse[] multiFieldsArray = multiFieldsResult == null ? null : multiFieldsResult.toArray(new MultiFieldMappingResponse[multiFieldsResult.size()]);
                FieldMappingResponse fieldMappingResponse = new FieldMappingResponse(field, fieldType, indexType, format, multiFieldsArray);
                fields[y++] = fieldMappingResponse;
            }
            TypeMappingResponse typeMappingResponse = new TypeMappingResponse(typeName, fields);
            types[x++] = typeMappingResponse;
        }
        return new IndexMappingResponse(index, types);
    }

    @Override
    public CountResponse count(String[] index, String... type) throws ElasticSearchClientGenericException {
        if (client == null) throw new IllegalArgumentException("elasticsearchRESTEasyClient instance is not build.");

        Count.Builder countBuilder = new Count.Builder().addIndex(Arrays.asList(index));
        if (type != null) countBuilder = countBuilder.addType(Arrays.asList(type));
        Count count = countBuilder.build();
        try {
            CountResult result = client.execute(count);

            double hitCount = result.getCount();
            int totalShards = result.getJsonObject().get("_shards").getAsJsonObject().get("total").getAsInt();
            return new CountResponse((long) hitCount, totalShards);
        } catch (Exception e) {
            throw new ElasticSearchClientGenericException("Cannot count.", e);
        }
    }

    /**
     * @param definition The dataset definition.
     * @param request    The search request.
     * @return
     * @throws ElasticSearchClientGenericException
     */
    @Override
    public SearchResponse search(DataSetDef definition, DataSetMetadata metadata, SearchRequest request) throws ElasticSearchClientGenericException {
        if (client == null) throw new NullPointerException("No client");

        ElasticSearchDataSetDef elasticSearchDataSetDef = (ElasticSearchDataSetDef) definition;
        int start = request.getStart();
        int size = request.getSize();
        List<DataSetGroup> aggregations = request.getAggregations();
        List<DataSetSort> sorting = request.getSorting();
        Query query = request.getQuery();

        // The order for column ids in the resulting data set is already given by the provider (based on the lookup definition).
        List<DataColumn> columns = Collections.unmodifiableList(request.getColumns());

        // Crate the Gson builder and instance.
        GsonBuilder builder = new GsonBuilder();
    
        // Create another client for aggregations serializer (dashbuilder does a second query for finding max and min values when using intervals)
        JsonSerializer aggregationSerializer = new AggregationSerializer(this, metadata, columns, request);
        JsonSerializer querySerializer = new QuerySerializer(this, metadata, columns);
        JsonSerializer searchQuerySerializer = new SearchQuerySerializer(this, metadata, columns);
        JsonDeserializer searchResponseDeserializer = new SearchResponseDeserializer(this, metadata, columns);
        JsonDeserializer hitDeserializer = new HitDeserializer(this, metadata, columns);
        JsonDeserializer aggreationsDeserializer = new AggregationsDeserializer(this, metadata, columns);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        builder.registerTypeAdapter(Query.class, querySerializer);
        builder.registerTypeAdapter(SearchQuery.class, searchQuerySerializer);
        builder.registerTypeAdapter(SearchResponse.class, searchResponseDeserializer);
        builder.registerTypeAdapter(SearchHitResponse.class, hitDeserializer);
        builder.registerTypeAdapter(SearchHitResponse[].class, aggreationsDeserializer);
        Gson gson = builder.create();

        // Set request lookup constraints into the query JSON request.
        JsonElement gsonQueryElement = gson.toJsonTree(query);
        JsonObject gsonQuery = null;
        if (gsonQueryElement instanceof JsonObject) gsonQuery = (JsonObject) gsonQueryElement;

        // Add the group functions translated as query aggregations.
        List<JsonObject> aggregationObjects = null;
        if (aggregations != null && !aggregations.isEmpty()) {
            aggregationObjects = new LinkedList<JsonObject>();
            for (DataSetGroup aggregation : aggregations) {
                JsonElement object = gson.toJsonTree(aggregation, DataSetGroup.class);
                if (object != null && object.isJsonObject()) {
                    aggregationObjects.add((JsonObject) object);
                }
            }
        }

        // Build the search request.
        SearchQuery searchQuery = new SearchQuery(getColumnIds(columns), gsonQuery, aggregationObjects, start, size);
        String serializedSearchQuery = gson.toJson(searchQuery, SearchQuery.class);
        Search.Builder searchRequestBuilder = new Search.Builder(serializedSearchQuery).addIndex(index[0]);
        if (type != null && type.length > 0) searchRequestBuilder.addType(type[0]);

        // Add sorting.
        if (sorting != null && !sorting.isEmpty()) {
            for (DataSetSort sortOp : sorting) {
                List<ColumnSort> columnSorts = sortOp.getColumnSortList();
                if (columnSorts != null && !columnSorts.isEmpty()) {
                    for (ColumnSort columnSort : columnSorts) {
                        Sort sort = new Sort(columnSort.getColumnId(), columnSort.getOrder().asInt() == 1 ? Sort.Sorting.ASC : Sort.Sorting.DESC);
                        searchRequestBuilder.addSort(sort);
                    }
                }
            }
        }

        // Perform the query to the EL server.
        Search searchRequest = searchRequestBuilder.build();
        JestResult result = null;
        try {
            log("REQUEST");
            log("*******");
            log(serializedSearchQuery);
            result = client.execute(searchRequest);
        } catch (Exception e) {
            throw new ElasticSearchClientGenericException("An error ocurred during search operation.", e);
        }
        JsonObject resultObject = result.getJsonObject();
        if (resultObject.get("error") != null) {
            String errorMessage = resultObject.get("error").getAsString();
            throw new ElasticSearchClientGenericException("An error ocurred during search operation. This is the internal error: \n" + errorMessage);
        }
        log("RESPONSE");
        log("********");
        log(resultObject.toString());
        return gson.fromJson(resultObject, SearchResponse.class);
        
    }

    public ElasticSearchJestClient getAnotherClient(ElasticSearchDataSetDef def) {
        if (anotherClient == null) {
            anotherClient = (ElasticSearchJestClient) clientFactory.newClient(def);
        }
        return anotherClient;
    }

    protected String[] getColumnIds(List<DataColumn> columns) {
        if (columns == null || columns.isEmpty()) return null;
        String[] result = new String[columns.size()];
        for (int x = 0; x < columns.size(); x++) {
            DataColumn column = columns.get(x);
            result[x] = column.getId();
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        if (anotherClient != null) {
            anotherClient.close();
        }
        if (client != null) {
            client.shutdownClient();
        }
    }

    public static class SearchQuery {
        String[] fields;
        JsonObject query;
        List<JsonObject> aggregations;
        int start;
        int size;

        public SearchQuery(String[] fields, JsonObject query, List<JsonObject> aggregations, int start, int size) {
            this.fields = fields;
            this.query = query;
            this.aggregations = aggregations;
            this.start = start;
            this.size = size;
        }

        public String[] getFields() {
            return fields;
        }

        public JsonObject getQuery() {
            return query;
        }

        public List<JsonObject> getAggregations() {
            return aggregations;
        }

        public int getStart() {
            return start;
        }

        public int getSize() {
            return size;
        }
    }

    /*
     *********************************************************************
       * Helper methods.
     *********************************************************************
     */

    /**
     * Parses a given value (for a given column type) returned by response JSON query body from EL server.
     *
     * @param column       The data column definition.
     * @param valueElement The value element from JSON query response to format.
     * @return The object value for the given column type.
     */
    public Object parseValue(DataSetMetadata metadata, DataColumn column, JsonElement valueElement) throws ParseException {
        if (column == null || valueElement == null || valueElement.isJsonNull()) return null;
        if (!valueElement.isJsonPrimitive())
            throw new RuntimeException("Not expected JsonElement type to parse from query response.");
        
        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) metadata.getDefinition();
        JsonPrimitive valuePrimitive = valueElement.getAsJsonPrimitive();
        ColumnType columnType = column.getColumnType();

        if (ColumnType.TEXT.equals(columnType)) {
            return typeMapper.parseText(def, column.getId(), valueElement.getAsString());
        } else if (ColumnType.LABEL.equals(columnType)) {
            boolean isColumnGroup = column.getColumnGroup() != null && column.getColumnGroup().getStrategy().equals(GroupStrategy.FIXED);
            return typeMapper.parseLabel(def, column.getId(), valueElement.getAsString(), isColumnGroup);
            
        } else if (ColumnType.NUMBER.equals(columnType)) {
            return typeMapper.parseNumeric(def, column.getId(), valueElement.getAsString());
            
        } else if (ColumnType.DATE.equals(columnType)) {

            // We can expect two return core types from EL server when handling dates:
            // 1.- String type, using the field pattern defined in the index' mappings, when it's result of a query without aggregations.
            // 2.- Numeric type, when it's result from a scalar function or a value pickup.

            if (valuePrimitive.isString()) {
                return typeMapper.parseDate(def, column.getId(), valuePrimitive.getAsString());
            }

            if (valuePrimitive.isNumber()) {
                return typeMapper.parseDate(def, column.getId(), valuePrimitive.getAsLong());
            }

        }
        
        throw new UnsupportedOperationException("Cannot parse value for column with id [" + column.getId() + "] (Data Set UUID [" + def.getUUID() + "]). Value core type not supported. Expecting string or number or date core field types.");

    }

    /**
     * Formats the given value in a String type in order to send the JSON query body to the EL server.
     */
    public String formatValue(String columnId, DataSetMetadata metadata, Object value) {
        if (value == null) return null;

        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) metadata.getDefinition();
        ColumnType columnType = metadata.getColumnType(columnId);

        if (ColumnType.TEXT.equals(columnType)) {
            return typeMapper.formatText(def, columnId, value != null ? value.toString() : null);
        } else if (ColumnType.LABEL.equals(columnType)) {
            return typeMapper.formatLabel(def, columnId, value != null ? value.toString() : null);
        } else if (ColumnType.DATE.equals(columnType)) {
            return typeMapper.formatDate(def, columnId, (Date) value);
        } else if (ColumnType.NUMBER.equals(columnType)) {
            return typeMapper.formatNumeric(def, columnId, (Number) value);
        }

        throw new UnsupportedOperationException("Cannot format value for column with id [" + columnId + "] (Data Set UUID [" + def.getUUID() + "]). Value core type not supported. Expecting string or number or date core field types.");
    }

    public static String getInterval(DateIntervalType dateIntervalType) {
        String intervalExpression;
        switch (dateIntervalType) {
            case MILLISECOND:
                intervalExpression = "0.001s";
                break;
            case HUNDRETH:
                intervalExpression = "0.01s";
                break;
            case TENTH:
                intervalExpression = "0.1s";
                break;
            case SECOND:
                intervalExpression = "1s";
                break;
            case MINUTE:
                intervalExpression = "1m";
                break;
            case HOUR:
                intervalExpression = "1h";
                break;
            case DAY:
                intervalExpression = "1d";
                break;
            case DAY_OF_WEEK:
                intervalExpression = "1d";
                break;
            case WEEK:
                intervalExpression = "1w";
                break;
            case MONTH:
                intervalExpression = "1M";
                break;
            case QUARTER:
                intervalExpression = "1q";
                break;
            case YEAR:
                intervalExpression = "1y";
                break;
            case DECADE:
                intervalExpression = "10y";
                break;
            case CENTURY:
                intervalExpression = "100y";
                break;
            case MILLENIUM:
                intervalExpression = "1000y";
                break;
            default:
                throw new RuntimeException("No interval mapping for date interval type [" + dateIntervalType.name() + "].");
        }
        return intervalExpression;
    }

    protected JestClient buildClient() throws IllegalArgumentException {
        return client = buildNewClient(serverURL, clusterName, timeout);
    }

    public static JestClient buildNewClient(String serverURL, String clusterName, int timeout) throws IllegalArgumentException {
        if (serverURL == null || serverURL.trim().length() == 0)
            throw new IllegalArgumentException("Parameter serverURL is missing.");
        if (clusterName == null || clusterName.trim().length() == 0)
            throw new IllegalArgumentException("Parameter clusterName is missing.");

        // TODO: use clusterName.
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(serverURL)
                .multiThreaded(true)
                .connTimeout(timeout)
                .build());

        return factory.getObject();
    }

    public ElasticSearchClientFactory getClientFactory() {
        return clientFactory;
    }

    public ElasticSearchValueTypeMapper getValueTypeMapper() {
        return typeMapper;
    }

    public ElasticSearchUtils getUtils() {
        return utils;
    }

    private void log(String message) {
        /*if (logger.isDebugEnabled()) {
            logger.debug(message);
        }*/
    }

}
