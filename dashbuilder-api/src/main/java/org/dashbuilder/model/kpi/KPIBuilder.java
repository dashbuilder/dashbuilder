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
 * It allows for the building of KPI instances in a friendly manner.
 *
 * <pre>
 KPI kpi = new KPIBuilder()
 .dataset("target-dataset-uuid")
 .group("product")
 .function("amount", ScalarFunctionType.SUM)
 .title("By Product")
 .type(DataDisplayerType.BARCHART)
 .column("Product")
 .column("Total amount")
 .build();
 </pre>

 */
@Portable
public class KPIBuilder {

    protected DataSetLookupBuilder lookupBuilder = new DataSetLookupBuilder();
    protected DataDisplayerBuilder displayerBuilder = new DataDisplayerBuilder();
    protected DataDisplayer dataDisplayer;
    protected DataSetRef dataSetRef;
    protected KPIImpl kpi = new KPIImpl();

    public KPIBuilder uuid(String uuid) {
        kpi.setUUID(uuid);
        return this;
    }

    public KPIBuilder dataset(DataSetRef dataSetRef) {
        this.dataSetRef = dataSetRef;
        return this;
    }

    public KPIBuilder displayer(DataDisplayer dataDisplayer) {
        this.dataDisplayer = dataDisplayer;
        return this;
    }

    public KPI build() {
        if (dataSetRef != null) kpi.setDataSetRef(dataSetRef);
        else kpi.setDataSetRef(lookupBuilder.build());

        if (dataDisplayer != null) kpi.setDataDisplayer(dataDisplayer);
        else kpi.setDataDisplayer(displayerBuilder.build());

        return kpi;
    }

    // DataSetLookup section

    public KPIBuilder dataset(String uuid) {
        lookupBuilder.uuid(uuid);
        return this;
    }

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

    public KPIBuilder group(String columnId, String newColumnId, GroupStrategy strategy) {
        lookupBuilder.group(columnId, newColumnId, strategy);
        return this;
    }

    public KPIBuilder group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize) {
        lookupBuilder.group(columnId, newColumnId, strategy, maxIntervals, intervalSize);
        return this;
    }

    public KPIBuilder fixed(DateIntervalType type) {
        lookupBuilder.fixed(type);
        return this;
    }

    public KPIBuilder fixed(DateIntervalType type, boolean ascending) {
        lookupBuilder.fixed(type, ascending);
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

    public KPIBuilder function(String columnId, ScalarFunctionType function) {
        lookupBuilder.function(columnId, function);
        return this;
    }

    public KPIBuilder function(String columnId, String newColumnId, ScalarFunctionType function) {
        lookupBuilder.function(columnId, newColumnId, function);
        return this;
    }

    public KPIBuilder sort(String columnId, String order) {
        lookupBuilder.sort(columnId, order);
        return this;
    }

    // DataDisplayer section

    public KPIBuilder title(String title) {
        displayerBuilder.title(title);
        return this;
    }

    public KPIBuilder type(DataDisplayerType type) {
        displayerBuilder.type(type);
        return this;        
    }

    public KPIBuilder width(int width) {
        displayerBuilder.width(width);
        return this;
    }

    public KPIBuilder height(int height) {
        displayerBuilder.height(height);
        return this;
    }

    public KPIBuilder type(String type) {
        displayerBuilder.type(type);
        return this;
    }

    public KPIBuilder renderer(String renderer) {
        displayerBuilder.renderer(renderer);
        return this;
    }

    public KPIBuilder renderer(DataDisplayerRenderer renderer) {
        displayerBuilder.renderer(renderer);
        return this;
    }

    public KPIBuilder column(String displayName) {
        displayerBuilder.column(displayName);
        return this;
    }

    public KPIBuilder column(String columnId, String displayName) {
        displayerBuilder.column(columnId, displayName);
        return this;
    }

    public KPIBuilder meter(long start, long warning, long critical, long end) {
        displayerBuilder.meter(start, warning, critical, end);
        return this;
    }
}
