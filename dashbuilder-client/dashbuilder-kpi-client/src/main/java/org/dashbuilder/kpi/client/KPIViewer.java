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
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DataViewerHelper;
import org.dashbuilder.dataset.DataSetRef;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.kpi.KPI;

public class KPIViewer extends Composite {

    protected SimplePanel container = new SimplePanel();
    protected Label label = new Label();
    protected Displayer displayer;
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

        // Locate the Displayer widget to display the KPI
        DisplayerSettings displayerSettings = kpi.getDisplayerSettings();
        DataSetRef dataSetRef = kpi.getDataSetRef();
        displayer = DataViewerHelper.lookup(dataSetRef, displayerSettings );
    }

    public Displayer getDisplayer() {
        return displayer;
    }

    public KPIViewer draw() {
        try {
            container.clear();
            container.add( displayer );

            DataViewerHelper.draw( displayer );
        } catch (Exception e) {
            displayMessage(e.getMessage());
        }
        return this;
    }

    public KPIViewer redraw() {
        try {
            container.clear();
            container.add( displayer );

            DataViewerHelper.redraw( displayer );
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
