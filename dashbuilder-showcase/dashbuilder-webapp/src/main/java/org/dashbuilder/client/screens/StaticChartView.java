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
package org.dashbuilder.client.screens;

import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.client.displayer.DataDisplayerViewerLocator;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;

import static org.dashbuilder.model.displayer.DataDisplayerRenderer.*;
import static org.dashbuilder.model.displayer.DataDisplayerType.*;

public class StaticChartView extends Composite implements StaticChartScreen.View {

    @Inject
    protected DataDisplayerViewerLocator viewerLocator;

    protected FlowPanel container = new FlowPanel();
    private DataDisplayerViewer viewer;

    public void onDataReady(DataSet dataSet) {
        try {
            DataDisplayer displayer = createDisplayer();
            viewer = viewerLocator.lookupViewer(displayer);
            viewer.setDataSet(dataSet);
            viewer.setDataDisplayer(displayer);
            container.add(viewer);
        } catch (Exception e) {
            Label label = new Label(e.getMessage());
            container.add(label);
        }
        initWidget(container);
    }

    public DataDisplayer createDisplayer() {
        return new DataDisplayerBuilder()
                .title("Sales Evolution Per Year")
                .type(LINECHART)
                .renderer(GOOGLE)
                .x("month", "Pipeline")
                .y("2012", "Sales in 2012")
                .y("2013", "Sales in 2013")
                .y("2014", "Sales in 2014")
                .build();
    }
}