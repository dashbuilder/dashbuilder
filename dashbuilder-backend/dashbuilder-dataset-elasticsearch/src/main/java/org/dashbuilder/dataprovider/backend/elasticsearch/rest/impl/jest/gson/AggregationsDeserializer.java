package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.*;

public class AggregationsDeserializer extends AbstractAdapter<AggregationsDeserializer> implements JsonDeserializer<SearchHitResponse[]> {

    public AggregationsDeserializer(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns) {
        super(client, metadata, columns);
    }

    @Override
    public SearchHitResponse[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<SearchHitResponse> result = null;

        if (typeOfT.equals(SearchHitResponse[].class)) {
            JsonObject aggregationsObject = (JsonObject) json;
            Set<Map.Entry<String, JsonElement>> entries = aggregationsObject.entrySet();
            if (entries != null && !entries.isEmpty()) {
                Map<String, JsonElement> noBucketFields = new HashMap<String, JsonElement>();
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
                                Map<String, JsonElement> bucketFields = new HashMap<String, JsonElement>();
                                JsonObject bucket = (JsonObject) bucketsIt.next();
                                Set<Map.Entry<String, JsonElement>> bucketEntries = bucket.entrySet();
                                boolean keyAsStringFound = false;
                                for (Map.Entry<String, JsonElement> bucketEntry : bucketEntries) {
                                    String aggName = bucketEntry.getKey();
                                    if ("key_as_string".equals(aggName)) {
                                        bucketFields.put(columnId, bucketEntry.getValue());
                                        keyAsStringFound = true;
                                    }
                                    else if (!keyAsStringFound && "key".equals(aggName)) {
                                        bucketFields.put(columnId, bucketEntry.getValue());
                                    } else if ("doc_count".equals(aggName)) {
                                        // Do nothing.
                                    } else if (getColumn(aggName) != null) {
                                        JsonElement aggValueElement = bucketEntry.getValue();
                                        if (aggValueElement != null && aggValueElement.isJsonPrimitive()) bucketFields.put(aggName, aggValueElement);
                                        else if (aggValueElement != null && aggValueElement.isJsonObject()) bucketFields.put(aggName, ((JsonObject) bucketEntry.getValue()).get("value"));
                                    }
                                }
                                result.add(createHitResponse(metadata, bucketFields, columns));
                            }
                        } else {
                            // Process no bucketed aggregations.
                            JsonElement aggValueElement = ((JsonObject)columnAggregationElement).get("value");
                            if (aggValueElement != null && aggValueElement.isJsonPrimitive()) noBucketFields.put(columnId, aggValueElement);
                        }
                    }
                }
                if (!noBucketFields.isEmpty()) result.add(createHitResponse(metadata, noBucketFields, columns));
            }
        }

        if (result == null) return null;
        return result.toArray(new SearchHitResponse[result.size()]);
    }

    private SearchHitResponse createHitResponse(DataSetMetadata metadata, Map<String, JsonElement> fields, List<DataColumn> columns) throws JsonParseException {
        try {
            return new SearchHitResponse(orderAndParseFields(metadata, fields, columns));
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
    
}