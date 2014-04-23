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
package org.dashbuilder.client.google;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.displayer.DataDisplayer;

public abstract class GoogleChartViewer extends DataDisplayerViewer {

    @Inject protected GoogleRenderer googleRenderer;
    protected boolean isApiReady = false;
    protected FlowPanel panel = new FlowPanel();

    @PostConstruct
    public void init() {
        initWidget(panel);
        googleRenderer.registerChart(this);
    }

    public boolean isDisplayReady() {
        return isApiReady && dataSet != null && dataDisplayer != null;
    }

    public void setDataSet(DataSet dataSet) {
        super.setDataSet(dataSet);
        if (isDisplayReady()) {
            drawChart();
        }
    }

    public void setDataDisplayer(DataDisplayer dataDisplayer) {
        super.setDataDisplayer(dataDisplayer);
        if (isDisplayReady()) {
            drawChart();
        }
    }

    public void onApiReady() {
        isApiReady = true;
        if (isDisplayReady()) {
            drawChart();
        }
    }

    public void drawChart() {
        Widget w = createChart();
        panel.add(w);
    }

    public abstract Widget createChart();
    public abstract String getPackage();

    public AbstractDataTable.ColumnType getColumnType(DataColumn dataColumn) {
        ColumnType type = dataColumn.getColumnType();
        if (ColumnType.LABEL.equals(type)) return AbstractDataTable.ColumnType.STRING;
        if (ColumnType.NUMBER.equals(type)) return AbstractDataTable.ColumnType.NUMBER;
        if (ColumnType.DATE.equals(type)) return AbstractDataTable.ColumnType.DATETIME;
        return AbstractDataTable.ColumnType.STRING;
    }
}
