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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchClientFactory;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchDataSetTestBase;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.Query;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.*;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.when;

/**
 * <p>Test unit for Jest EL REST api client.</p>
 *
 * @since 0.3.0
 */
@RunWith(Arquillian.class)
public class ElasticSearchJestClientTest {

    @Mock
    protected DataSetMetadata metadata;
    
    @Mock
    protected ElasticSearchDataSetDef definition;

    protected ElasticSearchClientFactory clientFactory = new ElasticSearchClientFactory();
    protected ElasticSearchValueTypeMapper typeMapper = new ElasticSearchValueTypeMapper();
    protected ElasticSearchUtils utils = new ElasticSearchUtils(typeMapper);
    
    protected ElasticSearchJestClient client = new ElasticSearchJestClient(clientFactory, typeMapper, utils);
    protected ElasticSearchJestClient anotherClient = new ElasticSearchJestClient(clientFactory, typeMapper, utils);

    @Before
    public void setUp() throws Exception {
        // Init the annotated mocks.
        MockitoAnnotations.initMocks(this);
        
        // Init the metadata mocked instance.
        when(metadata.getNumberOfColumns()).thenReturn(6);
        when(metadata.getColumnId(0)).thenReturn("id");
        when(metadata.getColumnType(0)).thenReturn(ColumnType.NUMBER);
        when(metadata.getColumnType("id")).thenReturn(ColumnType.NUMBER);
        when(metadata.getColumnId(1)).thenReturn("city");
        when(metadata.getColumnType(1)).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnType("city")).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnId(2)).thenReturn("department");
        when(metadata.getColumnType(2)).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnType("department")).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnId(3)).thenReturn("employee");
        when(metadata.getColumnType(3)).thenReturn(ColumnType.TEXT);
        when(metadata.getColumnType("employee")).thenReturn(ColumnType.TEXT);
        when(metadata.getColumnId(4)).thenReturn("date");
        when(metadata.getColumnType(4)).thenReturn(ColumnType.DATE);
        when(metadata.getColumnType("date")).thenReturn(ColumnType.DATE);
        when(metadata.getColumnId(5)).thenReturn("amount");
        when(metadata.getColumnType(5)).thenReturn(ColumnType.NUMBER);
        when(metadata.getColumnType("amount")).thenReturn(ColumnType.NUMBER);
        when(metadata.getDefinition()).thenReturn(definition);
        
        // Init the dataset defintion mocked instance.
        when(definition.getServerURL()).thenReturn(ElasticSearchDataSetTestBase.EL_SERVER);
        when(definition.getClusterName()).thenReturn("elasticsearch");
        when(definition.getIndex()).thenReturn("expensereports");
        when(definition.getType()).thenReturn("expense");
        when(definition.getPattern("id")).thenReturn("integer");
        when(definition.getPattern("date")).thenReturn("MM-dd-YYYY");
        
    }

    @Test
    public void testQuerySerializer() {
        GsonBuilder builder = new GsonBuilder();

        QuerySerializer querySerializer = new QuerySerializer(client, metadata, new ArrayList<DataColumn>());
        builder.registerTypeAdapter(Query.class, querySerializer);
        Gson gson = builder.create();
        
        // Match ALL query.
        Query query = new Query(Query.Type.MATCH_ALL);
        String serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"match_all\":{}}}");

        // Match query..
        query = new Query("department", Query.Type.MATCH);
        query.setParam(Query.Parameter.VALUE.name(), "Sales");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"match\":{\"department\":\"Sales\"}}}");

        // Wildcard query..
        query = new Query("department", Query.Type.WILDCARD);
        query.setParam(Query.Parameter.VALUE.name(), "Sal%");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"wildcard\":{\"department\":\"Sal%\"}}}");

        // Query String query.
        query = new Query("employee", Query.Type.QUERY_STRING);
        query.setParam(Query.Parameter.DEFAULT_FIELD.name(), "employee");
        query.setParam(Query.Parameter.DEFAULT_OPERATOR.name(), "AND");
        query.setParam(Query.Parameter.QUERY.name(), "Tony%");
        query.setParam(Query.Parameter.LOWERCASE_EXPANDED_TERMS.name(),"false");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"query_string\":{\"default_field\":\"employee\",\"default_operator\":\"AND\",\"query\":\"Tony%\",\"lowercase_expanded_terms\":\"false\"}}}");

        // Filtered query.
        query = new Query("department", Query.Type.FILTERED);
        Query subQuery = new Query(Query.Type.MATCH_ALL);
        Query subFilter = new Query("amount", Query.Type.TERM);
        subFilter.setParam(Query.Parameter.VALUE.name(), "Sales");
        query.setParam(Query.Parameter.QUERY.name(), subQuery);
        query.setParam(Query.Parameter.FILTER.name(), subFilter);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"term\":{\"amount\":\"Sales\"}}}}}");

        // Boolean query.
        query = new Query("department", Query.Type.BOOL);
        query.setParam(Query.Parameter.MUST.name(), new Query(Query.Type.MATCH_ALL));
        query.setParam(Query.Parameter.MUST_NOT.name(), new Query(Query.Type.MATCH_ALL));
        query.setParam(Query.Parameter.SHOULD.name(), new Query(Query.Type.MATCH_ALL));
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"bool\":{\"must\":{\"match_all\":{}},\"must_not\":{\"match_all\":{}},\"should\":{\"match_all\":{}}}}}");

        // Term filter.
        query = new Query("department", Query.Type.TERM);
        query.setParam(Query.Parameter.VALUE.name(), "Sales");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"filter\":{\"term\":{\"department\":\"Sales\"}}}");

        // And filter.
        query = new Query("department", Query.Type.AND);
        List<Query> filters = new LinkedList<Query>();
        filters.add(new Query("department", Query.Type.TERM).setParam(Query.Parameter.VALUE.name(), "Sales"));
        filters.add(new Query("amount", Query.Type.RANGE).setParam(Query.Parameter.GT.name(), 100));
        query.setParam(Query.Parameter.FILTERS.name(), filters);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"filter\":{\"and\":[{\"term\":{\"department\":\"Sales\"}},{\"range\":{\"amount\":{\"gt\":100}}}]}}");

        // Or filter.
        query = new Query("department", Query.Type.OR);
        filters = new LinkedList<Query>();
        filters.add(new Query("department", Query.Type.TERM).setParam(Query.Parameter.VALUE.name(), "Sales"));
        filters.add(new Query("amount", Query.Type.RANGE).setParam(Query.Parameter.GT.name(), 100));
        query.setParam(Query.Parameter.FILTERS.name(), filters);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"filter\":{\"or\":[{\"term\":{\"department\":\"Sales\"}},{\"range\":{\"amount\":{\"gt\":100}}}]}}");


        // Not filter.
        query = new Query("department", Query.Type.NOT);
        query.setParam(Query.Parameter.FILTER.name(), new Query("city", Query.Type.TERM).setParam(Query.Parameter.VALUE.name(), "London"));
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"filter\":{\"not\":{\"term\":{\"city\":\"London\"}}}}");

        // Exist filter.
        query = new Query("department", Query.Type.EXISTS);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"filter\":{\"exists\":{\"field\":\"department\"}}}");

        // Not filter.
        query = new Query("amount", Query.Type.RANGE);
        query.setParam(Query.Parameter.LT.name(), 100);
        query.setParam(Query.Parameter.LTE.name(), 200);
        query.setParam(Query.Parameter.GT.name(), 300);
        query.setParam(Query.Parameter.GTE.name(), 400);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"filter\":{\"range\":{\"amount\":{\"lt\":100,\"lte\":200,\"gt\":300,\"gte\":400}}}}");
    }
    
    @Test
    public void testSearchResponseDeserialization() throws Exception {
        String response = "{\"took\":4,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\"hits\":{\"total\":8,\"max_score\":2.609438,\"hits\":[{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"12\",\"_score\":2.609438,\"_source\":{\"id\":12, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-02-2012\" , \"amount\":344.9}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"20\",\"_score\":2.609438,\"_source\":{\"id\":20, \"city\": \"Brno\", \"department\": \"Sales\", \"employee\": \"Neva Hunger\" ,\"date\": \"06-11-2011\" , \"amount\":995.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"21\",\"_score\":2.609438,\"_source\":{\"id\":21, \"city\": \"Brno\", \"department\": \"Sales\", \"employee\": \"Neva Hunger\" ,\"date\": \"06-11-2011\" , \"amount\":234.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"10\",\"_score\":2.2039728,\"_source\":{\"id\":10, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-11-2012\" , \"amount\":100}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"27\",\"_score\":2.2039728,\"_source\":{\"id\":27, \"city\": \"Westford\", \"department\": \"Sales\", \"employee\": \"Jerri Preble\" ,\"date\": \"12-23-2010\" , \"amount\":899.03}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"9\",\"_score\":1.9162908,\"_source\":{\"id\":9, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"05-11-2012\" , \"amount\":75.75}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"11\",\"_score\":1.9162908,\"_source\":{\"id\":11, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-16-2012\" , \"amount\":220.8}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"28\",\"_score\":1.9162908,\"_source\":{\"id\":28, \"city\": \"Westford\", \"department\": \"Sales\", \"employee\": \"Jerri Preble\" ,\"date\": \"11-30-2010\" , \"amount\":343.45}}]}}";

        // Build the resulting column definitions list.        
        DataColumn col0 = new DataColumnImpl("id", ColumnType.NUMBER);
        DataColumn col1 = new DataColumnImpl("city", ColumnType.LABEL);
        DataColumn col2 = new DataColumnImpl("department", ColumnType.LABEL);
        DataColumn col3 = new DataColumnImpl("employee", ColumnType.TEXT);
        DataColumn col4 = new DataColumnImpl("date", ColumnType.DATE);
        DataColumn col5 = new DataColumnImpl("amount", ColumnType.NUMBER);
        
        List<DataColumn> columns = new LinkedList<DataColumn>();
        columns.add(col0);
        columns.add(col1);
        columns.add(col2);
        columns.add(col3);
        columns.add(col4);
        columns.add(col5);

        // Build the Json serializer.
        GsonBuilder builder = new GsonBuilder();
        SearchResponseDeserializer searchResponseDeserializer = new SearchResponseDeserializer(client, metadata, columns);
        HitDeserializer hitDeserializer = new HitDeserializer(client, metadata, columns);
        builder.registerTypeAdapter(SearchResponse.class, searchResponseDeserializer);
        builder.registerTypeAdapter(SearchHitResponse.class, hitDeserializer);
        Gson gson = builder.create();
        SearchResponse result = gson.fromJson(response, SearchResponse.class);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getTookInMillis(), 4);
        Assert.assertEquals(result.getMaxScore(), 2.609438f, 0.1);
        Assert.assertEquals(result.getResponseStatus(), 200);
        Assert.assertEquals(result.getShardFailures(), 0);
        Assert.assertEquals(result.getTotalShards(), 5);
        Assert.assertEquals(result.getSuccessfulShards(), 5);
        Assert.assertEquals(result.getTotalHits(), 8);
        Assert.assertEquals(result.getHits().length, 8);
        
        // Assert first response hit.
        SearchHitResponse hit0 = result.getHits()[0];
        Assert.assertNotNull(hit0);
        Assert.assertEquals(hit0.getIndex(), "expensereports");
        Assert.assertEquals(hit0.getType(), "expense");
        Assert.assertEquals(hit0.getId(), "12");
        Assert.assertEquals(hit0.getScore(), 2.609438f, 0.1);
        Map<String, Object> hit0Fields = hit0.getFields();
        Assert.assertNotNull(hit0Fields);
        Assert.assertEquals(hit0Fields.size(), 6);
        Assert.assertEquals(hit0Fields.get("id").toString(), "12.0");
        Assert.assertEquals(hit0Fields.get("city").toString(), "Madrid");
        Assert.assertEquals(hit0Fields.get("department").toString(), "Sales");
        Assert.assertEquals(hit0Fields.get("employee").toString(), "Nita Marling");
        Date date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2012-03-02");
        Assert.assertEquals(hit0Fields.get("date"), date);
        Assert.assertEquals(hit0Fields.get("amount").toString(), "344.9");

        // Assert first response hit.
        SearchHitResponse hit7 = result.getHits()[7];
        Assert.assertNotNull(hit7);
        Assert.assertEquals(hit7.getIndex(), "expensereports");
        Assert.assertEquals(hit7.getType(), "expense");
        Assert.assertEquals(hit7.getId(), "28");
        Assert.assertEquals(hit7.getScore(), 1.9162908f, 0.1);
        Map<String, Object> hit7Fields = hit7.getFields();
        Assert.assertNotNull(hit7Fields);
        Assert.assertEquals(hit7Fields.size(), 6);
        Assert.assertEquals(hit7Fields.get("id").toString(), "28.0");
        Assert.assertEquals(hit7Fields.get("city").toString(), "Westford");
        Assert.assertEquals(hit7Fields.get("department").toString(), "Sales");
        Assert.assertEquals(hit7Fields.get("employee").toString(), "Jerri Preble");
        date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2010-11-30");
        Assert.assertEquals(hit7Fields.get("date"), date);
        Assert.assertEquals(hit7Fields.get("amount").toString(), "343.45");
    }

    @Test
    public void testAggregationSerializer() {

        ElasticSearchJestClient c = (ElasticSearchJestClient) ElasticSearchClientFactory.configure(anotherClient, definition);

        // **************
        // Core functions
        // **************
        
        GsonBuilder builder = new GsonBuilder();
        // Build the resulting column definitions list.        
        DataColumn col0 = new DataColumnImpl("department-count", ColumnType.NUMBER);
        DataColumn col1 = new DataColumnImpl("amount-min", ColumnType.NUMBER);
        DataColumn col2 = new DataColumnImpl("amount-max", ColumnType.NUMBER);
        DataColumn col3 = new DataColumnImpl("amount-avg", ColumnType.NUMBER);
        DataColumn col4 = new DataColumnImpl("amount-sum", ColumnType.NUMBER);
        DataColumn col5 = new DataColumnImpl("amount-distinct", ColumnType.NUMBER);
        List<DataColumn> columns = new LinkedList<DataColumn>();
        columns.add(col0);
        columns.add(col1);
        columns.add(col2);
        columns.add(col3);
        columns.add(col4);
        columns.add(col5);
        AggregationSerializer aggregationSerializer = new AggregationSerializer(client, metadata, columns, c);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        Gson gson = builder.create();
        
        // Count, min, max, avg, distinct, sum. ( no grouping by columns).
        DataSetGroup aggregation = new DataSetGroup();
        aggregation.setDataSetUUID("testUUID");
        aggregation.setColumnGroup(null);
        aggregation.setJoin(false);
        GroupFunction countFunction = new GroupFunction("department", "department-count", AggregateFunctionType.COUNT);
        GroupFunction minFunction = new GroupFunction("amount", "amount-min", AggregateFunctionType.MIN);
        GroupFunction maxFunction = new GroupFunction("amount", "amount-max", AggregateFunctionType.MAX);
        GroupFunction avgFunction = new GroupFunction("amount", "amount-avg", AggregateFunctionType.AVERAGE);
        GroupFunction sumFunction = new GroupFunction("amount", "amount-sum", AggregateFunctionType.SUM);
        GroupFunction distinctFunction = new GroupFunction("amount", "amount-distinct", AggregateFunctionType.DISTINCT);
        aggregation.addGroupFunction(countFunction, minFunction, maxFunction, avgFunction, sumFunction, distinctFunction);
        String aggregationResult = gson.toJson(aggregation,  DataSetGroup.class);
        Assert.assertEquals(aggregationResult, "{\"aggregations\":{\"department-count\":{\"value_count\":{\"field\":\"department\"}},\"amount-min\":{\"min\":{\"field\":\"amount\"}},\"amount-max\":{\"max\":{\"field\":\"amount\"}},\"amount-avg\":{\"avg\":{\"field\":\"amount\"}},\"amount-sum\":{\"sum\":{\"field\":\"amount\"}},\"amount-distinct\":{\"cardinality\":{\"field\":\"amount\"}}}}");
        
        // *****************
        // GroupBy functions
        // *****************

        // Term aggregation.
        builder = new GsonBuilder();
        col0 = new DataColumnImpl("departmentGrouped", ColumnType.NUMBER);
        col1 = new DataColumnImpl("amount-count", ColumnType.NUMBER);
        col2 = new DataColumnImpl("amount-min", ColumnType.NUMBER);
        columns = new LinkedList<DataColumn>();
        columns.add(col0);
        columns.add(col1);
        columns.add(col2);
        aggregationSerializer = new AggregationSerializer(client, metadata, columns, c);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        gson = builder.create();
        
        DataSetGroup groupByAggregation = new DataSetGroup();
        groupByAggregation.setDataSetUUID("testUUID");
        groupByAggregation.setJoin(false);
        groupByAggregation.setColumnGroup(new ColumnGroup("department", "departmentGrouped", GroupStrategy.DYNAMIC));
        GroupFunction groupByCountFunction = new GroupFunction("amount", "amount-count", AggregateFunctionType.COUNT);
        GroupFunction groupByMinFunction = new GroupFunction("amount", "amount-min", AggregateFunctionType.MIN);
        groupByAggregation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        aggregationResult = gson.toJson(groupByAggregation,  DataSetGroup.class);
        Assert.assertEquals(aggregationResult, "{\"aggregations\":{\"departmentGrouped\":{\"terms\":{\"field\":\"department\",\"order\":{\"_term\":\"asc\"},\"min_doc_count\":1,\"size\":0},\"aggregations\":{\"amount-count\":{\"value_count\":{\"field\":\"amount\"}},\"amount-min\":{\"min\":{\"field\":\"amount\"}}}}}}");

        // Histogram aggregation.
        builder = new GsonBuilder();
        col0 = new DataColumnImpl("amount", ColumnType.NUMBER);
        col1 = new DataColumnImpl("amount-max", ColumnType.NUMBER);
        col2 = new DataColumnImpl("amount-min", ColumnType.NUMBER);
        columns = new LinkedList<DataColumn>();
        columns.add(col0);
        columns.add(col1);
        columns.add(col2);
        aggregationSerializer = new AggregationSerializer(client, metadata, columns, c);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        gson = builder.create();

        DataSetGroup histogramAggreagation = new DataSetGroup();
        histogramAggreagation.setDataSetUUID("testUUID");
        histogramAggreagation.setColumnGroup(new ColumnGroup("amount", "amount", GroupStrategy.DYNAMIC, 99, "20"));
        histogramAggreagation.setJoin(false);
        groupByCountFunction = new GroupFunction("amount", "amount-max", AggregateFunctionType.MAX);
        groupByMinFunction = new GroupFunction("amount", "amount-min", AggregateFunctionType.MIN);
        histogramAggreagation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        aggregationResult = gson.toJson(histogramAggreagation,  DataSetGroup.class);
        Assert.assertEquals(aggregationResult, "{\"aggregations\":{\"amount\":{\"histogram\":{\"field\":\"amount\",\"interval\":20,\"order\":{\"_key\":\"asc\"},\"min_doc_count\":1},\"aggregations\":{\"amount-max\":{\"max\":{\"field\":\"amount\"}},\"amount-min\":{\"min\":{\"field\":\"amount\"}}}}}}");

        // Histogram aggregation.
        // Allowing empty intervals.
        builder = new GsonBuilder();
        col0 = new DataColumnImpl("amount", ColumnType.NUMBER);
        col1 = new DataColumnImpl("amount-max", ColumnType.NUMBER);
        col2 = new DataColumnImpl("amount-min", ColumnType.NUMBER);
        columns = new LinkedList<DataColumn>();
        columns.add(col0);
        columns.add(col1);
        columns.add(col2);
        aggregationSerializer = new AggregationSerializer(client, metadata, columns, c);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        gson = builder.create();
        
        histogramAggreagation = new DataSetGroup();
        histogramAggreagation.setDataSetUUID("testUUID");
        ColumnGroup cg = new ColumnGroup("amount", "amount", GroupStrategy.DYNAMIC, 99, "20");
        cg.setEmptyIntervalsAllowed(true);
        histogramAggreagation.setColumnGroup(cg);
        histogramAggreagation.setJoin(false);
        groupByCountFunction = new GroupFunction("amount", "amount-max", AggregateFunctionType.MAX);
        groupByMinFunction = new GroupFunction("amount", "amount-min", AggregateFunctionType.MIN);
        histogramAggreagation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        aggregationResult = gson.toJson(histogramAggreagation,  DataSetGroup.class);
        Assert.assertEquals(aggregationResult, "{\"aggregations\":{\"amount\":{\"histogram\":{\"field\":\"amount\",\"interval\":20,\"order\":{\"_key\":\"asc\"},\"min_doc_count\":0},\"aggregations\":{\"amount-max\":{\"max\":{\"field\":\"amount\"}},\"amount-min\":{\"min\":{\"field\":\"amount\"}}}}}}");

    }

    @Test
    public void testAggregationDeserializer() {

        String aggregations1 = "{ \"departmentGrouped\": {\n" +
                "        \"doc_count_error_upper_bound\": 0,\n" +
                "        \"sum_other_doc_count\": 0,\n" +
                "        \"buckets\": [\n" +
                "            {\n" +
                "                \"key\": \"Engineering\",\n" +
                "                \"doc_count\": 19,\n" +
                "                \"amount-count\": {\n" +
                "                    \"value\": 19\n" +
                "                },\n" +
                "                \"amount-min\": {\n" +
                "                    \"value\": 120.3499984741211\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"key\": \"Management\",\n" +
                "                \"doc_count\": 11,\n" +
                "                \"amount-count\": {\n" +
                "                    \"value\": 11\n" +
                "                },\n" +
                "                \"amount-min\": {\n" +
                "                    \"value\": 43.029998779296875\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"key\": \"Sales\",\n" +
                "                \"doc_count\": 8,\n" +
                "                \"amount-count\": {\n" +
                "                    \"value\": 8\n" +
                "                },\n" +
                "                \"amount-min\": {\n" +
                "                    \"value\": 75.75\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"key\": \"Services\",\n" +
                "                \"doc_count\": 5,\n" +
                "                \"amount-count\": {\n" +
                "                    \"value\": 5\n" +
                "                },\n" +
                "                \"amount-min\": {\n" +
                "                    \"value\": 152.25\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"key\": \"Support\",\n" +
                "                \"doc_count\": 7,\n" +
                "                \"amount-count\": {\n" +
                "                    \"value\": 7\n" +
                "                },\n" +
                "                \"amount-min\": {\n" +
                "                    \"value\": 300.010009765625\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        // The resulting datacolumns to obtain.
        
        DataColumn deptColumn = new DataColumnImpl("departmentGrouped", ColumnType.LABEL);
        DataColumn amounutCountColumn = new DataColumnImpl("amount-count", ColumnType.NUMBER);
        DataColumn amounutMinColumn = new DataColumnImpl("amount-min", ColumnType.NUMBER);
        List<DataColumn> columns = new LinkedList<DataColumn>();
        columns.add(deptColumn);
        columns.add(amounutCountColumn);
        columns.add(amounutMinColumn);
        GsonBuilder builder = new GsonBuilder();
        AggregationsDeserializer aggregationsDeserializer = new AggregationsDeserializer(client, metadata, columns);
        builder.registerTypeAdapter(SearchHitResponse[].class, aggregationsDeserializer);
        Gson gson = builder.create();
        SearchHitResponse[] hits = gson.fromJson(aggregations1, SearchHitResponse[].class);
        
        Assert.assertTrue(hits != null);
        Assert.assertTrue(hits.length == 5);
        
        SearchHitResponse hit0 = hits[0];
        Assert.assertTrue(hit0 != null);
        Map<String, Object> hit0Fields = hit0.getFields();
        Assert.assertTrue(hit0Fields != null);
        Assert.assertTrue(hit0Fields.size() == 3);
        String hit0AmountCount = hit0Fields.get("amount-count").toString();
        String hit0Dept = hit0Fields.get("departmentGrouped").toString();
        String hit0AmountMin = hit0Fields.get("amount-min").toString();
        Assert.assertEquals(hit0AmountCount, "19.0");
        Assert.assertEquals(hit0Dept, "Engineering");
        Assert.assertEquals(hit0AmountMin, "120.3499984741211");

        SearchHitResponse hit1 = hits[1];
        Assert.assertTrue(hit1 != null);
        Map<String, Object> hit1Fields = hit1.getFields();
        Assert.assertTrue(hit1Fields != null);
        Assert.assertTrue(hit1Fields.size() == 3);
        String hit1AmountCount = hit1Fields.get("amount-count").toString();
        String hit1Dept = hit1Fields.get("departmentGrouped").toString();
        String hit1AmountMin = hit1Fields.get("amount-min").toString();
        Assert.assertEquals(hit1AmountCount, "11.0");
        Assert.assertEquals(hit1Dept, "Management");
        Assert.assertEquals(hit1AmountMin, "43.029998779296875");

        SearchHitResponse hit4 = hits[4];
        Assert.assertTrue(hit4 != null);
        Map<String, Object> hit4Fields = hit4.getFields();
        Assert.assertTrue(hit4Fields != null);
        Assert.assertTrue(hit4Fields.size() == 3);
        String hit4AmountCount = hit4Fields.get("amount-count").toString();
        String hit4Dept = hit4Fields.get("departmentGrouped").toString();
        String hit4AmountMin = hit4Fields.get("amount-min").toString();
        Assert.assertEquals(hit4AmountCount, "7.0");
        Assert.assertEquals(hit4Dept, "Support");
        Assert.assertEquals(hit4AmountMin, "300.010009765625");

    }


}
