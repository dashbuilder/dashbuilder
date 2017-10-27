/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
import org.dashbuilder.DataSetCore;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.StaticDataSetProvider;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.*;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.util.ElasticSearchUtils;
import org.dashbuilder.dataset.*;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.engine.group.IntervalBuilder;
import org.dashbuilder.dataset.engine.group.IntervalBuilderLocator;
import org.dashbuilder.dataset.engine.group.IntervalList;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.group.*;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.impl.DataSetMetadataImpl;
import org.dashbuilder.dataset.impl.MemSizeEstimator;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
public class ElasticSearchDataSetProvider implements DataSetProvider, DataSetDefRegistryListener {

    private static final String GROUPING_FUNCTION_NON_EXISTING_COLUMN = "Grouping function by a non existing column [";
    private static final String IN_DATASET = "] in dataset ";
    private static final String AND_TYPE = "] and type [";
    public static final int RESPONSE_CODE_OK = 200;
    private static final String COMMA = ",";

    private static ElasticSearchDataSetProvider SINGLETON = null;

    public static ElasticSearchDataSetProvider get() {
        if (SINGLETON == null) {

            DataSetCore dataSetCore = DataSetCore.get();
            StaticDataSetProvider staticDataSetProvider = DataSetCore.get().getStaticDataSetProvider();
            DataSetDefRegistry dataSetDefRegistry = DataSetCore.get().getDataSetDefRegistry();
            IntervalBuilderLocator intervalBuilderLocator = DataSetCore.get().getIntervalBuilderLocator();
            IntervalBuilderDynamicDate intervalBuilderDynamicDate = dataSetCore.getIntervalBuilderDynamicDate();

            SINGLETON = new ElasticSearchDataSetProvider(staticDataSetProvider,
                    intervalBuilderLocator,
                    intervalBuilderDynamicDate);

            dataSetDefRegistry.addListener(SINGLETON);
        }
        return SINGLETON;
    }


    protected Logger log = LoggerFactory.getLogger(ElasticSearchDataSetProvider.class);
    protected StaticDataSetProvider staticDataSetProvider;
    protected IntervalBuilderLocator intervalBuilderLocator;
    protected IntervalBuilderDynamicDate intervalBuilderDynamicDate;
    protected ElasticSearchClientFactory clientFactory;
    protected ElasticSearchValueTypeMapper typeMapper;
    protected ElasticSearchQueryBuilderFactory queryBuilderFactory;

    /** Backend cache map. **/
    protected final Map<String,DataSetMetadata> _metadataMap = new HashMap<String,DataSetMetadata>();

    /** Singleton clients for each data set definition.. **/
    protected final Map<String,ElasticSearchClient> _clientsMap = new HashMap<String,ElasticSearchClient>();

    public ElasticSearchDataSetProvider() {
    }

    public ElasticSearchDataSetProvider(StaticDataSetProvider staticDataSetProvider,
                                        IntervalBuilderLocator intervalBuilderLocator,
                                        IntervalBuilderDynamicDate intervalBuilderDynamicDate) {

        this.staticDataSetProvider = staticDataSetProvider;
        this.intervalBuilderLocator = intervalBuilderLocator;
        this.intervalBuilderDynamicDate = intervalBuilderDynamicDate;

        this.typeMapper = new ElasticSearchValueTypeMapper();
        ElasticSearchUtils searchUtils = new ElasticSearchUtils(typeMapper);
        this.clientFactory = new ElasticSearchClientFactory(typeMapper, intervalBuilderDynamicDate, searchUtils);
        this.queryBuilderFactory = new ElasticSearchQueryBuilderFactory(typeMapper, searchUtils);
    }

    public void destroy() {
        // Destroy all clients.
        for (ElasticSearchClient client : _clientsMap.values()) {
            destroyClient(client);
        }
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
        final boolean isTestMode = lookup != null && lookup.testMode();
        DataSetMetadata metadata = (DataSetMetadata) getDataSetMetadata(elDef, isTestMode);

        // Add the data set filter specified in the definition, if any.
        DataSetFilter dataSetFilter = elDef.getDataSetFilter();
        if (dataSetFilter != null) {
            lookup.addOperation(dataSetFilter);
        }

        // Create the search request.
        SearchRequest request = new SearchRequest(metadata);

        // Pagination constraints.
        int numRows = lookup.getNumberOfRows();
        boolean trim = (numRows > 0);
        if (trim) {
            int rowOffset = lookup.getRowOffset();
            request.setStart(rowOffset);
            request.setSize(numRows);
        }

        // Check group operations. Check that operation source fields exist as data set columns.
        // Calculate the resulting data set columns too and set the collection into the ELS query to perform.
        DataSetGroup _groupOp = null;
        boolean allColumns = true;
        int groupIdx = lookup.getFirstGroupOpIndex(0, null, false);
        if (groupIdx == -1) groupIdx = lookup.getFirstGroupOpIndex(0, null, true);
        if (groupIdx != -1) _groupOp = lookup.getOperation(groupIdx);

        // Single group operations to process. The others have to be group with intervals selection, and are not considerent grouped column, just filters.
        List<DataColumn> columns = new ArrayList<DataColumn>();
        if (_groupOp != null && !_groupOp.isSelect()) {
            List<GroupFunction> groupFunctions = _groupOp.getGroupFunctions();
            boolean exitsFunction = false;
            if (groupFunctions != null && !groupFunctions.isEmpty()) {
                for (GroupFunction groupFunction : groupFunctions) {
                    if (groupFunction.getFunction() != null) {
                        exitsFunction = true;
                    }
                    // If no source column specified, use first available one by default.
                    String columnId = groupFunction.getSourceId() != null  ? groupFunction.getSourceId() : metadata.getColumnId(0);
                    DataColumn column = getColumnById(metadata, columnId);
                    if (column != null) {
                        DataColumn c = column.cloneEmpty();
                        String cId = groupFunction.getColumnId() != null ? groupFunction.getColumnId() : groupFunction.getSourceId();
                        c.setId(cId);
                        c.setGroupFunction(groupFunction);
                        // If a function is applied, the resulting column type is numeric.
                        if (groupFunction.getFunction() != null) {
                            c.setColumnType(ColumnType.NUMBER);
                        }
                        columns.add(c);
                    } else {
                        throw new IllegalArgumentException(GROUPING_FUNCTION_NON_EXISTING_COLUMN + columnId + IN_DATASET);
                    }
                }
            }

            if (exitsFunction) {
                // Use the main group operation as a request aggregation.
                request.setAggregations(Collections.singletonList(_groupOp));
            }

        }

        // Set the resulting columns on the request object.
        request.setColumns(!columns.isEmpty() ? columns : getAllColumns(metadata));

        // Filter operations.
        List<DataSetFilter> filters = lookup.getOperationList(DataSetFilter.class);
        if (filters != null && !filters.isEmpty()) {
            // Check columns for the filter operations.
            for (DataSetFilter filerOp  : filters) {
                List<ColumnFilter> columnFilters = filerOp.getColumnFilterList();
                if (columnFilters != null && !columnFilters.isEmpty()) {
                    for (ColumnFilter filter: columnFilters) {
                        String fcId = filter.getColumnId();
                        if (fcId != null && !existColumn(metadata, fcId)) throw new IllegalArgumentException("Filtering by a non existing column [" + fcId + IN_DATASET);
                    }
                }
            }
        }

        // Append the interval selections (drill-down) as data set filter operations.
        List<DataSetGroup> intervalSelects = lookup.getFirstGroupOpSelections();
        if (intervalSelects != null && !intervalSelects.isEmpty()) {
            if (filters == null) {
                filters = new ArrayList<DataSetFilter>(intervalSelects.size());
            }
            DataSetFilter filterOp = new DataSetFilter();
            filterOp.setDataSetUUID(metadata.getUUID());
            for (DataSetGroup intervalSelect : intervalSelects) {
                ColumnFilter drillDownFilter = _getIntervalSelectionFilter(intervalSelect);
                filterOp.addFilterColumn(drillDownFilter);
            }
            filters.add(filterOp);
        }

        // The query is build from a given filters and/or from interval selections. Built it.
        Query query = queryBuilderFactory.newQueryBuilder().metadata(metadata)
                .groupInterval(_groupOp != null ? Arrays.asList(_groupOp) : null)
                .filter(filters).build();

        request.setQuery(query);

        // Sort operations.
        List<DataSetSort> sortOps = lookup.getOperationList(DataSetSort.class);
        if ( (sortOps == null || sortOps.isEmpty()) && elDef.getColumnSort() != null) {

            // If no sort operation defined for this lookup, sort for the specified default field on dataset definition, if exists.
            if (sortOps == null) {
                sortOps = new ArrayList<DataSetSort>();
            }

            DataSetSort defaultSort = new DataSetSort();
            defaultSort.addSortColumn(elDef.getColumnSort());
            sortOps.add(defaultSort);

        } else if (sortOps != null) {

            // Check columns for the given sort operations.
            for (DataSetSort sortOp : sortOps) {
                List<ColumnSort> sorts  = sortOp.getColumnSortList();
                if (sorts != null && !sorts.isEmpty()) {
                    for (ColumnSort sort : sorts) {
                        if (!existColumn(metadata, sort.getColumnId())) throw new IllegalArgumentException("Sorting by a non existing column [" + sort.getColumnId() + IN_DATASET);
                    }
                }
            }

        }

        // Set the sorting operations, if any, into the ELS request.
        request.setSorting(sortOps);


        // Perform the query & generate the resulting dataset.
        DataSet dataSet = DataSetFactory.newEmptyDataSet();
        dataSet.setColumns(request.getColumns());

        ElasticSearchClient client = getClient(elDef);
        SearchResponse searchResponse = client.search(elDef, metadata, request);

        // If there are no results, return an empty data set.
        if (searchResponse instanceof EmptySearchResponse) return dataSet;

        // There exist values. Populate the data set.
        fillDataSetValues(elDef, dataSet, searchResponse.getHits());

        // Post process the data set for supporting extended features.
        postProcess(metadata, dataSet);

        if (trim) {
            int totalRows = (int) searchResponse.getTotalHits();
            totalRows = lookup.getNumberOfRows() > dataSet.getRowCount() ? dataSet.getRowCount() : totalRows;
            dataSet.setRowCountNonTrimmed(totalRows);
        }
        return dataSet;
    }

    protected ColumnFilter _getIntervalSelectionFilter(DataSetGroup intervalSel) {
        ColumnFilter filter = null;
        if (intervalSel != null && intervalSel.isSelect()) {
            ColumnGroup cg = intervalSel.getColumnGroup();
            List<Interval> intervalList = intervalSel.getSelectedIntervalList();

            // Get the filter values
            List<Comparable> names = new ArrayList<Comparable>();
            Comparable min = null;
            Comparable max = null;
            for (Interval interval : intervalList) {
                names.add(interval.getName());
                Comparable intervalMin = (Comparable) interval.getMinValue();
                Comparable intervalMax = (Comparable) interval.getMaxValue();

                if (intervalMin != null) {
                    if (min == null) min = intervalMin;
                    else if (min.compareTo(intervalMin) > 0) min = intervalMin;
                }
                if (intervalMax != null) {
                    if (max == null) max = intervalMax;
                    else if (max.compareTo(intervalMax) > 0) max = intervalMax;
                }
            }
            // Min can't be greater than max.
            if (min != null && max != null && min.compareTo(max) > 0) {
                min = max;
            }

            // Apply the filter
            if (min != null && max != null) {
                filter = FilterFactory.between(cg.getSourceId(), min, max);
            }
            else if (min != null) {
                filter = FilterFactory.greaterOrEqualsTo(cg.getSourceId(), min);
            }
            else if (max != null) {
                filter = FilterFactory.lowerOrEqualsTo(cg.getSourceId(), max);
            }
            else {
                filter = FilterFactory.equalsTo(cg.getSourceId(), names);
            }
        }
        return filter;
    }

    private void postProcess(DataSetMetadata metadata, DataSet dataSet) {
        for (DataColumn column : dataSet.getColumns()) {
            ColumnType columnType = column.getColumnType();

            // Group by date column using empty intervals special post process.
            // Aggregations for achieving empty intervals are hard to perform in ELS queries,
            // as expected data set resulting from the query to the server is not large, do it in memory.
            if (ColumnType.LABEL.equals(columnType)) {
                ColumnGroup cg = column.getColumnGroup();
                if (cg != null && cg.areEmptyIntervalsAllowed()) {
                    ColumnType type = metadata.getColumnType(cg.getSourceId());
                    if (type != null && ColumnType.DATE.equals(type)){
                        DateIntervalType intervalType = DateIntervalType.getByName(cg.getIntervalSize());
                        Month firstMonth = cg.getFirstMonthOfYear();
                        DayOfWeek firstDayOfWeek = cg.getFirstDayOfWeek();
                        int startIndex = 0;
                        int intervalSize = -1;
                        if (intervalType != null) {
                            if (firstMonth != null && intervalType.equals(DateIntervalType.MONTH)) {
                                startIndex = firstMonth.getIndex() - 1;
                                intervalSize = Month.values().length;
                            }
                            if (firstDayOfWeek!= null && intervalType.equals(DateIntervalType.DAY_OF_WEEK)) {
                                startIndex = firstDayOfWeek.getIndex() - 1;
                                intervalSize = DayOfWeek.values().length;
                            }
                        }
                        fillEmptyRows(dataSet, column, startIndex, intervalSize);
                    }
                }
            }
        }
    }

    private void fillEmptyRows(DataSet dataSet, DataColumn dateGroupColumn, int startIndex, int intervalSize) {
        IntervalBuilder intervalBuilder = intervalBuilderLocator.lookup(ColumnType.DATE, dateGroupColumn.getColumnGroup().getStrategy());
        IntervalList intervalList = intervalBuilder.build(dateGroupColumn);
        if (intervalList.size() > dataSet.getRowCount()) {
            List values = dateGroupColumn.getValues();
            for (int counter = 0, intervalIdx = startIndex; counter < intervalList.size(); counter++, intervalIdx++) {
                if (intervalSize != -1 && intervalIdx >= intervalSize) {
                    intervalIdx = 0;
                }
                String interval = intervalList.get(intervalIdx).getName();
                String value = values.isEmpty() || values.size() < ( intervalIdx + 1 ) ? null : (String) values.get(intervalIdx);
                if (value == null || !value.equals(interval)) {
                    dataSet.addEmptyRowAt(intervalIdx);
                    dateGroupColumn.getValues().set(intervalIdx, interval);
                }
            }
        }
    }

    protected List<DataColumn> getAllColumns(DataSetMetadata metadata) {
        int numberOfColumns = metadata.getNumberOfColumns();
        List<DataColumn> columns = new ArrayList<DataColumn>(numberOfColumns);
        for (int x = 0; x < numberOfColumns; x++) {
            DataColumn column = getColumnById(metadata, metadata.getColumnId(x));
            columns.add(column);
        }
        return columns;
    }

    protected DataColumn getColumnById(DataSetMetadata metadata, String columnId) {
        if (metadata == null || columnId == null || columnId.trim().length() == 0) return null;

        int numCols = metadata.getNumberOfColumns();
        for (int x = 0; x < numCols; x++) {
            String metaColumnId = metadata.getColumnId(x);
            if (columnId.equals(metaColumnId)) {
                DataColumn column = new DataColumnImpl(metadata.getColumnId(x), metadata.getColumnType(x));
                return column;
            }
        }

        return null;
    }

    protected boolean existColumn(DataSetMetadata metadata, String columnId) {
        return getColumnById(metadata, columnId) != null;
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
        return getDataSetMetadata(def, true);
    }

    private DataSetMetadata getDataSetMetadata(DataSetDef def, boolean isTestMode) throws Exception {
        // Type casting.
        ElasticSearchDataSetDef elasticSearchDataSetDef = (ElasticSearchDataSetDef) def;

        // Check if metadata already exists in cache, if no test mode is enabled.
        if (!isTestMode) {
            DataSetMetadata result = _metadataMap.get(elasticSearchDataSetDef.getUUID());
            if (result != null) return result;
        } else if (log.isDebugEnabled()) {
            log.debug("Using look-up in test mode. Skipping read data set metadata for uuid [" + def.getUUID() + "] from cache.");
        }

        // Data Set parameters.
        String[] index = fromString(elasticSearchDataSetDef.getIndex());
        String[] type = fromString(elasticSearchDataSetDef.getType());

        // Get the row count.
        long rowCount = getRowCount(elasticSearchDataSetDef);

        // Obtain the indexMappings
        ElasticSearchClient client = getClient(elasticSearchDataSetDef);
        MappingsResponse mappingsResponse = client.getMappings(index);
        if (mappingsResponse == null || mappingsResponse.getStatus() != RESPONSE_CODE_OK) throw new IllegalArgumentException("Cannot retrieve index mappings for index: [" + index[0] + "]. See previous errors.");

        // Obtain the columns (ids and types).
        List<String> columnIds = new LinkedList<String>();
        List<ColumnType> columnTypes = new LinkedList<ColumnType>();

        // Retrieve column definitions given by the index mappings response.
        Map<String, DataColumn> indexMappingsColumnsMap = parseColumnsFromIndexMappings(mappingsResponse.getIndexMappings(), elasticSearchDataSetDef);
        if (indexMappingsColumnsMap == null || indexMappingsColumnsMap.isEmpty()) throw new RuntimeException("There are no column for index [" + index[0] + AND_TYPE + ArrayUtils.toString(type) + "].");
        Collection<DataColumn> indexMappingsColumns = indexMappingsColumnsMap.values();

        // Get the column definitions from the data set definition, if any.
        Collection<DataColumnDef> dataSetColumns = elasticSearchDataSetDef.getColumns();
        if (dataSetColumns != null && !dataSetColumns.isEmpty()) {
            for (final DataColumnDef column : dataSetColumns) {
                String columnId = column.getId();

                DataColumn indexCol = indexMappingsColumnsMap.get(columnId);
                if (indexCol == null) {
                    // ERROR - Column definition specified in the data set definition, is no longer present in the current index mappings. Skip it!
                    log.warn("The column [" + columnId + "] for the data set definition with UUID [" + def.getUUID() + "] is no longer present in the mappings.");
                } else {
                    // Column type specified in the data set defintion.
                    ColumnType columnType = column.getColumnType();

                    // Check that analyzed fields on EL index definition are analyzed too in the dataset definition.
                    ColumnType indexColumnType = indexCol.getColumnType();
                    if (indexColumnType.equals(ColumnType.TEXT) && columnType.equals(ColumnType.LABEL)) {
                        throw new RuntimeException("The column [" + columnId + "] is defined in dataset definition as LABEL, but the column in the index [" + index[0] + AND_TYPE + ArrayUtils.toString(type) + "] is using ANALYZED index, you cannot use it as a label.");
                    }

                    columnIds.add(columnId);
                    columnTypes.add(columnType);
                }
            }
        }

        // If all columns requested, add the missing ones in the data set defintion from the information given by the mappings response for the index.
        if (def.isAllColumnsEnabled()) {
            for (DataColumn indexMappingsColumn : indexMappingsColumns) {
                String columnId = indexMappingsColumn.getId();
                int columnIdx = columnIds.indexOf(columnId);
                boolean columnExists  = columnIdx != -1;

                // Add or skip non-existing columns depending on the data set definition.
                if (!columnExists) {
                    // Add all the columns from the index, included the ones not defined in the data set definition.
                    columnIds.add(columnId);
                    columnTypes.add(indexMappingsColumn.getColumnType());
                }
            }
        }

        // Check that columns collection is not empty.
        if (columnIds.isEmpty()) {
            throw new RuntimeException("Cannot obtain data set metadata columns for data set with UUID [" + def.getUUID() + "]. All columns flag is not set and there are no column definitions in the data set definition.");
        }

        // Estimated data set size.
        int _rowCount = (int) rowCount;
        int estimatedSize = estimateSize(columnTypes, _rowCount);

        // Build the metadata instance.
        DataSetMetadata result = new DataSetMetadataImpl(def, def.getUUID(), _rowCount,
                columnIds.size(), columnIds, columnTypes, estimatedSize);

        // Put into cache.
        if (!isTestMode) {
            _metadataMap.put(def.getUUID(), result);
        } else if (log.isDebugEnabled()) {
            log.debug("Using look-up in test mode. Skipping adding data set metadata for uuid [" + def.getUUID() + "] into cache.");
        }

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
     * @param def The data set definition.
     * @return The Data columns from index mappings.
     */
    protected Map<String, DataColumn> parseColumnsFromIndexMappings(IndexMappingResponse[] indexMappings, ElasticSearchDataSetDef def) {
        Map<String, DataColumn> result = null;

        // Iterate over index/es.
        for (IndexMappingResponse indexMapping : indexMappings) {
            result = new HashMap<String, DataColumn>();
            String indexName = indexMapping.getIndexName();

            // Document Type/s.
            TypeMappingResponse[] typeMappings = indexMapping.getTypeMappings();
            if (typeMappings == null || typeMappings.length == 0) throw new IllegalArgumentException("There are no types for index: [" + indexName + "[");
            for (TypeMappingResponse typeMapping : typeMappings) {
                String typeName = typeMapping.getTypeName();

                // Document type field/s
                FieldMappingResponse[] properties = typeMapping.getFields();
                if (properties == null || properties.length == 0) throw new IllegalArgumentException("There are no fields for index: [" + indexName + AND_TYPE + typeName + "[");
                for (FieldMappingResponse fieldMapping : properties) {
                    String fieldName = fieldMapping.getName();
                    String format = fieldMapping.getFormat();

                    // Field.
                    DataColumn _column = parseColumnFromIndexMappings(def, indexName, typeName, fieldName, format, fieldMapping.getDataType(), fieldMapping.getIndexType());
                    if (_column != null) {
                        result.put(_column.getId(), _column);
                    }

                    // Multi-fields.
                    MultiFieldMappingResponse[] multiFieldMappingResponse = fieldMapping.getMultiFields();
                    if (multiFieldMappingResponse != null && multiFieldMappingResponse.length > 0) {
                        for (MultiFieldMappingResponse multiField : multiFieldMappingResponse) {
                            DataColumn _multiFieldColumn = parseColumnFromIndexMappings(def, indexName, typeName, fieldName + "." + multiField.getName(), null, multiField.getDataType(), multiField.getIndexType());
                            if (_multiFieldColumn != null) {
                                result.put(_multiFieldColumn.getId(), _multiFieldColumn);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    protected DataColumn parseColumnFromIndexMappings(ElasticSearchDataSetDef def,
                                                      String indexName,
                                                      String typeName,
                                                      String fieldName,
                                                      String format,
                                                      FieldMappingResponse.FieldType fieldType,
                                                      FieldMappingResponse.IndexType indexType) {


        // Data Column model generation from index mappigns and  data set definition column patterns definition
        String columnId = getColumnId(indexName, typeName, fieldName);
        ColumnType columnType = getDataType(fieldType, indexType);

        // Use columns with supported column types, discard others.
        DataColumn column = null;
        if (columnType != null) {

            // Handle date and numric field formats.
            final boolean isLabelColumn = ColumnType.LABEL.equals(columnType);
            final boolean isTextColumn = ColumnType.TEXT.equals(columnType);
            final boolean isNumberColumn = ColumnType.NUMBER.equals(columnType);

            String pattern = def.getPattern(columnId);
            // Check if the given column has any pattern format specified in the data set definition.
            if (!isEmpty(pattern)) {

                // Exists a pattern format specified in the data set definition. Use it.
                if ( log.isDebugEnabled() ) {
                    log.debug("Using pattern [" + pattern + "] given in the data set definition with uuid [" + def.getUUID() + "] for column [" + columnId + "].");
                }

                // LABEL or TEXT types.
            } else if (isLabelColumn || isTextColumn) {

                // If not specified, by defalt index is ANALYZED.
                if (indexType == null) indexType = FieldMappingResponse.IndexType.ANALYZED;
                def.setPattern(columnId, indexType.name().toLowerCase());

                // NUMERIC - If no pattern for number, use the givens from index mappings response.
            } else if (isNumberColumn) {

                // Use the column pattern as the core number type for this field.
                def.setPattern(columnId, fieldType.name().toLowerCase());

                // DATE - If no pattern for date use the givens from index mappings response.
            } else if (!isEmpty(format)) {

                // No pattern format specified in the data set definition for this column. Use the one given by the index mappings response and set the pattern into the column's definition for further use.
                def.setPattern(columnId, format);

                if (log.isDebugEnabled()) {
                    log.debug("Using pattern [" + format + "] given by the index mappings response for column [" + columnId + "].");
                }

                // DATE - If no pattern for date use and neithier in index mappings response, use the default.
            }  else {

                // No pattern format specified neither in the data set definition or in the index mappings response for this column. Using default ones.
                String defaultPattern = typeMapper.defaultDateFormat();
                def.setPattern(columnId, defaultPattern);

                if ( log.isDebugEnabled() ) {
                    log.debug("Using default pattern [" + defaultPattern + "] for column [" + columnId + "].");
                }

            }

            // Add the resulting column given by the index mappings response into the resulting collection.
            column = new DataColumnImpl(columnId, columnType);

        } else if (log.isDebugEnabled()) {

            log.debug("[" + indexName + "]/[" + typeName + "] - Skipping column retrieved from index mappings response with id [" + columnId + "], as data type for this column is not supported.");

        }

        return column;
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
     * @param fieldType The ElasticSearch field type..
     * @param indexType The index type for the field.
     * @return The dashbuilder data type.
     * @throws IllegalArgumentException If ElasticSearch core data type is not supported.
     */
    protected ColumnType getDataType(FieldMappingResponse.FieldType fieldType, FieldMappingResponse.IndexType indexType) throws IllegalArgumentException {
        if (fieldType == null) return null;
        switch (fieldType) {
            case STRING:
                if (indexType != null  && indexType.equals(FieldMappingResponse.IndexType.NOT_ANALYZED)) return ColumnType.LABEL;
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

        if (log.isDebugEnabled()) {
            log.debug("The ElasticSearch core data type [" + fieldType.toString() + "] is not suppored.");
        }
        return null;
    }

    protected long getRowCount(ElasticSearchDataSetDef elasticSearchDataSetDef) throws Exception {
        String[] index = fromString(elasticSearchDataSetDef.getIndex());
        String[] type = fromString(elasticSearchDataSetDef.getType());

        ElasticSearchClient client = getClient(elasticSearchDataSetDef);
        CountResponse response = client.count(index, type);
        if (response != null) return response.getCount();
        return 0;
    }

    private ElasticSearchClient getClient(ElasticSearchDataSetDef def) {
        ElasticSearchClient client = _clientsMap.get(def.getUUID());
        if (client == null) {
            client = clientFactory.newClient(def);
            _clientsMap.put(def.getUUID(), client);
        }
        return client;
    }

    private ElasticSearchClient destroyClient(String uuid) {
        ElasticSearchClient client = _clientsMap.get(uuid);
        if (client != null) {
            _clientsMap.remove(uuid);
            destroyClient(client);
        }
        return client;
    }

    private ElasticSearchClient destroyClient(ElasticSearchClient client) {
        try {
            client.close();
        } catch (IOException e) {
            log.error("Error closing elastic search data provider client.", e);
        }
        return client;
    }

    // Listen to changes on the data set definition registry

    @Override
    public void onDataSetDefStale(DataSetDef def) {
        if (DataSetProviderType.ELASTICSEARCH.equals(def.getProvider())) {
            remove(def.getUUID());
        }
    }

    @Override
    public void onDataSetDefModified(DataSetDef oldDef, DataSetDef newDef) {
        if (DataSetProviderType.ELASTICSEARCH.equals(oldDef.getProvider())) {
            remove(oldDef.getUUID());
        }
    }

    @Override
    public void onDataSetDefRemoved(DataSetDef oldDef) {
        if (DataSetProviderType.ELASTICSEARCH.equals(oldDef.getProvider())) {
            remove(oldDef.getUUID());
        }
    }

    @Override
    public void onDataSetDefRegistered(DataSetDef newDef) {

    }

    protected void remove(final String uuid) {
        _metadataMap.remove(uuid);
        destroyClient(uuid);
        staticDataSetProvider.removeDataSet(uuid);
    }

    public static String[] fromString(String str) {
        if (str == null) return null;
        if (str.trim().length() == 0) return new String[] {""};


        return str.split(COMMA);
    }

    public static String toString(String[] array) {
        if (array == null) return null;
        if (array.length == 0) return "";
        final StringBuilder builder = new StringBuilder();
        final int total =  array.length;
        for (int x = 0; x < total; x++) {
            String s = array[x];
            builder.append(s);
            if (x < (total - 1)) builder.append(COMMA);
        }
        return builder.toString();
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

}
