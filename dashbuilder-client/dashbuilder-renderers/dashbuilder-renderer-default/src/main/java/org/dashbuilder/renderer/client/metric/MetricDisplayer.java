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

import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.renderer.client.resources.i18n.MetricConstants;

public class MetricDisplayer extends AbstractMetricDisplayer {

    protected MetricView metricView = null;

    @Override
    protected Widget createMetricWidget() {
        metricView = new MetricView(displayerSettings);
        updateMetricWidget();
        return metricView;
    }

    @Override
    protected void updateMetricWidget() {
        if (dataSet.getRowCount() == 0) {
            metricView.updateMetric(MetricConstants.INSTANCE.metricDisplayer_noDataAvailable());
        } else {
            Number value = (Number) dataSet.getValueAt(0, 0);
            String valueStr = super.formatValue(value, dataSet.getColumnByIndex(0));
            metricView.updateMetric(valueStr);
        }
    }
}
