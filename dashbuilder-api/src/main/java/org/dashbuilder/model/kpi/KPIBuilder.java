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
package org.dashbuilder.model.kpi;

import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.GroupStrategy;
import org.dashbuilder.model.dataset.group.ScalarFunctionType;
import org.dashbuilder.model.date.DayOfWeek;
import org.dashbuilder.model.date.Month;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.displayer.DataDisplayerRenderer;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.dashbuilder.model.kpi.impl.KPIImpl;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Base class for building KPI instances in a friendly manner.
 */
public abstract class KPIBuilder<T extends KPIBuilder<?>> {

    protected DataSetLookupBuilder lookupBuilder = new DataSetLookupBuilder();
    protected DataDisplayerBuilder displayerBuilder = createDisplayerBuilder();
    protected DataDisplayer dataDisplayer;
    protected DataSetRef dataSetRef;
    protected KPIImpl kpi = new KPIImpl();

    protected abstract DataDisplayerBuilder createDisplayerBuilder();

    public T uuid(String uuid) {
        kpi.setUUID(uuid);
        return (T) this;
    }

    public T dataset(DataSetRef dataSetRef) {
        this.dataSetRef = dataSetRef;
        return (T) this;
    }

    public T displayer(DataDisplayer dataDisplayer) {
        this.dataDisplayer = dataDisplayer;
        return (T) this;
    }

    public KPI build() {
        if (dataSetRef != null) kpi.setDataSetRef(dataSetRef);
        else kpi.setDataSetRef(lookupBuilder.build());

        if (dataDisplayer != null) kpi.setDataDisplayer(dataDisplayer);
        else kpi.setDataDisplayer(displayerBuilder.build());

        return kpi;
    }

    // DataSetLookup section

    public T dataset(String uuid) {
        lookupBuilder.uuid(uuid);
        return (T) this;
    }

    public T rowOffset(int offset) {
        lookupBuilder.rowOffset(offset);
        return (T) this;
    }

    public T rowNumber(int rows) {
        lookupBuilder.rowNumber(rows);
        return (T) this;
    }

    public T group(String columnId) {
        lookupBuilder.group(columnId);
        return (T) this;
    }

    /**
     * Set the column we want the target data set to be grouped for.
     */
    public T group(String columnId, String newColumnId) {
        lookupBuilder.group(columnId, newColumnId);
        return (T) this;
    }

    public T group(String columnId, DateIntervalType type) {
        lookupBuilder.group(columnId, type);
        return (T) this;
    }

    public T group(String columnId, int maxIntervals, DateIntervalType type) {
        lookupBuilder.group(columnId, maxIntervals, type);
        return (T) this;
    }

    public T group(String columnId, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, maxIntervals, intervalSize);
        return (T) this;
    }

    public T group(String columnId, String newColumnId, GroupStrategy strategy) {
        lookupBuilder.group(columnId, newColumnId, strategy);
        return (T) this;
    }

    public T group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, newColumnId, strategy, maxIntervals, intervalSize);
        return (T) this;
    }

    public T fixed(DateIntervalType type) {
        lookupBuilder.fixed(type);
        return (T) this;
    }

    public T fixed(DateIntervalType type, boolean ascending) {
        lookupBuilder.fixed(type, ascending);
        return (T) this;
    }

    public T firstDay(DayOfWeek dayOfWeek) {
        lookupBuilder.firstDay(dayOfWeek);
        return (T) this;
    }

    public T firstMonth(Month month) {
        lookupBuilder.firstMonth(month);
        return (T) this;
    }

    public T distinct(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.DISTICNT);
    }

    public T distinct(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.DISTICNT);
    }

    public T count(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.COUNT);
    }

    public T count(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.COUNT);
    }

    public T min(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.MIN);
    }

    public T min(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.MIN);
    }

    public T max(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.MAX);
    }

    public T max(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.MAX);
    }

    public T avg(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.AVERAGE);
    }

    public T avg(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.AVERAGE);
    }

    public T sum(String columnId) {
        return function(columnId, columnId, ScalarFunctionType.SUM);
    }

    public T sum(String columnId, String newColumnId) {
        return function(columnId, newColumnId, ScalarFunctionType.SUM);
    }

    public T function(String columnId, ScalarFunctionType function) {
        lookupBuilder.function(columnId, function);
        return (T) this;
    }

    public T function(String columnId, String newColumnId, ScalarFunctionType function) {
        lookupBuilder.function(columnId, newColumnId, function);
        return (T) this;
    }

    public T sort(String columnId, String order) {
        lookupBuilder.sort(columnId, order);
        return (T) this;
    }

    // DataDisplayer section

    public T title(String title) {
        displayerBuilder.title(title);
        return (T) this;
    }

    public T type(DataDisplayerType type) {
        displayerBuilder.type(type);
        return (T) this;        
    }

    public T type(String type) {
        displayerBuilder.type(type);
        return (T) this;
    }

    public T renderer(String renderer) {
        displayerBuilder.renderer(renderer);
        return (T) this;
    }

    public T renderer(DataDisplayerRenderer renderer) {
        displayerBuilder.renderer(renderer);
        return (T) this;
    }

    public T column(String displayName) {
        displayerBuilder.column(displayName);
        return (T) this;
    }

    public T column(String columnId, String displayName) {
        displayerBuilder.column(columnId, displayName);
        return (T) this;
    }
}
