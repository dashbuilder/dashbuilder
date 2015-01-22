package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.Query;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class QuerySerializer extends AbstractAdapter<QuerySerializer> implements JsonSerializer<Query> {
    public static final String SEARCH_API_FIELD = "field";
    public static final String SEARCH_API_EXISTS = "exists";
    public static final String SEARCH_API_TERM = "term";
    public static final String SEARCH_API_LT = "lt";
    public static final String SEARCH_API_LTE = "lte";
    public static final String SEARCH_API_GT = "gt";
    public static final String SEARCH_API_GTE = "gte";
    public static final String SEARCH_API_RANGE = "range";
    public static final String SEARCH_API_AND = "and";
    public static final String SEARCH_API_OR = "or";
    public static final String SEARCH_API_NOT  = "not";
    public static final String SEARCH_API_FILTER = "filter";
    public static final String SEARCH_API_FILTERED = "filtered";
    public static final String SEARCH_API_QUERY = "query";
    public static final String SEARCH_API_MATCH = "match";
    public static final String SEARCH_API_MATCH_ALL = "match_all";
    public static final String SEARCH_API_MUST = "must";
    public static final String SEARCH_API_MUST_NOT = "must_not";
    public static final String SEARCH_API_SHOULD = "should";
    public static final String SEARCH_API_BOOL = "bool";
    private Query query;
    private Gson gson = new GsonBuilder().create();

    public QuerySerializer(DataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns) {
        super(metadata, definition, columns);
    }

    public JsonObject serialize(Query src, Type typeOfSrc, JsonSerializationContext context) {
        this.query = src;

        JsonObject result = new JsonObject();
        JsonObject subResult = translate(query);
        String searchkey = isFilter(subResult) ? SEARCH_API_FILTER : SEARCH_API_QUERY;
        result.add(searchkey, subResult);
        return result;
    }

    private boolean isFilter(JsonObject object) {
        if (object == null) return false;
        String serializedObject = gson.toJson(object).trim();
        boolean isTermQuery = serializedObject.startsWith("{\"" + SEARCH_API_TERM);
        boolean isRangeQuery = serializedObject.startsWith("{\"" + SEARCH_API_RANGE);
        boolean isExistsQuery = serializedObject.startsWith("{\"" + SEARCH_API_EXISTS);
        boolean isNotQuery = serializedObject.startsWith("{\"" + SEARCH_API_NOT);
        boolean isOrQuery = serializedObject.startsWith("{\"" + SEARCH_API_OR);
        boolean isAndQuery = serializedObject.startsWith("{\"" + SEARCH_API_AND);
        return isTermQuery || isRangeQuery || isExistsQuery || isNotQuery || isOrQuery || isAndQuery;
    }

    private JsonObject translate(Query query) {
        if (query == null) return null;

        Query.Type type = query.getType();

        JsonObject result = null;

        switch (type) {
            case BOOL:
                return translateBool(query);
            case MATCH:
                return translateMatch(query);
            case MATCH_ALL:
                return translateMatchAll(query);
            case FILTERED:
                return translateFiltered(query);
            case AND:
                return translateAnd(query);
            case OR:
                return translateOr(query);
            case NOT:
                return translateNot(query);
            case EXISTS:
                return translateExists(query);
            case TERM:
                return translateTerm(query);
            case RANGE:
                return translateRange(query);
        }

        return result;
    }


    private JsonObject translateExists(Query query) {
        if (query == null) return null;

        String field = query.getField();
        JsonObject result = new JsonObject();
        JsonObject subResult = new JsonObject();
        subResult.addProperty(SEARCH_API_FIELD, field);
        result.add(SEARCH_API_EXISTS, subResult);
        return result;
    }

    private JsonObject translateTerm(Query query) {
        if (query == null) return null;

        String field = query.getField();
        Object value = query.getParam(Query.Parameter.VALUE.name());
        JsonObject result = new JsonObject();
        JsonObject subResult = new JsonObject();
        subResult.addProperty(field, (String) value);
        result.add(SEARCH_API_TERM, subResult);
        return result;
    }

    private JsonObject translateRange(Query query) {
        if (query == null) return null;

        String field = query.getField();
        JsonObject result = new JsonObject();

        JsonObject subResult = new JsonObject();
        addPrimitiveProperty(subResult, SEARCH_API_LT, query.getParam(Query.Parameter.LT.name()));
        addPrimitiveProperty(subResult, SEARCH_API_LTE, query.getParam(Query.Parameter.LTE.name()));
        addPrimitiveProperty(subResult, SEARCH_API_GT, query.getParam(Query.Parameter.GT.name()));
        addPrimitiveProperty(subResult, SEARCH_API_GTE, query.getParam(Query.Parameter.GTE.name()));
        JsonObject subObject = new JsonObject();
        subObject.add(field, subResult);
        result.add(SEARCH_API_RANGE, subObject);
        return result;
    }

    private void addPrimitiveProperty(JsonObject object, String key, Object value) {
        if (value != null) {
            if (value instanceof Number) {
                object.addProperty(key, (Number) value);
            } else if (value instanceof Date) {
                String datePattern = definition.getPattern(key);
                String formattedValue = new SimpleDateFormat(datePattern).format(value);
                object.addProperty(key, formattedValue);
            } else {
                object.addProperty(key, value.toString());
            }
        }
    }

    private JsonObject translateAnd(Query query) {
        if (query == null) return null;

        JsonObject result = new JsonObject();
        JsonElement filterObjects = null;
        try {
            filterObjects = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.FILTERS.name()));
        } catch (ClassCastException e) {
            filterObjects = translate((Query) query.getParam(Query.Parameter.FILTERS.name()));
        }
        result.add(SEARCH_API_AND, filterObjects);
        return result;
    }

    private JsonObject translateOr(Query query) {
        if (query == null) return null;

        JsonObject result = new JsonObject();
        JsonElement filterObjects = null;
        try {
            filterObjects = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.FILTERS.name()));
        } catch (ClassCastException e) {
            filterObjects = translate((Query) query.getParam(Query.Parameter.FILTERS.name()));
        }
        result.add(SEARCH_API_OR, filterObjects);
        return result;
    }

    private JsonObject translateNot(Query query) {
        if (query == null) return null;

        JsonObject result = new JsonObject();
        JsonElement filterObjects = null;
        try {
            filterObjects = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.FILTER.name()));
        } catch (ClassCastException e) {
            filterObjects = translate((Query) query.getParam(Query.Parameter.FILTER.name()));
        }
        result.add(SEARCH_API_NOT, filterObjects);
        return result;
    }

    private JsonObject translateFiltered(Query query) {
        if (query == null) return null;

        Query _query = (Query) query.getParam(Query.Parameter.QUERY.name());
        Query filter = (Query) query.getParam(Query.Parameter.FILTER.name());

        JsonObject queryObject = translate(_query);
        JsonObject filterObject = translate(filter);

        JsonObject result = new JsonObject();
        result.add(SEARCH_API_QUERY, queryObject);
        result.add(SEARCH_API_FILTER, filterObject);

        JsonObject filteredQuery = new JsonObject();
        filteredQuery.add(SEARCH_API_FILTERED, result);
        return filteredQuery;
    }

    private JsonObject translateMatch(Query query) {
        if (query == null) return null;

        String field = query.getField();
        Object value = query.getParam(Query.Parameter.VALUE.name());

        JsonObject result = new JsonObject();
        JsonObject subObject= new JsonObject();
        subObject.addProperty(field, (String) value);
        result.add(SEARCH_API_MATCH, subObject);
        return result;
    }

    private JsonObject translateMatchAll(Query query) {
        if (query == null) return null;

        JsonObject result = new JsonObject();
        result.add(SEARCH_API_MATCH_ALL, new JsonObject());
        return result;
    }

    private JsonObject translateBool(Query query) {
        if (query == null) return null;

        JsonObject result = new JsonObject();

        JsonElement mustObject = null;
        JsonElement mustNotObject = null;
        JsonElement shouldObject = null;
        try {
            mustObject = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.MUST.name()));
        } catch (ClassCastException e) {
            mustObject = translate((Query) query.getParam(Query.Parameter.MUST.name()));
        }
        try {
            mustNotObject = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.MUST_NOT.name()));
        } catch (ClassCastException e) {
            mustNotObject = translate((Query) query.getParam(Query.Parameter.MUST.name()));
        }
        try {
            shouldObject = translateGsonQueries((List<Query>) query.getParam(Query.Parameter.SHOULD.name()));
        } catch (ClassCastException e) {
            shouldObject = translate((Query) query.getParam(Query.Parameter.MUST.name()));
        }

        JsonObject bool = new JsonObject();
        if (mustObject != null) bool.add(SEARCH_API_MUST, mustObject);
        if (mustNotObject != null) bool.add(SEARCH_API_MUST_NOT, mustNotObject);
        if (shouldObject!= null) bool.add(SEARCH_API_SHOULD, shouldObject);
        result.add(SEARCH_API_BOOL, bool);
        return result;
    }

    private JsonElement translateGsonQueries(List<Query> queries) {
        JsonElement result = null;
        if (queries != null && !queries.isEmpty()) {
            result = new JsonObject();
            List<JsonObject> jsonObjects = translateQueries(queries);
            if (jsonObjects.size() == 1) {
                result = jsonObjects.get(0);
            } else if (jsonObjects.size() > 1) {
                JsonArray mustArray = new JsonArray();
                for (JsonObject jsonObject : jsonObjects) {
                    mustArray.add(jsonObject);
                }
                result = mustArray;
            }
        }
        return result;
    }

    private List<JsonObject> translateQueries(List<Query> queries) {
        List<JsonObject> result = new LinkedList<JsonObject>();
        for (Query subQuery : queries) {
            JsonObject subObject = translate(subQuery);
            result.add(subObject);
        }
        return result;
    }
}