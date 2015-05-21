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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model.*;
import org.dashbuilder.dataset.*;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.events.DataSetStaleEvent;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.impl.ElasticSearchDataSetMetadata;
import org.dashbuilder.dataset.impl.MemSizeEstimator;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
@Named("elasticsearch")
public class ElasticSearchDataSetProvider implements DataSetProvider {

    public static final DateTimeFormatter EL_DEFAULT_DATETIME_FORMATTER = ISODateTimeFormat.dateOptionalTimeParser();
    public static final int RESPONSE_CODE_OK = 200;

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    protected ElasticSearchClientFactory clientFactory;

    @Inject
    protected ElasticSearchQueryBuilderFactory queryBuilderFactory;
    
    protected final Map<String,DataSetMetadata> _metadataMap = new HashMap<String,DataSetMetadata>();

    public ElasticSearchDataSetProvider() {
    }

    public DataSetProviderType getType() {
        return DataSetProviderType.ELASTICSEARCH;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        ElasticSearchDataSetDef elDef = (ElasticSearchDataSetDef) def;

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
        ElasticSearchDataSetMetadata metadata = (ElasticSearchDataSetMetadata) getDataSetMetadata(elDef);
        int numRows = lookup.getNumberOfRows();
        int rowOffset = lookup.getRowOffset();
        String[] index = elDef.getIndex();
        String[] type = elDef.getType();
        
        boolean trim = (lookup != null && numRows > 0);

        SearchRequest request = new SearchRequest(metadata);

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
                Query query = queryBuilderFactory.newQueryBuilder().metadata(metadata).groupInterval(groupOps).filter(filters).build();
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
        SearchResponse searchResponse = clientFactory.newClient(elDef).search(elDef, metadata, request);

        // Add the dataset columns.
        addDataSetColumns(dataSet, searchResponse);

        // There are no results. Return an empty dataset.
        if (searchResponse instanceof EmptySearchResponse) return dataSet;

        // There exist values. Fill the dataset.
        fillDataSetValues(elDef, dataSet, searchResponse.getHits());

        if (trim) {
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
                Object value = hit.getFieldValue(columnId);
                dataSet.setValueAt(position, columnNumber, value);
                columnNumber++;
            }
            position++;
        }
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
                dataSet.addColumn(column);
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
        ElasticSearchDataSetMetadata result = (ElasticSearchDataSetMetadata) _metadataMap.get(elasticSearchDataSetDef.getUUID());
        if (result != null) return result;

        // Data Set parameters.
        String[] index = elasticSearchDataSetDef.getIndex();
        String[] type = elasticSearchDataSetDef.getType();
        
        // Get the row count.
        long rowCount = getRowCount(elasticSearchDataSetDef);

        // Obtain the indexMappings
        MappingsResponse mappingsResponse = clientFactory.newClient(elasticSearchDataSetDef).getMappings(index);
        if (mappingsResponse == null || mappingsResponse.getStatus() != RESPONSE_CODE_OK) throw new IllegalArgumentException("Cannot retrieve index mappings for index: [" + index[0] + "]. See previous errors.");


        // Obtain the columns (ids and types).
        List<String> columnIds = new LinkedList<String>();
        List<ColumnType> columnTypes = new LinkedList<ColumnType>();
        
        // Check if custom columns has been configured in the dataset definition or we have to query the index mappings and retrieve column information from it.
        Map<String, Object[]> columns = parseColumns(mappingsResponse.getIndexMappings(), elasticSearchDataSetDef);
        if (columns == null || columns.isEmpty()) throw new RuntimeException("There are no column for index [" + index[0] + "] and type [" + ArrayUtils.toString(type) + "].");

        boolean isAllColumns = elasticSearchDataSetDef.isAllColumnsEnabled();
        Collection<DataColumnDef> dataSetColumns = elasticSearchDataSetDef.getColumns();
        
        if (isAllColumns) {
            // Use colmns given from EL index mapping.
            for (Map.Entry<String, Object[]> entry : columns.entrySet()) {
                String columnId = entry.getKey();
                ColumnType columnType = (ColumnType) entry.getValue()[0];

                // Check if there is any column definition override.
                final DataColumnDef definitionColumn = elasticSearchDataSetDef.getColumnById(columnId);
                if (definitionColumn != null) {
                    ColumnType definitionColumnType = definitionColumn.getColumnType();
                    if (columnType.equals(ColumnType.TEXT) && definitionColumnType.equals(ColumnType.LABEL)) throw new IllegalArgumentException("The column [" + columnId + "] is defined in dataset definition as LABEL, but the column in the index [" + index[0] + "] and type [" + ArrayUtils.toString(type) + "] is using ANALYZED index, you cannot use it as a label.");
                    columnType = definitionColumnType;
                }

                columnIds.add(columnId);
                columnTypes.add(columnType);
            }
            
        } else {
            
            // Use given columns from dataset definition.
            if (dataSetColumns != null && !dataSetColumns.isEmpty()) {
                for (final DataColumnDef column : dataSetColumns) {
                    String columnId = column.getId();
                    ColumnType columnType = column.getColumnType();

                    ColumnType indexColumnType = (ColumnType) columns.get(columnId)[0];
                    String format = (String) columns.get(columnId)[1];
                    // Check user defined column exists in the index/type.
                    if (indexColumnType == null) throw new IllegalArgumentException("The column [" + columnId + "] defined in dataset definition does not exist for the index [" + index[0] + "] and type [" + ArrayUtils.toString(type) + "].");
                    // Check that analyzed fields on EL index definition are analyzed too in the dataset definition.
                    if (indexColumnType.equals(ColumnType.TEXT) && columnType.equals(ColumnType.LABEL)) throw new IllegalArgumentException("The column [" + columnId + "] is defined in dataset definition as LABEL, but the column in the index [" + index[0] + "] and type [" + ArrayUtils.toString(type) + "] is using ANALYZED index, you cannot use it as a label.");
                    columnIds.add(columnId);
                    columnTypes.add(columnType);
                }
            }
        }

        int _rowCount = (int) rowCount;
        int estimatedSize = estimateSize(columnTypes, _rowCount);

        // Build the metadata instance.
        result = new ElasticSearchDataSetMetadata(def, def.getUUID(), _rowCount,
                        columnIds.size(), columnIds, columnTypes, estimatedSize);
        
        // Set the index field patterns from EL server.
        for (Map.Entry<String, Object[]> entry : columns.entrySet()) {
            String pattern = (String) entry.getValue()[1];
            if (pattern != null && pattern.trim().length() > 0) result.setFieldPattern(entry.getKey(), pattern);
        }
        
        // Put into cache.
        _metadataMap.put(def.getUUID(), result);
        
        return result;
    }
    
    private int estimateSize(List<ColumnType> columnTypes, int rowCount) {
        int estimatedSize = 0;
        
        if (columnTypes != null && !columnTypes.isEmpty()) {
            for (ColumnType type : columnTypes) {
                if (ColumnType.DATE.equals(type)) {
                    estimatedSize += MemSizeEstimator.sizeOf(Date.class) * rowCount;
                }
                else if (ColumnType.NUMBER.equals(type)) {
                    estimatedSize += MemSizeEstimator.sizeOf(Double.class) * rowCount;
                }
                else {
                    // For string use an approximated value as EL does not provide size attribute for fields.
                    estimatedSize += 30 * rowCount;
                }
            }
        }
        
        return estimatedSize;
    }
    
    /**
     * Parse a given index' field definitions from EL index mappings response.
     * 
     * @param indexMappings The mappings response from EL server.
     * @param def The dataset definition.
     * @return Return the column for the dataset as a Map where the key is the column identifier, and the value is an Object[] that contains the columnType and the column pattern for that field.
     */
    protected Map<String,Object[]> parseColumns(IndexMappingResponse[] indexMappings, ElasticSearchDataSetDef def) {
        Map<String, Object[]> result = null;
        
        for (IndexMappingResponse indexMapping : indexMappings) {
            result = new LinkedHashMap<String, Object[]>();
            String indexName = indexMapping.getIndexName();
            TypeMappingResponse[] typeMappings = indexMapping.getTypeMappings();
            if (typeMappings == null || typeMappings.length == 0) throw new IllegalArgumentException("There are no types for index: [" + indexName + "[");
            for (TypeMappingResponse typeMapping : typeMappings) {
                String typeName = typeMapping.getTypeName();
                FieldMappingResponse[] properties = typeMapping.getFields();
                if (properties == null || properties.length == 0) throw new IllegalArgumentException("There are no fields for index: [" + indexName + "] and type [" + typeName + "[");
                for (FieldMappingResponse fieldMapping : properties) {
                    String fieldName = fieldMapping.getName();
                    String format = fieldMapping.getFormat();

                    String columnId = getColumnId(indexName, typeName, fieldName);
                    ColumnType columnType = getDataType(fieldMapping);
                    
                    // Only use supported column types.
                    if (columnType != null) {
                        boolean columnExists = result.containsKey(columnId);
                        if ( columnExists ) {
                            // Check column type for existing column.
                            ColumnType existingColumnType = (ColumnType) result.get(columnId)[0];
                            if (existingColumnType != null && !existingColumnType.equals(columnType)) throw new IllegalArgumentException("Column [" + columnId + "] is already present in data set with type [" + existingColumnType + "] and you are trying to add it again as type [" + columnType.toString() + "[");

                            // Check column format for existing column.
                            if (!StringUtils.isBlank(format)) {
                                String existingPattern = def.getPattern(columnId);
                                if (existingPattern != null && !existingPattern.equals(format)) throw new IllegalArgumentException("Column [" + columnId + "] is already present in data set with pattern [" + existingPattern + "] and you are trying to add it again with pattern [" + format + "[");
                            }
                        } else {
                            result.put(columnId, new Object[] {columnType, format});
                        }    
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
        if (fieldType == null) return null;
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
        
        CountResponse response = clientFactory.newClient(elasticSearchDataSetDef).count(index, type);
        
        if (response != null) return response.getCount();
        return 0;
    }

    // Listen to changes on the data set definition registry

    private void onDataSetStaleEvent(@Observes DataSetStaleEvent event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.ELASTICSEARCH.equals(def.getProvider())) {
            remove(def.getUUID());
        }
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.ELASTICSEARCH.equals(def.getProvider())) {
            remove(def.getUUID());
        }
    }
    
    private void remove(final String uuid) {
        _metadataMap.remove(uuid);
        staticDataSetProvider.removeDataSet(uuid);
    }
}
