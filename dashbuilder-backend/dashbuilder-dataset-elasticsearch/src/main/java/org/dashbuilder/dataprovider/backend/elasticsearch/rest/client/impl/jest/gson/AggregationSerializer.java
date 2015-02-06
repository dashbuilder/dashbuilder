package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson;

import com.google.gson.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchRequest;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.SearchResponse;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.backend.BackendIntervalBuilderDynamicDate;
import org.dashbuilder.dataset.backend.date.DateUtils;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.*;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.impl.ElasticSearchDataSetMetadata;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Serializes DataSetGroup operations.
 *
 */
public class AggregationSerializer extends AbstractAdapter<AggregationSerializer> implements JsonSerializer<DataSetGroup> {
    protected static final String AGG_ORDER_ASC = "asc";
    protected static final String AGG_ORDER_DESC = "desc";
    protected static final String AGG_FIELD = "field";
    protected static final String AGG_TERM = "_term";
    protected static final String AGG_ORDER = "order";
    protected static final String AGG_MIN_DOC_COUNT = "min_doc_count";
    protected static final String AGG_TERMS = "terms";
    protected static final String AGG_AGGREGATIONS = "aggregations";
    protected static final String AGG_INTERVAL = "interval";
    protected static final String AGG_KEY = "_key";
    protected static final String AGG_HISTORGRAM = "histogram";
    protected static final String AGG_FORMAT = "format";
    protected static final String AGG_DATE_HISTORGRAM = "date_histogram";

    // TODO: @Inject
    protected BackendIntervalBuilderDynamicDate intervalBuilder;

    public AggregationSerializer(ElasticSearchDataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns) {
        super(metadata, definition, columns);
        intervalBuilder = new BackendIntervalBuilderDynamicDate();
    }

    public AggregationSerializer(ElasticSearchDataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns, SearchRequest request) {
        super(metadata, definition, columns, request);
        intervalBuilder = new BackendIntervalBuilderDynamicDate();
    }

    public JsonObject serialize(DataSetGroup groupOp, Type typeOfSrc, JsonSerializationContext context) {
        ColumnGroup columnGroup = groupOp.getColumnGroup();
        List<GroupFunction> groupFunctions = groupOp.getGroupFunctions();

        List<GroupFunction> columnPickUps = new LinkedList<GroupFunction>();
        
        // Group functions.
        JsonObject aggregationsObject = null;
        if (groupFunctions != null && !groupFunctions.isEmpty()) {
            aggregationsObject = new JsonObject();
            for (GroupFunction groupFunction : groupFunctions) {
                // If not a "group" lookup operation (not the groupby column), seralize the core function.
                if (groupFunction.getFunction() != null) {
                    serializeCoreFunction(aggregationsObject, groupFunction);
                } else {
                    columnPickUps.add(groupFunction);
                }
            }
        }

        // Group by columns.
        JsonObject groupByObject = null;
        if (columnGroup != null) {
            groupByObject = new JsonObject();
            String columnId = columnGroup.getColumnId();
            String sourceId = columnGroup.getSourceId();

            // Check that all column pickups are also column groups.
            if (!columnPickUps.isEmpty()) {
                for (GroupFunction groupFunction : columnPickUps) {
                    if (groupFunction.getFunction() == null) {
                        columnId = groupFunction.getColumnId();
                        if (!sourceId.equals(groupFunction.getSourceId())) throw new RuntimeException("Grouping by this source property [" + sourceId + "] not possible.");
                        if (!existColumnInMetadataDef(sourceId)) throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
                    }
                }
            }

            serializeGroupByFunction(groupByObject, columnGroup, columnId, aggregationsObject);

        } else {

            // If there is no group function, cannot use column pickups.
            if (!columnPickUps.isEmpty()) throw new RuntimeException("Column [" + columnPickUps.get(0).getSourceId() + "] pickup  failed. No grouping is set for this column.");

        }

        return groupByObject != null ? buildAggregations(groupByObject) : buildAggregations(aggregationsObject);
    }

    protected JsonObject buildAggregations(JsonObject object) {
        JsonObject result = new JsonObject();
        result.add("aggregations", object);
        return result;
    }

    /**
     * <p>Serializes a groupby function.</p>
     * <p>Example of TERM HISTOGRAM function serialization:</p>
     * <code>
     *     "column_id" : {
     *          "terms" : { "field" : "change" },
     *          "aggregations": {
     *              ....
     *          }
     *     }
     * </code>
     * @return
     */
    protected void serializeGroupByFunction(JsonObject parent, ColumnGroup columnGroup, String resultingColumnId, JsonObject aggregationsObject) {
        if (columnGroup == null || metadata == null) return;

        String sourceId = columnGroup.getSourceId();
        if (resultingColumnId == null) resultingColumnId = sourceId;
        boolean asc = columnGroup.isAscendingOrder();
        String order = asc ? AGG_ORDER_ASC : AGG_ORDER_DESC;
        ColumnType columnType = metadata.getColumnType(sourceId);
        GroupStrategy groupStrategy = columnGroup.getStrategy();
        String intervalSize = columnGroup.getIntervalSize();
        boolean areEmptyIntervalsAllowed = columnGroup.areEmptyIntervalsAllowed();
        int minDocCount = areEmptyIntervalsAllowed ? 0 : 1;
        // TODO: Support for maxIntervals.
        int maxIntervals = columnGroup.getMaxIntervals();

        if (ColumnType.LABEL.equals(columnType)) {
            // Translate into a TERMS aggregation.
            JsonObject subObject = new JsonObject();
            subObject.addProperty(AGG_FIELD, sourceId);
            JsonObject orderObject = new JsonObject();
            orderObject.addProperty(AGG_TERM, order);
            subObject.add(AGG_ORDER, orderObject);
            subObject.addProperty(AGG_MIN_DOC_COUNT, minDocCount);
            JsonObject result = new JsonObject();
            result.add(AGG_TERMS, subObject);
            if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
            parent.add(resultingColumnId, result);

            // Add the resulting dataset column.
            if (columns != null) {
                DataColumn column = new DataColumnImpl(resultingColumnId, columnType);
                column.setColumnGroup(new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize()));
                columns.add(0, column);
            }

        } else if (ColumnType.NUMBER.equals(columnType)) {
            // Translate into a HISTOGRAM aggregation.
            JsonObject subObject = new JsonObject();
            subObject.addProperty(AGG_FIELD, sourceId);
            if (intervalSize != null) subObject.addProperty(AGG_INTERVAL, Long.parseLong(intervalSize));
            JsonObject orderObject = new JsonObject();
            orderObject.addProperty(AGG_KEY, order);
            subObject.add(AGG_ORDER, orderObject);
            subObject.addProperty(AGG_MIN_DOC_COUNT, minDocCount);
            JsonObject result = new JsonObject();
            result.add(AGG_HISTORGRAM, subObject);
            if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
            parent.add(resultingColumnId, result);

            // Add the resulting dataset column.
            if (columns != null) {
                DataColumn column = new DataColumnImpl(resultingColumnId, columnType);
                column.setColumnGroup(new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize()));
                columns.add(0, column);
            }
        } else if (ColumnType.DATE.equals(columnType)) {
            // Translate into a DATE HISTOGRAM aggregation.
            DateIntervalType dateIntervalType = null;

            if (GroupStrategy.DYNAMIC.equals(columnGroup.getStrategy())) {
                Date[] limits = null;
                try {
                    limits = calculateDateLimits(columnGroup.getSourceId());
                } catch (ElasticSearchClientGenericException e) {
                    throw new RuntimeException("Cannot calculate date limits.", e);
                }
                if (limits != null) {
                    dateIntervalType = intervalBuilder.calculateIntervalSize(limits[0], limits[1], columnGroup);
                }
            } 
            
            if (dateIntervalType == null) {
                dateIntervalType = DateIntervalType.valueOf(intervalSize);
            }

            String interval = ElasticSearchJestClient.getInterval(dateIntervalType);
            String returnFormat = DateUtils.PATTERN_DAY;
            switch (dateIntervalType) {
                case MILLISECOND:
                    break;
                case HUNDRETH:
                    break;
                case TENTH:
                    break;
                case SECOND:
                    returnFormat = DateUtils.PATTERN_SECOND;
                    break;
                case MINUTE:
                    returnFormat = DateUtils.PATTERN_MINUTE;
                    break;
                case HOUR:
                    returnFormat = DateUtils.PATTERN_HOUR;
                    break;
                case DAY:
                    returnFormat = DateUtils.PATTERN_DAY;
                    break;
                case DAY_OF_WEEK:
                    returnFormat = DateUtils.PATTERN_DAY;
                    break;
                case WEEK:
                    break;
                case MONTH:
                    returnFormat = DateUtils.PATTERN_MONTH;
                    break;
                case QUARTER:
                    break;
                case YEAR:
                    returnFormat = DateUtils.PATTERN_YEAR;
                    break;
                case DECADE:
                    break;
                case CENTURY:
                    break;
                case MILLENIUM:
                    break;
                default:
                    throw new RuntimeException("No interval mapping for date interval type [" + dateIntervalType.name() + "].");
            }

            JsonObject subObject = new JsonObject();
            subObject.addProperty(AGG_FIELD, sourceId);
            subObject.addProperty(AGG_INTERVAL, interval);
            subObject.addProperty(AGG_FORMAT, returnFormat);
            JsonObject orderObject = new JsonObject();
            orderObject.addProperty(AGG_KEY, order);
            subObject.add(AGG_ORDER, orderObject);
            subObject.addProperty(AGG_MIN_DOC_COUNT, minDocCount);
            JsonObject result = new JsonObject();
            result.add(AGG_DATE_HISTORGRAM, subObject);
            if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
            parent.add(resultingColumnId, result);

            // Add the resulting dataset column.
            if (columns != null) {
                DataColumn column = new DataColumnImpl(resultingColumnId, ColumnType.LABEL);
                column.setIntervalType(dateIntervalType.name());
                column.setColumnGroup(new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize()));
                columns.add(0, column);
            }
        } else {
            throw new RuntimeException("No translation supported for column group with sourceId [" + sourceId + "] and group strategy [" + groupStrategy.name() + "].");
        }
    }


    /**
     * <p>Serializes a core function.</p>
     * <p>Example of SUM function serialization:</p>
     * <code>
     *     "column_id" : {
     *          "sum" : { "field" : "change" }
     *     }
     * </code>
     * @return
     */
    protected void serializeCoreFunction(JsonObject parent, GroupFunction groupFunction) {
        if (parent != null && groupFunction != null) {
            String sourceId = groupFunction.getSourceId();
            if (sourceId != null && !existColumnInMetadataDef(sourceId)) throw new RuntimeException("Aggregation by column [" + sourceId + "] failed. No column with the given id.");
            if (sourceId == null) sourceId = metadata.getColumnId(0);
            if (sourceId == null) throw new IllegalArgumentException("Aggregation from unknown column id.");
            String columnId = groupFunction.getColumnId();
            if (columnId == null) columnId = sourceId;

            AggregateFunctionType type = groupFunction.getFunction();
            String aggregationName = null;
            ColumnType sourceColumnType = metadata.getColumnType(sourceId);
            ColumnType resultingColumnType = sourceColumnType.equals(ColumnType.DATE) ? ColumnType.DATE : ColumnType.NUMBER;
            switch (type) {
                case COUNT:
                    aggregationName = "value_count";
                    break;
                case DISTINCT:
                    aggregationName = "cardinality";
                    break;
                case AVERAGE:
                    aggregationName = "avg";
                    break;
                case SUM:
                    aggregationName = "sum";
                    break;
                case MIN:
                    aggregationName = "min";
                    break;
                case MAX:
                    aggregationName = "max";
                    break;

            }
            JsonObject fieldObject = new JsonObject();
            fieldObject.addProperty("field", sourceId);
            JsonObject subObject = new JsonObject();
            subObject.add(aggregationName, fieldObject);
            parent.add(columnId, subObject);

            // Add the resulting dataset column.
            if (columns != null) {
                DataColumn column = new DataColumnImpl(columnId, resultingColumnType);
                columns.add(column);
            }
        }
    }

    /**
     * <p>Obtain the minimum date and maximum date values for the given column with identifier <code>dateColumnId</code>.</p>
     *
     * @param dateColumnId The column identifier for the date type column.
     * @return The minimum and maximum dates.
     */
    protected Date[] calculateDateLimits(String dateColumnId) throws ElasticSearchClientGenericException{
        ElasticSearchJestClient client = new ElasticSearchJestClient()
                .serverURL(definition.getServerURL()).clusterName(definition.getClusterName()).index(definition.getIndex());
        
        String minDateColumnId = dateColumnId + "_min";
        String maxDateColumnId = dateColumnId + "_max";
        
        // Create the aggregation model to bulid the query to EL server.
        DataSetGroup aggregation = new DataSetGroup();
        GroupFunction minFunction = new GroupFunction(dateColumnId, minDateColumnId, AggregateFunctionType.MIN);
        GroupFunction maxFunction = new GroupFunction(dateColumnId, maxDateColumnId, AggregateFunctionType.MAX);
        aggregation.addGroupFunction(minFunction, maxFunction);

        SearchRequest request = new SearchRequest(metadata);
        request.setAggregations(Arrays.asList(aggregation));
        
        // Append the filter clauses
        if (this.request != null && this.request.getQuery() != null) request.setQuery(this.request.getQuery());
        
        // Perform the query.
        SearchResponse searchResult = client.search(definition, metadata, request);
        if (searchResult != null) {
            SearchHitResponse[] hits = searchResult.getHits();
            if (hits != null && hits.length == 1) {
                SearchHitResponse hit0 = hits[0];
                Map<String, Object> fields = hit0.getFields();
                if (fields != null && !fields.isEmpty()) {
                    Date minValue = (Date) fields.get(minDateColumnId);
                    Date maxValue = (Date) fields.get(maxDateColumnId);
                    return new Date[] {minValue, maxValue};
                }
            }
        }

        return null;
    }

    protected boolean existColumnInMetadataDef(String name) {
        if (name == null || metadata == null) return false;

        int cols = metadata.getNumberOfColumns();
        for (int x = 0; x < cols; x++) {
            String colName = metadata.getColumnId(x);
            if (name.equals(colName)) return true;
        }
        return false;
    }
}
