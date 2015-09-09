package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.*;

public class HitDeserializer extends AbstractAdapter<HitDeserializer> implements JsonDeserializer<SearchHitResponse> {

    public HitDeserializer(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns) {
        super(client, metadata, columns);
    }

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
            Map<String ,JsonElement> fields = new HashMap<String, JsonElement>();
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
                            if (element != null && element.isJsonPrimitive()) fields.put(fieldName, element);
                        }
                    }
                }
            }

            if (sourceObject != null && sourceObject.isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> _fields = ((JsonObject)sourceObject).entrySet();
                for (Map.Entry<String, JsonElement> field : _fields) {
                    String fieldName = field.getKey();
                    fields.put(fieldName, field.getValue());
                }

            }

            result = createHitResponse(score, index, id, type, version, fields);
        }

        return result;
    }

    private SearchHitResponse createHitResponse(float score, String index, String id, String type, long version, Map<String ,JsonElement> fields) throws JsonParseException {
        try {
            return new SearchHitResponse(score, index, id, type, version, orderAndParseFields(metadata, fields, columns));
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }


}