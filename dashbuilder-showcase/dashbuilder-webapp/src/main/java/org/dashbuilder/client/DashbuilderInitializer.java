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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.client.RendererLibLocator;
import org.dashbuilder.dataset.client.DataSetLookupClient;
import org.dashbuilder.renderer.google.client.GoogleRenderer;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.dataset.DataSetLookupService;
import org.dashbuilder.renderer.selector.client.SelectorRenderer;
import org.jboss.errai.common.client.api.Caller;

/**
 * This class holds the initialization logic for the Dashbuilder subsystem
 */
@ApplicationScoped
public class DashbuilderInitializer {

    @Inject RendererLibLocator rendererLibLocator;
    @Inject DataSetLookupClient dataSetLookupClient;
    @Inject Caller<DataSetLookupService> dataSetLookupService;

    @PostConstruct
    public void init() {
        // Enable the data set lookup backend service so that the DataSetLookupClient is able to send requests
        // not only to the ClientDataSetManager but also to the remote DataSetLookupService.
        dataSetLookupClient.setLookupService(dataSetLookupService);

        // Set the default renderer lib for each displayer type.
        rendererLibLocator.setDefaultRenderer(DisplayerType.BARCHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.PIECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.AREACHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.LINECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.BUBBLECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.METERCHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.MAP, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.TABLE, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer(DisplayerType.SELECTOR, SelectorRenderer.UUID);
    }
}
