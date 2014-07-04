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
package org.dashbuilder.client;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.client.RendererLibLocator;
import org.dashbuilder.client.sales.SalesDataSetGenerator;
import org.dashbuilder.dataset.client.DataSetLookupClient;
import org.dashbuilder.renderer.google.client.GoogleRenderer;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.displayer.DataDisplayerType;
import org.dashbuilder.client.sales.SalesConstants;
import org.dashbuilder.dataset.DataSetLookupService;
import org.jboss.errai.common.client.api.Caller;

/**
 * This class holds the initialization logic for the Dashbuilder subsystem
 */
@ApplicationScoped
public class DashbuilderInitializer {

    @Inject DataSetManager dataSetManager;
    @Inject RendererLibLocator rendererLibLocator;
    @Inject DataSetLookupClient dataSetLookupClient;
    @Inject Caller<DataSetLookupService> dataSetLookupService;

    @Inject SalesDataSetGenerator salesDataSetGenerator;

    @PostConstruct
    public void init() {
        // Enable the data set lookup backend service so that the DataSetLookupClient is able to send requests
        // not only to the ClientDataSetManager but also to the remote DataSetLookupService.
        dataSetLookupClient.setLookupService(dataSetLookupService);

        // Enable the ability to push and handle on client data sets greater than 1 Mb
        dataSetLookupClient.setPushRemoteDataSetEnabled(true);
        dataSetLookupClient.setPushRemoteDataSetMaxSize(1024);

        // Set the default renderer lib for each displayer type.
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.BARCHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.PIECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.AREACHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.LINECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.BUBBLECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.METERCHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.MAP, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DataDisplayerType.TABLE, GoogleRenderer.UUID);

        // Generate the data set to be used by the Showcase Gallery and by the Sales sample dashboards.
        Date currentDate = new Date();
        DataSet salesDataSet = salesDataSetGenerator.generateDataSet(SalesConstants.SALES_OPPS, 30, currentDate.getYear()-1, currentDate.getYear()+3);
        dataSetManager.registerDataSet(salesDataSet);
    }
}
