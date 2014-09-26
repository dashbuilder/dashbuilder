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
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
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
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;

public class DataSetLookupJSONMarshaller {

    private static final String JSON_VALIDATION_ERROR = "JSON validation error: ";

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
    private static final String ASCENDING = "asc";
    private static final String FIRSTMONTHOFYEAR = "firstMonthOfYear";
    private static final String FIRSTDAYOFWEEK = "firstDayOfWeek";

    private static final String GROUPFUNCTIONS = "groupFunctions";
    private static final String FUNCTION = "function";

    private static final String SELECTEDINTERVALS = "selectedIntervals";

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
            json.put( UUID, new JSONString( dataSetLookup.getDataSetUUID() ) );
            json.put( ROWCOUNT, new JSONString( Integer.toString(dataSetLookup.getNumberOfRows()) ) );
            json.put( ROWOFFSET, new JSONString( Integer.toString(dataSetLookup.getRowOffset()) ) );

            List<DataSetFilter> filterOps = dataSetLookup.getOperationList( DataSetFilter.class );
            json.put( FILTEROPS, formatFilterOperations( filterOps ) );

            List<DataSetGroup> groupOps = dataSetLookup.getOperationList( DataSetGroup.class );
            json.put( GROUPOPS, formatGroupOperations( groupOps ) );

            List<DataSetSort> sortOps = dataSetLookup.getOperationList( DataSetSort.class );
            json.put( SORTOPS, formatSortOperations( sortOps ) );
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
            for ( Comparable param : cff.getParameters() ) {
                paramsJsonArray.set( paramCounter++, new JSONString( param.toString() ) );
            }
            colFilterJson.put( FUNCTION_TERMS, paramsJsonArray );

        } else {
            GWT.log( "Unsupported ColumnFilter" );
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
        dataSetGroupJson.put( GROUPFUNCTIONS, formatgroupFunctions( dataSetGroup.getGroupFunctions() ) );
        dataSetGroupJson.put( SELECTEDINTERVALS, formatSelectedIntervals( dataSetGroup.getSelectedIntervalNames() ) );
        return dataSetGroupJson;
    }

    private JSONObject formatColumnGroup( ColumnGroup columnGroup ) {
        if ( columnGroup == null ) throw new RuntimeException( "ColumnGroup cannot be null" );
        JSONObject columnGroupJson = new JSONObject();
        columnGroupJson.put( SOURCEID, columnGroup.getSourceId() != null ? new JSONString( columnGroup.getSourceId() ) : null );
        columnGroupJson.put( COLUMNID, columnGroup.getColumnId() != null ? new JSONString( columnGroup.getColumnId() ) : null );
        columnGroupJson.put( GROUPSTRATEGY, columnGroup.getStrategy() != null ? new JSONString( columnGroup.getStrategy().toString() ) : null );
        columnGroupJson.put( MAXINTERVALS, new JSONString( Integer.toString( columnGroup.getMaxIntervals() ) ) );
        columnGroupJson.put( INTERVALSIZE, columnGroup.getIntervalSize() != null ? new JSONString( columnGroup.getIntervalSize() ) : null );
        columnGroupJson.put( ASCENDING, new JSONString( columnGroup.isAscendingOrder() ? "true" : "false" ) );
        columnGroupJson.put( FIRSTMONTHOFYEAR, columnGroup.getFirstMonthOfYear() != null ? new JSONString( columnGroup.getFirstMonthOfYear().toString() ) : null );
        columnGroupJson.put( FIRSTDAYOFWEEK, columnGroup.getFirstDayOfWeek() != null ? new JSONString( columnGroup.getFirstDayOfWeek().toString() ) : null );
        return columnGroupJson;
    }

    private JSONArray formatgroupFunctions( List<GroupFunction> groupFunctions ) {
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

    private JSONArray formatSelectedIntervals( List<String> selectedIntervalNames ) {
        JSONArray selectedIntervalNamesJsonArray = new JSONArray();
        int intervalNamesCounter = 0;
        for ( String intervalName : selectedIntervalNames ) {
            selectedIntervalNamesJsonArray.set( intervalNamesCounter++, new JSONString( intervalName ) );
        }
        return selectedIntervalNamesJsonArray;
    }

    private JSONArray formatSortOperations( List<DataSetSort> sortOps ) {
        if ( sortOps.size() == 0 ) return null;
        // There should be only one DataSetFilter
        return formatColumnSorts( sortOps.get( 0 ).getColumnSortList() );
    }

    private JSONArray formatColumnSorts( List<ColumnSort> columnSorts ) {
        JSONArray columnSortsJsonArray = new JSONArray();
        int columnSortCounter = 0;
        for ( ColumnSort columnSort : columnSorts ) {
            columnSortsJsonArray.set( columnSortCounter, formatColumnSort( columnSort ) );
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
        dataSetLookup.setNumberOfRows( json.get( ROWCOUNT ) != null ? Integer.parseInt( json.get( ROWCOUNT ).isString().stringValue(), 10 ) : 0 );
        dataSetLookup.setNumberOfRows( json.get( ROWOFFSET ) != null ? Integer.parseInt( json.get( ROWOFFSET ).isString().stringValue(), 10 ) : 0 );

        List<DataSetOp> dataSetOpList = dataSetLookup.getOperationList();

        Collection c = null;
        JSONValue array = json.get( FILTEROPS );

        if ( (c = parseFilterOperations( array != null ? array.isArray() : null )) != null )
            dataSetOpList.addAll( c );

        if ( (c = parseGroupOperations( ( array = json.get( GROUPOPS ) ) != null ? array.isArray() : null )) != null )
            dataSetOpList.addAll( c );

        if ( (c = parseSortOperations( ( array = json.get( SORTOPS ) ) != null ? array.isArray() : null )) != null )
            dataSetOpList.addAll( c );

        return dataSetLookup;
    }

    private List<DataSetFilter> parseFilterOperations( JSONArray columnFiltersJsonArray ) {
        if ( columnFiltersJsonArray == null ) return null;
        List<DataSetFilter> dataSetFilters = new ArrayList<DataSetFilter>();
        // There's only one DataSetFilter, the json array is an array of column filters
        DataSetFilter dataSetFilter = new DataSetFilter();
        dataSetFilters.add( dataSetFilter );
        List<ColumnFilter> columnFilters = parseColumnFilters( columnFiltersJsonArray );
        if ( columnFilters != null ) dataSetFilter.getColumnFilterList().addAll( columnFilters );

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
        if ( checkNotNull( value, false, "the column id field of a column filter cannot be null." ) ) {
            columnId = value.isString() != null ? value.isString().stringValue() : null;
        }

        value = columnFilterJson.get( FUNCTION_TYPE );
        if ( checkNotNull( value, false, "the function type field of a column filter cannot be null." ) ) {
            functionType = value.isString() != null ? value.isString().stringValue() : null;
        }

        value = columnFilterJson.get( FUNCTION_TERMS );

        if ( isCoreFilter( functionType ) ) {
            CoreFunctionFilter cff = new CoreFunctionFilter();
            cff.setColumnId( columnId );
            cff.setType( CoreFunctionType.getByName( functionType ) );

            if ( checkNotNull( value, false, "the parameters of a core function filter cannot be null." ) ) {
                cff.setParameters( parseCoreFunctionParameters( value.isArray() ).toArray( new Comparable[]{} ) );
            }

            return cff;

        } else if ( isLogicalFilter( functionType ) ) {
            LogicalExprFilter lef = new LogicalExprFilter();
            lef.setColumnId( columnId );
            lef.setLogicalOperator( LogicalExprType.getByName( functionType ) );

            if ( checkNotNull( value, false, "the parameters of a logical expression filter cannot be null." ) ) {
                // Logical expression terms are an an array of column filters
                lef.setLogicalTerms( parseColumnFilters( value.isArray() ) );
            }

            return lef;
        }
        else throw new RuntimeException( "Wrong type of column filter has been specified." );
    }

    private List<Comparable> parseCoreFunctionParameters( JSONArray paramsJsonArray ) {
        if ( paramsJsonArray == null ) return null;
        List<Comparable> params = new ArrayList<Comparable>( paramsJsonArray.size() );
        for (  int i = 0; i < paramsJsonArray.size(); i++) {
            params.add( paramsJsonArray.get( i ).isString().stringValue() );
        }
        return params;
    }


    private List<DataSetGroup> parseGroupOperations( JSONArray groupOpsJsonArray ) {
        if ( groupOpsJsonArray == null ) return null;
        List<DataSetGroup> dataSetGroups = new ArrayList<DataSetGroup>();
        for ( int i = 0; i < groupOpsJsonArray.size(); i++) {
            JSONObject dataSetGroupOpJson = groupOpsJsonArray.get( i ).isObject();
            dataSetGroups.add( parseDataSetGroup( dataSetGroupOpJson ) );
        }
        return dataSetGroups;
    }

    private DataSetGroup parseDataSetGroup( JSONObject dataSetGroupJson ) {
        if ( dataSetGroupJson == null ) return null;

        DataSetGroup dataSetGroup = new DataSetGroup();
        JSONValue value = dataSetGroupJson.get( COLUMNGROUP );

        dataSetGroup.setColumnGroup( parseColumnGroup( value != null ? value.isObject() : null ) );

        List<GroupFunction> groupFunctions = parseGroupFunctions( ( value = dataSetGroupJson.get( GROUPFUNCTIONS ) ) != null ? value.isArray() : null );
        if ( groupFunctions != null ) dataSetGroup.getGroupFunctions().addAll( groupFunctions );

        dataSetGroup.setSelectedIntervalNames(
            parseSelectedIntervals( (value = dataSetGroupJson.get(SELECTEDINTERVALS)) != null ? value.isArray() : null ) );

        return dataSetGroup;
    }

    private ColumnGroup parseColumnGroup( JSONObject columnGroupJson ) {
        if ( columnGroupJson == null ) return null;
        ColumnGroup columnGroup = new ColumnGroup();
        JSONValue value = columnGroupJson.get( SOURCEID );
        columnGroup.setSourceId( value != null ? value.isString().stringValue() : null );
        columnGroup.setColumnId( (value = columnGroupJson.get(COLUMNID)) != null ? value.isString().stringValue() : null );
        columnGroup.setStrategy( (value = columnGroupJson.get(GROUPSTRATEGY)) != null ? GroupStrategy.getByName( value.isString().stringValue() ) : null );
        columnGroup.setMaxIntervals( (value = columnGroupJson.get(MAXINTERVALS)) != null ? Integer.parseInt(value.isString().stringValue()) : null );
        columnGroup.setIntervalSize( (value = columnGroupJson.get(INTERVALSIZE)) != null ? value.isString().stringValue() : null );
        columnGroup.setAscendingOrder( (value = columnGroupJson.get(ASCENDING)) != null ? Boolean.valueOf(value.isString().stringValue()) : null );
        columnGroup.setFirstMonthOfYear( (value = columnGroupJson.get(FIRSTMONTHOFYEAR)) != null ? Month.getByName(value.isString().stringValue()) : null );
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

    private List<String> parseSelectedIntervals( JSONArray selectedIntervalsJson ) {
        if ( selectedIntervalsJson == null ) return null;
        List<String> intervalNames = new ArrayList<String>( selectedIntervalsJson.size() );
        for ( int i = 0; i < selectedIntervalsJson.size(); i++) {
            JSONString value = selectedIntervalsJson.get( i ).isString();
            if ( value != null ) intervalNames.add( value.stringValue() );
        }
        return intervalNames;
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
            else  throw new RuntimeException( JSON_VALIDATION_ERROR + ( !StringUtils.isBlank( errorMessage ) ? errorMessage : "") );
        }
    }
}
