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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.service.DataSetLookupService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

/**
 * Proxy interface to the list of available DataSetManager implementations.
 * It hides to client widgets where the data sets are stored and how they are fetched and processed.
 */
@ApplicationScoped
public class DataSetLookupClient {

    @Inject
    private ClientDataSetManager clientDataSetManager;

    /**
     * The service caller used to lookup data sets from the backend.
     */
    private Caller<DataSetLookupService> dataSetLookupService = null;

    /**
     * The DataSetLookupService instance is disabled by default.
     * Those client modules interested in activate the data set backend should set an instance.
     */
    public void setLookupService(Caller<DataSetLookupService> dataSetLookupService) {
        this.dataSetLookupService = dataSetLookupService;
    }

    /**
     * Fetch the metadata instance for the specified data set.
     *
     * @param request The data set lookup request
     * @throws Exception It there is an unexpected error trying to execute the lookup request.
     */
    public void fetchMetadata(final DataSetLookup request, final DataSetMetadataCallback listener) throws Exception {

        DataSetMetadata metadata = clientDataSetManager.lookupDataSetMetadata(request);
        if (metadata != null) {
            listener.callback(metadata);
        }
        else if (dataSetLookupService != null) {
            dataSetLookupService.call(
                new RemoteCallback<DataSetMetadata>() {
                public void callback(DataSetMetadata result) {
                    if (result == null) listener.notFound();
                    else listener.callback(result);
                }}).lookupDataSetMetadata(request);
        }
        else {
            listener.notFound();
        }
    }

    /**
     * Process the specified data set lookup request.
     *
     * @param request The data set lookup request
     * @throws Exception It there is an unexpected error trying to execute the lookup request.
     */
    public void lookupDataSet(final DataSetLookup request, final DataSetReadyCallback listener) throws Exception {

        if (clientDataSetManager.getDataSet(request.getDataSetUUID()) != null) {
            DataSet dataSet = clientDataSetManager.lookupDataSet(request);
            listener.callback(dataSet);
        }
        else if (dataSetLookupService != null) {
            // TODO: move from backend to client small data sets?
            dataSetLookupService.call(
                new RemoteCallback<DataSet>() {
                public void callback(DataSet result) {
                    if (result == null) listener.notFound();
                    else listener.callback(result);
                }}).lookupDataSet(request);
        }
        else {
            listener.notFound();
        }
    }
}
