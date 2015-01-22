/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.ElasticSearchQueryBuilder;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.ElasticSearchQueryBuilderImpl;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.impl.jest.ElasticSearchJestClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.*;
import org.dashbuilder.dataset.*;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.events.DataSetStaleEvent;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.impl.DataSetMetadataImpl;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * <p>Data provider for an ElasticSearch server.</p>
 * <p>It's basically implemented as a REST client for querying an ElasticSearch server instance.</p>
 * 
 * <p>If a given type field is not explicitly mapped as a concrete dashbuilder datatype using the <code>columns</code> parameter, the implicit data type bindings are:</p>
 * <table>
 *     <tr>
 *         <th>ElasticSearch type</th>
 *         <th>Dashbuilder type</th>
 *     </tr>
 *     <tr>
 *         <td>string</td>
 *         <td>TEXT ( if <code>index</code> value is <code>analyzed</code> ) or LABEL ( if <code>index</code> value is <code>not_analyzed</code> )</td>
 *     </tr>
 *     <tr>
 *         <td>float</td>
 *         <td>NUMBER</td>
 *     </tr>
 *     <tr>
 *         <td>double</td>
 *         <td>NUMBER</td>
 *     </tr>
 *     <tr>
 *         <td>byte</td>
 *         <td>NUMBER</td>
 *     </tr>
 *     <tr>
 *         <td>short</td>
 *         <td>NUMBER</td>
 *     </tr>
 *     <tr>
 *         <td>integer</td>
 *         <td>NUMBER</td>
 *     </tr>
 *     <tr>
 *         <td>long</td>
 *         <td>NUMBER</td>
 *     </tr>
 *     <tr>
 *         <td>token_count</td>
 *         <td>LABEL</td>
 *     </tr>
 *     <tr>
 *         <td>date</td>
 *         <td>DATE</td>
 *     </tr>
 *     <tr>
 *         <td>boolean</td>
 *         <td>LABEL</td>
 *     </tr>
 *     <tr>
 *         <td>binary</td>
 *         <td>LABEL</td>
 *     </tr>
 * </table>
 * 
 * @since 0.3.0
 * 
 */
@Named("elasticsearch")
@Dependent
public class ElasticSearchDataSetProvider implements DataSetProvider {

    protected static final String NULL_VALUE = "---";
    protected static final String DEFAULT_DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    // TODO: @Inject
    protected ElasticSearchClient client;
    protected ElasticSearchQueryBuilder queryBuilder;
    
    protected final Map<String,DataSetMetadata> _metadataMap = new HashMap<String,DataSetMetadata>();

    public ElasticSearchDataSetProvider() {
    }

    @PostConstruct
    public void init() {
        // TODO: Remove when injection /producer method works.
        client = new ElasticSearchJestClient();
        queryBuilder = new ElasticSearchQueryBuilderImpl();
    }
    
    public DataSetProviderType getType() {
        return DataSetProviderType.ELASTICSEARCH;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        ElasticSearchDataSetDef elDef = (ElasticSearchDataSetDef) def;

         // TODO: compute any existing def.getDataSetFilter() setting both in getRowCount and in _lookupDataSet.

        // Look first into the static data set provider cache.
        if (elDef.isCacheEnabled()) {
            DataSet dataSet = staticDataSetProvider.lookupDataSet(def.getUUID(), null);
            if (dataSet != null) {

                // Lookup from cache.
                return staticDataSetProvider.lookupDataSet(def.getUUID(), lookup);
            }  else  {

                // Fetch always from EL server if existing rows are greater than the cache max. rows
                long rows = getRowCount(elDef);
                if (rows > elDef.getCacheMaxRows()) {
                    return _lookupDataSet(elDef, lookup);
                }
                // Fetch from EL server and register into the static cache. Further requests will lookup from cache.
                dataSet = _lookupDataSet(elDef, null);
                dataSet.setUUID(def.getUUID());
                dataSet.setDefinition(def);
                staticDataSetProvider.registerDataSet(dataSet);
                return staticDataSetProvider.lookupDataSet(def.getUUID(), lookup);
            }
        }

        // If cache is disabled then always fetch from EL server.
        return _lookupDataSet(elDef, lookup);
    }

    protected DataSet _lookupDataSet(ElasticSearchDataSetDef elDef, DataSetLookup lookup) throws Exception {
        DataSetMetadata metadata = getDataSetMetadata(elDef);
        int numRows = lookup.getNumberOfRows();
        int rowOffset = lookup.getRowOffset();
        String[] index = elDef.getIndex();
        String[] type = elDef.getType();
        
        boolean trim = (lookup != null && numRows > 0);

        SearchRequest request = new SearchRequest(metadata);

        if (index != null) request.setIndexes(index);
        if (type != null) request.setTypes(type);
        int numberOfColumns = metadata.getNumberOfColumns();
        List<String> columnIds = new ArrayList<String>(numberOfColumns);
        for (int x = 0; x < numberOfColumns; x++) {
            columnIds.add(metadata.getColumnId(x));
        }
        if (!columnIds.isEmpty()) {
            request.setFields(columnIds.toArray(new String[columnIds.size()]));
        }

        // Pagination.
        if (trim) {
            request.setStart(rowOffset);
            request.setSize(numRows);
        }
        
        if (lookup != null && !lookup.getOperationList().isEmpty()) {
            List<DataSetGroup> groupOps = lookup.getOperationList(DataSetGroup.class);
            List<DataSetFilter> filters = lookup.getOperationList(DataSetFilter.class);
            List<DataSetSort> sortOps = lookup.getOperationList(DataSetSort.class);

            // Check that operation source fields exist as dataset columns.
            checkOperations(metadata, groupOps, filters, sortOps);

            // Group operations.
            request.setAggregations(groupOps);

            // Filter operations.
            if (filters != null && !filters.isEmpty()) {
                
                // The query is build from a given filters and/or from interval selections. Built it.
                Query query = getQueryBuilder().metadata(metadata).groupInterval(groupOps).filter(filters).build();
                request.setQuery(query);
            }
            
            // Sort operations.
            request.setSorting(sortOps);
        }

        // Default sorting.
        // If no sort operation defined for this lookup, sort for the specified default field on dataset definition, if exists.
        List<DataSetSort> sortOps = request.getSorting();
        if ( (sortOps == null || sortOps.isEmpty()) && elDef.getColumnSort() != null) {
            if (sortOps == null) sortOps = new ArrayList<DataSetSort>();
            DataSetSort defaultSort = new DataSetSort();
            defaultSort.addSortColumn(elDef.getColumnSort());
            sortOps.add(defaultSort);
            request.setSorting(sortOps);
        }
        
        
        
        // Perform the query & generate the resulting dataset.
        DataSet dataSet = DataSetFactory.newEmptyDataSet();
        SearchResponse searchResponse = getClient(elDef).search(elDef, request);

        // Add the dataset columns.
        addDataSetColumns(dataSet, searchResponse);

        // There are no results. Return an empty dataset.
        if (searchResponse instanceof EmptySearchResponse) return dataSet;

        // There exist values. Fill the dataset.
        fillDataSetValues(elDef, dataSet, searchResponse.getHits());

        if (trim) {
            // TODO: Do not truncate!
            dataSet.setRowCountNonTrimmed((int)searchResponse.getTotalHits());            
        }
        return dataSet;
    }

    private void checkOperations(DataSetMetadata metadata, List<DataSetGroup> groupOps, List<DataSetFilter> filterOps, List<DataSetSort> sortOps) {
        if (metadata == null) return;
        
        // Check group operations.
        if (groupOps != null && !groupOps.isEmpty()) {
            for (DataSetGroup groupOp : groupOps) {
                if (groupOp.getColumnGroup() != null && !existColumn(metadata, groupOp.getColumnGroup().getSourceId())) throw new IllegalArgumentException("Grouping by a non existing column [" + groupOp.getColumnGroup().getSourceId() + "] in dataset ");
                List<GroupFunction> groupFunctions = groupOp.getGroupFunctions();
                if (groupFunctions != null && !groupFunctions.isEmpty()) {
                    for (GroupFunction groupFunction : groupFunctions) {
                        if (groupFunction.getSourceId() != null && 
                                !existColumn(metadata, groupFunction.getSourceId())) throw new IllegalArgumentException("Grouping function by a non existing column [" + groupFunction.getSourceId() + "] in dataset ");
                    }
                }
            }
        }

        // Check filter operations.
        if (filterOps != null && !filterOps.isEmpty()) {
            for (DataSetFilter filerOp  : filterOps) {
                List<ColumnFilter> filters = filerOp.getColumnFilterList();
                if (filters != null && !filters.isEmpty()) {
                    for (ColumnFilter filter: filters) {
                        if (!existColumn(metadata, filter.getColumnId())) throw new IllegalArgumentException("Filtering by a non existing column [" + filter.getColumnId() + "] in dataset "); 
                    }
                }
            }
        }

        // Check filter operations.
        if (sortOps != null && !sortOps.isEmpty()) {
            for (DataSetSort sortOp : sortOps) {
                
                List<ColumnSort> sorts  = sortOp.getColumnSortList();
                if (sorts != null && !sorts.isEmpty()) {
                    for (ColumnSort sort : sorts) {
                        if (!existColumn(metadata, sort.getColumnId())) throw new IllegalArgumentException("Sorting by a non existing column [" + sort.getColumnId() + "] in dataset ");
                    }
                }
            }
        }
    }
    
    private boolean existColumn(DataSetMetadata metadata, String columnId) {
        if (metadata == null || columnId == null || columnId.trim().length() == 0) return false;
        
        int numCols = metadata.getNumberOfColumns();
        for (int x = 0; x < numCols; x++) {
            String metaColumnId = metadata.getColumnId(x);
            if (columnId.equals(metaColumnId)) return true;
        }
        
        return false;
    }

    /**
     * Fills the dataset values.
     * 
     * @param dataSet The dataset instance to fill. Note that dataset columns must be added before calling this method.
     * @param hits The search result hits.
     *             
     * @throws Exception 
     */
    protected void fillDataSetValues(ElasticSearchDataSetDef elDef, DataSet dataSet, SearchHitResponse[] hits) throws Exception {
        List<DataColumn> dataSetColumns = dataSet.getColumns();
        int position = 0;
        for (SearchHitResponse hit : hits) {
            int columnNumber = 0;
            for (DataColumn column : dataSetColumns) {
                String columnId = column.getId();
                ColumnType columnType = column.getColumnType();
                Object value = hit.getFieldValue(columnId);
                Object formattedValue = formatColumnValue(elDef, column, value);
                dataSet.setValueAt(position, columnNumber, formattedValue);
                columnNumber++;
            }
            position++;
        }
    }

    /**
     * Formats a given value for a given column type.
     * @param column The data column definition.
     * @param value The value to format
     * @return The formatted value for the given column type.
     */
    protected Object formatColumnValue(ElasticSearchDataSetDef elDef, DataColumn column, Object value) throws Exception {

        ColumnType columnType = column.getColumnType();
        
        if (ColumnType.NUMBER.equals(columnType)) {
            if (value == null || value.toString().trim().length() == 0) return 0d;
            if (value instanceof Number) return ((Number)value).doubleValue();
            else if (value instanceof String) return Double.parseDouble((String)value);
        }
        else if (ColumnType.DATE.equals(columnType)) {
            if (value == null || value.toString().trim().length() == 0) return new Date();
            return value;
            
            /*
            
            // Obtain the date value from the EL pattern specified.
            String datePattern = elDef.getPattern(column.getId());
            DateTime dateTime = DateTimeFormat.forPattern(datePattern).parseDateTime(value.toString());
            Date date = dateTime.toDate();
            return date;
            
            
            String intervalType = column.getIntervalType();
            DateIntervalType type = DateIntervalType.getByName(intervalType);
            GroupStrategy strategy = column.getColumnGroup() != null ? column.getColumnGroup().getStrategy() : null;
            if (type == null) type = DateIntervalType.DAY;
            if (strategy == null) strategy= GroupStrategy.DYNAMIC;
            return DateUtils.parseDate(type, strategy, date);
            */
        }
        
        // LABEL or TEXT colum types.
        if (value == null || value.toString().trim().length() == 0) return NULL_VALUE;
        else return value.toString();
    }

    /**
     * Creates the columns for the dataset.
     * @param dataSet The dataset instance.
     * @param searchResponse The resulting columns for the performed query.
     *
     * @throws Exception
     */
    protected void addDataSetColumns(DataSet dataSet, SearchResponse searchResponse) throws Exception {
        List<DataColumn> columns = searchResponse.getColumns();
        if (columns != null && !columns.isEmpty()) {
            int x = 0;
            for (DataColumn column : columns) {
                String columnId = column.getId();
                ColumnType columnType = column.getColumnType();
                dataSet.addColumn(columnId, columnId, columnType);
            }
        }
        
    }

    public boolean isDataSetOutdated(DataSetDef def) {
        try {
            // If cache is disabled then no way for a data set to get outdated
            ElasticSearchDataSetDef elDef = (ElasticSearchDataSetDef) def;
            if (!elDef.isCacheEnabled()) return false;

            // ... for non cached data sets either.
            DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);
            if (dataSet == null) return false;

            // Compare the cached vs elasticsearch server rows.
            long rows = getRowCount(elDef);
            
            return rows != dataSet.getRowCount();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public DataSetMetadata getDataSetMetadata(DataSetDef def) throws Exception {
        // Type casting.
        ElasticSearchDataSetDef elasticSearchDataSetDef = (ElasticSearchDataSetDef) def;
        // Check if metadata already exists in cache.
        DataSetMetadata result = _metadataMap.get(elasticSearchDataSetDef.getUUID());
        if (result != null) return result;

        // Data Set parameters.
        String[] index = elasticSearchDataSetDef.getIndex();
        String[] type = elasticSearchDataSetDef.getType();
        
        // Get the row count.
        long rowCount = getRowCount(elasticSearchDataSetDef);

        // Obtain the indexMappings
        MappingsResponse mappingsResponse = getClient(elasticSearchDataSetDef).getMappings(index);
        // TODO: Check response code too.
        if (mappingsResponse == null) throw new IllegalArgumentException("Cannot retrieve index mappings for index: [" + index[0] + "]");


        // Obtain the columns (ids and types).
        List<String> columnIds = new LinkedList<String>();
        List<ColumnType> columnTypes = new LinkedList<ColumnType>();
        
        // Check if custom columns has been configured in the dataset definition or we have to query the index mappings and retrieve column information from it.
        Map<String, ColumnType> columns = parseColumns(mappingsResponse.getIndexMappings(), elasticSearchDataSetDef);
        if (columns == null || columns.isEmpty()) throw new RuntimeException("There are no column for index [" + index[0] + "] and type [" + ArrayUtils.toString(type) + "].");
        
        List<DataColumn> dataSetColumns = elasticSearchDataSetDef.getDataSet().getColumns();
        if (dataSetColumns != null && !dataSetColumns.isEmpty()) {
            for (DataColumn column : dataSetColumns) {
                String columnId = column.getId();
                ColumnType columnType = column.getColumnType();
                
                ColumnType indexColumnType = columns.get(columnId);
                // Check user defined column exists in the index/type.
                if (indexColumnType == null) throw new IllegalArgumentException("The column [" + columnId + "] defined in dataset definition does not exist for the index [" + index[0] + "] and type [" + ArrayUtils.toString(type) + "].");
                // Check that analyzed fields on EL index definition are analyzed too in the dataset definition.
                if (indexColumnType.equals(ColumnType.TEXT) && columnType.equals(ColumnType.LABEL)) throw new IllegalArgumentException("The column [" + columnId + "] is defined in dataset definition as LABEL, but the column in the index [" + index[0] + "] and type [" + ArrayUtils.toString(type) + "] is using ANALYZED index, you cannot use it as a label.");
                columnIds.add(columnId);
                columnTypes.add(columnType);
            }
        }
        // No custom columns has been configured in the dataset definition, obtain the columns and column types for the dataset to build by querying the index mappings.        
        else {
            for (Map.Entry<String, ColumnType> entry : columns.entrySet()) {
                String columnId = entry.getKey();
                ColumnType columnType = entry.getValue();
                columnIds.add(columnId);
                columnTypes.add(columnType);
            }
        }
        
        // TODO: Do not change the data type to int!
        int _rowCount = (int) rowCount;
        // TODO: Improve estimation.
        int estimatedSize = 100 * _rowCount;
        
        
        // Put into cache.
        _metadataMap.put(def.getUUID(), result =
                new DataSetMetadataImpl(def, def.getUUID(), _rowCount,
                        columnIds.size(), columnIds, columnTypes, estimatedSize));
        
        return result;
    }
    
    protected Map<String,ColumnType> parseColumns(IndexMappingResponse[] indexMappings, ElasticSearchDataSetDef def) {
        Map<String, ColumnType> result = null;
        
        for (IndexMappingResponse indexMapping : indexMappings) {
            result = new LinkedHashMap<String, ColumnType>();
            String indexName = indexMapping.getIndexName();
            TypeMappingResponse[] typeMappings = indexMapping.getTypeMappings();
            if (typeMappings == null || typeMappings.length == 0) throw new IllegalArgumentException("There are no types for index: [" + indexName + "[");
            for (TypeMappingResponse typeMapping : typeMappings) {
                String typeName = typeMapping.getTypeName();
                FieldMappingResponse[] properties = typeMapping.getFields();
                if (properties == null || properties.length == 0) throw new IllegalArgumentException("There are no fields for index: [" + indexName + "] and type [" + typeName + "[");
                for (FieldMappingResponse fieldMapping : properties) {
                    // TODO: Support for other field properties.
                    String fieldName = fieldMapping.getName();
                    String format = fieldMapping.getFormat();

                    String columnId = getColumnId(indexName, typeName, fieldName);
                    ColumnType columnType = getDataType(fieldMapping);
                    boolean columnExists = result.containsKey(columnId);
                    if ( columnExists ) {
                        // Check column type for existing column.
                        ColumnType existingColumnType = result.get(columnExists);
                        if (existingColumnType != null && !existingColumnType.equals(columnType)) throw new IllegalArgumentException("Column [" + columnId + "] is already present in data set with type [" + existingColumnType + "] and you are trying to add it again as type [" + columnType.toString() + "[");

                        // Check column format for existing column.
                        if (!StringUtils.isBlank(format)) {
                            String existingPattern = def.getPattern(columnId);
                            if (existingPattern != null && !existingPattern.equals(format)) throw new IllegalArgumentException("Column [" + columnId + "] is already present in data set with pattern [" + existingPattern + "] and you are trying to add it again with pattern [" + format + "[");
                            def.setPattern(columnId, format);
                        } else {
                            // Apply elasticsearch default date format.
                            def.setPattern(columnId, DEFAULT_DATE_PATTERN);
                        }
                    } else {
                        result.put(columnId, columnType);
                    }

                }
            }
        }
        
        return result;
    }
    
    protected String getColumnId(String index, String type, String field) throws IllegalArgumentException {
        if (index == null || index.trim().length() == 0) throw new IllegalArgumentException("Cannot create the column identifier. Index name is not set.");
        if (type == null || type.trim().length() == 0) throw new IllegalArgumentException("Cannot create the column identifier. Document type name is not set.");
        if (field == null || field.trim().length() == 0) throw new IllegalArgumentException("Cannot create the column identifier. Field name is not set.");
        return field;
    }
    
    /**
     * <p>Return the dashbuilder data type for a given ElasticSearch field type.</p>
     *
     * @param fieldMapping The ElasticSearch field type..
     * @return The dashbuilder data type.
     * @throws IllegalArgumentException If ElasticSearch core data type is not supported.
     */
    protected ColumnType getDataType(FieldMappingResponse fieldMapping) throws IllegalArgumentException {
        FieldMappingResponse.FieldType fieldType = fieldMapping.getDataType();
        switch (fieldType) {
            case STRING:
                if (fieldMapping.getIndexType() != null  && fieldMapping.getIndexType().equals(FieldMappingResponse.IndexType.NOT_ANALYZED)) return ColumnType.LABEL;
                // Analyzed index are considered TEXT.
                return ColumnType.TEXT;
            case FLOAT:
                return ColumnType.NUMBER;
            case DOUBLE:
                return ColumnType.NUMBER;
            case BYTE:
                return ColumnType.NUMBER;
            case SHORT:
                return ColumnType.NUMBER;
            case INTEGER:
                return ColumnType.NUMBER;
            case LONG:
                return ColumnType.NUMBER;
            case TOKEN_COUNT:
                return ColumnType.LABEL;
            case DATE:
                return ColumnType.DATE;
            case BOOLEAN:
                return ColumnType.LABEL;
            case BINARY:
                return ColumnType.LABEL;
        }
        
        throw new IllegalArgumentException("The ElasticSearch core data type [" + fieldType.toString() + "] is not suppored.");
    }

    protected long getRowCount(ElasticSearchDataSetDef elasticSearchDataSetDef) throws Exception {
        String[] index = elasticSearchDataSetDef.getIndex();
        String[] type = elasticSearchDataSetDef.getType();
        
        CountResponse response = getClient(elasticSearchDataSetDef).count(index, type);
        
        if (response != null) return response.getCount();
        return 0;
    }

    /**
     * Obtain an elasticsearch client for a given definition.
     * 
     * TODO: Cache
     * @param elasticSearchDataSetDef The elasticsearch dataset definition.
     * @return The dashbuilder REST client for this definition.
     * @throws IllegalArgumentException
     */
    protected ElasticSearchClient getClient(ElasticSearchDataSetDef elasticSearchDataSetDef) throws IllegalArgumentException{
        String serverURL = elasticSearchDataSetDef.getServerURL();
        String clusterName = elasticSearchDataSetDef.getClusterName();
        if (serverURL == null || serverURL.trim().length() == 0) throw new IllegalArgumentException("Server URL is not set.");
        if (clusterName == null || clusterName.trim().length() == 0) throw new IllegalArgumentException("Cluster name is not set.");

        // TODO: New instance for every request.
        client = new ElasticSearchJestClient();
        client.serverURL(serverURL).clusterName(clusterName);
        
        String[] indexes = elasticSearchDataSetDef.getIndex();
        if (indexes != null && indexes.length > 0) client.index(indexes);
        String[] types  = elasticSearchDataSetDef.getType();
        if (types != null && types.length > 0) client.type(types);
        
        return client;
    }
    
    protected ElasticSearchQueryBuilder getQueryBuilder() {
        // TODO: New instance for every request.
        return queryBuilder = new ElasticSearchQueryBuilderImpl();
    }

    // Listen to changes on the data set definition registry

    protected void onDataSetStaleEvent(@Observes DataSetStaleEvent event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.ELASTICSEARCH.equals(def.getProvider())) {
            String uuid = def.getUUID();
            _metadataMap.remove(uuid);
            staticDataSetProvider.removeDataSet(uuid);
        }
    }
}
