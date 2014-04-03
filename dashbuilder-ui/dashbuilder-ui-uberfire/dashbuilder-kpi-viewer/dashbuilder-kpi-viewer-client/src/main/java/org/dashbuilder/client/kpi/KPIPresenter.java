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
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.dataset.ClientDataSetManager;
import org.dashbuilder.event.DataSetReadyEvent;
import org.dashbuilder.model.dataset.DataLookup;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.kpi.KPI;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

@WorkbenchScreen(identifier = "KPIPresenter")
@Dependent
public class KPIPresenter {

    public interface View extends IsWidget {

        void init(KPI kpi);
        void onDataLookup(DataLookup request);
        void onDataReady(DataSet dataSet);
    }

    /** The KPI to display */
    private KPI kpi;

    /** The KPI locator */
    @Inject private KPILocator kpiLocator;

    /** The data set manager */
    @Inject private ClientDataSetManager dataSetManager;

    /** The KPI widget */
    @Inject private View view;

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest) {
        // Locate the KPI specified as a parameter.
        String kpiUid = placeRequest.getParameter( "kpi", "sample0" );
        this.kpi = kpiLocator.getKPI(kpiUid);
        view.init(kpi);

        // Issue a data set lookup request
        DataLookup dt = kpi.getDataLookup();
        dataSetManager.lookupDataSet(dt);

        // Put the view on data lookup mode.
        view.onDataLookup(dt);
    }

    /**
     * Called when the data set has been fetched.
     */
    public void onDataReady(@Observes DataSetReadyEvent event) {
        String uuidLookup = kpi.getDataLookup().getDataSetUUID();
        String uuidFetched = event.getDataSet().getUUID();
        if (uuidLookup.equals(uuidFetched)) {
            view.onDataReady(event.getDataSet());
        }
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return kpi.getDataDisplayer().getTitle();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }
}
