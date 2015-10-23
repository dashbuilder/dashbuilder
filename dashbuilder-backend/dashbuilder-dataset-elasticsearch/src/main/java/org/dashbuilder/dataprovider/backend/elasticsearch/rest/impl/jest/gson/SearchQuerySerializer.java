package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;

import java.lang.reflect.Type;
import java.util.List;

public class SearchQuerySerializer extends AbstractAdapter<SearchQuerySerializer> implements JsonSerializer<ElasticSearchJestClient.SearchQuery> {

    protected static final String FIELDS = "fields";
    protected static final String FROM = "from";
    protected static final String QUERY = "query";
    protected static final String SIZE = "size";
    protected static final String AGGREGATIONS = "aggregations";

    public SearchQuerySerializer(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns) {
        super(client, metadata, columns);
    }


    public JsonObject serialize(ElasticSearchJestClient.SearchQuery searchQuery, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        String[] fields = searchQuery.getFields();
        JsonObject query = searchQuery.getQuery();
        List<JsonObject> aggregations = searchQuery.getAggregations();
        boolean existAggregations = aggregations != null && !aggregations.isEmpty();

        // Trimming.
        // If aggregations exist, we care about the aggregation results, not document results.
        int start = searchQuery.getStart();
        int size = searchQuery.getSize();
        int sizeToPull =  existAggregations ? 0 : size;
        int startToPull  = existAggregations ? 0 : start;

        result.addProperty(FROM, startToPull);
        if (sizeToPull > -1) result.addProperty(SIZE, sizeToPull);

        // Build the search request in EL expected JSON format.
        if (query != null)  {
            JsonObject queryObject = query.getAsJsonObject(QUERY);
            result.add(QUERY, queryObject);
        }

        if (existAggregations) {
            // TODO: Use all aggregations, not just first one.
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
        if (!existAggregations && fields != null && fields.length > 0) {
            JsonArray fieldsArray = new JsonArray();
            for (String field : fields) {
                fieldsArray.add(new JsonPrimitive(field));
            }
            result.add(FIELDS, fieldsArray);
        }

        return result;
    }
}