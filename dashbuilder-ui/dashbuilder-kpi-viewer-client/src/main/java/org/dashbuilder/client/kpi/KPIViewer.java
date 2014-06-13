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
import org.dashbuilder.client.displayer.DataViewer;
import org.dashbuilder.client.displayer.DataViewerLocator;
import org.dashbuilder.client.displayer.DataSetHandler;
import org.dashbuilder.client.displayer.DataSetHandlerLocator;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.kpi.KPI;

@Dependent
public class KPIViewer extends Composite {

    @Inject DataViewerLocator viewerLocator;
    @Inject DataSetHandlerLocator handlerLocator;

    SimplePanel container = new SimplePanel();
    Label label = new Label();
    DataViewer dataViewer;
    DataSetHandler dataHandler;

    @PostConstruct
    private void init() {
        initWidget(container);
    }

    public DataViewer getDataViewer() {
        return dataViewer;
    }

    public DataSetHandler getDataHandler() {
        return dataHandler;
    }

    public void draw(KPI kpi) {
        try {
            // Locate the low level UI components
            DataDisplayer dataDisplayer = kpi.getDataDisplayer();
            DataSetRef dataSetRef = kpi.getDataSetRef();
            dataViewer = viewerLocator.lookupViewer(dataDisplayer);
            dataHandler = handlerLocator.lookupHandler(dataSetRef);

            // Init the DataViewer
            dataViewer.setDataDisplayer(dataDisplayer);
            dataViewer.setDataSetHandler(dataHandler);

            // Draw
            container.clear();
            container.add(dataViewer);
            dataViewer.draw();
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
