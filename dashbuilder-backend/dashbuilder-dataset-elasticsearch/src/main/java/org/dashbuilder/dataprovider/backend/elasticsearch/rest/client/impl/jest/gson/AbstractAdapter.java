package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.gson;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract adapter implementation.
 */
public abstract  class AbstractAdapter<T extends AbstractAdapter> {
    protected DataSetMetadata metadata;
    protected List<DataColumn> columns;
    protected ElasticSearchDataSetDef definition;

    public AbstractAdapter(DataSetMetadata metadata, ElasticSearchDataSetDef definition, List<DataColumn> columns) {
        this.metadata = metadata;
        this.columns = columns;
        this.definition = definition;
    }

    /**
     * Order the resulting fields and its values from <code>fields</code> map as the order defined by the resulting dataset columns in <code>columnss/code> list.
     * @param fields The fields to order.
     * @param columns The resulting columns definition order.
     * @return The ordered fields/values map.
     */
    protected static LinkedHashMap<String, Object> orderFields(Map<String, Object> fields, List<DataColumn> columns) {
        if (fields == null) return null;
        if (columns == null) return new LinkedHashMap<String, Object>(fields);

        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (DataColumn column : columns) {
            String columnId = column.getId();
            if (fields.containsKey(columnId)) result.put(columnId, fields.get(columnId));
        }
        return result;
    }
    
}