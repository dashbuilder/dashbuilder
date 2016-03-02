/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.renderer.client.metric;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractDisplayer;

public class MetricDisplayer extends AbstractDisplayer<MetricDisplayer.View> {

    public interface View extends AbstractDisplayer.View<MetricDisplayer> {

        void showTitle(String title);

        void setWidth(int width);

        void setHeight(int height);

        void setMarginTop(int marginTop);

        void setMarginBottom(int marginBottom);

        void setMarginRight(int marginRight);

        void setMarginLeft(int marginLeft);

        void setBgColor(String color);

        void setFilterEnabled(boolean enabled);

        void setFilterActive(boolean active);

        void setValue(String value);

        void nodata();

        String getColumnsTitle();
    }

    protected View view;
    protected boolean filterOn = false;

    public MetricDisplayer() {
        this(new MetricView());
    }

    public MetricDisplayer(View view) {
        this.view = view;
        this.view.init(this);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupAllowed(false)
                .setMaxColumns(1)
                .setMinColumns(1)
                .setFunctionRequired(true)
                .setExtraColumnsAllowed(false)
                .setColumnsTitle(view.getColumnsTitle())
                .setColumnTypes(new ColumnType[] {
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute(DisplayerAttributeDef.TYPE)
                .supportsAttribute(DisplayerAttributeDef.RENDERER)
                .supportsAttribute(DisplayerAttributeGroupDef.COLUMNS_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.FILTER_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.REFRESH_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.GENERAL_GROUP)
                .supportsAttribute(DisplayerAttributeDef.CHART_WIDTH)
                .supportsAttribute(DisplayerAttributeDef.CHART_HEIGHT)
                .supportsAttribute(DisplayerAttributeDef.CHART_BGCOLOR)
                .supportsAttribute(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP);
    }

    @Override
    protected void createVisualization() {
        if (displayerSettings.isTitleVisible()) {
            view.showTitle(displayerSettings.getTitle());
        }
        view.setWidth(displayerSettings.getChartWidth());
        view.setHeight(displayerSettings.getChartHeight());
        view.setMarginTop(displayerSettings.getChartMarginTop());
        view.setMarginBottom(displayerSettings.getChartMarginBottom());
        view.setMarginRight(displayerSettings.getChartMarginRight());
        view.setMarginLeft(displayerSettings.getChartMarginLeft());
        view.setFilterEnabled(displayerSettings.isFilterEnabled() && fetchFilter() != null);
        if (!StringUtils.isBlank(displayerSettings.getChartBackgroundColor())) {
            view.setBgColor("#" + displayerSettings.getChartBackgroundColor());
        }
        updateVisualization();
    }

    @Override
    protected void updateVisualization() {
        if (dataSet.getRowCount() == 0) {
            view.nodata();
        } else {
            String valueStr = super.formatValue(0, 0);
            view.setValue(valueStr);
        }
    }

    public boolean isFilterOn() {
        return filterOn;
    }

    public void updateFilter() {
        if (filterOn) {
            filterReset();
        } else {
            if (displayerSettings.isFilterEnabled()) {
                filterApply();
            }
        }
    }

    public DataSetFilter fetchFilter() {
        if (displayerSettings.getDataSetLookup() == null) {
            return null;
        }
        List<DataSetFilter> filterOps = displayerSettings.getDataSetLookup().getOperationList(DataSetFilter.class);
        if (filterOps == null || filterOps.isEmpty()) {
            return null;
        }
        DataSetFilter filter = new DataSetFilter();
        for (DataSetFilter filterOp : filterOps) {
            filter.getColumnFilterList().addAll(filterOp.getColumnFilterList());
        }
        return filter;
    }

    public void filterApply() {
        DataSetFilter filter = fetchFilter();
        if (displayerSettings.isFilterEnabled() && filter != null) {
            filterOn = true;
            view.setFilterActive(true);
            super.filterApply(filter);
        }
    }

    @Override
    public void filterReset() {
        DataSetFilter filter = fetchFilter();
        if (filterOn && filter != null) {
            filterOn = false;
            view.setFilterActive(false);
            super.filterReset();
        }
    }
}
