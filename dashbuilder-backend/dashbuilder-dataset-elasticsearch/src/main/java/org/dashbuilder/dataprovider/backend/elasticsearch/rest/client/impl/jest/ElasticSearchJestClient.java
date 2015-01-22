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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest;

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
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson.FieldMapping;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.*;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.backend.BackendIntervalBuilderDynamicDate;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.*;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>The Jest/GSON client for ElasticSearch server.</p>
 
 * <p>Usage:</p>
 * <ul>
 *     <li><a href="https://github.com/searchbox-io/Jest/tree/master/jest">https://github.com/searchbox-io/Jest/tree/master/jest</a></li>
 *     <li><a href="http://www.ibm.com/developerworks/java/library/j-javadev2-24/index.html?ca=drs-">http://www.ibm.com/developerworks/java/library/j-javadev2-24/index.html?ca=drs-</a></li>
 *     <li><a href="https://sites.google.com/site/gson/gson-user-guide">https://sites.google.com/site/gson/gson-user-guide</a></li>
 * </ul> 
 * 
 * @see <a href="https://github.com/searchbox-io/Jest">https://github.com/searchbox-io/Jest</a>
 */
@ApplicationScoped
@Named("elasticsearchJestClient")
public class ElasticSearchJestClient implements ElasticSearchClient<ElasticSearchJestClient> {

    protected static final String EL_DATE_FORMAT_YEAR = "yyyy";
    protected static final String EL_DATE_FORMAT_MONTH = "yyyy-MM";
    protected static final String EL_DATE_FORMAT_DAY = "yyyy-MM-dd";
    protected static final String EL_DATE_FORMAT_DAY_OF_WEEK = "yyyy-MM-dd";
    protected static final String EL_DATE_FORMAT_HOUR = "hh";
    protected static final String EL_DATE_FORMAT_MINUTE= "mm";
    protected static final String EL_DATE_FORMAT_SECOND = "ss";
    protected static final int RESPONSE_CODE_OK = 200;
    
    protected String serverURL;
    protected String clusterName;
    protected String[] index;
    protected String[] type;
    // Defaults to 30sec.
    protected int timeout = 30000;
    
    // JestClient is designed to be singleton, don't construct it for each request.
    private JestClient client;
    // TODO: @Inject -> Not working
    protected BackendIntervalBuilderDynamicDate intervalBuilder;

    public ElasticSearchJestClient() {
        intervalBuilder = new BackendIntervalBuilderDynamicDate();
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
            if (index == null) throw new IllegalArgumentException("You cannot call elasticsearchRESTEasyClient#type before calling elasticsearchRESTEasyClient#index."); 
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
                result[x++]  = indexMappings;
            }
            return new MappingsResponse(RESPONSE_CODE_OK, result);
        } catch (Exception e) {
            throw  new ElasticSearchClientGenericException("Cannot obtain mappings.", e);
        }
    }

    protected IndexMappingResponse getMappings(String index, String type) throws Exception{
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
                FieldMapping fieldMappings = new Gson().fromJson(propertyMapping.getValue(), FieldMapping.class);
                FieldMappingResponse.FieldType fieldType = null;
                if (fieldMappings.getType() != null) fieldType = FieldMappingResponse.FieldType.valueOf(fieldMappings.getType().toUpperCase());
                FieldMappingResponse.IndexType indexType = null;
                if (fieldMappings.getIndex() != null) indexType = FieldMappingResponse.IndexType.valueOf(fieldMappings.getIndex().toUpperCase());
                String format = fieldMappings.getFormat();
                FieldMappingResponse fieldMappingResponse = new FieldMappingResponse(field, fieldType, indexType, format);
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
            return new CountResponse((long)hitCount, totalShards);
        } catch (Exception e) {
            throw new ElasticSearchClientGenericException("Cannot count.", e);
        }
    }

    /**
     * TODO: Improve using search types - http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-search-type.html        
     * @param definition The dataset definition.
     * @param request The search reuest.
     * @return
     * @throws ElasticSearchClientGenericException
     */
    @Override
    public SearchResponse search(DataSetDef definition, SearchRequest request) throws ElasticSearchClientGenericException {
        if (client == null) throw new IllegalArgumentException("elasticsearchRESTEasyClient instance is not build.");

        DataSetMetadata metadata = request.getMetadata();
        ElasticSearchDataSetDef elasticSearchDataSetDef = (ElasticSearchDataSetDef) definition;
        String[] index = request.getIndexes();
        String[] type = request.getTypes();
        String[] fields = request.getFields();
        int start = request.getStart();
        int size = request.getSize();
        List<DataSetGroup> aggregations = request.getAggregations();
        List<DataSetSort> sorting = request.getSorting();
        Query query = request.getQuery();

        // The order for column ids in the resulting dataset, based on the lookup definition.
        List<DataColumn> columns = new LinkedList<DataColumn>();

        // Crate the Gson builder and instance.        
        // Set the response deserializers passing the resuling columns order expected by dataset.
        GsonBuilder builder = new GsonBuilder();
        JsonSerializer aggregationSerializer = (JsonSerializer) buildAggregationSerializer().setDataSetMetadata(metadata).setDataSetDefinition(elasticSearchDataSetDef).setColumns(columns);
        JsonSerializer querySerializer = (JsonSerializer) buildQuerySerializer().setDataSetMetadata(metadata).setDataSetDefinition(elasticSearchDataSetDef).setColumns(columns);
        JsonSerializer searchQuerySerializer = (JsonSerializer) buildSearchQuerySerializer().setDataSetMetadata(metadata).setDataSetDefinition(elasticSearchDataSetDef).setColumns(columns);
        JsonDeserializer searchResponseDeserializer = (JsonDeserializer) buildSearchResponseDeserializer().setDataSetMetadata(metadata).setDataSetDefinition(elasticSearchDataSetDef).setColumns(columns);
        JsonDeserializer hitDeserializer = (JsonDeserializer) buildHitDeserializer().setDataSetMetadata(metadata).setDataSetDefinition(elasticSearchDataSetDef).setColumns(columns);
        JsonDeserializer aggreationsDeserializer = (JsonDeserializer) buildAggregationsDeserializer().setDataSetMetadata(metadata).setDataSetDefinition(elasticSearchDataSetDef).setColumns(columns);
        
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
        SearchQuery searchQuery = new SearchQuery(fields, gsonQuery, aggregationObjects, start, size);
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
            result = client.execute(searchRequest);
        } catch (Exception e) {
            throw new ElasticSearchClientGenericException("An error ocurred during search operation.", e);
        }
        JsonObject resultObject = result.getJsonObject();
        if (resultObject.get("error") != null) {
            String errorMessage = resultObject.get("error").getAsString();
            throw new ElasticSearchClientGenericException("An error ocurred during search operation. This is the internal error: \n" + errorMessage);
        }
        SearchResponse searchResult = gson.fromJson(resultObject, SearchResponse.class);
        return searchResult;
    }

    protected static LinkedHashMap<String, Object> orderFields(Map<String, Object> fields, List<DataColumn> columns) {
        if (fields == null) return null;
        if (columns == null) return new LinkedHashMap<String, Object>(fields);

        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (DataColumn column : columns) {
            String columnId = column.getId();
            if (fields.containsKey(columnId)) result.put(columnId, fields.get(columnId));            
        }
        return result;
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
    }

    /**
     * Abtract adaptar implementation. Serializers/deserializers need the DataSetMetadata, the source dataset definition and the columns list to fill during serialization.
     */
    public abstract  class AbstractAdapter<T extends AbstractAdapter> {
        protected DataSetMetadata metadata;
        protected List<DataColumn> columns;
        protected ElasticSearchDataSetDef definition;

        public AbstractAdapter() {
        }

        public AbstractAdapter setDataSetMetadata(DataSetMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public AbstractAdapter setColumns(List<DataColumn> columns) {
            this.columns = columns;
            return this;
        }

        public AbstractAdapter setDataSetDefinition(ElasticSearchDataSetDef definition) {
            this.definition = definition;
            return this;
        }
    }

    public class SearchQuerySerializer extends AbstractAdapter<SearchQuerySerializer> implements JsonSerializer<SearchQuery> {

        protected static final String FIELDS = "fields";
        protected static final String FROM = "from";
        protected static final String QUERY = "query";
        protected static final String SIZE = "size";
        protected static final String AGGREGATIONS = "aggregations";
        

        public JsonObject serialize(SearchQuery searchQuery, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            
            String[] fields = searchQuery.fields;
            JsonObject query = searchQuery.query;
            List<JsonObject> aggregations = searchQuery.aggregations;
            boolean existAggregations = aggregations != null;

            // Trimming.
            // If aggregations exist, we care about the aggregation results, not document results.
            int start = searchQuery.start;
            int size = searchQuery.size;
            int sizeToPull =  existAggregations ? 0 : size;
            int startToPull  = existAggregations ? 0 : start;

            result.addProperty(FROM, startToPull);
            result.addProperty(SIZE, sizeToPull);
            
            // Build the search request in EL expected JSON format.
            if (query != null)  {
                JsonObject queryObject = query.getAsJsonObject(QUERY);
                result.add(QUERY, queryObject);
            }

            // TODO: Use all aggregations, not just first one.
            if (existAggregations) {
                JsonObject aggregationObject = aggregations.get(0);
                JsonObject aggregationsSubObject = aggregationObject.getAsJsonObject(AGGREGATIONS);
                result.add(AGGREGATIONS, aggregationsSubObject);
            }
            
            // If neither query or aggregations exists (just retrieving all element with optinal sort operation), perform a "match_all" query to EL server.
            if (query == null && !existAggregations) {
                JsonObject queryObject = new JsonObject();
                queryObject.add("match_all", new JsonObject());
                result.add("query", queryObject);
            }

            // Add the fields to retrieve, if apply.
            if (!existAggregations) {
                JsonArray fieldsArray = new JsonArray();
                for (String field : fields) {
                    fieldsArray.add(new JsonPrimitive(field));

                    // As no exist aggreations, resulting columns to add into resulting dataset provides from metadata diefinition.
                    ColumnType columnType = metadata.getColumnType(field);
                    if (columns != null) {
                        DataColumn column = new DataColumnImpl(field, columnType);
                        columns.add(column);
                    }
                    
                }
                result.add(FIELDS, fieldsArray);
            }
            
            return result;
        }
    }

    
    
    /**
     * Serializes DataSetGroup operations.
     *
     * TODO: support for join attribute.
     */
    public class AggregationSerializer extends AbstractAdapter<AggregationSerializer> implements JsonSerializer<DataSetGroup> {
        protected static final String AGG_ORDER_ASC = "asc";
        protected static final String AGG_ORDER_DESC = "desc";
        protected static final String AGG_FIELD = "field";
        protected static final String AGG_TERM = "_term";
        protected static final String AGG_ORDER = "order";
        protected static final String AGG_MIN_DOC_COUNT = "min_doc_count";
        protected static final String AGG_TERMS = "terms";
        protected static final String AGG_AGGREGATIONS = "aggregations";
        protected static final String AGG_INTERVAL = "interval";
        protected static final String AGG_KEY = "_key";
        protected static final String AGG_HISTORGRAM = "histogram";
        protected static final String AGG_FORMAT = "format";
        protected static final String AGG_DATE_HISTORGRAM = "date_histogram";

        public JsonObject serialize(DataSetGroup groupOp, Type typeOfSrc, JsonSerializationContext context) {
            ColumnGroup columnGroup = groupOp.getColumnGroup();
            List<GroupFunction> groupFunctions = groupOp.getGroupFunctions();

            // Group functions.
            JsonObject aggregationsObject = null;
            if (groupFunctions != null && !groupFunctions.isEmpty()) {
                aggregationsObject = new JsonObject();
                for (GroupFunction groupFunction : groupFunctions) {
                    serializeCoreFunction(aggregationsObject, groupFunction);
                }
            }

            // Group by columns.
            JsonObject groupByObject = null;
            if (columnGroup != null) {
                groupByObject = new JsonObject();
                String columnId = columnGroup.getColumnId();
                String sourceId = columnGroup.getSourceId();

                if (groupFunctions != null && !groupFunctions.isEmpty()) {
                    for (GroupFunction groupFunction : groupFunctions) {
                        if (groupFunction.getFunction() == null) {
                            columnId = groupFunction.getColumnId();
                            if (!sourceId.equals(groupFunction.getSourceId())) throw new RuntimeException("Grouping by this source property [" + sourceId + "] not possible.");
                            if (!existColumnInMetadataDef(sourceId)) throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
                        }
                    }
                }

                serializeGroupByFunction(groupByObject, columnGroup, columnId, aggregationsObject);
            }

            return groupByObject != null ? buildAggregations(groupByObject) : buildAggregations(aggregationsObject);
        }
        
        protected JsonObject buildAggregations(JsonObject object) {
            JsonObject result = new JsonObject();
            result.add("aggregations", object);
            return result;
        }

        /**
         * <p>Serializes a groupby function.</p>
         * <p>Example of TERM HISTOGRAM function serialization:</p>
         * <code>
         *     "column_id" : {
         *          "terms" : { "field" : "change" },
         *          "aggregations": {
         *              ....
         *          }
         *     }
         * </code>
         * @return
         */
        protected void serializeGroupByFunction(JsonObject parent, ColumnGroup columnGroup, String resultingColumnId, JsonObject aggregationsObject) {
            if (columnGroup == null || metadata == null) return;

            String sourceId = columnGroup.getSourceId();
            if (resultingColumnId == null) resultingColumnId = sourceId;
            boolean asc = columnGroup.isAscendingOrder();
            String order = asc ? AGG_ORDER_ASC : AGG_ORDER_DESC;
            ColumnType columnType = metadata.getColumnType(sourceId);
            GroupStrategy groupStrategy = columnGroup.getStrategy();
            String intervalSize = columnGroup.getIntervalSize();
            // TODO: Support for maxIntervals.
            int maxIntervals = columnGroup.getMaxIntervals();

            if (ColumnType.LABEL.equals(columnType)) {
                // Translate into a TERMS aggregation.
                JsonObject subObject = new JsonObject();
                subObject.addProperty(AGG_FIELD, sourceId);
                JsonObject orderObject = new JsonObject();
                orderObject.addProperty(AGG_TERM, order);
                subObject.add(AGG_ORDER, orderObject);
                subObject.addProperty(AGG_MIN_DOC_COUNT, 0);
                JsonObject result = new JsonObject();
                result.add(AGG_TERMS, subObject);
                if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
                parent.add(resultingColumnId, result);

                // Add the resulting dataset column.
                if (columns != null) {
                    ColumnType columnType1 = metadata.getColumnType(resultingColumnId);
                    DataColumn column = new DataColumnImpl(resultingColumnId, columnType1);
                    columns.add(0, column);
                }
                
            } else if (ColumnType.NUMBER.equals(columnType)) {
                // Translate into a HISTOGRAM aggregation.
                JsonObject subObject = new JsonObject();
                subObject.addProperty(AGG_FIELD, sourceId);
                if (intervalSize != null) subObject.addProperty(AGG_INTERVAL, Long.parseLong(intervalSize));
                JsonObject orderObject = new JsonObject();
                orderObject.addProperty(AGG_KEY, order);
                subObject.add(AGG_ORDER, orderObject);
                subObject.addProperty(AGG_MIN_DOC_COUNT, 0);
                JsonObject result = new JsonObject();
                result.add(AGG_HISTORGRAM, subObject);
                if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
                parent.add(resultingColumnId, result);

                // Add the resulting dataset column.
                if (columns != null) {
                    ColumnType columnType1 = metadata.getColumnType(resultingColumnId);
                    DataColumn column = new DataColumnImpl(resultingColumnId, columnType1);
                    columns.add(0, column);
                }
            } else if (ColumnType.DATE.equals(columnType)) {
                // Translate into a DATE HISTOGRAM aggregation.
                DateIntervalType dateIntervalType = null;

                if (GroupStrategy.DYNAMIC.equals(columnGroup.getStrategy())) {
                    Date[] limits = null;
                    try {
                        limits = calculateDateLimits(columnGroup.getSourceId());
                    } catch (ElasticSearchClientGenericException e) {
                        throw new RuntimeException("Cannot calculate date limits.", e);
                    }
                    if (limits != null) {
                        dateIntervalType = intervalBuilder.calculateIntervalSize(limits[0], limits[1], columnGroup);
                    }
                } else {
                    dateIntervalType = DateIntervalType.valueOf(intervalSize);
                }

                String intervalFormat = null;
                String returnFormat = null;
                switch (dateIntervalType) {
                    case MILLISECOND:
                        intervalFormat = "0.001s";
                        break;
                    case HUNDRETH:
                        intervalFormat = "0.01s";
                        break;
                    case TENTH:
                        intervalFormat = "0.1s";
                        break;
                    case SECOND:
                        returnFormat = EL_DATE_FORMAT_SECOND;
                        intervalFormat = "1s";
                        break;
                    case MINUTE:
                        returnFormat = EL_DATE_FORMAT_MINUTE;
                        intervalFormat = "1m";
                        break;
                    case HOUR:
                        returnFormat = EL_DATE_FORMAT_HOUR;
                        intervalFormat = "1h";
                        break;
                    case DAY:
                        intervalFormat = "1d";
                        returnFormat = EL_DATE_FORMAT_DAY;
                        break;
                    case DAY_OF_WEEK:
                        returnFormat = EL_DATE_FORMAT_DAY_OF_WEEK;
                        intervalFormat = "1d";
                        break;
                    case WEEK:
                        intervalFormat = "1w";
                        break;
                    case MONTH:
                        intervalFormat = "1M";
                        returnFormat = EL_DATE_FORMAT_MONTH;
                        break;
                    case QUARTER:
                        intervalFormat = "1q";
                        break;
                    case YEAR:
                        intervalFormat = "1y";
                        returnFormat = EL_DATE_FORMAT_YEAR;
                        break;
                    case DECADE:
                        intervalFormat = "10y";
                        break;
                    case CENTURY:
                        intervalFormat = "100y";
                        break;
                    case MILLENIUM:
                        intervalFormat = "1000y";
                        break;
                    default:
                        throw new RuntimeException("No interval mapping for date interval type [" + dateIntervalType.name() + "].");
                }

                JsonObject subObject = new JsonObject();
                subObject.addProperty(AGG_FIELD, sourceId);
                subObject.addProperty(AGG_INTERVAL, intervalFormat);
                if (returnFormat != null) subObject.addProperty(AGG_FORMAT, returnFormat);
                JsonObject orderObject = new JsonObject();
                orderObject.addProperty(AGG_KEY, order);
                subObject.add(AGG_ORDER, orderObject);
                subObject.addProperty(AGG_MIN_DOC_COUNT, 0);
                JsonObject result = new JsonObject();
                result.add(AGG_DATE_HISTORGRAM, subObject);
                if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
                parent.add(resultingColumnId, result);

                // Add the resulting dataset column.
                if (columns != null) {
                    ColumnType columnType1 = metadata.getColumnType(resultingColumnId);
                    DataColumn column = new DataColumnImpl(resultingColumnId, columnType1);
                    columns.add(0, column);
                }
            } else {
                throw new RuntimeException("No translation supported for column group with sourceId [" + sourceId + "] and group strategy [" + groupStrategy.name() + "].");
            }
        }
        

        /**
         * <p>Serializes a core function.</p>
         * <p>Example of SUM function serialization:</p>
         * <code>
         *     "column_id" : {
         *          "sum" : { "field" : "change" }
         *     }
         * </code>
         * @return
         */
        protected void serializeCoreFunction(JsonObject parent, GroupFunction groupFunction) {
            if (parent != null && groupFunction != null) {
                String sourceId = groupFunction.getSourceId();
                if (sourceId != null && !existColumnInMetadataDef(sourceId)) throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
                if (sourceId == null) sourceId = metadata.getColumnId(0);
                if (sourceId == null) throw new IllegalArgumentException("Aggregation from unknown column id.");
                String columnId = groupFunction.getColumnId();
                if (columnId == null) columnId = sourceId;

                AggregateFunctionType type = groupFunction.getFunction();
                String aggregationName = null;
                ColumnType resultingColumnType = null;
                switch (type) {
                    case COUNT:
                        aggregationName = "value_count";
                        resultingColumnType = ColumnType.NUMBER;
                        break;
                    case DISTINCT:
                        aggregationName = "cardinality";
                        resultingColumnType = ColumnType.LABEL;
                        break;
                    case AVERAGE:
                        aggregationName = "avg";
                        resultingColumnType = ColumnType.NUMBER;
                        break;
                    case SUM:
                        aggregationName = "sum";
                        resultingColumnType = ColumnType.NUMBER;
                        break;
                    case MIN:
                        aggregationName = "min";
                        resultingColumnType = ColumnType.NUMBER;
                        break;
                    case MAX:
                        aggregationName = "max";
                        resultingColumnType = ColumnType.NUMBER;
                        break;

                }
                JsonObject fieldObject = new JsonObject();
                fieldObject.addProperty("field", sourceId);
                JsonObject subObject = new JsonObject();
                subObject.add(aggregationName, fieldObject);
                parent.add(columnId, subObject);
                
                // Add the resulting dataset column.
                if (columns != null) {
                    DataColumn column = new DataColumnImpl(columnId, resultingColumnType);
                    columns.add(column);                    
                }
            }
        }

        /**
         * <p>Obtain the minimum date and maximum date values for the given column with identifier <code>dateColumnId</code>.</p>
         *
         * TODO: Apply filters?
         * @param dateColumnId The column identifier for the date type column.
         * @return The minimum and maximum dates.
         */
        protected Date[] calculateDateLimits(String dateColumnId) throws ElasticSearchClientGenericException{
            if (client == null) throw new IllegalArgumentException("ElasticSearchRestClient instance is not build.");

            String minDateColumnId = dateColumnId + "_min";
            String maxDateColumnId = dateColumnId + "_max";

            // Create the aggregation model to bulid the query to EL server.
            DataSetGroup aggregation = new DataSetGroup();
            GroupFunction minFunction = new GroupFunction(dateColumnId, minDateColumnId, AggregateFunctionType.MIN);
            GroupFunction maxFunction = new GroupFunction(dateColumnId, maxDateColumnId, AggregateFunctionType.MAX);
            aggregation.addGroupFunction(minFunction, maxFunction);
            
            // Serialize the aggregation.
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(DataSetGroup.class, new AggregationSerializer().setDataSetMetadata(metadata));
            Gson gson = builder.create();
            String serializedAggregation = gson.toJson(aggregation, DataSetGroup.class);

            Search.Builder searchRequestBuilder = new Search.Builder(serializedAggregation).addIndex(index[0]);
            if (type != null && type.length > 0) searchRequestBuilder.addType(type[0]);
            
            Search searchRequest = searchRequestBuilder.build();
            JestResult result = null;
            try {
                result = client.execute(searchRequest);
            } catch (Exception e) {
                throw new ElasticSearchClientGenericException("An error ocurred during search operation.", e);
            }

            SearchResponse searchResult = gson.fromJson(result.getJsonObject(), SearchResponse.class);
            if (searchResult != null) {
                SearchHitResponse[] hits = searchResult.getHits();
                if (hits != null && hits.length == 1) {
                    SearchHitResponse hit0 = hits[0];
                    Map<String, Object> fields = hit0.getFields();
                    if (fields != null && !fields.isEmpty()) {
                        long minValue = (Long) fields.get(minDateColumnId);
                        long maxValue = (Long) fields.get(maxDateColumnId);
                        return new Date[] {new Date(minValue), new Date(maxValue)};
                    }
                }
            }
            
            return null;
        }
        
        protected boolean existColumnInMetadataDef(String name) {
            if (name == null || metadata == null) return false;

            int cols = metadata.getNumberOfColumns();
            for (int x = 0; x < cols; x++) {
                String colName = metadata.getColumnId(x);
                if (name.equals(colName)) return true;
            }
            return false;
        }
    }

    public class AggregationsDeserializer extends AbstractAdapter<AggregationsDeserializer> implements JsonDeserializer<SearchHitResponse[]> {

        @Override
        public SearchHitResponse[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<SearchHitResponse> result = null;

            if (typeOfT.equals(SearchHitResponse[].class)) {
                JsonObject aggregationsObject = (JsonObject) json;
                Set<Map.Entry<String, JsonElement>> entries = aggregationsObject.entrySet();
                if (entries != null && !entries.isEmpty()) {
                    Map<String, Object> noBucketFields = new HashMap<String, Object>();
                    result = new LinkedList<SearchHitResponse>();
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        String columnId = entry.getKey();
                        JsonElement columnAggregationElement = entry.getValue();
                        
                        // Process bucketed aggregations.
                        if (columnAggregationElement != null && columnAggregationElement.isJsonObject()) {
                            // Process the result buckets.
                            JsonElement bucketsElement = ((JsonObject)columnAggregationElement).get("buckets");
                            if (bucketsElement != null && bucketsElement.isJsonArray()) {
                                Iterator<JsonElement> bucketsIt = ((JsonArray)bucketsElement).iterator();
                                while (bucketsIt.hasNext()) {
                                    Map<String, Object> bucketFields = new HashMap<String, Object>();
                                    JsonObject bucket = (JsonObject) bucketsIt.next();
                                    Set<Map.Entry<String, JsonElement>> bucketEntries = bucket.entrySet();
                                    for (Map.Entry<String, JsonElement> bucketEntry : bucketEntries) {
                                        String aggName = bucketEntry.getKey();
                                        if ("key".equals(aggName)) {
                                            String value = bucketEntry.getValue().getAsString();
                                            bucketFields.put(columnId, value);
                                        } else if ("doc_count".equals(aggName)) {
                                            // Do nothing.
                                        } else {
                                            JsonElement aggValue = ((JsonObject) bucketEntry.getValue()).get("value");
                                            String _aggValue = null;
                                            if (aggValue != null) _aggValue = aggValue.getAsString();
                                            bucketFields.put(aggName, _aggValue);
                                        }
                                    }
                                    result.add(new SearchHitResponse(orderFields(bucketFields, columns)));
                                }
                            } else {
                                // Process no bucketed aggregations.
                                JsonElement aggValueElement = ((JsonObject)columnAggregationElement).get("value");
                                if (aggValueElement != null && aggValueElement.isJsonPrimitive()) noBucketFields.put(columnId, aggValueElement.getAsString());    
                            }
                        }
                    }
                    if (!noBucketFields.isEmpty()) result.add(new SearchHitResponse(orderFields(noBucketFields, columns)));
                }
            }

            if (result == null) return null;
            return result.toArray(new SearchHitResponse[result.size()]);
        }
    }

    public class SearchResponseDeserializer extends AbstractAdapter<SearchResponseDeserializer> implements JsonDeserializer<SearchResponse> {

        @Override
        public SearchResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SearchResponse result = null;
            if (typeOfT.equals(SearchResponse.class)) {
                JsonObject responseObject = json.getAsJsonObject();
                
                if (responseObject != null) {
                    long tookInMillis = responseObject.get("took").getAsLong();
                    int responseStatus = RESPONSE_CODE_OK;

                    JsonObject shardsObject = responseObject.getAsJsonObject("_shards");
                    int totalShards = shardsObject.get("total").getAsInt();
                    int successfulShards = shardsObject.get("successful").getAsInt();
                    int shardFailures = shardsObject.get("failed").getAsInt();

                    List<SearchHitResponse> hits = null;
                    Object[] hitsParseResult = parseTotalAndScore(responseObject);
                    long totalHits = (Long) hitsParseResult[0];
                    float maxScore = (Float) hitsParseResult[1];
                    
                    // Check the resulting aggregations, if exist.
                    JsonElement aggregations = responseObject.get("aggregations");
                    boolean existAggregations = aggregations != null && aggregations.isJsonObject();
                    
                    // If exist aggregations, discard hit results.
                    if (!existAggregations) {
                        // Parse hit results from "hits" resulting field.
                        hits = parseHits(responseObject, context); 
                    } else {
                        // Parse hit results from "aggregations" resulting field.
                        hits = parseAggregations(responseObject, context);
                    }

                    result = new SearchResponse(tookInMillis, responseStatus, totalHits, maxScore, totalShards, successfulShards, shardFailures, columns, hits.toArray(new SearchHitResponse[hits.size()]));
                }
            }
            
            return result;
        }
        
        protected List<SearchHitResponse> parseAggregations(JsonObject responseObject, JsonDeserializationContext context) {
            JsonObject aggregationsObject = responseObject.getAsJsonObject("aggregations");
            if (aggregationsObject != null) {
                SearchHitResponse[] hits = context.deserialize(aggregationsObject, SearchHitResponse[].class);
                if (hits != null) return Arrays.asList(hits);
            }
            
            return null;
        }
        
        protected List<SearchHitResponse> parseHits(JsonObject responseObject, JsonDeserializationContext context) {
            List<SearchHitResponse> hits = null;
            JsonObject hitsObject = responseObject.getAsJsonObject("hits");
            if (hitsObject != null) {
                JsonArray hitsArray = hitsObject.getAsJsonArray("hits");
                if (hitsArray != null && hitsArray.size() > 0) {
                    hits = new LinkedList<SearchHitResponse>();
                    for (int i = 0; i < hitsArray.size() ; i++) {
                        JsonElement hitResponseElement = hitsArray.get(i);
                        SearchHitResponse hit = context.deserialize(hitResponseElement, SearchHitResponse.class);
                        hits.add(hit);
                    }
                }
            }
            return hits;
        }

        protected Object[] parseTotalAndScore(JsonObject responseObject) {
            long totalHits = 0;
            float maxScore = 0;
            JsonObject hitsObject = responseObject.getAsJsonObject("hits");
            if (hitsObject != null) {
                JsonElement totalElement = hitsObject.get("total"); 
                if (totalElement.isJsonPrimitive()) totalHits = totalElement.getAsLong();
                JsonElement scoreElement = hitsObject.get("max_score");
                if (scoreElement.isJsonPrimitive()) maxScore = scoreElement.getAsFloat();
            }
            return new Object[] {totalHits, maxScore};
        }
    }

    public class HitDeserializer extends AbstractAdapter<HitDeserializer> implements JsonDeserializer<SearchHitResponse> {
        
        @Override
        public SearchHitResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SearchHitResponse result = null;
            if (typeOfT.equals(SearchHitResponse.class)) {

                JsonObject hitObject = (JsonObject) json;
                float score = 0;
                JsonElement scoreElement = hitObject.get("_score"); 
                if (scoreElement != null && scoreElement.isJsonPrimitive()) score = scoreElement.getAsFloat();
                String index = hitObject.get("_index").getAsString();
                String id = hitObject.get("_id").getAsString();
                String type = hitObject.get("_type").getAsString();
                long version = 0;
                Map<String ,Object> fields = new HashMap<String, Object>();
                JsonElement sourceObject = hitObject.get("_source");
                JsonElement fieldsObject = hitObject.get("fields");
                
                if (fieldsObject != null && fieldsObject.isJsonObject()) {
                    Set<Map.Entry<String, JsonElement>> _fields = ((JsonObject)fieldsObject).entrySet();
                    for (Map.Entry<String, JsonElement> field : _fields) {
                        String fieldName = field.getKey();
                        JsonElement fieldValueArray = field.getValue();
                        if (fieldValueArray != null && fieldValueArray.isJsonArray()) {
                            Iterator fieldValueArrayIt = ((JsonArray)fieldValueArray).iterator();
                            while (fieldValueArrayIt.hasNext()) {
                                JsonElement element = (JsonElement) fieldValueArrayIt.next();
                                if (element != null && element.isJsonPrimitive()) fields.put(fieldName, element.getAsString()); 
                            }
                        }
                    }
                }
                
                if (sourceObject != null && sourceObject.isJsonObject()) {
                    Set<Map.Entry<String, JsonElement>> _fields = ((JsonObject)sourceObject).entrySet();
                    for (Map.Entry<String, JsonElement> field : _fields) {
                        String fieldName = field.getKey();
                        String fieldValue = field.getValue().getAsString();
                        fields.put(fieldName, fieldValue);
                    }
                    
                }
                
                result = new SearchHitResponse(score, index, id, type, version, orderFields(fields, columns));
            }
            
            return result;
        }
        
    }

    public class QuerySerializer extends AbstractAdapter<QuerySerializer> implements JsonSerializer<Query> {
        public static final String SEARCH_API_FIELD = "field";
        public static final String SEARCH_API_EXISTS = "exists";
        public static final String SEARCH_API_TERM = "term";
        public static final String SEARCH_API_LT = "lt";
        public static final String SEARCH_API_LTE = "lte";
        public static final String SEARCH_API_GT = "gt";
        public static final String SEARCH_API_GTE = "gte";
        public static final String SEARCH_API_RANGE = "range";
        public static final String SEARCH_API_AND = "and";
        public static final String SEARCH_API_OR = "or";
        public static final String SEARCH_API_NOT  = "not";
        public static final String SEARCH_API_FILTER = "filter";
        public static final String SEARCH_API_QUERY = "query";
        public static final String SEARCH_API_MATCH = "match";
        public static final String SEARCH_API_MATCH_ALL = "match_all";
        public static final String SEARCH_API_MUST = "must";
        public static final String SEARCH_API_MUST_NOT = "must_not";
        public static final String SEARCH_API_SHOULD = "should";
        public static final String SEARCH_API_BOOL = "bool";
        private Query query;
        private Gson gson = new GsonBuilder().create();

        public JsonObject serialize(Query src, Type typeOfSrc, JsonSerializationContext context) {
            this.query = src;
            
            JsonObject result = new JsonObject();
            JsonObject subResult = translate(query);
            String searchkey = isFilter(subResult) ? SEARCH_API_FILTER : SEARCH_API_QUERY; 
            result.add(searchkey, subResult);
            return result;
        }
        
        private boolean isFilter(JsonObject object) {
            if (object == null) return false;
            String serializedObject = gson.toJson(object).trim();
            boolean isTermQuery = serializedObject.startsWith("{\"" + SEARCH_API_TERM);
            boolean isRangeQuery = serializedObject.startsWith("{\"" + SEARCH_API_RANGE);
            boolean isExistsQuery = serializedObject.startsWith("{\"" + SEARCH_API_EXISTS);
            boolean isNotQuery = serializedObject.startsWith("{\"" + SEARCH_API_NOT);
            boolean isOrQuery = serializedObject.startsWith("{\"" + SEARCH_API_OR);
            boolean isAndQuery = serializedObject.startsWith("{\"" + SEARCH_API_AND);            
            return isTermQuery || isRangeQuery || isExistsQuery || isNotQuery || isOrQuery || isAndQuery;
        }

        private JsonObject translate(Query query) {
            if (query == null) return null;

            Query.Type type = query.getType();

            JsonObject result = null;

            switch (type) {
                case BOOL:
                    return translateBool(query);
                case MATCH:
                    return translateMatch(query);
                case MATCH_ALL:
                    return translateMatchAll(query);
                case FILTERED:
                    return translateFiltered(query);
                case AND:
                    return translateAnd(query);
                case OR:
                    return translateOr(query);
                case NOT:
                    return translateNot(query);
                case EXISTS:
                    return translateExists(query);
                case TERM:
                    return translateTerm(query);
                case RANGE:
                    return translateRange(query);
            }

            return result;
        }


        private JsonObject translateExists(Query query) {
            if (query == null) return null;

            String field = query.getField();
            JsonObject result = new JsonObject();
            JsonObject subResult = new JsonObject();
            subResult.addProperty(SEARCH_API_FIELD, field);
            result.add(SEARCH_API_EXISTS, subResult);
            return result;
        }

        private JsonObject translateTerm(Query query) {
            if (query == null) return null;

            String field = query.getField();
            Object value = query.getParam(Query.Parameter.VALUE.name());
            JsonObject result = new JsonObject();
            JsonObject subResult = new JsonObject();
            subResult.addProperty(field, (String) value);
            result.add(SEARCH_API_TERM, subResult);
            return result;
        }

        private JsonObject translateRange(Query query) {
            if (query == null) return null;

            String field = query.getField();
            JsonObject result = new JsonObject();

            JsonObject subResult = new JsonObject();
            addPrimitiveProperty(subResult, SEARCH_API_LT, query.getParam(Query.Parameter.LT.name()));
            addPrimitiveProperty(subResult, SEARCH_API_LTE, query.getParam(Query.Parameter.LTE.name()));
            addPrimitiveProperty(subResult, SEARCH_API_GT, query.getParam(Query.Parameter.GT.name()));
            addPrimitiveProperty(subResult, SEARCH_API_GTE, query.getParam(Query.Parameter.GTE.name()));
            JsonObject subObject = new JsonObject();
            subObject.add(field, subResult);
            result.add(SEARCH_API_RANGE, subObject);
            return result;
        }

        private void addPrimitiveProperty(JsonObject object, String key, Object value) {
            if (value != null) {
                if (value instanceof Number) {
                    object.addProperty(key, (Number) value);
                } else if (value instanceof Date) {
                    String datePattern = definition.getPattern(key);
                    String formattedValue = new SimpleDateFormat(datePattern).format(value);
                    object.addProperty(key, formattedValue);
                } else {
                    object.addProperty(key, value.toString());
                }
            }
        }

        private JsonObject translateAnd(Query query) {
            if (query == null) return null;

            JsonObject result = new JsonObject();
            JsonElement filterObjects = null;
            try {
                filterObjects = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.FILTERS.name()));
            } catch (ClassCastException e) {
                filterObjects = translate((Query) query.getParam(Query.Parameter.FILTERS.name()));
            }
            result.add(SEARCH_API_AND, filterObjects);
            return result;
        }

        private JsonObject translateOr(Query query) {
            if (query == null) return null;

            JsonObject result = new JsonObject();
            JsonElement filterObjects = null;
            try {
                filterObjects = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.FILTERS.name()));
            } catch (ClassCastException e) {
                filterObjects = translate((Query) query.getParam(Query.Parameter.FILTERS.name()));
            }
            result.add(SEARCH_API_OR, filterObjects);
            return result;
        }

        private JsonObject translateNot(Query query) {
            if (query == null) return null;

            JsonObject result = new JsonObject();
            JsonElement filterObjects = null;
            try {
                filterObjects = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.FILTER.name()));
            } catch (ClassCastException e) {
                filterObjects = translate((Query) query.getParam(Query.Parameter.FILTER.name()));
            }
            result.add(SEARCH_API_NOT, filterObjects);
            return result;
        }

        private JsonObject translateFiltered(Query query) {
            if (query == null) return null;

            Query _query = (Query) query.getParam(Query.Parameter.QUERY.name());
            Query filter = (Query) query.getParam(Query.Parameter.FILTER.name());

            JsonObject queryObject = translate(_query);
            JsonObject filterObject = translate(filter);

            JsonObject result = new JsonObject();
            result.add(SEARCH_API_QUERY, queryObject);
            result.add(SEARCH_API_FILTER, filterObject);
            return result;
        }

        private JsonObject translateMatch(Query query) {
            if (query == null) return null;

            String field = query.getField();
            Object value = query.getParam(Query.Parameter.VALUE.name());

            JsonObject result = new JsonObject();
            JsonObject subObject= new JsonObject();
            subObject.addProperty(field, (String) value);
            result.add(SEARCH_API_MATCH, subObject);
            return result;
        }

        private JsonObject translateMatchAll(Query query) {
            if (query == null) return null;

            JsonObject result = new JsonObject();
            result.add(SEARCH_API_MATCH_ALL, new JsonObject());
            return result;
        }

        private JsonObject translateBool(Query query) {
            if (query == null) return null;

            JsonObject result = new JsonObject();

            JsonElement mustObject = null;
            JsonElement mustNotObject = null;
            JsonElement shouldObject = null;
            try {
                mustObject = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.MUST.name()));
            } catch (ClassCastException e) {
                mustObject = translate((Query) query.getParam(Query.Parameter.MUST.name()));
            }
            try {
                mustNotObject = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.MUST_NOT.name()));
            } catch (ClassCastException e) {
                mustNotObject = translate((Query) query.getParam(Query.Parameter.MUST.name()));
            }
            try {
                shouldObject = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.SHOULD.name()));
            } catch (ClassCastException e) {
                shouldObject = translate((Query) query.getParam(Query.Parameter.MUST.name()));
            }

            JsonObject bool = new JsonObject();
            if (mustObject != null) bool.add(SEARCH_API_MUST, mustObject);
            if (mustNotObject != null) bool.add(SEARCH_API_MUST_NOT, mustNotObject);
            if (shouldObject!= null) bool.add(SEARCH_API_SHOULD, shouldObject);
            result.add(SEARCH_API_BOOL, bool);
            return result;
        }

        private JsonElement translateGsonQueries(List<Query> queries) {
            JsonElement result = null;
            if (queries != null && !queries.isEmpty()) {
                result = new JsonObject();
                List<JsonObject> jsonObjects = translateQueries(queries);
                if (jsonObjects.size() == 1) {
                    result = jsonObjects.get(0);
                } else if (jsonObjects.size() > 1) {
                    JsonArray mustArray = new JsonArray();
                    for (JsonObject jsonObject : jsonObjects) {
                        mustArray.add(jsonObject);
                    }
                    result = mustArray;
                }
            }
            return result;
        }

        private List<JsonObject> translateQueries(List<Query> queries) {
            List<JsonObject> result = new LinkedList<JsonObject>();
            for (Query subQuery : queries) {
                JsonObject subObject = translate(subQuery);
                result.add(subObject);
            }
            return result;
        }
    }
    
    /*
     *********************************************************************
       * Helper methods.
     *********************************************************************
     */

    protected JestClient buildClient() throws IllegalArgumentException{
        return  client = buildNewClient();
    }

    protected JestClient buildNewClient() throws IllegalArgumentException{
        if (serverURL == null || serverURL.trim().length() == 0) throw new IllegalArgumentException("Parameter serverURL is missing.");
        if (clusterName == null || clusterName.trim().length() == 0) throw new IllegalArgumentException("Parameter clusterName is missing.");

        // TODO: use clusterName.
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(serverURL)
                .multiThreaded(true)
                .connTimeout(timeout)
                .build());
        
        return factory.getObject();
    }
    
    public AggregationSerializer buildAggregationSerializer() {
        return new AggregationSerializer();
    }
    
    public QuerySerializer buildQuerySerializer() {
        return new QuerySerializer();
    }
    
    public SearchQuerySerializer buildSearchQuerySerializer() {
        return new SearchQuerySerializer();  
    } 
    
    public SearchResponseDeserializer buildSearchResponseDeserializer() {
        return new SearchResponseDeserializer();
    }
    
    public HitDeserializer buildHitDeserializer() {
        return new HitDeserializer();
    }
    
    public AggregationsDeserializer buildAggregationsDeserializer() {
        return new AggregationsDeserializer();
    }
}
