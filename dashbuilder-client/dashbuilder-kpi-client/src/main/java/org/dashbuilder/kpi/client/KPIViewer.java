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
package org.dashbuilder.kpi.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.dashbuilder.displayer.client.DataViewer;
import org.dashbuilder.displayer.client.DataViewerHelper;
import org.dashbuilder.dataset.DataSetRef;
import org.dashbuilder.displayer.DataDisplayer;
import org.dashbuilder.kpi.KPI;

public class KPIViewer extends Composite {

    protected SimplePanel container = new SimplePanel();
    protected Label label = new Label();
    protected DataViewer dataViewer;
    protected KPI kpi;

    public KPIViewer() {
        initWidget(container);
    }

    public KPIViewer(KPI kpi) {
        this();
        setKpi(kpi);
    }

    public KPI getKpi() {
        return kpi;
    }

    public void setKpi(KPI kpi) {
        this.kpi = kpi;

        // Locate the DataViewer widget to display the KPI
        DataDisplayer dataDisplayer = kpi.getDataDisplayer();
        DataSetRef dataSetRef = kpi.getDataSetRef();
        dataViewer = DataViewerHelper.lookup(dataSetRef, dataDisplayer);
    }

    public DataViewer getDataViewer() {
        return dataViewer;
    }

    public KPIViewer draw() {
        try {
            container.clear();
            container.add(dataViewer);

            DataViewerHelper.draw(dataViewer);
        } catch (Exception e) {
            displayMessage(e.getMessage());
        }
        return this;
    }

    private void displayMessage(String msg) {
        container.clear();
        container.add(label);
        label.setText(msg);
    }
}
