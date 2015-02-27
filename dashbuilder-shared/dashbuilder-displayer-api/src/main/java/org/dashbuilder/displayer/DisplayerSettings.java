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

import java.util.HashMap;
import java.util.Map;

import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DisplayerSettings {

    private Map<String, String> settings = new HashMap<String, String>( 30 );

    private static final String _true = "true";
    private static final String _false = "false";

    protected String UUID;
    protected DataSet dataSet;
    protected DataSetLookup dataSetLookup;

    public DisplayerSettings( DisplayerType displayerType ) {
        this();
        setType( displayerType );
        if ( DisplayerType.PIECHART.equals( displayerType ) ) {
            settings.put( getSettingPath( DisplayerAttributeDef.CHART_3D ), _true );
        }
    }

    public DisplayerSettings() {
        settings.put( getSettingPath( DisplayerAttributeDef.TITLE_VISIBLE ), _true );
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_ENABLED ), _false );
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_SELFAPPLY_ENABLED ), _false );
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_NOTIFICATION_ENABLED ), _false );
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_LISTENING_ENABLED ), _false );
        settings.put( getSettingPath( DisplayerAttributeDef.REFRESH_STALE_DATA), _false );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_WIDTH ), "600" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_HEIGHT ), "300" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_BGCOLOR), "#FFFFFF" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_TOP ), "20" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_BOTTOM ), "50" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_LEFT ), "80" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_RIGHT ), "80" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_SHOWLEGEND ), _true );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_LEGENDPOSITION ), Position.RIGHT.toString() );
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_PAGESIZE ), "20" );
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_WIDTH ), "0" );
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_SORTENABLED ), _true );
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_SORTORDER ), "asc" );
        settings.put( getSettingPath( DisplayerAttributeDef.XAXIS_SHOWLABELS ), _false );
//        settings.put( getSettingPath( DisplayerAttributeDef.XAXIS_LABELSANGLE ), "0" );
        settings.put( getSettingPath( DisplayerAttributeDef.YAXIS_SHOWLABELS ), _false );
//        settings.put( getSettingPath( DisplayerAttributeDef.YAXIS_LABELSANGLE ), "0" );
        settings.put( getSettingPath( DisplayerAttributeDef.METER_START ), "0" );
        settings.put( getSettingPath( DisplayerAttributeDef.METER_WARNING ), "0" );
        settings.put( getSettingPath( DisplayerAttributeDef.METER_CRITICAL ), "0" );
        settings.put( getSettingPath( DisplayerAttributeDef.METER_END ), "0" );
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_3D ), _false );
        settings.put( getSettingPath( DisplayerAttributeDef.BARCHART_HORIZONTAL ), _false );
    }

    public DisplayerSettings cloneInstance() {
        DisplayerSettings clone = new DisplayerSettings();
        clone.UUID = UUID;
        clone.settings = new HashMap(settings);
        if (dataSet != null) clone.dataSet = dataSet.cloneInstance();
        if (dataSetLookup != null) clone.dataSetLookup = dataSetLookup.cloneInstance();
        return clone;
    }

    private String getSettingPath( DisplayerAttributeDef displayerAttributeDef ) {
        return displayerAttributeDef.getFullId();
    }

    private int parseInt(String value, int defaultValue) {
        if (StringUtils.isBlank(value)) return defaultValue;
        return Integer.parseInt(value);
    }

    private long parseLong(String value, long defaultValue) {
        if (StringUtils.isBlank(value)) return defaultValue;
        return Long.parseLong(value);
    }

    private boolean parseBoolean(String value) {
        if (StringUtils.isBlank(value)) return false;
        return Boolean.parseBoolean(value);
    }

    private String parseString(String value) {
        if (StringUtils.isBlank(value)) return "";
        return value;
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
        clearDataSetRelatedSettings();
    }

    public DataSetLookup getDataSetLookup() {
        return dataSetLookup;
    }

    public void setDataSetLookup( DataSetLookup dataSetLookup ) {
        this.dataSetLookup = dataSetLookup;
        clearDataSetRelatedSettings();
    }

    private void clearDataSetRelatedSettings() {
        setTableDefaultSortColumnId(null);
    }

    // 'Generic' getter method
    public String getDisplayerSetting( DisplayerAttributeDef displayerAttributeDef ) {
        return settings.get( getSettingPath( displayerAttributeDef ) );
    }

    // 'Generic' setter method
    public void setDisplayerSetting( DisplayerAttributeDef displayerAttributeDef, String value ) {
        settings.put(getSettingPath(displayerAttributeDef), value);
    }

    // 'Generic' setter method
    public void setDisplayerSetting( String displayerAttributeDef, String value ) {
        settings.put( displayerAttributeDef, value );
    }

    public Map<String, String> getSettingsFlatMap() {
        return settings;
    }

    public void setSettingsFlatMap( Map<String, String> settings ) {
        this.settings = settings;
    }

    public DisplayerType getType() {
        String strType = settings.get( getSettingPath( DisplayerAttributeDef.TYPE ) );
        return DisplayerType.getByName( strType );
    }

    public void setType( DisplayerType displayerType ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TYPE ), displayerType.toString() );
    }

    public String getRenderer() {
        return settings.get(getSettingPath(DisplayerAttributeDef.RENDERER));
    }

    public void setRenderer( String renderer ) {
        settings.put( getSettingPath( DisplayerAttributeDef.RENDERER ), renderer );
    }

    public String getTitle() {
        return parseString(settings.get(getSettingPath(DisplayerAttributeDef.TITLE)));
    }

    public void setTitle( String title ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TITLE ), title );
    }

    public boolean isTitleVisible() {
        return parseBoolean(settings.get(getSettingPath(DisplayerAttributeDef.TITLE_VISIBLE)));
    }

    public void setTitleVisible( boolean titleVisible ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TITLE_VISIBLE ), Boolean.toString( titleVisible ) );
    }

    public int getRefreshInterval() {
        return parseInt(settings.get(getSettingPath(DisplayerAttributeDef.REFRESH_INTERVAL)), -1);
    }

    public void setRefreshInterval( int refreshInSeconds ) {
        settings.put( getSettingPath( DisplayerAttributeDef.REFRESH_INTERVAL ), Integer.toString( refreshInSeconds ) );
    }

    public boolean isRefreshStaleData() {
        return parseBoolean(settings.get(getSettingPath(DisplayerAttributeDef.REFRESH_STALE_DATA)));
    }

    public void setRefreshStaleData( boolean refresh) {
        settings.put( getSettingPath( DisplayerAttributeDef.REFRESH_STALE_DATA ), Boolean.toString( refresh ) );
    }

    public boolean isFilterEnabled() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.FILTER_ENABLED ) ) );
    }

    public void setFilterEnabled( boolean filterEnabled ) {
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_ENABLED ), Boolean.toString( filterEnabled ) );
    }

    public boolean isFilterSelfApplyEnabled() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.FILTER_SELFAPPLY_ENABLED ) ) );
    }

    public void setFilterSelfApplyEnabled( boolean filterSelfApplyEnabled ) {
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_SELFAPPLY_ENABLED ), Boolean.toString( filterSelfApplyEnabled ) );
    }

    public boolean isFilterNotificationEnabled() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.FILTER_NOTIFICATION_ENABLED ) ) );
    }

    public void setFilterNotificationEnabled( boolean filterNotificationEnabled ) {
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_NOTIFICATION_ENABLED ), Boolean.toString( filterNotificationEnabled ) );
    }

    public boolean isFilterListeningEnabled() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.FILTER_LISTENING_ENABLED ) ) );
    }

    public void setFilterListeningEnabled( boolean filterListeningEnabled ) {
        settings.put( getSettingPath( DisplayerAttributeDef.FILTER_LISTENING_ENABLED ), Boolean.toString( filterListeningEnabled ) );
    }

    public int getChartWidth() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.CHART_WIDTH ) ), 500 );
    }

    public void setChartWidth( int chartWidth ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_WIDTH ), Integer.toString( chartWidth ) );
    }
    
    public String getChartBackgroundColor() {
        return settings.get(getSettingPath( DisplayerAttributeDef.CHART_BGCOLOR));
    }

    public void setChartBackgroundColor(String color) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_BGCOLOR ), color );
    }

    public int getChartHeight() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.CHART_HEIGHT ) ), 300 );
    }

    public void setChartHeight( int chartHeight ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_HEIGHT ), Integer.toString( chartHeight ) );
    }

    public int getChartMarginTop() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_TOP ) ), 10 );
    }

    public void setChartMarginTop( int chartMarginTop ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_TOP ), Integer.toString( chartMarginTop ) );
    }

    public int getChartMarginBottom() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_BOTTOM ) ), 10 );
    }

    public void setChartMarginBottom( int chartMarginBottom ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_BOTTOM ), Integer.toString( chartMarginBottom ) );
    }

    public int getChartMarginLeft() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_LEFT ) ), 10 );
    }

    public void setChartMarginLeft( int chartMarginLeft ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_LEFT ), Integer.toString( chartMarginLeft ) );
    }

    public int getChartMarginRight() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_RIGHT ) ), 10 );
    }

    public void setChartMarginRight( int chartMarginRight ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_MARGIN_RIGHT ), Integer.toString( chartMarginRight ) );
    }

    public boolean isChartShowLegend() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.CHART_SHOWLEGEND ) ) );
    }

    public void setChartShowLegend( boolean chartShowLegend ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_SHOWLEGEND ), Boolean.toString( chartShowLegend ) );
    }

    public Position getChartLegendPosition() {
        Position pos = Position.getByName( settings.get( getSettingPath( DisplayerAttributeDef.CHART_LEGENDPOSITION ) ) );
        if (pos == null) return Position.RIGHT;
        return pos;
    }

    public void setChartLegendPosition( Position chartLegendPosition ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_LEGENDPOSITION ), chartLegendPosition.toString() );
    }

    public int getTablePageSize() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.TABLE_PAGESIZE ) ), 10 );
    }

    public void setTablePageSize( int tablePageSize ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_PAGESIZE ), Integer.toString( tablePageSize ) );
    }

    public int getTableWidth() {
        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.TABLE_WIDTH ) ), 0 );
    }

    public void setTableWidth( int tableWidth ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_WIDTH ), Integer.toString( tableWidth ) );
    }

    public boolean isTableSortEnabled() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.TABLE_SORTENABLED ) ) );
    }

    public void setTableSortEnabled( boolean tableSortEnabled ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_SORTENABLED ), Boolean.toString( tableSortEnabled ) );
    }

    public String getTableDefaultSortColumnId() {
        return parseString(settings.get(getSettingPath(DisplayerAttributeDef.TABLE_SORTCOLUMNID)));
    }

    public void setTableDefaultSortColumnId( String tableDefaultSortColumnId ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_SORTCOLUMNID ), tableDefaultSortColumnId );
    }

    public SortOrder getTableDefaultSortOrder() {
        SortOrder order = SortOrder.getByName( settings.get( getSettingPath( DisplayerAttributeDef.TABLE_SORTORDER ) ) );
        if (order == null) return SortOrder.ASCENDING;
        return order;
    }

    public void setTableDefaultSortOrder( SortOrder tableDefaultSortOrder ) {
        settings.put( getSettingPath( DisplayerAttributeDef.TABLE_SORTORDER ), tableDefaultSortOrder.toString() );
    }

    public boolean isXAxisShowLabels() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.XAXIS_SHOWLABELS ) ) );
    }

    public void setXAxisShowLabels( boolean axisShowLabels ) {
        settings.put( getSettingPath( DisplayerAttributeDef.XAXIS_SHOWLABELS ), Boolean.toString( axisShowLabels ) );
    }

//    public int getXAxisLabelsAngle() {
//        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.XAXIS_LABELSANGLE ) ), 10 );
//    }
//
//    public void setXAxisLabelsAngle( int axisLabelsAngle ) {
//        settings.put( getSettingPath( DisplayerAttributeDef.XAXIS_LABELSANGLE ), Integer.toString( axisLabelsAngle ) );
//    }

    public String getXAxisTitle() {
        return parseString(settings.get( getSettingPath( DisplayerAttributeDef.XAXIS_TITLE ) ));
    }

    public void setXAxisTitle( String axisTitle ) {
        settings.put( getSettingPath( DisplayerAttributeDef.XAXIS_TITLE ), axisTitle );
    }

    public boolean isYAxisShowLabels() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.YAXIS_SHOWLABELS ) ) );
    }

    public void setYAxisShowLabels( boolean axisShowLabels ) {
        settings.put( getSettingPath( DisplayerAttributeDef.YAXIS_SHOWLABELS ), Boolean.toString( axisShowLabels ) );
    }

//    public int getYAxisLabelsAngle() {
//        return parseInt( settings.get( getSettingPath( DisplayerAttributeDef.YAXIS_LABELSANGLE ) ), 10 );
//    }
//
//    public void setYAxisLabelsAngle( int axisLabelsAngle ) {
//        settings.put( getSettingPath( DisplayerAttributeDef.YAXIS_LABELSANGLE ), Integer.toString( axisLabelsAngle ) );
//    }

    public String getYAxisTitle() {
        return parseString(settings.get( getSettingPath( DisplayerAttributeDef.YAXIS_TITLE ) ));
    }

    public void setYAxisTitle( String axisTitle ) {
        settings.put( getSettingPath( DisplayerAttributeDef.YAXIS_TITLE ), axisTitle );
    }

    public long getMeterStart() {
        return parseLong( settings.get( getSettingPath( DisplayerAttributeDef.METER_START ) ), 0 );
    }

    public void setMeterStart( long meterStart ) {
        settings.put( getSettingPath( DisplayerAttributeDef.METER_START ), Long.toString( meterStart ) );
    }

    public long getMeterWarning() {
        return parseLong( settings.get( getSettingPath( DisplayerAttributeDef.METER_WARNING ) ), 60 );
    }

    public void setMeterWarning( long meterWarning ) {
        settings.put( getSettingPath( DisplayerAttributeDef.METER_WARNING ), Long.toString( meterWarning ) );
    }

    public long getMeterCritical() {
        return parseLong( settings.get( getSettingPath( DisplayerAttributeDef.METER_CRITICAL ) ), 90 );
    }

    public void setMeterCritical( long meterCritical ) {
        settings.put( getSettingPath( DisplayerAttributeDef.METER_CRITICAL ), Long.toString( meterCritical ) );
    }

    public long getMeterEnd() {
        return parseLong( settings.get( getSettingPath( DisplayerAttributeDef.METER_END ) ), 100 );
    }

    public void setMeterEnd( long meterEnd ) {
        settings.put( getSettingPath( DisplayerAttributeDef.METER_END ), Long.toString( meterEnd ) );
    }

    public boolean isChart3D() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.CHART_3D ) ) );
    }

    public void setChart3D( boolean barchartThreeDimension ) {
        settings.put( getSettingPath( DisplayerAttributeDef.CHART_3D ), Boolean.toString( barchartThreeDimension ) );
    }

    public boolean isBarchartHorizontal() {
        return parseBoolean( settings.get( getSettingPath( DisplayerAttributeDef.BARCHART_HORIZONTAL ) ) );
    }

    public void setBarchartHorizontal( boolean barchartHorizontal ) {
        settings.put( getSettingPath( DisplayerAttributeDef.BARCHART_HORIZONTAL ), Boolean.toString( barchartHorizontal ) );
    }
}
