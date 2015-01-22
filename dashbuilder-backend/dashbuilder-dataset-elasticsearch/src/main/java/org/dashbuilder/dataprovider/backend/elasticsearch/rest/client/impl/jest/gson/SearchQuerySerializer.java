package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.impl.DataColumnImpl;

import java.lang.reflect.Type;
import java.util.List;

public class SearchQuerySerializer extends AbstractAdapter<SearchQuerySerializer> implements JsonSerializer<ElasticSearchJestClient.SearchQuery> {

    protected static final String FIELDS = "fields";
    protected static final String FROM = "from";
    protected static final String QUERY = "query";
    protected static final String SIZE = "size";
    protected static final String AGGREGATIONS = "aggregations";

    public SearchQuerySerializer(DataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns) {
        super(metadata, definition, columns);
    }


    public JsonObject serialize(ElasticSearchJestClient.SearchQuery searchQuery, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        String[] fields = searchQuery.getFields();
        JsonObject query = searchQuery.getQuery();
        List<JsonObject> aggregations = searchQuery.getAggregations();
        boolean existAggregations = aggregations != null;

        // Trimming.
        // If aggregations exist, we care about the aggregation results, not document results.
        int start = searchQuery.getStart();
        int size = searchQuery.getSize();
        int sizeToPull =  existAggregations ? 0 : size;
        int startToPull  = existAggregations ? 0 : start;

        result.addProperty(FROM, startToPull);
        result.addProperty(SIZE, sizeToPull);

        // Build the search request in EL expected JSON format.
        if (query != null)  {
            JsonObject queryObject = query.getAsJsonObject(QUERY);
            result.add(QUERY, queryObject);
        }

        // TODO: Use all aggregations, not just first one.
        if (existAggregations) {
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
        if (!existAggregations) {
            JsonArray fieldsArray = new JsonArray();
            for (String field : fields) {
                fieldsArray.add(new JsonPrimitive(field));

                // As no exist aggreations, resulting columns to add into resulting dataset provides from metadata diefinition.
                ColumnType columnType = metadata.getColumnType(field);
                if (columns != null) {
                    DataColumn column = new DataColumnImpl(field, columnType);
                    columns.add(column);
                }

            }
            result.add(FIELDS, fieldsArray);
        }

        return result;
    }
}