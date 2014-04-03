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

import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import org.dashbuilder.model.dataset.DataLookup;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.kpi.KPI;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.client.displayer.DataDisplayerViewerLocator;

public class KPIView extends Composite implements KPIPresenter.View {

    @Inject
    protected DataDisplayerViewerLocator viewerLocator;

    protected FlowPanel container = new FlowPanel();
    protected Label label = new Label();
    protected DataDisplayerViewer viewer;

    public void init(KPI kpi) {
        try {
            DataDisplayer displayer = kpi.getDataDisplayer();
            viewer = viewerLocator.lookupViewer(displayer);
            viewer.setDataDisplayer(kpi.getDataDisplayer());
        } catch (Exception e) {
            label.setText(e.getMessage());
        }
        container.add(label);
        initWidget(container);
    }

    public void onDataLookup(DataLookup lookup) {
        label.setText("Loading data ...");
        container.add(label);
    }

    public void onDataReady(DataSet dataSet) {
        container.remove(label);
        container.add(viewer);

        viewer.setDataSet(dataSet);
        viewer.onDataReady();
    }
}
