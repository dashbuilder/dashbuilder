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
package org.dashbuilder.client.dataset;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.service.DataSetLookupService;
import org.jboss.errai.common.client.api.Caller;

/**
 * This class enables the DataSetLookupService so that the DataSetLookupClient is able to send requests not only
 * to the ClientDataSetManager but also to the remote DataSetLookupService.
 */
@ApplicationScoped
public class DataSetLookupServiceActivator {

    @Inject DataSetLookupClient dataSetLookupClient;
    @Inject Caller<DataSetLookupService> dataSetLookupService;

    @PostConstruct
    public void init() {
        // Enable the data set lookup backend service
        dataSetLookupClient.setLookupService(dataSetLookupService);
    }
}
