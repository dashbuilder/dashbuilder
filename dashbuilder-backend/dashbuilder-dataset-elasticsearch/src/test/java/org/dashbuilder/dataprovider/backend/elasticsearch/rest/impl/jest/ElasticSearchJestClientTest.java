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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchClientFactory;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.Query;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataprovider.backend.elasticsearch.suite.ElasticSearchTestSuite;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.*;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class ElasticSearchJestClientTest {

    @Mock
    protected DataSetMetadata metadata;
    
    @Mock
    protected ElasticSearchDataSetDef definition;

    protected ElasticSearchJestClient client;

    @Before
    public void setUp() throws Exception {
        ElasticSearchValueTypeMapper typeMapper = new ElasticSearchValueTypeMapper();
        ElasticSearchUtils utils = new ElasticSearchUtils(typeMapper);
        ElasticSearchClientFactory clientFactory = new ElasticSearchClientFactory(typeMapper, utils);
        client = new ElasticSearchJestClient(clientFactory, typeMapper, utils);

        // Init the annotated mocks.
        MockitoAnnotations.initMocks(this);
        
        // Init the metadata mocked instance.
        when(metadata.getNumberOfColumns()).thenReturn(6);
        when(metadata.getColumnId(0)).thenReturn("ID");
        when(metadata.getColumnType(0)).thenReturn(ColumnType.NUMBER);
        when(metadata.getColumnType("ID")).thenReturn(ColumnType.NUMBER);
        when(metadata.getColumnId(1)).thenReturn("CITY");
        when(metadata.getColumnType(1)).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnType("CITY")).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnId(2)).thenReturn("DEPARTMENT");
        when(metadata.getColumnType(2)).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnType("DEPARTMENT")).thenReturn(ColumnType.LABEL);
        when(metadata.getColumnId(3)).thenReturn("EMPLOYEE");
        when(metadata.getColumnType(3)).thenReturn(ColumnType.TEXT);
        when(metadata.getColumnType("EMPLOYEE")).thenReturn(ColumnType.TEXT);
        when(metadata.getColumnId(4)).thenReturn("DATE");
        when(metadata.getColumnType(4)).thenReturn(ColumnType.DATE);
        when(metadata.getColumnType("DATE")).thenReturn(ColumnType.DATE);
        when(metadata.getColumnId(5)).thenReturn("AMOUNT");
        when(metadata.getColumnType(5)).thenReturn(ColumnType.NUMBER);
        when(metadata.getColumnType("AMOUNT")).thenReturn(ColumnType.NUMBER);
        when(metadata.getDefinition()).thenReturn(definition);
        
        // Init the dataset defintion mocked instance.
        when(definition.getServerURL()).thenReturn(ElasticSearchTestSuite.EL_SERVER);
        when(definition.getClusterName()).thenReturn("elasticsearch");
        when(definition.getIndex()).thenReturn("expensereports");
        when(definition.getType()).thenReturn("expense");
        when(definition.getPattern("ID")).thenReturn("integer");
        when(definition.getPattern("DATE")).thenReturn("MM-dd-YYYY");
        
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
        Assert.assertEquals("{\"query\":{\"match_all\":{}}}", serializedQuery);

        // Match query..
        query = new Query("DEPARTMENT", Query.Type.MATCH);
        query.setParam(Query.Parameter.VALUE.name(), "Sales");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"query\":{\"match\":{\"DEPARTMENT\":\"Sales\"}}}", serializedQuery);

        // Wildcard query..
        query = new Query("DEPARTMENT", Query.Type.WILDCARD);
        query.setParam(Query.Parameter.VALUE.name(), "Sal%");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"query\":{\"wildcard\":{\"DEPARTMENT\":\"Sal%\"}}}", serializedQuery);

        // Query String query.
        query = new Query("EMPLOYEE", Query.Type.QUERY_STRING);
        query.setParam(Query.Parameter.DEFAULT_FIELD.name(), "EMPLOYEE");
        query.setParam(Query.Parameter.DEFAULT_OPERATOR.name(), "AND");
        query.setParam(Query.Parameter.QUERY.name(), "Tony%");
        query.setParam(Query.Parameter.LOWERCASE_EXPANDED_TERMS.name(),"false");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"query\":{\"query_string\":{\"default_field\":\"EMPLOYEE\",\"default_operator\":\"AND\",\"query\":\"Tony%\",\"lowercase_expanded_terms\":\"false\"}}}", serializedQuery);

        // Filtered query.
        query = new Query("DEPARTMENT", Query.Type.FILTERED);
        Query subQuery = new Query(Query.Type.MATCH_ALL);
        Query subFilter = new Query("AMOUNT", Query.Type.TERM);
        subFilter.setParam(Query.Parameter.VALUE.name(), "Sales");
        query.setParam(Query.Parameter.QUERY.name(), subQuery);
        query.setParam(Query.Parameter.FILTER.name(), subFilter);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"query\":{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"term\":{\"AMOUNT\":\"Sales\"}}}}}", serializedQuery);

        // Boolean query.
        query = new Query("DEPARTMENT", Query.Type.BOOL);
        query.setParam(Query.Parameter.MUST.name(), new Query(Query.Type.MATCH_ALL));
        query.setParam(Query.Parameter.MUST_NOT.name(), new Query(Query.Type.MATCH_ALL));
        query.setParam(Query.Parameter.SHOULD.name(), new Query(Query.Type.MATCH_ALL));
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"query\":{\"bool\":{\"must\":{\"match_all\":{}},\"must_not\":{\"match_all\":{}},\"should\":{\"match_all\":{}},\"minimum_should_match\":1}}}", serializedQuery);

        // Term filter.
        query = new Query("DEPARTMENT", Query.Type.TERM);
        query.setParam(Query.Parameter.VALUE.name(), "Sales");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"filter\":{\"term\":{\"DEPARTMENT\":\"Sales\"}}}", serializedQuery);

        // And filter.
        query = new Query("DEPARTMENT", Query.Type.AND);
        List<Query> filters = new LinkedList<Query>();
        filters.add(new Query("DEPARTMENT", Query.Type.TERM).setParam(Query.Parameter.VALUE.name(), "Sales"));
        filters.add(new Query("AMOUNT", Query.Type.RANGE).setParam(Query.Parameter.GT.name(), 100));
        query.setParam(Query.Parameter.FILTERS.name(), filters);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"filter\":{\"and\":[{\"term\":{\"DEPARTMENT\":\"Sales\"}},{\"range\":{\"AMOUNT\":{\"gt\":100}}}]}}", serializedQuery);

        // Or filter.
        query = new Query("DEPARTMENT", Query.Type.OR);
        filters = new LinkedList<Query>();
        filters.add(new Query("DEPARTMENT", Query.Type.TERM).setParam(Query.Parameter.VALUE.name(), "Sales"));
        filters.add(new Query("AMOUNT", Query.Type.RANGE).setParam(Query.Parameter.GT.name(), 100));
        query.setParam(Query.Parameter.FILTERS.name(), filters);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"filter\":{\"or\":[{\"term\":{\"DEPARTMENT\":\"Sales\"}},{\"range\":{\"AMOUNT\":{\"gt\":100}}}]}}", serializedQuery);


        // Not filter.
        query = new Query("DEPARTMENT", Query.Type.NOT);
        query.setParam(Query.Parameter.FILTER.name(), new Query("CITY", Query.Type.TERM).setParam(Query.Parameter.VALUE.name(), "London"));
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"filter\":{\"not\":{\"term\":{\"CITY\":\"London\"}}}}", serializedQuery);

        // Exist filter.
        query = new Query("DEPARTMENT", Query.Type.EXISTS);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"filter\":{\"exists\":{\"field\":\"DEPARTMENT\"}}}", serializedQuery);

        // Not filter.
        query = new Query("AMOUNT", Query.Type.RANGE);
        query.setParam(Query.Parameter.LT.name(), 100);
        query.setParam(Query.Parameter.LTE.name(), 200);
        query.setParam(Query.Parameter.GT.name(), 300);
        query.setParam(Query.Parameter.GTE.name(), 400);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals("{\"filter\":{\"range\":{\"AMOUNT\":{\"lt\":100,\"lte\":200,\"gt\":300,\"gte\":400}}}}", serializedQuery);
    }
    
    @Test
    public void testSearchResponseDeserialization() throws Exception {
        String response = "{\"took\":4,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\"hits\":{\"total\":8,\"max_score\":2.609438,\"hits\":[{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"12\",\"_score\":2.609438,\"_source\":{\"ID\":12, \"CITY\": \"Madrid\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Nita Marling\" ,\"DATE\": \"03-02-2012\" , \"AMOUNT\":344.9}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"20\",\"_score\":2.609438,\"_source\":{\"ID\":20, \"CITY\": \"Brno\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Neva Hunger\" ,\"DATE\": \"06-11-2011\" , \"AMOUNT\":995.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"21\",\"_score\":2.609438,\"_source\":{\"ID\":21, \"CITY\": \"Brno\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Neva Hunger\" ,\"DATE\": \"06-11-2011\" , \"AMOUNT\":234.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"10\",\"_score\":2.2039728,\"_source\":{\"ID\":10, \"CITY\": \"Madrid\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Nita Marling\" ,\"DATE\": \"03-11-2012\" , \"AMOUNT\":100}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"27\",\"_score\":2.2039728,\"_source\":{\"ID\":27, \"CITY\": \"Westford\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Jerri Preble\" ,\"DATE\": \"12-23-2010\" , \"AMOUNT\":899.03}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"9\",\"_score\":1.9162908,\"_source\":{\"ID\":9, \"CITY\": \"Madrid\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Nita Marling\" ,\"DATE\": \"05-11-2012\" , \"AMOUNT\":75.75}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"11\",\"_score\":1.9162908,\"_source\":{\"ID\":11, \"CITY\": \"Madrid\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Nita Marling\" ,\"DATE\": \"03-16-2012\" , \"AMOUNT\":220.8}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"28\",\"_score\":1.9162908,\"_source\":{\"ID\":28, \"CITY\": \"Westford\", \"DEPARTMENT\": \"Sales\", \"EMPLOYEE\": \"Jerri Preble\" ,\"DATE\": \"11-30-2010\" , \"AMOUNT\":343.45}}]}}";

        // Build the resulting column definitions list.        
        DataColumn col0 = new DataColumnImpl("ID", ColumnType.NUMBER);
        DataColumn col1 = new DataColumnImpl("CITY", ColumnType.LABEL);
        DataColumn col2 = new DataColumnImpl("DEPARTMENT", ColumnType.LABEL);
        DataColumn col3 = new DataColumnImpl("EMPLOYEE", ColumnType.TEXT);
        DataColumn col4 = new DataColumnImpl("DATE", ColumnType.DATE);
        DataColumn col5 = new DataColumnImpl("AMOUNT", ColumnType.NUMBER);
        
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
        Assert.assertEquals(4, result.getTookInMillis());
        Assert.assertEquals(result.getMaxScore(), 2.609438f, 0.1);
        Assert.assertEquals(200, result.getResponseStatus());
        Assert.assertEquals(0, result.getShardFailures());
        Assert.assertEquals(5, result.getTotalShards());
        Assert.assertEquals(5, result.getSuccessfulShards());
        Assert.assertEquals(8, result.getTotalHits());
        Assert.assertEquals(8, result.getHits().length);
        
        // Assert first response hit.
        SearchHitResponse hit0 = result.getHits()[0];
        Assert.assertNotNull(hit0);
        Assert.assertEquals("expensereports", hit0.getIndex());
        Assert.assertEquals("expense", hit0.getType());
        Assert.assertEquals("12", hit0.getId());
        Assert.assertEquals(hit0.getScore(), 2.609438f, 0.1);
        Map<String, Object> hit0Fields = hit0.getFields();
        Assert.assertNotNull(hit0Fields);
        Assert.assertEquals(hit0Fields.size(), 6);
        Assert.assertEquals("12.0", hit0Fields.get("ID").toString());
        Assert.assertEquals("Madrid", hit0Fields.get("CITY").toString());
        Assert.assertEquals("Sales", hit0Fields.get("DEPARTMENT").toString());
        Assert.assertEquals("Nita Marling", hit0Fields.get("EMPLOYEE").toString());
        Date date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2012-03-02");
        Assert.assertEquals(date, hit0Fields.get("DATE"));
        Assert.assertEquals("344.9", hit0Fields.get("AMOUNT").toString());

        // Assert first response hit.
        SearchHitResponse hit7 = result.getHits()[7];
        Assert.assertNotNull(hit7);
        Assert.assertEquals("expensereports", hit7.getIndex());
        Assert.assertEquals("expense", hit7.getType());
        Assert.assertEquals("28", hit7.getId());
        Assert.assertEquals(hit7.getScore(), 1.9162908f, 0.1);
        Map<String, Object> hit7Fields = hit7.getFields();
        Assert.assertNotNull(hit7Fields);
        Assert.assertEquals(6, hit7Fields.size());
        Assert.assertEquals("28.0", hit7Fields.get("ID").toString());
        Assert.assertEquals("Westford", hit7Fields.get("CITY").toString());
        Assert.assertEquals("Sales", hit7Fields.get("DEPARTMENT").toString());
        Assert.assertEquals("Jerri Preble", hit7Fields.get("EMPLOYEE").toString());
        date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2010-11-30");
        Assert.assertEquals(date, hit7Fields.get("DATE"));
        Assert.assertEquals("343.45", hit7Fields.get("AMOUNT").toString());
    }

    @Test
    public void testAggregationSerializer() {

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
        AggregationSerializer aggregationSerializer = new AggregationSerializer(client, metadata, columns);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        Gson gson = builder.create();
        
        // Count, min, max, avg, distinct, sum. ( no grouping by columns).
        DataSetGroup aggregation = new DataSetGroup();
        aggregation.setDataSetUUID("testUUID");
        aggregation.setColumnGroup(null);
        aggregation.setJoin(false);
        GroupFunction countFunction = new GroupFunction("DEPARTMENT", "department-count", AggregateFunctionType.COUNT);
        GroupFunction minFunction = new GroupFunction("AMOUNT", "amount-min", AggregateFunctionType.MIN);
        GroupFunction maxFunction = new GroupFunction("AMOUNT", "amount-max", AggregateFunctionType.MAX);
        GroupFunction avgFunction = new GroupFunction("AMOUNT", "amount-avg", AggregateFunctionType.AVERAGE);
        GroupFunction sumFunction = new GroupFunction("AMOUNT", "amount-sum", AggregateFunctionType.SUM);
        GroupFunction distinctFunction = new GroupFunction("AMOUNT", "amount-distinct", AggregateFunctionType.DISTINCT);
        aggregation.addGroupFunction(countFunction, minFunction, maxFunction, avgFunction, sumFunction, distinctFunction);
        String aggregationResult = gson.toJson(aggregation,  DataSetGroup.class);
        Assert.assertEquals("{\"aggregations\":{\"department-count\":{\"value_count\":{\"field\":\"DEPARTMENT\"}},\"amount-min\":{\"min\":{\"field\":\"AMOUNT\"}},\"amount-max\":{\"max\":{\"field\":\"AMOUNT\"}},\"amount-avg\":{\"avg\":{\"field\":\"AMOUNT\"}},\"amount-sum\":{\"sum\":{\"field\":\"AMOUNT\"}},\"amount-distinct\":{\"cardinality\":{\"field\":\"AMOUNT\"}}}}", aggregationResult );
        
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
        aggregationSerializer = new AggregationSerializer(client, metadata, columns);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        gson = builder.create();
        
        DataSetGroup groupByAggregation = new DataSetGroup();
        groupByAggregation.setDataSetUUID("testUUID");
        groupByAggregation.setJoin(false);
        groupByAggregation.setColumnGroup(new ColumnGroup("DEPARTMENT", "departmentGrouped", GroupStrategy.DYNAMIC));
        GroupFunction groupByCountFunction = new GroupFunction("AMOUNT", "amount-count", AggregateFunctionType.COUNT);
        GroupFunction groupByMinFunction = new GroupFunction("AMOUNT", "amount-min", AggregateFunctionType.MIN);
        groupByAggregation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        aggregationResult = gson.toJson(groupByAggregation,  DataSetGroup.class);
        Assert.assertEquals("{\"aggregations\":{\"departmentGrouped\":{\"terms\":{\"field\":\"DEPARTMENT\",\"order\":{\"_term\":\"asc\"},\"min_doc_count\":1,\"size\":0},\"aggregations\":{\"amount-count\":{\"value_count\":{\"field\":\"AMOUNT\"}},\"amount-min\":{\"min\":{\"field\":\"AMOUNT\"}}}}}}", aggregationResult);

        // Histogram aggregation.
        builder = new GsonBuilder();
        col0 = new DataColumnImpl("AMOUNT", ColumnType.NUMBER);
        col1 = new DataColumnImpl("amount-max", ColumnType.NUMBER);
        col2 = new DataColumnImpl("amount-min", ColumnType.NUMBER);
        columns = new LinkedList<DataColumn>();
        columns.add(col0);
        columns.add(col1);
        columns.add(col2);
        aggregationSerializer = new AggregationSerializer(client, metadata, columns);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        gson = builder.create();

        DataSetGroup histogramAggreagation = new DataSetGroup();
        histogramAggreagation.setDataSetUUID("testUUID");
        histogramAggreagation.setColumnGroup(new ColumnGroup("AMOUNT", "AMOUNT", GroupStrategy.DYNAMIC, 99, "20"));
        histogramAggreagation.setJoin(false);
        groupByCountFunction = new GroupFunction("AMOUNT", "amount-max", AggregateFunctionType.MAX);
        groupByMinFunction = new GroupFunction("AMOUNT", "amount-min", AggregateFunctionType.MIN);
        histogramAggreagation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        aggregationResult = gson.toJson(histogramAggreagation,  DataSetGroup.class);
        Assert.assertEquals("{\"aggregations\":{\"AMOUNT\":{\"histogram\":{\"field\":\"AMOUNT\",\"interval\":20,\"order\":{\"_key\":\"asc\"},\"min_doc_count\":1},\"aggregations\":{\"amount-max\":{\"max\":{\"field\":\"AMOUNT\"}},\"amount-min\":{\"min\":{\"field\":\"AMOUNT\"}}}}}}", aggregationResult);

        // Histogram aggregation.
        // Allowing empty intervals.
        builder = new GsonBuilder();
        col0 = new DataColumnImpl("AMOUNT", ColumnType.NUMBER);
        col1 = new DataColumnImpl("amount-max", ColumnType.NUMBER);
        col2 = new DataColumnImpl("amount-min", ColumnType.NUMBER);
        columns = new LinkedList<DataColumn>();
        columns.add(col0);
        columns.add(col1);
        columns.add(col2);
        aggregationSerializer = new AggregationSerializer(client, metadata, columns);
        builder.registerTypeAdapter(DataSetGroup.class, aggregationSerializer);
        gson = builder.create();
        
        histogramAggreagation = new DataSetGroup();
        histogramAggreagation.setDataSetUUID("testUUID");
        ColumnGroup cg = new ColumnGroup("AMOUNT", "AMOUNT", GroupStrategy.DYNAMIC, 99, "20");
        cg.setEmptyIntervalsAllowed(true);
        histogramAggreagation.setColumnGroup(cg);
        histogramAggreagation.setJoin(false);
        groupByCountFunction = new GroupFunction("AMOUNT", "amount-max", AggregateFunctionType.MAX);
        groupByMinFunction = new GroupFunction("AMOUNT", "amount-min", AggregateFunctionType.MIN);
        histogramAggreagation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        aggregationResult = gson.toJson(histogramAggreagation,  DataSetGroup.class);
        Assert.assertEquals("{\"aggregations\":{\"AMOUNT\":{\"histogram\":{\"field\":\"AMOUNT\",\"interval\":20,\"order\":{\"_key\":\"asc\"},\"min_doc_count\":0},\"aggregations\":{\"amount-max\":{\"max\":{\"field\":\"AMOUNT\"}},\"amount-min\":{\"min\":{\"field\":\"AMOUNT\"}}}}}}", aggregationResult);

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
        Assert.assertEquals("19.0", hit0AmountCount);
        Assert.assertEquals("Engineering", hit0Dept);
        Assert.assertEquals("120.3499984741211", hit0AmountMin);

        SearchHitResponse hit1 = hits[1];
        Assert.assertTrue(hit1 != null);
        Map<String, Object> hit1Fields = hit1.getFields();
        Assert.assertTrue(hit1Fields != null);
        Assert.assertTrue(hit1Fields.size() == 3);
        String hit1AmountCount = hit1Fields.get("amount-count").toString();
        String hit1Dept = hit1Fields.get("departmentGrouped").toString();
        String hit1AmountMin = hit1Fields.get("amount-min").toString();
        Assert.assertEquals("11.0", hit1AmountCount);
        Assert.assertEquals("Management", hit1Dept);
        Assert.assertEquals("43.029998779296875", hit1AmountMin);

        SearchHitResponse hit4 = hits[4];
        Assert.assertTrue(hit4 != null);
        Map<String, Object> hit4Fields = hit4.getFields();
        Assert.assertTrue(hit4Fields != null);
        Assert.assertTrue(hit4Fields.size() == 3);
        String hit4AmountCount = hit4Fields.get("amount-count").toString();
        String hit4Dept = hit4Fields.get("departmentGrouped").toString();
        String hit4AmountMin = hit4Fields.get("amount-min").toString();
        Assert.assertEquals("7.0", hit4AmountCount);
        Assert.assertEquals("Support", hit4Dept);
        Assert.assertEquals("300.010009765625", hit4AmountMin);

    }


}
