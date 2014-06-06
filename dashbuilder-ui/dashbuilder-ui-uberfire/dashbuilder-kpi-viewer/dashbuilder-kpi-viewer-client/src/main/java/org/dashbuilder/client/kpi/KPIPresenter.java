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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.model.kpi.KPI;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

@WorkbenchScreen(identifier = "KPIScreen")
@Dependent
public class KPIPresenter {

    /** The KPI to display */
    private KPI kpi;

    /** The KPI manager */
    @Inject private ClientKPIManager kpiManager;

    /** The KPI widget */
    @Inject private KPIViewer kpiViewer;

    /** The dashboard context */
    @Inject private DashboardContext dashboardContext;

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest) {
        // Locate the KPI specified as a parameter.
        String kpiUid = placeRequest.getParameter("kpi", "");
        this.kpi = kpiManager.getKPI(kpiUid);
        if (kpi == null) throw new IllegalArgumentException("KPI not found.");

        // Draw the KPI.
        kpiViewer.draw(kpi);

        // Add the KPI to the current dashboard context
        dashboardContext.addKPIViewer(kpiViewer);
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return kpi.getDataDisplayer().getTitle();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return kpiViewer;
    }
}
