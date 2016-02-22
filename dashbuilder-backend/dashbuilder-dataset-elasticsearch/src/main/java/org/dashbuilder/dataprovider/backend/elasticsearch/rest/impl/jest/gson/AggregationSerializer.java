package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.dashbuilder.DataSetCore;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchRequest;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.IntervalBuilderDynamicDate;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.*;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Serializes DataSetGroup operations.
 *
 */
public class AggregationSerializer extends AbstractAdapter<AggregationSerializer> implements JsonSerializer<DataSetGroup> {
    protected static final String AGG_ORDER_ASC = "asc";
    protected static final String AGG_ORDER_DESC = "desc";
    protected static final String AGG_FIELD = "field";
    protected static final String AGG_SCRIPT = "script";
    protected static final String AGG_SIZE = "size";
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

    protected IntervalBuilderDynamicDate intervalBuilder;

    public AggregationSerializer(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns) {
        this(client, metadata, columns, null);
    }

    public AggregationSerializer(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns, SearchRequest request) {
        super(client, metadata, columns, request);
        intervalBuilder = DataSetCore.get().getIntervalBuilderDynamicDate();
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
                    if (groupFunction.getFunction() == null && sourceId.equals(groupFunction.getSourceId())) {
                        columnId = groupFunction.getColumnId();
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
            subObject.addProperty(AGG_SIZE, 0);
            JsonObject result = new JsonObject();
            result.add(AGG_TERMS, subObject);
            if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
            parent.add(resultingColumnId, result);

            // Add the resulting dataset column.
            if (columns != null) {
                DataColumn column = getColumn(resultingColumnId);
                column.setColumnGroup(new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize()));
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
                DataColumn column = getColumn(resultingColumnId);
                column.setColumnGroup(new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize()));
            }
        } else if (ColumnType.DATE.equals(columnType)) {
            DateIntervalType dateIntervalType = null;
            
            // Fixed grouping -> use term field aggregation with a date format script.
            if (GroupStrategy.FIXED.equals(columnGroup.getStrategy())) {
                String dateColumnPattern = getDefinition().getPattern(sourceId);

                if (intervalSize != null) {
                    dateIntervalType = DateIntervalType.valueOf(intervalSize);
                } 

                if (dateIntervalType == null) {
                    throw new RuntimeException("Column [" + columnGroup.getColumnId() + "] is type Date and grouped using a fixed strategy, but the ate interval type is not specified. Please specify it.");
                }
                JsonObject subObject = new JsonObject();
                String[] scripts = buildIntervalExtractorScript(sourceId, columnGroup);
                String valueScript = scripts[0];
                String orderScript = scripts[1];
                subObject.addProperty(AGG_SCRIPT, valueScript);
                JsonObject orderObject = new JsonObject();
                if (orderScript == null) orderObject.addProperty(AGG_TERM, order);
                else orderObject.addProperty("_sortOrder", AGG_ORDER_ASC);
                subObject.add(AGG_ORDER, orderObject);
                subObject.addProperty(AGG_SIZE, 0);
                subObject.addProperty(AGG_FORMAT, dateColumnPattern);
                subObject.addProperty(AGG_MIN_DOC_COUNT, minDocCount);
                JsonObject result = new JsonObject();
                result.add(AGG_TERMS, subObject);
                
                if (orderScript != null) {
                    JsonObject scriptOrderObject = new JsonObject();
                    scriptOrderObject.addProperty("script",orderScript);
                    JsonObject minOrderObject = new JsonObject();
                    minOrderObject.add("min", scriptOrderObject);
                    if (aggregationsObject == null) aggregationsObject = new JsonObject();
                    aggregationsObject.add("_sortOrder", minOrderObject);
                }
                
                if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
                parent.add(resultingColumnId, result);
            }
            
            // Dynamic grouping -> use date histograms.
            if (GroupStrategy.DYNAMIC.equals(columnGroup.getStrategy())) {
                // If interval size specified by the lookup group operation, use it.
                if (intervalSize != null) {
                    dateIntervalType = DateIntervalType.valueOf(intervalSize);
                // If interval size is not specified by the lookup group operation, calculate the current date limits for index document's date field and the interval size that fits..
                } else {
                    try {
                        ElasticSearchClient anotherClient = client.getAnotherClient((ElasticSearchDataSetDef) metadata.getDefinition());
                        Date[] limits = client.getUtils().calculateDateLimits(anotherClient, metadata, columnGroup.getSourceId(), this.request != null ? this.request.getQuery() : null);
                        if (limits != null) {
                            dateIntervalType = intervalBuilder.calculateIntervalSize(limits[0], limits[1], columnGroup);
                        }
                    } catch (ElasticSearchClientGenericException e) {
                        throw new RuntimeException("Cannot calculate date limits.", e);
                    }
                }
                
                if (dateIntervalType == null) {
                    // Not limits found. No matches. No matter the interval type used.
                    dateIntervalType = DateIntervalType.MILLISECOND;
                }

                String interval = ElasticSearchJestClient.getInterval(dateIntervalType);
                String intervalPattern = DateIntervalPattern.getPattern(dateIntervalType);
                JsonObject subObject = new JsonObject();
                subObject.addProperty(AGG_FIELD, sourceId);
                subObject.addProperty(AGG_INTERVAL, interval);
                subObject.addProperty(AGG_FORMAT, intervalPattern);
                JsonObject orderObject = new JsonObject();
                orderObject.addProperty(AGG_KEY, order);
                subObject.add(AGG_ORDER, orderObject);
                subObject.addProperty(AGG_MIN_DOC_COUNT, minDocCount);
                JsonObject result = new JsonObject();
                result.add(AGG_DATE_HISTORGRAM, subObject);
                if (aggregationsObject != null) result.add(AGG_AGGREGATIONS, aggregationsObject);
                parent.add(resultingColumnId, result);
            } 

            // Add the resulting dataset column.
            if (columns != null) {
                DataColumn column = getColumn(resultingColumnId);
                column.setColumnType(ColumnType.LABEL);
                column.setIntervalType(dateIntervalType.name());
                ColumnGroup cg = new ColumnGroup(sourceId, resultingColumnId, columnGroup.getStrategy(), columnGroup.getMaxIntervals(), columnGroup.getIntervalSize());
                cg.setEmptyIntervalsAllowed(areEmptyIntervalsAllowed);
                cg.setFirstMonthOfYear(columnGroup.getFirstMonthOfYear());
                cg.setFirstDayOfWeek(columnGroup.getFirstDayOfWeek());
                column.setColumnGroup(cg);
            }
        } else {
            throw new RuntimeException("No translation supported for column group with sourceId [" + sourceId + "] and group strategy [" + groupStrategy.name() + "].");
        }
    }
    
    private String[] buildIntervalExtractorScript(String sourceId, ColumnGroup columnGroup) {
        DateIntervalType intervalType = DateIntervalType.getByName(columnGroup.getIntervalSize());
        Month firstMonth = columnGroup.getFirstMonthOfYear();
        DayOfWeek firstDayOfWeek = columnGroup.getFirstDayOfWeek();
        
        // Supported intervals for FIXED strategy - @see DateIntervalType.FIXED_INTERVALS_SUPPORTED
        String script = "new Date(doc[\"{0}\"].value).format(\"{1}\")";
        String pattern;
        switch (intervalType) {
            case QUARTER:
                // For quarters use this pseudocode script: <code>quarter = round-up(date.month / 3)</code>
                script = "ceil(new Date(doc[\"{0}\"].value).format(\"{1}\").toInteger() / 3).toInteger()";
                pattern = "M";
                break;
            case MONTH:
                pattern = "MM";
                break;
            case DAY_OF_WEEK:
                // Consider that scripts are executed in Groovy language, so the Date class uses SimpleDateFormat for formatting the value.
                // As SimpleDateFormat considers first day of week on monday, and we need it to be sunday, let's do the trick by 
                // parsing the date and increment it by one day (next function), then we can extract the day of week using "uu" pattern.
                script = "new Date(doc[\"{0}\"].value).next().format(\"{1}\")";
                pattern = "uu";
                break;
            case HOUR:
                pattern = "HH";
                break;
            case MINUTE:
                pattern = "mm";
                break;
            case SECOND:
                pattern = "ss";
                break;
            default:
                throw new UnsupportedOperationException("Fixed grouping strategy by interval type " + intervalType.name() + " is not supported.");
        }
        
        String valueScript = MessageFormat.format(script, sourceId, pattern);
        
        String orderScript = null;
        
        if (firstMonth != null && intervalType.equals(DateIntervalType.MONTH)) {
            int firstMonthIndex = firstMonth.getIndex();
            int[] positions = buildPositionsArray(firstMonthIndex, 12, columnGroup.isAscendingOrder());
            orderScript = "month="+valueScript+".toInteger(); list = "+Arrays.toString(positions)+"; list.indexOf(month)";
        }

        if (firstDayOfWeek!= null && intervalType.equals(DateIntervalType.DAY_OF_WEEK)) {
            int firstDayIndex = firstDayOfWeek.getIndex();
            int[] positions = buildPositionsArray(firstDayIndex, 7, columnGroup.isAscendingOrder());
            orderScript = "day="+valueScript+".toInteger(); list = "+Arrays.toString(positions)+"; list.indexOf(day)";
        }
        
        return new String[] { valueScript, orderScript};
    }
    
    private int[] buildPositionsArray(int firstElementIndex, int end, boolean asc) {
        int[] positions = new int[end];

        for (int x = 0, month = firstElementIndex; x < end; x++) {
            if (month > end) month = 1;
            if (month < 1 ) month = end;
            positions[x] = month;
            if (asc) month ++; else month--;
        }
        
        return positions;
        
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
            
        }
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
