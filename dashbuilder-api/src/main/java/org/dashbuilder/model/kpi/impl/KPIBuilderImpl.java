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
package org.dashbuilder.model.kpi.impl;

import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.model.dataset.filter.ColumnFilter;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.GroupStrategy;
import org.dashbuilder.model.dataset.impl.DataSetLookupBuilderImpl;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.date.DayOfWeek;
import org.dashbuilder.model.date.Month;
import org.dashbuilder.model.displayer.BarChartBuilder;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.displayer.DisplayerFactory;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.dashbuilder.model.displayer.MeterChartBuilder;
import org.dashbuilder.model.displayer.TableDisplayerBuilder;
import org.dashbuilder.model.displayer.impl.AbstractChartBuilder;
import org.dashbuilder.model.kpi.KPI;
import org.dashbuilder.model.kpi.KPIBuilder;

/**
 * Base class for building KPI instances in a friendly manner.
 */
public class KPIBuilderImpl implements KPIBuilder {

    protected DataSetLookupBuilder lookupBuilder = new DataSetLookupBuilderImpl();
    protected DataDisplayerBuilder displayerBuilder = null;
    protected DataDisplayer dataDisplayer;
    protected DataSetRef dataSetRef;
    protected KPIImpl kpi = new KPIImpl();

    public KPIBuilderImpl(DataDisplayerType type) {
        if (DataDisplayerType.BARCHART.equals(type)) {
            displayerBuilder = DisplayerFactory.newBarChartDisplayer();
        } else if (DataDisplayerType.PIECHART.equals(type)) {
            displayerBuilder = DisplayerFactory.newPieChartDisplayer();
        } else if (DataDisplayerType.LINECHART.equals(type)) {
            displayerBuilder = DisplayerFactory.newLineChartDisplayer();
        } else if (DataDisplayerType.AREACHART.equals(type)) {
            displayerBuilder = DisplayerFactory.newAreaChartDisplayer();
        } else if (DataDisplayerType.METERCHART.equals(type)) {
            displayerBuilder = DisplayerFactory.newMeterChartDisplayer();
        } else if (DataDisplayerType.MAP.equals(type)) {
            displayerBuilder = DisplayerFactory.newMapChartDisplayer();
        } else if (DataDisplayerType.TABLE.equals(type)) {
            displayerBuilder = DisplayerFactory.newTableDisplayer();
        } else {
            throw new IllegalStateException("Missing data displayer type: " + type);
        }
        displayerBuilder.type(type);
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
        lookupBuilder.uuid(uuid);
        return this;
    }

    public KPIBuilder displayer(DataDisplayer dataDisplayer) {
        this.dataDisplayer = dataDisplayer;
        return this;
    }

    public DataSetLookup buildLookup() {
        return lookupBuilder.buildLookup();
    }

    public DataDisplayer buildDisplayer() {
        return displayerBuilder.buildDisplayer();
    }

    public KPI buildKPI() {
        if (dataSetRef != null) kpi.setDataSetRef(dataSetRef);
        else kpi.setDataSetRef(buildLookup());

        if (dataDisplayer != null) kpi.setDataDisplayer(dataDisplayer);
        else kpi.setDataDisplayer(buildDisplayer());

        return kpi;
    }

    // DataSetLookup section

    public KPIBuilder rowOffset(int offset) {
        lookupBuilder.rowOffset(offset);
        return this;
    }

    public KPIBuilder rowNumber(int rows) {
        lookupBuilder.rowNumber(rows);
        return  this;
    }

    public KPIBuilder group(String columnId) {
        lookupBuilder.group(columnId);
        return  this;
    }

    /**
     * Set the column we want the target data set to be grouped for.
     */
    public KPIBuilder group(String columnId, String newColumnId) {
        lookupBuilder.group(columnId, newColumnId);
        return  this;
    }

    public KPIBuilder group(String columnId, DateIntervalType type) {
        lookupBuilder.group(columnId, type);
        return  this;
    }

    public KPIBuilder group(String columnId, int maxIntervals, DateIntervalType type) {
        lookupBuilder.group(columnId, maxIntervals, type);
        return  this;
    }

    public KPIBuilder group(String columnId, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, maxIntervals, intervalSize);
        return  this;
    }

    public KPIBuilder group(String columnId, String newColumnId, GroupStrategy strategy) {
        lookupBuilder.group(columnId, newColumnId, strategy);
        return  this;
    }

    public KPIBuilder group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, newColumnId, strategy, maxIntervals, intervalSize);
        return  this;
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

    public KPIBuilder fixed(DateIntervalType type) {
        lookupBuilder.fixed(type);
        return  this;
    }

    public KPIBuilder fixed(DateIntervalType type, boolean ascending) {
        lookupBuilder.fixed(type, ascending);
        return  this;
    }

    public KPIBuilder firstDay(DayOfWeek dayOfWeek) {
        lookupBuilder.firstDay(dayOfWeek);
        return  this;
    }

    public KPIBuilder firstMonth(Month month) {
        lookupBuilder.firstMonth(month);
        return  this;
    }

    public KPIBuilder distinct(String columnId) {
        lookupBuilder.distinct(columnId);
        return  this;
    }

    public KPIBuilder distinct(String columnId, String newColumnId) {
        lookupBuilder.distinct(columnId, newColumnId);
        return  this;
    }

    public KPIBuilder count(String newColumnId) {
        lookupBuilder.count(newColumnId);
        return  this;
    }

    public KPIBuilder min(String columnId) {
        lookupBuilder.min(columnId);
        return  this;
    }

    public KPIBuilder min(String columnId, String newColumnId) {
        lookupBuilder.min(columnId, newColumnId);
        return  this;
    }

    public KPIBuilder max(String columnId) {
        lookupBuilder.max(columnId);
        return  this;
    }

    public KPIBuilder max(String columnId, String newColumnId) {
        lookupBuilder.max(columnId, newColumnId);
        return  this;
    }

    public KPIBuilder avg(String columnId) {
        lookupBuilder.avg(columnId);
        return  this;
    }

    public KPIBuilder avg(String columnId, String newColumnId) {
        lookupBuilder.avg(columnId, newColumnId);
        return  this;
    }

    public KPIBuilder sum(String columnId) {
        lookupBuilder.sum(columnId);
        return  this;
    }

    public KPIBuilder sum(String columnId, String newColumnId) {
        lookupBuilder.sum(columnId, newColumnId);
        return  this;
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
        return  this;
    }

    public KPIBuilder sort(String columnId, SortOrder order) {
        lookupBuilder.sort(columnId, order);
        return  this;
    }

    // DataDisplayer section

    public KPIBuilder title(String title) {
        displayerBuilder.title(title);
        return  this;
    }

    public KPIBuilder titleVisible(boolean visible) {
        displayerBuilder.titleVisible(visible);
        return  this;
    }

    public KPIBuilder type(DataDisplayerType type) {
        displayerBuilder.type(type);
        return  this;        
    }

    public KPIBuilder type(String type) {
        displayerBuilder.type(type);
        return  this;
    }

    public KPIBuilder renderer(String renderer) {
        displayerBuilder.renderer(renderer);
        return  this;
    }

    public KPIBuilder column(String displayName) {
        displayerBuilder.column(displayName);
        return  this;
    }

    public KPIBuilder column(String columnId, String displayName) {
        displayerBuilder.column(columnId, displayName);
        return  this;
    }

    // Generic ChartBuilder

    public KPIBuilder width(int width) {
        ((AbstractChartBuilder) displayerBuilder).width(width);
        return  this;
    }

    public KPIBuilder height(int height) {
        ((AbstractChartBuilder) displayerBuilder).height(height);
        return  this;
    }

    // BarChartBuilder

    public KPIBuilder set3d(boolean d) {
        ((BarChartBuilder) displayerBuilder).set3d(d);
        return  this;
    }

    public KPIBuilder vertical() {
        ((BarChartBuilder) displayerBuilder).vertical();
        return  this;
    }

    public KPIBuilder horizontal() {
        ((BarChartBuilder) displayerBuilder).horizontal();
        return  this;
    }

    // MeterChartBuilder

    public KPIBuilder meter(long start, long warning, long critical, long end) {
        ((MeterChartBuilder) displayerBuilder).meter(start, warning, critical, end);
        return  this;
    }

    // TableDisplayerBuilder

    public KPIBuilder tablePageSize(int pageSize) {
        ((TableDisplayerBuilder) displayerBuilder).tablePageSize(pageSize);
        return  this;
    }

    public KPIBuilder tableOrderEnabled(boolean enabled) {
        ((TableDisplayerBuilder) displayerBuilder).tableOrderEnabled(enabled);
        return  this;
    }

    public KPIBuilder tableOrderDefault(String columnId, SortOrder order) {
        ((TableDisplayerBuilder) displayerBuilder).tableOrderDefault(columnId, order);
        return  this;
    }

    public KPIBuilder tableOrderDefault(String columnId, String order) {
        ((TableDisplayerBuilder) displayerBuilder).tableOrderDefault(columnId, order);
        return  this;
    }
}
