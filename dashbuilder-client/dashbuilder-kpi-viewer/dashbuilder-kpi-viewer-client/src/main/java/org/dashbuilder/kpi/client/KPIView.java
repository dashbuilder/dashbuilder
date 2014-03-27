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

import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import org.dashbuilder.client.displayer.DataDisplayerViewerLocator;
import org.dashbuilder.client.displayer.DataDisplayer;
import org.dashbuilder.client.displayer.DataDisplayerViewer;
import org.dashbuilder.client.kpi.KPI;

public class KPIView extends Composite implements KPIPresenter.View {

    @Inject
    protected DataDisplayerViewerLocator viewerLocator;

    public void init(KPI kpi) {
        FlowPanel container = new FlowPanel();
        try {
            DataDisplayer displayer = kpi.getDataDisplayer();
            DataDisplayerViewer viewer = viewerLocator.lookupViewer(displayer);
            container.add(viewer);
        } catch (Exception e) {
            Label messageLabel = new Label(e.getMessage());
            container.add(messageLabel);
        }
        initWidget(container);
    }
}