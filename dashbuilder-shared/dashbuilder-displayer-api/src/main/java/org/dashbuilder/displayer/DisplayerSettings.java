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
package org.dashbuilder.displayer;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jboss.errai.common.client.api.annotations.Portable;

import static org.dashbuilder.displayer.DisplayerEditorConfig.ATTRIBUTE_PATH_SEPARATOR;

@Portable
public class DisplayerSettings {

    // Transient because no need to marshall this through errai.
    private transient JSONObject json = new JSONObject();

    private static final String _true = "true";
    private static final String _false = "false";

    protected String UUID;
    protected DataSet dataSet;
    protected DataSetLookup dataSetLookup;
    protected List<DisplayerSettingsColumn> columnList = new ArrayList<DisplayerSettingsColumn>();

    public DisplayerSettings( DisplayerType displayerType ) {
        this();
        setType( displayerType );
        if ( DisplayerType.PIECHART.equals( displayerType ) ) {
            setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_3D ), _true );
        }
    }

    public DisplayerSettings() {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TITLE_VISIBLE ), _true );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_ENABLED ), _false );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_SELFAPPLY_ENABLED ), _false );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_NOTIFICATION_ENABLED ), _false );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_LISTENING_ENABLED ), _false);
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_WIDTH ), "600" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_HEIGHT ), "300" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_TOP ), "20" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_BOTTOM ), "50" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_LEFT ), "80" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_RIGHT ), "80" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_SHOWLEGEND ), _true );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_LEGENDPOSITION ), "POSITION_RIGHT" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_PAGESIZE ), "20" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_WIDTH ), "0" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTENABLED ), _true);
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTORDER ), "asc" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_SHOWLABELS ), _false );
//        setNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_LABELSANGLE ), "0" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_SHOWLABELS ), _false );
//        setNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_LABELSANGLE ), "0" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_START ), "0" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_WARNING ), "0" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_CRITICAL ), "0" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_END ), "0" );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_3D ), _false );
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.BARCHART_HORIZONTAL ), _false );
    }

    public static DisplayerSettings getInstanceFromJson( String jsonString ) {
        DisplayerSettings ds = new DisplayerSettings();
        if ( !StringUtils.isBlank( jsonString ) ) {
            JSONValue parseResult = JSONParser.parseStrict( jsonString );
            JSONObject jsonObject = parseResult.isObject();
            if ( parseResult.isNull() == null && jsonObject != null ) ds.json = jsonObject;
        }
        return ds;
    }

    private void setNodeValue( JSONObject node, String path, String value ) {
        if ( node == null || StringUtils.isBlank( path ) || value == null ) return;

        int separatorIndex = path.lastIndexOf( ATTRIBUTE_PATH_SEPARATOR );
        String nodesPath = separatorIndex > 0 ? path.substring( 0, separatorIndex ) : "";
        String leaf = separatorIndex > 0 ? path.substring( separatorIndex + 1 ) : path;

        findNode( node, nodesPath, true ).put( leaf, new JSONString( value ) );
    }

    private String getNodeValue( JSONObject node, String path ) {
        if ( node == null || StringUtils.isBlank( path ) ) return null;

        int separatorIndex = path.lastIndexOf( ATTRIBUTE_PATH_SEPARATOR );
        String subNodesPath = separatorIndex > 0 ? path.substring( 0, separatorIndex ) : "";
        String leaf = separatorIndex > 0 ? path.substring( separatorIndex + 1 ) : path;

        JSONObject childNode = findNode( node, subNodesPath, false );
        String value = null;
        if ( childNode != null) {
            JSONValue jsonValue = childNode.get( leaf );
            if (jsonValue != null && jsonValue.isString() != null) value = jsonValue.isString().stringValue();
        }
        return value;
    }

    private JSONObject findNode(JSONObject parent, String path, boolean createPath) {
        if ( parent == null ) return null;
        if ( StringUtils.isBlank( path ) ) return parent;

        int separatorIndex = path.indexOf( DisplayerEditorConfig.ATTRIBUTE_PATH_SEPARATOR );
        String strChildNode = separatorIndex > 0 ? path.substring( 0, separatorIndex ) : path;
        String remainingNodes = separatorIndex > 0 ? path.substring( separatorIndex + 1 ) : "";

        JSONObject childNode = (JSONObject) parent.get( strChildNode );
        if ( childNode == null && createPath ) {
            childNode = new JSONObject();
            parent.put( strChildNode, childNode );
        }

        return findNode( childNode, remainingNodes, createPath );
    }

    private String getSettingPath( DisplayerAttributeDef displayerAttributeDef ) {
        return displayerAttributeDef.getFullId();
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID( String UUID ) {
        this.UUID = UUID;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet( DataSet dataSet ) {
        this.dataSet = dataSet;
    }

    public DataSetLookup getDataSetLookup() {
        return dataSetLookup;
    }

    public void setDataSetLookup( DataSetLookup dataSetLookup ) {
        this.dataSetLookup = dataSetLookup;
    }

    public List<DisplayerSettingsColumn> getColumnList() {
        return columnList;
    }

    // 'Generic' getter method
    public String getDisplayerSetting( DisplayerAttributeDef displayerAttributeDef ) {
        return getNodeValue( json, getSettingPath( displayerAttributeDef ) );
    }

    // 'Generic' setter method
    public void setDisplayerSetting( DisplayerAttributeDef displayerAttributeDef, String value ) {
        setNodeValue( json, getSettingPath( displayerAttributeDef ), value );
    }

    public DisplayerType getType() {
        String strType = getNodeValue( json, getSettingPath( DisplayerAttributeDef.TYPE ) );
        return DisplayerType.getByName( strType );
    }

    public void setType( DisplayerType displayerType ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TYPE ), displayerType.toString() );
    }

    public String getRenderer() {
        return getNodeValue( json, getSettingPath( DisplayerAttributeDef.RENDERER ) );
    }

    public void setRenderer( String renderer ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.RENDERER ), renderer );
    }

    public String getTitle() {
        return getNodeValue( json, getSettingPath( DisplayerAttributeDef.TITLE ) );
    }

    public void setTitle( String title ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TITLE ), title );
    }

    public boolean isTitleVisible() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.TITLE_VISIBLE ) ) );
    }

    public void setTitleVisible( boolean titleVisible ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TITLE_VISIBLE ), Boolean.toString( titleVisible ) );
    }

    public boolean isFilterEnabled() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_ENABLED ) ) );
    }

    public void setFilterEnabled( boolean filterEnabled ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_ENABLED ), Boolean.toString( filterEnabled ) );
    }

    public boolean isFilterSelfApplyEnabled() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_SELFAPPLY_ENABLED ) ) );
    }

    public void setFilterSelfApplyEnabled( boolean filterSelfApplyEnabled ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_SELFAPPLY_ENABLED ), Boolean.toString( filterSelfApplyEnabled ) );
    }

    public boolean isFilterNotificationEnabled() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_NOTIFICATION_ENABLED ) ) );
    }

    public void setFilterNotificationEnabled( boolean filterNotificationEnabled ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_NOTIFICATION_ENABLED ), Boolean.toString( filterNotificationEnabled ) );
    }

    public boolean isFilterListeningEnabled() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_LISTENING_ENABLED ) ) );
    }

    public void setFilterListeningEnabled( boolean filterListeningEnabled ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.FILTER_LISTENING_ENABLED ), Boolean.toString( filterListeningEnabled ) );
    }

    public int getChartWidth() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_WIDTH ) ), 10 );
    }

    public void setChartWidth( int chartWidth ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_WIDTH ), Integer.toString( chartWidth ) );
    }

    public int getChartHeight() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_HEIGHT ) ), 10 );
    }

    public void setChartHeight( int chartHeight ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_HEIGHT ), Integer.toString( chartHeight ) );
    }

    public int getChartMarginTop() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_TOP ) ), 10 );
    }

    public void setChartMarginTop( int chartMarginTop ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_TOP ), Integer.toString( chartMarginTop ) );
    }

    public int getChartMarginBottom() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_BOTTOM ) ), 10 );
    }

    public void setChartMarginBottom( int chartMarginBottom ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_BOTTOM ), Integer.toString( chartMarginBottom ) );
    }

    public int getChartMarginLeft() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_LEFT ) ), 10 );
    }

    public void setChartMarginLeft( int chartMarginLeft ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_LEFT ), Integer.toString( chartMarginLeft ) );
    }

    public int getChartMarginRight() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_RIGHT ) ), 10 );
    }

    public void setChartMarginRight( int chartMarginRight ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_MARGIN_RIGHT ), Integer.toString( chartMarginRight ) );
    }

    public boolean isChartShowLegend() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_SHOWLEGEND ) ) );
    }

    public void setChartShowLegend( boolean chartShowLegend ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_SHOWLEGEND ), Boolean.toString( chartShowLegend ) );
    }

    public Position getChartLegendPosition() {
        return Position.getByName( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_LEGENDPOSITION ) ) );
    }

    public void setChartLegendPosition( Position chartLegendPosition ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_LEGENDPOSITION ), chartLegendPosition.toString() );
    }

    public int getTablePageSize() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_PAGESIZE ) ), 10 );
    }

    public void setTablePageSize( int tablePageSize ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_PAGESIZE ), Integer.toString( tablePageSize ) );
    }

    public int getTableWidth() {
        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_WIDTH ) ), 10 );
    }

    public void setTableWidth( int tableWidth ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_WIDTH ), Integer.toString( tableWidth ) );
    }

    public boolean isTableSortEnabled() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTENABLED ) ) );
    }

    public void setTableSortEnabled( boolean tableSortEnabled ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTENABLED ), Boolean.toString( tableSortEnabled ) );
    }

    public String getTableDefaultSortColumnId() {
        return getNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTCOLUMNID ) );
    }

    public void setTableDefaultSortColumnId( String tableDefaultSortColumnId ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTCOLUMNID ), tableDefaultSortColumnId );
    }

    public SortOrder getTableDefaultSortOrder() {
        return SortOrder.getByName( getNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTORDER ) ) );
    }

    public void setTableDefaultSortOrder( SortOrder tableDefaultSortOrder ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.TABLE_SORTORDER ), tableDefaultSortOrder.toString() );
    }

    public boolean isXAxisShowLabels() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_SHOWLABELS ) ) );
    }

    public void setXAxisShowLabels( boolean axisShowLabels ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_SHOWLABELS ), Boolean.toString( axisShowLabels ) );
    }

//    public int getXAxisLabelsAngle() {
//        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_LABELSANGLE ) ), 10 );
//    }
//
//    public void setXAxisLabelsAngle( int axisLabelsAngle ) {
//        setNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_LABELSANGLE ), Integer.toString( axisLabelsAngle ) );
//    }

    public String getXAxisTitle() {
        return getNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_TITLE ) );
    }

    public void setXAxisTitle( String axisTitle ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.XAXIS_TITLE ), axisTitle );
    }

    public boolean isYAxisShowLabels() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_SHOWLABELS ) ) );
    }

    public void setYAxisShowLabels( boolean axisShowLabels ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_SHOWLABELS ), Boolean.toString( axisShowLabels ) );
    }

//    public int getYAxisLabelsAngle() {
//        return Integer.parseInt( getNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_LABELSANGLE ) ), 10 );
//    }
//
//    public void setYAxisLabelsAngle( int axisLabelsAngle ) {
//        setNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_LABELSANGLE ), Integer.toString( axisLabelsAngle ) );
//    }

    public String getYAxisTitle() {
        return getNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_TITLE ) );
    }

    public void setYAxisTitle( String axisTitle ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.YAXIS_TITLE ), axisTitle );
    }

    public long getMeterStart() {
        return Long.parseLong( getNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_START ) ), 10 );
    }

    public void setMeterStart( long meterStart ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_START ), Long.toString( meterStart ) );
    }

    public long getMeterWarning() {
        return Long.parseLong( getNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_WARNING ) ), 10 );
    }

    public void setMeterWarning( long meterWarning ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_WARNING ), Long.toString( meterWarning ) );
    }

    public long getMeterCritical() {
        return Long.parseLong( getNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_CRITICAL ) ), 10 );
    }

    public void setMeterCritical( long meterCritical ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_CRITICAL ), Long.toString( meterCritical ) );
    }

    public long getMeterEnd() {
        return Long.parseLong( getNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_END ) ), 10 );
    }

    public void setMeterEnd( long meterEnd ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.METER_END ), Long.toString( meterEnd ) );
    }

    public boolean isChart3D() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_3D ) ) );
    }

    public void setChart3D( boolean barchartThreeDimension ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.CHART_3D ), Boolean.toString( barchartThreeDimension ) );
    }

    public boolean isBarchartHorizontal() {
        return Boolean.parseBoolean( getNodeValue( json, getSettingPath( DisplayerAttributeDef.BARCHART_HORIZONTAL ) ) );
    }

    public void setBarchartHorizontal( boolean barchartHorizontal ) {
        setNodeValue( json, getSettingPath( DisplayerAttributeDef.BARCHART_HORIZONTAL ), Boolean.toString( barchartHorizontal ) );
    }
}
