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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.Query;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchResponse;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.group.*;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    }

    @Test
    public void testQuerySerializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Query.class, new ElasticSearchJestClient.QuerySerializer());
        Gson gson = builder.create();
        
        // Match ALL query.
        Query query = new Query(Query.Type.MATCH_ALL);
        String serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"match_all\":{}}}");

        // Match ALL query..
        query = new Query("department", Query.Type.MATCH);
        query.setParam(Query.Parameter.VALUE.name(), "Sales");
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"match\":{\"department\":\"Sales\"}}}");

        // Filtered query.
        query = new Query("department", Query.Type.FILTERED);
        Query subQuery = new Query(Query.Type.MATCH_ALL);
        Query subFilter = new Query("amount", Query.Type.TERM);
        subFilter.setParam(Query.Parameter.VALUE.name(), "Sales");
        query.setParam(Query.Parameter.QUERY.name(), subQuery);
        query.setParam(Query.Parameter.FILTER.name(), subFilter);
        serializedQuery = gson.toJson(query, Query.class);
        Assert.assertEquals(serializedQuery, "{\"query\":{\"query\":{\"match_all\":{}},\"filter\":{\"term\":{\"amount\":\"Sales\"}}}}");

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
    public void testSearchResponseDeserialization() {
        String response = "{\"took\":4,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\"hits\":{\"total\":8,\"max_score\":2.609438,\"hits\":[{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"12\",\"_score\":2.609438,\"_source\":{\"id\":12, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-02-2012\" , \"amount\":344.9}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"20\",\"_score\":2.609438,\"_source\":{\"id\":20, \"city\": \"Brno\", \"department\": \"Sales\", \"employee\": \"Neva Hunger\" ,\"date\": \"06-11-2011\" , \"amount\":995.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"21\",\"_score\":2.609438,\"_source\":{\"id\":21, \"city\": \"Brno\", \"department\": \"Sales\", \"employee\": \"Neva Hunger\" ,\"date\": \"06-11-2011\" , \"amount\":234.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"10\",\"_score\":2.2039728,\"_source\":{\"id\":10, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-11-2012\" , \"amount\":100}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"27\",\"_score\":2.2039728,\"_source\":{\"id\":27, \"city\": \"Westford\", \"department\": \"Sales\", \"employee\": \"Jerri Preble\" ,\"date\": \"12-23-2010\" , \"amount\":899.03}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"9\",\"_score\":1.9162908,\"_source\":{\"id\":9, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"05-11-2012\" , \"amount\":75.75}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"11\",\"_score\":1.9162908,\"_source\":{\"id\":11, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-16-2012\" , \"amount\":220.8}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"28\",\"_score\":1.9162908,\"_source\":{\"id\":28, \"city\": \"Westford\", \"department\": \"Sales\", \"employee\": \"Jerri Preble\" ,\"date\": \"11-30-2010\" , \"amount\":343.45}}]}}";

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(SearchResponse.class, new ElasticSearchJestClient.SearchResponseDeserializer());
        builder.registerTypeAdapter(SearchHitResponse.class, new ElasticSearchJestClient.HitDeserializer());
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
        Assert.assertEquals(hit0Fields.get("id").toString(), "12");
        Assert.assertEquals(hit0Fields.get("city").toString(), "Madrid");
        Assert.assertEquals(hit0Fields.get("department").toString(), "Sales");
        Assert.assertEquals(hit0Fields.get("employee").toString(), "Nita Marling");
        Assert.assertEquals(hit0Fields.get("date").toString(), "03-02-2012");
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
        Assert.assertEquals(hit7Fields.get("id").toString(), "28");
        Assert.assertEquals(hit7Fields.get("city").toString(), "Westford");
        Assert.assertEquals(hit7Fields.get("department").toString(), "Sales");
        Assert.assertEquals(hit7Fields.get("employee").toString(), "Jerri Preble");
        Assert.assertEquals(hit7Fields.get("date").toString(), "11-30-2010");
        Assert.assertEquals(hit7Fields.get("amount").toString(), "343.45");
    }

    @Test
    public void testAggregationSerializer() {
        
        GsonBuilder builder = new GsonBuilder();
        ElasticSearchJestClient client = new ElasticSearchJestClient();
        ElasticSearchJestClient.AggregationSerializer serializer = client.buildAggregationsSerializer();
        builder.registerTypeAdapter(DataSetGroup.class, serializer.setDataSetMetadata(metadata));
        Gson gson = builder.create();

        // **************
        // Core functions
        // **************
        
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
        DataSetGroup groupByAggregation = new DataSetGroup();
        groupByAggregation.setDataSetUUID("testUUID");
        groupByAggregation.setJoin(false);
        groupByAggregation.setColumnGroup(new ColumnGroup("department", "departmentGrouped", GroupStrategy.DYNAMIC));
        GroupFunction groupByCountFunction = new GroupFunction("amount", "amount-count", AggregateFunctionType.COUNT);
        GroupFunction groupByMinFunction = new GroupFunction("amount", "amount-min", AggregateFunctionType.MIN);
        groupByAggregation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        aggregationResult = gson.toJson(groupByAggregation,  DataSetGroup.class);
        Assert.assertEquals(aggregationResult, "{\"aggregations\":{\"departmentGrouped\":{\"terms\":{\"field\":\"department\",\"order\":{\"_term\":\"asc\"},\"min_doc_count\":0},\"aggregations\":{\"amount-count\":{\"value_count\":{\"field\":\"amount\"}},\"amount-min\":{\"min\":{\"field\":\"amount\"}}}}}}");

        /* TODO

        DataSetGroup histogramAggreagation = new DataSetGroup();
        histogramAggreagation.setDataSetUUID("testUUID");
        histogramAggreagation.setColumnGroup(new ColumnGroup("amount", "amount", GroupStrategy.DYNAMIC));
        histogramAggreagation.setJoin(false);
        GroupFunction groupByCountFunction = new GroupFunction("amount", "amount-count", AggregateFunctionType.COUNT);
        GroupFunction groupByMinFunction = new GroupFunction("amount", "amount-min", AggregateFunctionType.MIN);
        histogramAggreagation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        String aggregationResult = gson.toJson(dateHistogramAggreagation,  DataSetGroup.class);
        System.out.println(aggregationResult);

        // Date Histogram aggregation.
        DataSetGroup dateHistogramAggreagation = new DataSetGroup();
        dateHistogramAggreagation.setDataSetUUID("testUUID");
        dateHistogramAggreagation.setColumnGroup(new ColumnGroup("date", "dateGroupped", GroupStrategy.DYNAMIC, -1, DateIntervalType.YEAR.name()));
        dateHistogramAggreagation.setJoin(false);
        GroupFunction groupByCountFunction = new GroupFunction("amount", "amount-count", AggregateFunctionType.COUNT);
        GroupFunction groupByMinFunction = new GroupFunction("amount", "amount-min", AggregateFunctionType.MIN);
        dateHistogramAggreagation.addGroupFunction(groupByCountFunction, groupByMinFunction);
        String aggregationResult = gson.toJson(dateHistogramAggreagation,  DataSetGroup.class);
        System.out.println(aggregationResult); */
        
    }

}
