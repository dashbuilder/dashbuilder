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
package org.dashbuilder.displayer.client.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetOp;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.filter.LogicalExprFilter;
import org.dashbuilder.dataset.filter.LogicalExprType;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;

public class DataSetLookupJSONMarshaller {

    private static final String UUID = "dataSetUuid";
    private static final String ROWCOUNT = "rowCount";
    private static final String ROWOFFSET = "rowOffset";

    private static final String COLUMNID = "columnId";
    private static final String SOURCEID = "sourceId";
    private static final String FILTEROPS = "filterOps";

    private static final String FUNCTION_TYPE = "functionType";
    private static final String FUNCTION_TERMS = "terms";

    private static final String GROUPOPS = "groupOps";
    private static final String COLUMNGROUP = "columnGroup";
    private static final String GROUPSTRATEGY = "groupStrategy";
    private static final String MAXINTERVALS = "maxIntervals";
    private static final String INTERVALSIZE = "intervalSize";
    private static final String EMPTYINTERVALS = "emptyIntervals";
    private static final String ASCENDING = "asc";
    private static final String FIRSTMONTHOFYEAR = "firstMonthOfYear";
    private static final String FIRSTDAYOFWEEK = "firstDayOfWeek";

    private static final String GROUPFUNCTIONS = "groupFunctions";
    private static final String FUNCTION = "function";

    private static final String SELECTEDINTERVALS = "selectedIntervals";
    private static final String INTERVAL_NAME = "name";
    private static final String INTERVAL_TYPE = "type";
    private static final String INTERVAL_IDX = "index";
    private static final String INTERVAL_MIN = "min";
    private static final String INTERVAL_MAX = "max";
    private static final String JOIN = "join";

    private static final String SORTOPS = "sortOps";
    private static final String SORTORDER = "sortOrder";

    private List<String> coreFunctionTypes = new ArrayList<String>();
    private List<String> logicalFunctionTypes = new ArrayList<String>();

    public DataSetLookupJSONMarshaller() {
        for ( LogicalExprType type : LogicalExprType.values() ) {
            logicalFunctionTypes.add( type.toString() );
        }
        for ( CoreFunctionType type : CoreFunctionType.values() ) {
            coreFunctionTypes.add( type.toString() );
        }
    }

    public JSONObject toJson( DataSetLookup dataSetLookup ) {
        JSONObject json = new JSONObject();
        if ( dataSetLookup != null ) {
            String uuid = dataSetLookup.getDataSetUUID();

            if (!StringUtils.isBlank(uuid)) json.put( UUID, new JSONString( uuid ) );
            json.put( ROWCOUNT, new JSONString( Integer.toString(dataSetLookup.getNumberOfRows()) ) );
            json.put( ROWOFFSET, new JSONString( Integer.toString(dataSetLookup.getRowOffset()) ) );

            List<DataSetFilter> filterOps = dataSetLookup.getOperationList( DataSetFilter.class );
            if (!filterOps.isEmpty()) {
                json.put(FILTEROPS, formatFilterOperations(filterOps));
            }
            List<DataSetGroup> groupOps = dataSetLookup.getOperationList( DataSetGroup.class );
            if (!groupOps.isEmpty()) {
                json.put(GROUPOPS, formatGroupOperations(groupOps));
            }
            List<DataSetSort> sortOps = dataSetLookup.getOperationList( DataSetSort.class );
            if (!sortOps.isEmpty()) {
                json.put(SORTOPS, formatSortOperations(sortOps));
            }
        }
        return json;
    }

    private JSONArray formatFilterOperations( List<DataSetFilter> filterOps ) {
        if ( filterOps.size() == 0 ) return null;
        // There should be only one DataSetFilter
        return formatColumnFilters( filterOps.get( 0 ).getColumnFilterList() );
    }

    private JSONArray formatColumnFilters( List<ColumnFilter> columnFilters ) {
        JSONArray colFiltersJsonArray = new JSONArray();
        int colFilterCounter = 0;
        // DataSetFilter ==> ColumnFilter[]
        for ( ColumnFilter columnFilter : columnFilters ) {
            colFiltersJsonArray.set( colFilterCounter++, formatColumnFilter( columnFilter ) );
        }
        return colFiltersJsonArray;
    }

    private JSONObject formatColumnFilter( ColumnFilter columnFilter ) {
        if ( columnFilter == null ) return null;
        JSONObject colFilterJson = new JSONObject();
        // LogicalExprFilter o CoreFunctionFilter
        if ( columnFilter instanceof LogicalExprFilter ) {
            LogicalExprFilter lef = (LogicalExprFilter) columnFilter;
            colFilterJson.put( COLUMNID, new JSONString( lef.getColumnId() ) );
            colFilterJson.put( FUNCTION_TYPE, new JSONString( lef.getLogicalOperator().toString() ) );
            colFilterJson.put( FUNCTION_TERMS, formatColumnFilters( lef.getLogicalTerms() ) );

        } else if ( columnFilter instanceof CoreFunctionFilter ) {
            CoreFunctionFilter cff = (CoreFunctionFilter) columnFilter;
            colFilterJson.put( COLUMNID, new JSONString( cff.getColumnId() ) );
            colFilterJson.put( FUNCTION_TYPE, new JSONString( cff.getType().toString() ) );
            JSONArray paramsJsonArray = new JSONArray();
            int paramCounter = 0;
            for ( Object param : cff.getParameters() ) {
                JSONValue jsonParam = formatValue(param);
                paramsJsonArray.set(paramCounter++, jsonParam);
            }
            colFilterJson.put( FUNCTION_TERMS, paramsJsonArray );

        } else {
            GWT.log( CommonConstants.INSTANCE.json_datasetlookup_unsupported_column_filter());
        }
        return colFilterJson;
    }

    private JSONArray formatGroupOperations( List<DataSetGroup> groupOps ) {
        if ( groupOps.size() == 0 ) return null;
        JSONArray groupOpsJsonArray = new JSONArray();
        int groupOpCounter = 0;
        for ( DataSetGroup groupOp : groupOps ) {
            groupOpsJsonArray.set( groupOpCounter++, formatDataSetGroup( groupOp ) );
        }
        return groupOpsJsonArray;
    }

    private JSONObject formatDataSetGroup( DataSetGroup dataSetGroup ) {
        if ( dataSetGroup == null ) return null;
        JSONObject dataSetGroupJson = new JSONObject();
        dataSetGroupJson.put( COLUMNGROUP, formatColumnGroup( dataSetGroup.getColumnGroup() ) );
        dataSetGroupJson.put( GROUPFUNCTIONS, formatgroupFunctions(dataSetGroup.getGroupFunctions()) );
        dataSetGroupJson.put( SELECTEDINTERVALS, formatSelectedIntervals(dataSetGroup.getSelectedIntervalList()) );
        dataSetGroupJson.put( JOIN, new JSONString( dataSetGroup.isJoin() ? "true" : "false" ));
        return dataSetGroupJson;
    }

    private JSONObject formatColumnGroup( ColumnGroup columnGroup ) {
        if ( columnGroup == null ) return null;
        JSONObject columnGroupJson = new JSONObject();
        columnGroupJson.put( SOURCEID, columnGroup.getSourceId() != null ? new JSONString( columnGroup.getSourceId() ) : null );
        columnGroupJson.put( COLUMNID, columnGroup.getColumnId() != null ? new JSONString( columnGroup.getColumnId() ) : null );
        columnGroupJson.put( GROUPSTRATEGY, columnGroup.getStrategy() != null ? new JSONString( columnGroup.getStrategy().toString() ) : null );
        columnGroupJson.put( MAXINTERVALS, new JSONString( Integer.toString( columnGroup.getMaxIntervals() ) ) );
        columnGroupJson.put( INTERVALSIZE, columnGroup.getIntervalSize() != null ? new JSONString( columnGroup.getIntervalSize() ) : null );
        columnGroupJson.put( EMPTYINTERVALS, new JSONString( columnGroup.areEmptyIntervalsAllowed() ? "true" : "false" ) );
        columnGroupJson.put( ASCENDING, new JSONString( columnGroup.isAscendingOrder() ? "true" : "false" ) );
        columnGroupJson.put( FIRSTMONTHOFYEAR, columnGroup.getFirstMonthOfYear() != null ? new JSONString( columnGroup.getFirstMonthOfYear().toString() ) : null );
        columnGroupJson.put( FIRSTDAYOFWEEK, columnGroup.getFirstDayOfWeek() != null ? new JSONString( columnGroup.getFirstDayOfWeek().toString() ) : null );
        return columnGroupJson;
    }

    private JSONArray formatgroupFunctions( List<GroupFunction> groupFunctions ) {
        if ( groupFunctions.size() == 0 ) return null;
        JSONArray groupOpsJsonArray = new JSONArray();
        int groupFunctionCounter = 0;
        for ( GroupFunction groupFunction : groupFunctions ) {
            groupOpsJsonArray.set( groupFunctionCounter++, formatGroupFunction( groupFunction ) );
        }
        return groupOpsJsonArray;
    }

    private JSONObject formatGroupFunction( GroupFunction groupFunction ) {
        if ( groupFunction == null ) return null;
        JSONObject groupFunctionJson = new JSONObject();
        groupFunctionJson.put( SOURCEID, groupFunction.getSourceId() != null ? new JSONString( groupFunction.getSourceId() ) : null );
        groupFunctionJson.put( COLUMNID, groupFunction.getColumnId() != null ? new JSONString( groupFunction.getColumnId() ) : null );
        groupFunctionJson.put( FUNCTION, groupFunction.getFunction() != null ? new JSONString( groupFunction.getFunction().toString() ) : null );
        return groupFunctionJson;
    }

    private JSONArray formatSelectedIntervals( List<Interval> selectedIntervalList ) {
        if ( selectedIntervalList.size() == 0 ) return null;
        JSONArray selectedIntervalNamesJsonArray = new JSONArray();
        int intervalNamesCounter = 0;
        for ( Interval interval : selectedIntervalList ) {
            selectedIntervalNamesJsonArray.set( intervalNamesCounter++, formatInterval(interval) );
        }
        return selectedIntervalNamesJsonArray;
    }

    private JSONObject formatInterval(Interval interval) {
        if (interval == null) {
            return null;
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(INTERVAL_NAME, new JSONString(interval.getName()));
        jsonObj.put(INTERVAL_IDX, new JSONString(Integer.toString(interval.getIndex())));
        if (interval.getType() != null) {
            jsonObj.put(INTERVAL_TYPE, new JSONString(interval.getName()));
        }
        if (interval.getMinValue() != null) {
            JSONValue jsonValue = formatValue(interval.getMinValue());
            jsonObj.put(INTERVAL_MIN, jsonValue);
        }
        if (interval.getMinValue() != null) {
            JSONValue jsonValue = formatValue(interval.getMaxValue());
            jsonObj.put(INTERVAL_MAX, jsonValue);
        }
        return jsonObj;
    }

    private JSONArray formatSortOperations( List<DataSetSort> sortOps ) {
        if ( sortOps.size() == 0 ) return null;
        // There should be only one DataSetFilter
        return formatColumnSorts( sortOps.get( 0 ).getColumnSortList() );
    }

    private JSONArray formatColumnSorts( List<ColumnSort> columnSorts ) {
        if ( columnSorts.size() == 0 ) return null;
        JSONArray columnSortsJsonArray = new JSONArray();
        int columnSortCounter = 0;
        for ( ColumnSort columnSort : columnSorts ) {
            columnSortsJsonArray.set( columnSortCounter++, formatColumnSort( columnSort ) );
        }
        return columnSortsJsonArray;
    }

    private JSONObject formatColumnSort( ColumnSort columnSort ) {
        if ( columnSort == null ) return null;
        JSONObject columnSortJson = new JSONObject();
        columnSortJson.put( COLUMNID, columnSort.getColumnId() != null ? new JSONString( columnSort.getColumnId() ) : null );
        columnSortJson.put( SORTORDER, columnSort.getOrder() != null ? new JSONString( columnSort.getOrder().toString() ) : null );
        return columnSortJson;
    }

    public DataSetLookup fromJson( String jsonString ) {
        if ( StringUtils.isBlank( jsonString ) ) return null;
        JSONObject dataSetLookupJson = JSONParser.parseStrict( jsonString ).isObject();
        return fromJson( dataSetLookupJson );
    }

    // todo complete and improve validation output (json type (i.e. JSONString or JSONArray) and real-type (i.e. number or string) checks)
    public DataSetLookup fromJson( JSONObject json ) {
        if ( json == null ) return null;
        DataSetLookup dataSetLookup = new DataSetLookup();

        dataSetLookup.setDataSetUUID( json.get( UUID ) != null ? json.get( UUID ).isString().stringValue() : null );
        dataSetLookup.setNumberOfRows( json.get( ROWCOUNT ) != null ? Integer.parseInt( json.get( ROWCOUNT ).isString().stringValue(), 10 ) : -1 );
        dataSetLookup.setRowOffset( json.get( ROWOFFSET ) != null ? Integer.parseInt( json.get( ROWOFFSET ).isString().stringValue(), 10 ) : 0 );

        List<DataSetOp> dataSetOpList = dataSetLookup.getOperationList();

        Collection c = null;
        JSONValue array = json.get( FILTEROPS );

        if ( (c = parseFilterOperations( array != null ? array.isArray() : null )) != null )
            dataSetOpList.addAll( c );

        if ( (c = parseGroupOperations( ( array = json.get( GROUPOPS ) ) != null ? array.isArray() : null )) != null )
            dataSetOpList.addAll( c );

        if ( (c = parseSortOperations( ( array = json.get( SORTOPS ) ) != null ? array.isArray() : null )) != null )
            dataSetOpList.addAll(c);

        return dataSetLookup;
    }

    private List<DataSetFilter> parseFilterOperations( JSONArray columnFiltersJsonArray ) {
        if ( columnFiltersJsonArray == null ) return null;
        List<DataSetFilter> dataSetFilters = new ArrayList<DataSetFilter>();
        // There's only one DataSetFilter, the json array is an array of column filters
        DataSetFilter dataSetFilter = new DataSetFilter();
        dataSetFilters.add( dataSetFilter );
        List<ColumnFilter> columnFilters = parseColumnFilters( columnFiltersJsonArray );
        if ( columnFilters != null ) dataSetFilter.getColumnFilterList().addAll(columnFilters);

        return dataSetFilters;
    }

    private List<ColumnFilter> parseColumnFilters( JSONArray columnFiltersJsonArray ) {
        if ( columnFiltersJsonArray == null ) return null;
        List<ColumnFilter> columnFilters = new ArrayList<ColumnFilter>( columnFiltersJsonArray.size() );
        for ( int i = 0; i < columnFiltersJsonArray.size(); i++ ) {
            //                                    todo: can be null, if someone puts a {} in the column list
            columnFilters.add( parseColumnFilter( columnFiltersJsonArray.get( i ).isObject() ) );
        }
        return columnFilters;
    }

    private ColumnFilter parseColumnFilter( JSONObject columnFilterJson ) {
        if ( columnFilterJson == null ) return null;

        String columnId = null;
        String functionType = null;

        JSONValue value = columnFilterJson.get( COLUMNID );
        if ( checkNotNull(value, false, CommonConstants.INSTANCE.json_datasetlookup_columnfilter_null_columnid())) {
            columnId = value.isString() != null ? value.isString().stringValue() : null;
        }

        value = columnFilterJson.get( FUNCTION_TYPE );
        if ( checkNotNull(value, false, CommonConstants.INSTANCE.json_datasetlookup_columnfilter_null_functiontype())) {
            functionType = value.isString() != null ? value.isString().stringValue() : null;
        }

        value = columnFilterJson.get( FUNCTION_TERMS );

        if ( isCoreFilter( functionType ) ) {
            CoreFunctionFilter cff = new CoreFunctionFilter();
            cff.setColumnId( columnId );
            cff.setType( CoreFunctionType.getByName( functionType ) );

            if ( checkNotNull(value, false, CommonConstants.INSTANCE.json_datasetlookup_corefunction_null_params())) {
                cff.setParameters( parseCoreFunctionParameters( value.isArray() ).toArray( new Comparable[]{} ) );
            }

            return cff;

        } else if ( isLogicalFilter( functionType ) ) {
            LogicalExprFilter lef = new LogicalExprFilter();
            lef.setColumnId( columnId );
            lef.setLogicalOperator( LogicalExprType.getByName( functionType ) );

            if ( checkNotNull(value, false, CommonConstants.INSTANCE.json_datasetlookup_logexpr_null_params())) {
                // Logical expression terms are an an array of column filters
                lef.setLogicalTerms( parseColumnFilters( value.isArray() ) );
            }

            return lef;
        }
        else throw new RuntimeException( CommonConstants.INSTANCE.json_datasetlookup_columnfilter_wrong_type());
    }

    private List<Comparable> parseCoreFunctionParameters( JSONArray paramsJsonArray ) {
        if ( paramsJsonArray == null ) return null;
        List<Comparable> params = new ArrayList<Comparable>( paramsJsonArray.size() );
        for (  int i = 0; i < paramsJsonArray.size(); i++) {
            JSONValue jsonValue = paramsJsonArray.get(i);
            params.add(parseValue(jsonValue));
        }
        return params;
    }

    private List<DataSetGroup> parseGroupOperations( JSONArray groupOpsJsonArray ) {
        if ( groupOpsJsonArray == null ) return null;
        List<DataSetGroup> dataSetGroups = new ArrayList<DataSetGroup>();
        for ( int i = 0; i < groupOpsJsonArray.size(); i++) {
            JSONObject dataSetGroupOpJson = groupOpsJsonArray.get( i ).isObject();
            dataSetGroups.add( parseDataSetGroup(dataSetGroupOpJson) );
        }
        return dataSetGroups;
    }

    private DataSetGroup parseDataSetGroup( JSONObject dataSetGroupJson ) {
        if ( dataSetGroupJson == null ) return null;

        DataSetGroup dataSetGroup = new DataSetGroup();

        dataSetGroup.setColumnGroup(null);
        JSONValue value = dataSetGroupJson.get( COLUMNGROUP );
        if (value != null) dataSetGroup.setColumnGroup( parseColumnGroup( value.isObject() ) );

        List<GroupFunction> groupFunctions = parseGroupFunctions( ( value = dataSetGroupJson.get( GROUPFUNCTIONS ) ) != null ? value.isArray() : null );
        if ( groupFunctions != null ) dataSetGroup.getGroupFunctions().addAll( groupFunctions );

        dataSetGroup.setSelectedIntervalList(null);
        value = dataSetGroupJson.get(SELECTEDINTERVALS);
        if (value != null) dataSetGroup.setSelectedIntervalList(parseSelectedIntervals(value.isArray()));

        dataSetGroup.setJoin(false);
        value = dataSetGroupJson.get(JOIN);
        if (value != null) dataSetGroup.setJoin(Boolean.valueOf(value.isString().stringValue()));

        return dataSetGroup;
    }

    private ColumnGroup parseColumnGroup( JSONObject columnGroupJson ) {
        if ( columnGroupJson == null ) return null;
        ColumnGroup columnGroup = new ColumnGroup();
        JSONValue value = columnGroupJson.get( SOURCEID );
        columnGroup.setSourceId(value != null ? value.isString().stringValue() : null);
        columnGroup.setColumnId((value = columnGroupJson.get(COLUMNID)) != null ? value.isString().stringValue() : null);
        columnGroup.setStrategy((value = columnGroupJson.get(GROUPSTRATEGY)) != null ? GroupStrategy.getByName(value.isString().stringValue()) : null);
        columnGroup.setMaxIntervals((value = columnGroupJson.get(MAXINTERVALS)) != null ? Integer.parseInt(value.isString().stringValue()) : -1);
        columnGroup.setIntervalSize((value = columnGroupJson.get(INTERVALSIZE)) != null ? value.isString().stringValue() : null);
        columnGroup.setEmptyIntervalsAllowed((value = columnGroupJson.get(EMPTYINTERVALS)) != null ? Boolean.valueOf(value.isString().stringValue()) : false);
        columnGroup.setAscendingOrder((value = columnGroupJson.get(ASCENDING)) != null ? Boolean.valueOf(value.isString().stringValue()) : false);
        columnGroup.setFirstMonthOfYear((value = columnGroupJson.get(FIRSTMONTHOFYEAR)) != null ? Month.getByName(value.isString().stringValue()) : null);
        columnGroup.setFirstDayOfWeek( (value = columnGroupJson.get(FIRSTDAYOFWEEK)) != null ? DayOfWeek.getByName(value.isString().stringValue()) : null );
        return columnGroup;
    }

    private List<GroupFunction> parseGroupFunctions( JSONArray groupFunctionsJson ) {
        if ( groupFunctionsJson == null ) return null;
        List<GroupFunction> groupFunctions = new ArrayList<GroupFunction>( groupFunctionsJson.size() );
        for ( int i = 0; i < groupFunctionsJson.size(); i++) {
            groupFunctions.add( parseGroupFunction( groupFunctionsJson.get( i ).isObject() ) );
        }
        return groupFunctions;
    }

    private GroupFunction parseGroupFunction( JSONObject groupFunctionJson ) {
        if ( groupFunctionJson == null ) return null;
        GroupFunction groupFunction = new GroupFunction();
        JSONValue value = groupFunctionJson.get( SOURCEID );
        groupFunction.setSourceId( value != null ? value.isString().stringValue() : null );
        groupFunction.setColumnId( (value = groupFunctionJson.get(COLUMNID)) != null ? value.isString().stringValue() : null );
        groupFunction.setFunction( ( value = groupFunctionJson.get( FUNCTION ) ) != null ? AggregateFunctionType.getByName(value.isString().stringValue()) : null );
        return groupFunction;
    }

    private List<Interval> parseSelectedIntervals(JSONArray selectedIntervalsJson) {
        if (selectedIntervalsJson == null) {
            return null;
        }
        List<Interval> intervalList = new ArrayList<Interval>(selectedIntervalsJson.size());
        for ( int i = 0; i < selectedIntervalsJson.size(); i++) {
            intervalList.add(parseInterval(selectedIntervalsJson.get(i).isObject()));
        }
        return intervalList;
    }

    private Interval parseInterval(JSONObject jsonObj) {
        if (jsonObj == null) {
            return null;
        }
        Interval interval = new Interval();

        JSONValue value = jsonObj.get(INTERVAL_NAME);
        interval.setName(value != null ? value.isString().stringValue() : null);

        value = jsonObj.get(INTERVAL_TYPE);
        interval.setType(value != null ? value.isString().stringValue() : null);

        value = jsonObj.get(INTERVAL_IDX);
        if (value != null) {
            interval.setIndex(Integer.parseInt(value.isString().stringValue()));
        }
        value = jsonObj.get(INTERVAL_MIN);
        if (value != null) {
            interval.setMinValue(parseValue(value));
        }
        value = jsonObj.get(INTERVAL_MAX);
        if (value != null) {
            interval.setMaxValue(parseValue(value));
        }

        return interval;
    }

    private List<DataSetSort> parseSortOperations( JSONArray columnSortsJsonArray ) {
        if ( columnSortsJsonArray == null ) return null;
        List<DataSetSort> dataSetSorts = new ArrayList<DataSetSort>();
        // There's only one DataSetSort, the json array is an array of column sorts
        DataSetSort dataSetSort = new DataSetSort();
        dataSetSorts.add( dataSetSort );

        List<ColumnSort> columnSorts = parseColumnSorts( columnSortsJsonArray );
        if ( columnSorts != null ) dataSetSort.getColumnSortList().addAll( columnSorts );

        return dataSetSorts;
    }

    private List<ColumnSort> parseColumnSorts( JSONArray columnSortsJsonArray ) {
        if ( columnSortsJsonArray == null ) return null;
        List<ColumnSort> columnSorts = new ArrayList<ColumnSort>( columnSortsJsonArray.size() );
        for ( int i = 0; i < columnSortsJsonArray.size(); i++) {
            columnSorts.add( parseColumnSort( columnSortsJsonArray.get( i ).isObject() ) );
        }
        return columnSorts;
    }

    private ColumnSort parseColumnSort( JSONObject columnSortJson ) {
        if ( columnSortJson == null ) return null;
        ColumnSort columnSort = new ColumnSort();
        JSONValue value = columnSortJson.get( COLUMNID );
        columnSort.setColumnId( value != null ? value.isString().stringValue() : null );
        columnSort.setOrder( (value = columnSortJson.get(SORTORDER)) != null ? SortOrder.getByName(value.isString().stringValue()) : null );
        return columnSort;
    }

    private boolean isLogicalFilter( String functionType ) {
        return logicalFunctionTypes.contains( functionType );
    }

    private boolean isCoreFilter( String functionType ) {
        return coreFunctionTypes.contains( functionType );
    }

    private boolean checkNotNull( JSONValue value, boolean nullable, String errorMessage) {
        if ( nullable ) return value != null;
        else {
            if ( value !=null ) return true;
            else  throw new RuntimeException(CommonConstants.INSTANCE.json_datasetlookup_validation_error() + (!StringUtils.isBlank(errorMessage) ? errorMessage : ""));
        }
    }

    private DateTimeFormat _dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

    private JSONValue formatValue(Object value) {
        if (value == null) {
            // Null
            return JSONNull.getInstance();
        }
        try {
            // Boolean
            return JSONBoolean.getInstance((Boolean) value);
        }
        catch (Exception e1) {
            try {
                // Number
                return new JSONNumber(((Number) value).doubleValue());
            } catch (Exception e2) {
                try {
                    // Date
                    return new JSONString(_dateFormat.format((Date) value));
                } catch (Exception e3) {
                    // String
                    return new JSONString(value.toString());
                }
            }
        }
    }

    private Comparable parseValue(JSONValue jsonValue) {
        if (jsonValue == null || jsonValue.isNull() != null) {
            // Null
            return null;
        }
        // Boolean
        if (jsonValue.isBoolean() != null) {
            return jsonValue.isBoolean().booleanValue();
        }
        // Number
        if (jsonValue.isNumber() != null) {
            return jsonValue.isNumber().doubleValue();
        }
        try {
            // Date
            return _dateFormat.parse(jsonValue.isString().stringValue());
        }
        catch (Exception e1) {
            // String
            return jsonValue.isString().stringValue();
        }
    }
}
