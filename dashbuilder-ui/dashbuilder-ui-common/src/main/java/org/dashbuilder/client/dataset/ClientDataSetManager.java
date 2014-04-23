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

import javax.inject.Inject;

import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.model.dataset.DataSetRef;
import org.dashbuilder.service.DataSetService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

/**
 * Main client interface for dealing with data sets.
 */
public class ClientDataSetManager {

    @Inject
    private Caller<DataSetService> dataSetService;

    public DataSet createDataSet() {
        return new DataSetImpl();
    }

    /**
     * Get the target data set instance by processing the specified data set reference.
     * @param ref The data set reference.
     */
    public void processRef(DataSetRef ref, DataSetReadyCallback listener) {
        if (ref instanceof DataSet) {
            listener.callback((DataSet) ref);
        }
        if (ref instanceof DataSetLookup) {
            lookupDataSet((DataSetLookup) ref, listener);
        }
    }

    /**
     * Request the server to process the specified data set lookup request
     * @param request The data set lookup request
     */
    public void lookupDataSet(final DataSetLookup request, final DataSetReadyCallback listener) {
        dataSetService.call(
            new RemoteCallback<DataSet>() {
                public void callback(DataSet result) {
                    listener.callback(result);
                }
            }).lookupDataSet(request);
    }
}
