package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchHitResponse;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.impl.ElasticSearchDataSetMetadata;

import java.lang.reflect.Type;
import java.util.*;

public class AggregationsDeserializer extends AbstractAdapter<AggregationsDeserializer> implements JsonDeserializer<SearchHitResponse[]> {

    public AggregationsDeserializer(ElasticSearchDataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns) {
        super(metadata, definition, columns);
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
                                result.add(new SearchHitResponse(orderAndParseFields(definition, metadata, bucketFields, columns)));
                            }
                        } else {
                            // Process no bucketed aggregations.
                            JsonElement aggValueElement = ((JsonObject)columnAggregationElement).get("value");
                            if (aggValueElement != null && aggValueElement.isJsonPrimitive()) noBucketFields.put(columnId, aggValueElement);
                        }
                    }
                }
                if (!noBucketFields.isEmpty()) result.add(new SearchHitResponse(orderAndParseFields(definition, metadata, noBucketFields, columns)));
            }
        }

        if (result == null) return null;
        return result.toArray(new SearchHitResponse[result.size()]);
    }
}