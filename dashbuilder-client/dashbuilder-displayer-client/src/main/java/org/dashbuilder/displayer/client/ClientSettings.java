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
package org.dashbuilder.displayer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.client.DataSetLookupClient;
import org.dashbuilder.dataset.DataSetLookupService;
import org.dashbuilder.displayer.DisplayerType;
import org.jboss.errai.common.client.api.Caller;

/**
 * This class provides some methods for defining the behaviour of the Dashbuilder client layer
 */
@ApplicationScoped
public class ClientSettings {

    @Inject RendererLibLocator rendererLibLocator;
    @Inject DataSetLookupClient dataSetLookupClient;
    @Inject Caller<DataSetLookupService> dataSetLookupService;

    /**
     * By default the Dashbuilder is configured to work on client mode only. We must turn on the backend layer to be
     * able to query any data set stored on the server.
     */
    public void turnOnBackend() {
        dataSetLookupClient.setLookupService(dataSetLookupService);
    }

    /**
     * Turns on the ability to push data sets from server. This is very useful when dealing with small size data
     * sets as the performance of any lookup request is much faster on client.
     */
    public void turnOnDataSetPush() {
        dataSetLookupClient.setPushRemoteDataSetEnabled( true );
    }

    /**
     * It's possible to have one or more renderer libs available per displayer type. If a displayer does not define
     * its renderer lib then the default one is taken. This method can be used to define the default renderers.
     *
     * @param displayerType The type of the displayer we want to configure.
     * @param rendererLib The UUID of the renderer library.
     */
    public void setDefaultRenderer(DisplayerType displayerType, String rendererLib) {
        rendererLibLocator.setDefaultRenderer( displayerType, rendererLib );
    }
}
