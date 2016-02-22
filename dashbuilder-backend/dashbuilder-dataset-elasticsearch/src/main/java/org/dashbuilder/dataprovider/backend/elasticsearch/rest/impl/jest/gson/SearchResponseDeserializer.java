package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchDataSetProvider;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.EmptySearchResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchResponse;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SearchResponseDeserializer extends AbstractAdapter<SearchResponseDeserializer> implements JsonDeserializer<SearchResponse> {


    public SearchResponseDeserializer(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns) {
        super(client, metadata, columns);
    }

    @Override
    public SearchResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        SearchResponse result = null;
        if (typeOfT.equals(SearchResponse.class)) {
            JsonObject responseObject = json.getAsJsonObject();

            if (responseObject != null) {
                long tookInMillis = responseObject.get("took").getAsLong();
                int responseStatus = ElasticSearchDataSetProvider.RESPONSE_CODE_OK;

                JsonObject shardsObject = responseObject.getAsJsonObject("_shards");
                int totalShards = shardsObject.get("total").getAsInt();
                int successfulShards = shardsObject.get("successful").getAsInt();
                int shardFailures = shardsObject.get("failed").getAsInt();

                List<SearchHitResponse> hits;
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

                // Build the response model.
                if (hits == null || hits.isEmpty()) result = new EmptySearchResponse(tookInMillis, responseStatus, totalHits, maxScore, totalShards, successfulShards, shardFailures);
                else result = new SearchResponse(tookInMillis, responseStatus, totalHits, maxScore, totalShards, successfulShards, shardFailures, hits.toArray(new SearchHitResponse[hits.size()]));
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