package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.gson;

import com.google.gson.JsonElement;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchRequest;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract adapter implementation.
 */
public abstract class AbstractAdapter<T extends AbstractAdapter> {
    protected DataSetMetadata metadata;
    protected List<DataColumn> columns;
    protected ElasticSearchJestClient client;
    protected SearchRequest request;

    public AbstractAdapter(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns) {
        this(client, metadata, columns, null);
    }

    public AbstractAdapter(ElasticSearchJestClient client, DataSetMetadata metadata, List<DataColumn> columns, SearchRequest request) {
        this.metadata = metadata;
        this.columns = columns;
        this.request = request;
        this.client = client;
    }
    
    protected ElasticSearchDataSetDef getDefinition() {
        return (ElasticSearchDataSetDef) metadata.getDefinition();
    }

    /**
     * Order the resulting fields and its values from <code>fields</code> map as the order defined by the resulting dataset columns in <code>columnss/code> list.
     *
     * @param fields  The fields to order.
     * @param columns The resulting columns definition order.
     * @return The ordered fields/values map.
     */
    protected LinkedHashMap<String, Object> orderAndParseFields(DataSetMetadata metadata, Map<String, JsonElement> fields, List<DataColumn> columns) throws ParseException {
        if (fields == null) return null;
        if (columns == null) return new LinkedHashMap<String, Object>(fields);

        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (DataColumn column : columns) {
            String columnId = column.getId();
            if (fields.containsKey(columnId)) {
                JsonElement valueElement = fields.get(columnId);
                Object value = client.parseValue(metadata, column, valueElement);
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