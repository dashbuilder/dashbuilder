package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchHitResponse;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;

import java.lang.reflect.Type;
import java.util.*;

public class AggregationsDeserializer extends AbstractAdapter<AggregationsDeserializer> implements JsonDeserializer<SearchHitResponse[]> {

    public AggregationsDeserializer(DataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns) {
        super(metadata, definition, columns);
    }

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