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
 * WITHOUKPIBuilder WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.kpi.impl;

import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupBuilder;
import org.dashbuilder.dataset.DataSetRef;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.impl.DataSetLookupBuilderImpl;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.displayer.BarChartSettingsBuilder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsBuilder;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.MeterChartSettingsBuilder;
import org.dashbuilder.displayer.TableDisplayerSettingsBuilder;
import org.dashbuilder.displayer.impl.AbstractChartSettingsBuilder;
import org.dashbuilder.kpi.KPI;
import org.dashbuilder.kpi.KPIBuilder;

/**
 * Base class for building KPI instances in a friendly manner.
 */
public class KPIBuilderImpl implements KPIBuilder {

    protected DataSetLookupBuilder lookupBuilder = new DataSetLookupBuilderImpl();
    protected DisplayerSettingsBuilder displayerSettingsBuilder = null;
    protected DisplayerSettings displayerSettings;
    protected DataSetRef dataSetRef;
    protected KPIImpl kpi = new KPIImpl();

    public KPIBuilderImpl(DisplayerType displayerType) {
        if ( DisplayerType.BARCHART.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newBarChartSettings();
        } else if ( DisplayerType.PIECHART.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newPieChartSettings();
        } else if ( DisplayerType.LINECHART.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newLineChartSettings();
        } else if ( DisplayerType.AREACHART.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newAreaChartSettings();
        } else if ( DisplayerType.BUBBLECHART.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newBubbleChartSettings();
        } else if ( DisplayerType.METERCHART.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newMeterChartSettings();
        } else if ( DisplayerType.MAP.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newMapChartSettings();
        } else if ( DisplayerType.TABLE.equals(displayerType)) {
            displayerSettingsBuilder = DisplayerSettingsFactory.newTableSettings();
        } else {
            throw new IllegalStateException("Missing displayer type: " + displayerType);
        }
    }

    public KPIBuilder uuid(String uuid) {
        kpi.setUUID(uuid);
        return this;
    }

    public KPIBuilder dataset(DataSetRef dataSetRef) {
        this.dataSetRef = dataSetRef;
        return this;
    }

    public KPIBuilder dataset(String uuid) {
        lookupBuilder.dataset(uuid);
        return this;
    }

    public KPIBuilder displayer(DisplayerSettings displayerSettings ) {
        this.displayerSettings = displayerSettings;
        return this;
    }

    public DataSetLookup buildLookup() {
        return lookupBuilder.buildLookup();
    }

    public DisplayerSettings buildDisplayerSettings() {
        return displayerSettingsBuilder.buildDisplayerSettings();
    }

    public KPI buildKPI() {
        if (dataSetRef != null) kpi.setDataSetRef(dataSetRef);
        else kpi.setDataSetRef(buildLookup());

        if ( displayerSettings != null) kpi.setDisplayerSettings( displayerSettings );
        else kpi.setDisplayerSettings( buildDisplayerSettings() );

        return kpi;
    }

    // DataSetLookup section

    public KPIBuilder rowOffset(int offset) {
        lookupBuilder.rowOffset(offset);
        return this;
    }

    public KPIBuilder rowNumber(int rows) {
        lookupBuilder.rowNumber(rows);
        return this;
    }

    public KPIBuilder group(String columnId) {
        lookupBuilder.group(columnId);
        return this;
    }

    /**
     * Set the column we want the target data set to be grouped for.
     */
    public KPIBuilder group(String columnId, String newColumnId) {
        lookupBuilder.group(columnId, newColumnId);
        return this;
    }

    public KPIBuilder group(String columnId, DateIntervalType type) {
        lookupBuilder.group(columnId, type);
        return this;
    }

    public KPIBuilder group(String columnId, int maxIntervals, DateIntervalType type) {
        lookupBuilder.group(columnId, maxIntervals, type);
        return this;
    }

    public KPIBuilder group(String columnId, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, String newColumnId, DateIntervalType intervalSize) {
        lookupBuilder.group(columnId, newColumnId, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, String newColumnId, GroupStrategy strategy) {
        lookupBuilder.group(columnId, newColumnId, strategy);
        return this;
    }

    public KPIBuilder group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, newColumnId, strategy, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, GroupStrategy strategy) {
        lookupBuilder.group(columnId, strategy);
        return this;
    }

    public KPIBuilder group(String columnId, String strategy, int maxIntervals, DateIntervalType intervalSize) {
        lookupBuilder.group(columnId, strategy, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, String strategy, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, strategy, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, GroupStrategy strategy, String intervalSize) {
        lookupBuilder.group(columnId, strategy, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, GroupStrategy strategy, DateIntervalType intervalSize) {
        lookupBuilder.group(columnId, strategy, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, strategy, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, String newColumnId, String strategy) {
        lookupBuilder.group(columnId, newColumnId, strategy);
        return this;
    }

    public KPIBuilder group(String columnId, String newColumnId, String strategy, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, newColumnId, strategy, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, DateIntervalType intervalSize) {
        lookupBuilder.group(columnId, newColumnId, strategy, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder select(String... intervalNames) {
        lookupBuilder.select(intervalNames);
        return this;
    }

    public KPIBuilder asc() {
        lookupBuilder.asc();
        return this;
    }

    public KPIBuilder desc() {
        lookupBuilder.desc();
        return this;
    }

    public KPIBuilder fixed(DateIntervalType type) {
        lookupBuilder.fixed(type);
        return this;
    }

    public KPIBuilder firstDay(DayOfWeek dayOfWeek) {
        lookupBuilder.firstDay(dayOfWeek);
        return this;
    }

    public KPIBuilder firstMonth(Month month) {
        lookupBuilder.firstMonth(month);
        return this;
    }

    public KPIBuilder distinct(String columnId) {
        lookupBuilder.distinct(columnId);
        return this;
    }

    public KPIBuilder distinct(String columnId, String newColumnId) {
        lookupBuilder.distinct(columnId, newColumnId);
        return this;
    }

    public KPIBuilder count(String newColumnId) {
        lookupBuilder.count(newColumnId);
        return this;
    }

    public KPIBuilder min(String columnId) {
        lookupBuilder.min(columnId);
        return this;
    }

    public KPIBuilder min(String columnId, String newColumnId) {
        lookupBuilder.min(columnId, newColumnId);
        return this;
    }

    public KPIBuilder max(String columnId) {
        lookupBuilder.max(columnId);
        return this;
    }

    public KPIBuilder max(String columnId, String newColumnId) {
        lookupBuilder.max(columnId, newColumnId);
        return this;
    }

    public KPIBuilder avg(String columnId) {
        lookupBuilder.avg(columnId);
        return this;
    }

    public KPIBuilder avg(String columnId, String newColumnId) {
        lookupBuilder.avg(columnId, newColumnId);
        return this;
    }

    public KPIBuilder sum(String columnId) {
        lookupBuilder.sum(columnId);
        return this;
    }

    public KPIBuilder sum(String columnId, String newColumnId) {
        lookupBuilder.sum(columnId, newColumnId);
        return this;
    }

    public KPIBuilder filter(ColumnFilter... filters) {
        return filter(null, filters);
    }

    public KPIBuilder filter(String columnId, ColumnFilter... filters) {
        lookupBuilder.filter(columnId, filters);
        return this;
    }

    public KPIBuilder sort(String columnId, String order) {
        lookupBuilder.sort(columnId, order);
        return this;
    }

    public KPIBuilder sort(String columnId, SortOrder order) {
        lookupBuilder.sort(columnId, order);
        return this;
    }

    // DisplayerSettings section

    public KPIBuilder title(String title) {
        displayerSettingsBuilder.title(title);
        return this;
    }

    public KPIBuilder titleVisible(boolean visible) {
        displayerSettingsBuilder.titleVisible(visible);
        return this;
    }

    public KPIBuilder renderer(String renderer) {
        displayerSettingsBuilder.renderer(renderer);
        return this;
    }

    public KPIBuilder column(String displayName) {
        displayerSettingsBuilder.column(displayName);
        return this;
    }

    public KPIBuilder column(String columnId, String displayName) {
        displayerSettingsBuilder.column(columnId, displayName);
        return this;
    }

    public KPIBuilder filterOn(boolean applySelf, boolean notifyOthers, boolean receiveFromOthers) {
        displayerSettingsBuilder.filterOn(applySelf, notifyOthers, receiveFromOthers);
        return this;
    }

    public KPIBuilder filterOff() {
        displayerSettingsBuilder.filterOff();
        return this;
    }

    // Generic ChartSettingsBuilder

    public KPIBuilder width(int width) {
        ((AbstractChartSettingsBuilder ) displayerSettingsBuilder).width(width);
        return this;
    }

    public KPIBuilder height(int height) {
        ((AbstractChartSettingsBuilder ) displayerSettingsBuilder).height(height);
        return this;
    }

    public KPIBuilder margins(int top, int bottom, int left, int right) {
        ((AbstractChartSettingsBuilder ) displayerSettingsBuilder).margins(top, bottom, left, right);
        return this;
    }

    // BarChartSettingsBuilder

    public KPIBuilder set3d(boolean d) {
        ((BarChartSettingsBuilder ) displayerSettingsBuilder).set3d(d);
        return this;
    }

    public KPIBuilder vertical() {
        ((BarChartSettingsBuilder ) displayerSettingsBuilder).vertical();
        return this;
    }

    public KPIBuilder horizontal() {
        ((BarChartSettingsBuilder ) displayerSettingsBuilder).horizontal();
        return this;
    }

    // MeterChartSettingsBuilder

    public KPIBuilder meter(long start, long warning, long critical, long end) {
        ((MeterChartSettingsBuilder ) displayerSettingsBuilder).meter(start, warning, critical, end);
        return this;
    }

    // TableDisplayerSettingsBuilder

    public KPIBuilder tablePageSize(int pageSize) {
        ((TableDisplayerSettingsBuilder ) displayerSettingsBuilder).tablePageSize(pageSize);
        return this;
    }

    public KPIBuilder tableOrderEnabled(boolean enabled) {
        ((TableDisplayerSettingsBuilder ) displayerSettingsBuilder).tableOrderEnabled(enabled);
        return this;
    }

    public KPIBuilder tableOrderDefault(String columnId, SortOrder order) {
        ((TableDisplayerSettingsBuilder ) displayerSettingsBuilder).tableOrderDefault(columnId, order);
        return this;
    }

    public KPIBuilder tableOrderDefault(String columnId, String order) {
        ((TableDisplayerSettingsBuilder ) displayerSettingsBuilder).tableOrderDefault(columnId, order);
        return this;
    }
}
