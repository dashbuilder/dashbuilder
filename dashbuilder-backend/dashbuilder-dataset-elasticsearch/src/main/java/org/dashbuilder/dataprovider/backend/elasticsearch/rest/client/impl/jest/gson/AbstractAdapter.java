package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson;

import com.google.gson.JsonElement;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.impl.ElasticSearchDataSetMetadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract adapter implementation.
 */
public abstract class AbstractAdapter<T extends AbstractAdapter> {
    protected ElasticSearchDataSetMetadata metadata;
    protected List<DataColumn> columns;
    protected ElasticSearchDataSetDef definition;

    public AbstractAdapter(ElasticSearchDataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns) {
        this.metadata = metadata;
        this.columns = columns;
        this.definition = definition;
    }

    /**
     * Order the resulting fields and its values from <code>fields</code> map as the order defined by the resulting dataset columns in <code>columnss/code> list.
     *
     * @param fields  The fields to order.
     * @param columns The resulting columns definition order.
     * @return The ordered fields/values map.
     */
    protected static LinkedHashMap<String, Object> orderAndParseFields(ElasticSearchDataSetDef definition, ElasticSearchDataSetMetadata metadata, Map<String, JsonElement> fields, List<DataColumn> columns) {
        if (fields == null) return null;
        if (columns == null) return new LinkedHashMap<String, Object>(fields);

        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (DataColumn column : columns) {
            String columnId = column.getId();
            if (fields.containsKey(columnId)) {
                JsonElement valueElement = fields.get(columnId);
                Object value = ElasticSearchJestClient.parseValue(definition, metadata, column, valueElement);
                result.put(columnId, value);
            }
        }
        return result;
    }

    protected DataColumn getColumn(String columnId) {
        if (columns != null && columnId != null && !columns.isEmpty()) {
            for (DataColumn column : columns) {
                if (columnId.equals(column.getId())) return column;
            }
        }
        return null;
    }

}