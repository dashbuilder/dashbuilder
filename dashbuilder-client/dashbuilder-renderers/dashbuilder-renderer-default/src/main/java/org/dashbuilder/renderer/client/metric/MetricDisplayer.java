/**
 * Copyright (C) 2015 JBoss Inc
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.renderer.client.resources.i18n.MetricConstants;

public class MetricDisplayer extends AbstractMetricDisplayer {

    protected MetricView metricView = null;
    protected boolean filterOn = false;

    @Override
    protected Widget createVisualization() {
        metricView = new MetricView();
        metricView.applySettings(displayerSettings);
        updateVisualization();

        // Enable filtering
        if (displayerSettings.isFilterEnabled()) {
            metricView.addClickHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent clickEvent) {
                    updateFilter();
                }
            });
        }
        return metricView;
    }

    @Override
    protected void updateVisualization() {
        if (dataSet.getRowCount() == 0) {
            metricView.updateMetric(MetricConstants.INSTANCE.metricDisplayer_noDataAvailable());
        } else {
            Number value = (Number) dataSet.getValueAt(0, 0);
            String valueStr = super.formatValue(value, dataSet.getColumnByIndex(0));
            metricView.updateMetric(valueStr);
        }
    }

    public boolean isFilterOn() {
        return filterOn;
    }

    protected void updateFilter() {
        if (filterOn) {
            filterReset();
        } else {
            filterApply();
        }
    }

    protected DataSetFilter fetchFilter() {
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

    public void applySettings(DisplayerSettings displayerSettings) {
        metricView.applySettings(displayerSettings);
    }

    public void filterApply() {
        filterOn = true;
        DisplayerSettings clone = displayerSettings.cloneInstance();
        clone.setChartBackgroundColor("DDDDDD");
        metricView.applySettings(clone);

        DataSetFilter filter = fetchFilter();
        super.filterApply(filter);
    }

    @Override
    public void filterReset() {
        filterOn = false;
        metricView.applySettings(displayerSettings);

        DataSetFilter filter = fetchFilter();
        if (filter != null) {
            super.filterReset();
        }
    }
}
