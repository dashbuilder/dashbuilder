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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DisplayerSettings {

    private Map<DisplayerSettingId, String> settings = new HashMap<DisplayerSettingId, String>( 30 );

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
            settings.put( DisplayerSettingId.CHART_3D, _true );
        }
    }

    public DisplayerSettings() {
        settings.put( DisplayerSettingId.TITLE_VISIBLE, _true );
        settings.put( DisplayerSettingId.FILTER_ENABLED, _false );
        settings.put( DisplayerSettingId.FILTER_SELFAPPLY_ENABLED, _false );
        settings.put( DisplayerSettingId.FILTER_NOTIFICATION_ENABLED, _false );
        settings.put( DisplayerSettingId.FILTER_LISTENING_ENABLED, _false);
        settings.put( DisplayerSettingId.CHART_WIDTH, "600" );
        settings.put( DisplayerSettingId.CHART_HEIGHT, "300" );
        settings.put( DisplayerSettingId.CHART_MARGIN_TOP, "20" );
        settings.put( DisplayerSettingId.CHART_MARGIN_BOTTOM, "50" );
        settings.put( DisplayerSettingId.CHART_MARGIN_LEFT, "80" );
        settings.put( DisplayerSettingId.CHART_MARGIN_RIGHT, "80" );
        settings.put( DisplayerSettingId.CHART_SHOWLEGEND, _true );
        settings.put( DisplayerSettingId.CHART_LEGENDPOSITION, "POSITION_RIGHT" );
        settings.put( DisplayerSettingId.TABLE_PAGESIZE, "20" );
        settings.put( DisplayerSettingId.TABLE_WIDTH, "0" );
        settings.put( DisplayerSettingId.TABLE_SORTENABLED, _true);
        settings.put( DisplayerSettingId.TABLE_SORTORDER, "asc" );
        settings.put( DisplayerSettingId.XAXIS_SHOWLABELS, _false );
        settings.put( DisplayerSettingId.XAXIS_LABELSANGLE, "0" );
        settings.put( DisplayerSettingId.YAXIS_SHOWLABELS, _false );
        settings.put( DisplayerSettingId.YAXIS_LABELSANGLE, "0" );
        settings.put( DisplayerSettingId.METER_START, "0" );
        settings.put( DisplayerSettingId.METER_WARNING, "0" );
        settings.put( DisplayerSettingId.METER_CRITICAL, "0" );
        settings.put( DisplayerSettingId.METER_END, "0" );
        settings.put( DisplayerSettingId.CHART_3D, _false );
        settings.put( DisplayerSettingId.BARCHART_HORIZONTAL, _false );
    }

    //    @Override
    public String getUUID() {
        return UUID;
    }

//    @Override
    public void setUUID( String UUID ) {
        this.UUID = UUID;
    }

//    @Override
    public DataSet getDataSet() {
        return dataSet;
    }

//    @Override
    public void setDataSet( DataSet dataSet ) {
        this.dataSet = dataSet;
    }

//    @Override
    public DataSetLookup getDataSetLookup() {
        return dataSetLookup;
    }

//    @Override
    public void setDataSetLookup( DataSetLookup dataSetLookup ) {
        this.dataSetLookup = dataSetLookup;
    }

//    @Override
    public List<DisplayerSettingsColumn> getColumnList() {
        return columnList;
    }

    // 'Generic' getter method
    public String getDisplayerSetting( DisplayerSettingId displayerSettingId ) {
        return settings.get( displayerSettingId );
    }

    // 'Generic' setter method
    public void setDisplayerSetting( DisplayerSettingId displayerSettingId, String value ) {
        settings.put( displayerSettingId, value );
    }

//    @Override
    public DisplayerType getType() {
        String strType = settings.get( DisplayerSettingId.TYPE );
        return DisplayerType.getByName( strType );
    }

    public void setType( DisplayerType displayerType ) {
        settings.put( DisplayerSettingId.TYPE, displayerType.toString() );
    }

    public String getRenderer() {
        return settings.get( DisplayerSettingId.RENDERER );
    }

    public void setRenderer( String renderer ) {
        settings.put( DisplayerSettingId.RENDERER, renderer );
    }

//    @Override
    public String getTitle() {
        return settings.get( DisplayerSettingId.TITLE );
    }

//    @Override
    public void setTitle( String title ) {
        settings.put( DisplayerSettingId.TITLE, title );
    }

//    @Override
    public boolean isTitleVisible() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.TITLE_VISIBLE ) );
    }

//    @Override
    public void setTitleVisible( boolean titleVisible ) {
        settings.put( DisplayerSettingId.TITLE_VISIBLE, Boolean.toString( titleVisible ) );
    }

//    @Override
    public boolean isFilterEnabled() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.FILTER_ENABLED ) );
    }

//    @Override
    public void setFilterEnabled( boolean filterEnabled ) {
        settings.put( DisplayerSettingId.FILTER_ENABLED, Boolean.toString( filterEnabled ) );
    }

//    @Override
    public boolean isFilterSelfApplyEnabled() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.FILTER_SELFAPPLY_ENABLED ) );
    }

//    @Override
    public void setFilterSelfApplyEnabled( boolean filterSelfApplyEnabled ) {
        settings.put( DisplayerSettingId.FILTER_SELFAPPLY_ENABLED, Boolean.toString( filterSelfApplyEnabled ) );
    }

//    @Override
    public boolean isFilterNotificationEnabled() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.FILTER_NOTIFICATION_ENABLED ) );
    }

//    @Override
    public void setFilterNotificationEnabled( boolean filterNotificationEnabled ) {
        settings.put( DisplayerSettingId.FILTER_NOTIFICATION_ENABLED, Boolean.toString( filterNotificationEnabled ) );
    }

//    @Override
    public boolean isFilterListeningEnabled() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.FILTER_LISTENING_ENABLED ) );
    }

//    @Override
    public void setFilterListeningEnabled( boolean filterListeningEnabled ) {
        settings.put( DisplayerSettingId.FILTER_LISTENING_ENABLED, Boolean.toString( filterListeningEnabled ) );
    }

    public int getChartWidth() {
        return Integer.parseInt( settings.get( DisplayerSettingId.CHART_WIDTH ), 10 );
    }

    public void setChartWidth( int chartWidth ) {
        settings.put( DisplayerSettingId.CHART_WIDTH, Integer.toString( chartWidth ) );
    }

    public int getChartHeight() {
        return Integer.parseInt( settings.get( DisplayerSettingId.CHART_HEIGHT ) , 10 );
    }

    public void setChartHeight( int chartHeight ) {
        settings.put( DisplayerSettingId.CHART_HEIGHT, Integer.toString( chartHeight ) );
    }

    public int getChartMarginTop() {
        return Integer.parseInt( settings.get( DisplayerSettingId.CHART_MARGIN_TOP ) , 10 );
    }

    public void setChartMarginTop( int chartMarginTop ) {
        settings.put( DisplayerSettingId.CHART_MARGIN_TOP, Integer.toString( chartMarginTop ) );
    }

    public int getChartMarginBottom() {
        return Integer.parseInt( settings.get( DisplayerSettingId.CHART_MARGIN_BOTTOM ) , 10 );
    }

    public void setChartMarginBottom( int chartMarginBottom ) {
        settings.put( DisplayerSettingId.CHART_MARGIN_BOTTOM, Integer.toString( chartMarginBottom ) );
    }

    public int getChartMarginLeft() {
        return Integer.parseInt( settings.get( DisplayerSettingId.CHART_MARGIN_LEFT ) , 10 );
    }

    public void setChartMarginLeft( int chartMarginLeft ) {
        settings.put( DisplayerSettingId.CHART_MARGIN_LEFT, Integer.toString( chartMarginLeft ) );
    }

    public int getChartMarginRight() {
        return Integer.parseInt( settings.get( DisplayerSettingId.CHART_MARGIN_RIGHT ) , 10 );
    }

    public void setChartMarginRight( int chartMarginRight ) {
        settings.put( DisplayerSettingId.CHART_MARGIN_RIGHT, Integer.toString( chartMarginRight ) );
    }

    public boolean isChartShowLegend() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.CHART_SHOWLEGEND ) );
    }

    public void setChartShowLegend( boolean chartShowLegend ) {
        settings.put( DisplayerSettingId.CHART_SHOWLEGEND, Boolean.toString( chartShowLegend ) );
    }

    public Position getChartLegendPosition() {
        return Position.getByName( settings.get( DisplayerSettingId.CHART_LEGENDPOSITION ) );
    }

    public void setChartLegendPosition( Position chartLegendPosition ) {
        settings.put( DisplayerSettingId.CHART_LEGENDPOSITION, chartLegendPosition.toString() );
    }

    public int getTablePageSize() {
        return Integer.parseInt( settings.get( DisplayerSettingId.TABLE_PAGESIZE ) , 10 );
    }

    public void setTablePageSize( int tablePageSize ) {
        settings.put( DisplayerSettingId.TABLE_PAGESIZE, Integer.toString( tablePageSize ) );
    }

    public int getTableWidth() {
        return Integer.parseInt( settings.get( DisplayerSettingId.TABLE_WIDTH ) , 10 );
    }

    public void setTableWidth( int tableWidth ) {
        settings.put( DisplayerSettingId.TABLE_WIDTH, Integer.toString( tableWidth ) );
    }

    public boolean isTableSortEnabled() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.TABLE_SORTENABLED ) );
    }

    public void setTableSortEnabled( boolean tableSortEnabled ) {
        settings.put( DisplayerSettingId.TABLE_SORTENABLED, Boolean.toString( tableSortEnabled ) );
    }

    public String getTableDefaultSortColumnId() {
        return settings.get( DisplayerSettingId.TABLE_SORTCOLUMNID );
    }

    public void setTableDefaultSortColumnId( String tableDefaultSortColumnId ) {
        settings.put( DisplayerSettingId.TABLE_SORTCOLUMNID, tableDefaultSortColumnId );
    }

    public SortOrder getTableDefaultSortOrder() {
        return SortOrder.getByName( settings.get( DisplayerSettingId.TABLE_SORTORDER ) );
    }

    public void setTableDefaultSortOrder( SortOrder tableDefaultSortOrder ) {
        settings.put( DisplayerSettingId.TABLE_SORTORDER, tableDefaultSortOrder.toString() );
    }

    public boolean isXAxisShowLabels() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.XAXIS_SHOWLABELS ) );
    }

    public void setXAxisShowLabels( boolean axisShowLabels ) {
        settings.put( DisplayerSettingId.XAXIS_SHOWLABELS, Boolean.toString( axisShowLabels ) );
    }

    public int getXAxisLabelsAngle() {
        return Integer.parseInt( settings.get( DisplayerSettingId.XAXIS_LABELSANGLE ) , 10 );
    }

    public void setXAxisLabelsAngle( int axisLabelsAngle ) {
        settings.put( DisplayerSettingId.XAXIS_LABELSANGLE, Integer.toString( axisLabelsAngle ) );
    }

    public String getXAxisTitle() {
        return settings.get( DisplayerSettingId.XAXIS_TITLE );
    }

    public void setXAxisTitle( String axisTitle ) {
        settings.put( DisplayerSettingId.XAXIS_TITLE, axisTitle );
    }

    public boolean isYAxisShowLabels() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.YAXIS_SHOWLABELS ) );
    }

    public void setYAxisShowLabels( boolean axisShowLabels ) {
        settings.put( DisplayerSettingId.YAXIS_SHOWLABELS, Boolean.toString( axisShowLabels ) );
    }

    public int getYAxisLabelsAngle() {
        return Integer.parseInt( settings.get( DisplayerSettingId.YAXIS_LABELSANGLE ) , 10 );
    }

    public void setYAxisLabelsAngle( int axisLabelsAngle ) {
        settings.put( DisplayerSettingId.YAXIS_LABELSANGLE, Integer.toString( axisLabelsAngle ) );
    }

    public String getYAxisTitle() {
        return settings.get( DisplayerSettingId.YAXIS_TITLE );
    }

    public void setYAxisTitle( String axisTitle ) {
        settings.put( DisplayerSettingId.YAXIS_TITLE, axisTitle );
    }

    public long getMeterStart() {
        return Long.parseLong( settings.get( DisplayerSettingId.METER_START ), 10 );
    }

    public void setMeterStart( long meterStart ) {
        settings.put( DisplayerSettingId.METER_START, Long.toString( meterStart ) );
    }

    public long getMeterWarning() {
        return Long.parseLong( settings.get( DisplayerSettingId.METER_WARNING ) , 10 );
    }

    public void setMeterWarning( long meterWarning ) {
        settings.put( DisplayerSettingId.METER_WARNING, Long.toString( meterWarning ) );
    }

    public long getMeterCritical() {
        return Long.parseLong( settings.get( DisplayerSettingId.METER_CRITICAL ) , 10 );
    }

    public void setMeterCritical( long meterCritical ) {
        settings.put( DisplayerSettingId.METER_CRITICAL, Long.toString( meterCritical ) );
    }

    public long getMeterEnd() {
        return Long.parseLong( settings.get( DisplayerSettingId.METER_END ) , 10 );
    }

    public void setMeterEnd( long meterEnd ) {
        settings.put( DisplayerSettingId.METER_END, Long.toString( meterEnd ) );
    }

    public boolean isChart3D() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.CHART_3D ) );
    }

    public void setChart3D( boolean barchartThreeDimension ) {
        settings.put( DisplayerSettingId.CHART_3D, Boolean.toString( barchartThreeDimension ) );
    }

    public boolean isBarchartHorizontal() {
        return Boolean.parseBoolean( settings.get( DisplayerSettingId.BARCHART_HORIZONTAL ) );
    }

    public void setBarchartHorizontal( boolean barchartHorizontal ) {
        settings.put( DisplayerSettingId.BARCHART_HORIZONTAL, Boolean.toString( barchartHorizontal ) );
    }
}
