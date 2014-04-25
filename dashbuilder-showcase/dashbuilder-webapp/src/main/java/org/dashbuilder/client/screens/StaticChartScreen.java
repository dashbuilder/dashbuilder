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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.dataset.ClientDataSetManager;
import org.dashbuilder.client.kpi.ClientKPIManager;
import org.dashbuilder.client.kpi.KPIViewer;
import org.dashbuilder.client.samples.gallery.GalleryData;
import org.dashbuilder.client.samples.gallery.GalleryDisplayers;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.model.date.Month;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.kpi.KPI;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnStartup;

import static org.dashbuilder.model.displayer.DataDisplayerType.LINECHART;

@ApplicationScoped
@WorkbenchScreen(identifier = "StaticChartScreen")
public class StaticChartScreen {

    public KPI createKPI() {
        return kpiManager.createKPI(
                "static_chart_sample",
                GalleryData.salesPerYear(),
                GalleryDisplayers.salesPerYear(LINECHART));
    }

    @Inject
    ClientKPIManager kpiManager;

    @Inject
    ClientDataSetManager dataSetManager;

    @Inject
    KPIViewer kpiViewer;

    @OnStartup
    public void init() {
        KPI kpi = createKPI();
        kpiViewer.draw(kpi);
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Static Chart";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return kpiViewer;
    }
}