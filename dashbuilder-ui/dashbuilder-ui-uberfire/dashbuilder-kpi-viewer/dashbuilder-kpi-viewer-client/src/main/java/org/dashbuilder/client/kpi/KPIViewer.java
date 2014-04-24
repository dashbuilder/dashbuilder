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
package org.dashbuilder.client.kpi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.dashbuilder.client.dataset.ClientDataSetManager;
import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.client.displayer.DataDisplayerViewerLocator;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.kpi.KPI;

@Dependent
public class KPIViewer extends Composite {

    @Inject ClientDataSetManager dataSetManager;

    @Inject DataDisplayerViewerLocator viewerLocator;

    SimplePanel container = new SimplePanel();
    Label label = new Label();

    @PostConstruct
    private void init() {
        displayMessage("Loading data ...");
        initWidget(container);
    }

    public void draw(KPI kpi) {
        try {
            DataDisplayer displayer = kpi.getDataDisplayer();
            displayMessage("Loading '" + displayer.getTitle() + "'...");
            final DataDisplayerViewer viewer = viewerLocator.lookupViewer(displayer);

            // Look up the data set
            dataSetManager.processRef(kpi.getDataSetRef(), new DataSetReadyCallback() {
                public void callback(DataSet dataSet) {
                    container.clear();
                    container.add(viewer);
                    viewer.setDataSet(dataSet);
                    viewer.draw();
                }
            });

        } catch (Exception e) {
            displayMessage(e.getMessage());
        }
    }

    private void displayMessage(String msg) {
        container.clear();
        container.add(label);
        label.setText(msg);
    }
}
