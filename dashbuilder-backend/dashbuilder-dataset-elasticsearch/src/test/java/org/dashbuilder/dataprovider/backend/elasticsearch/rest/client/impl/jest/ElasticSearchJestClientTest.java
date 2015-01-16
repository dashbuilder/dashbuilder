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
import com.google.gson.JsonObject;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.Query;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchResponse;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>Test unit for Jest EL REST api client.</p>
 *
 * @since 0.3.0
 */
@RunWith(Arquillian.class)
public class ElasticSearchJestClientTest {


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
    
    // TODO
    @Test
    public void testSearchResponseDeserialization() {
        String response = "{\"took\":4,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\"hits\":{\"total\":8,\"max_score\":2.609438,\"hits\":[{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"12\",\"_score\":2.609438,\"_source\":{\"id\":12, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-02-2012\" , \"amount\":344.9}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"20\",\"_score\":2.609438,\"_source\":{\"id\":20, \"city\": \"Brno\", \"department\": \"Sales\", \"employee\": \"Neva Hunger\" ,\"date\": \"06-11-2011\" , \"amount\":995.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"21\",\"_score\":2.609438,\"_source\":{\"id\":21, \"city\": \"Brno\", \"department\": \"Sales\", \"employee\": \"Neva Hunger\" ,\"date\": \"06-11-2011\" , \"amount\":234.3}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"10\",\"_score\":2.2039728,\"_source\":{\"id\":10, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-11-2012\" , \"amount\":100}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"27\",\"_score\":2.2039728,\"_source\":{\"id\":27, \"city\": \"Westford\", \"department\": \"Sales\", \"employee\": \"Jerri Preble\" ,\"date\": \"12-23-2010\" , \"amount\":899.03}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"9\",\"_score\":1.9162908,\"_source\":{\"id\":9, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"05-11-2012\" , \"amount\":75.75}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"11\",\"_score\":1.9162908,\"_source\":{\"id\":11, \"city\": \"Madrid\", \"department\": \"Sales\", \"employee\": \"Nita Marling\" ,\"date\": \"03-16-2012\" , \"amount\":220.8}},{\"_index\":\"expensereports\",\"_type\":\"expense\",\"_id\":\"28\",\"_score\":1.9162908,\"_source\":{\"id\":28, \"city\": \"Westford\", \"department\": \"Sales\", \"employee\": \"Jerri Preble\" ,\"date\": \"11-30-2010\" , \"amount\":343.45}}]}}";

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(SearchResponse.class, new ElasticSearchJestClient.SearchResponseDeserializer());
        builder.registerTypeAdapter(SearchHitResponse.class, new ElasticSearchJestClient.HitDeserializer());
        Gson gson = builder.create();
        SearchResponse result = gson.fromJson(response, SearchResponse.class);
        
        System.out.println(result);
    }
}
