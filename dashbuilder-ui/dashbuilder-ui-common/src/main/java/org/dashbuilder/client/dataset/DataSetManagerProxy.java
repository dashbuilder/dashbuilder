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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

/**
 * Proxy interface to the list of available DataSetManager implementations.
 * It hides to client widgets where the data sets are stored and how they are fetched and processed.
 */
@ApplicationScoped
public class DataSetManagerProxy {

    @Inject
    private ClientDataSetManager clientDataSetManager;

    @Inject
    // TODO: set the reference from the backend module instead of having it hard-coded.
    private Caller<DataSetManager> remoteDataSetManager;

    /**
     * Fetch the metadata instance for the specified data set.
     * @param uuid The UUID of the data set.
     * @throws Exception If the data set can't be found.
     */
    public void fetchMetadata(final String uuid, final DataSetMetadataCallback listener) throws Exception {

        DataSetMetadata metadata = clientDataSetManager.getDataSetMetadata(uuid);
        if (metadata != null) {
            listener.callback(metadata);
        } else {
            remoteDataSetManager.call(
                    new RemoteCallback<DataSetMetadata>() {
                        public void callback(DataSetMetadata result) {
                            if (result != null) {
                                listener.callback(result);
                            }
                        }
                    }).getDataSetMetadata(uuid);
        }
    }

    /**
     * Process the specified data set lookup request.
     * @param request The data set lookup request
     * @throws Exception It there is an unexpected error trying to execute the lookup request or
     * if the data set can't be found.
     */
    public void lookupDataSet(final DataSetLookup request, final DataSetReadyCallback listener) throws Exception {
        // TODO: move from backend to client storage those small enough data sets.

        if (clientDataSetManager.getDataSet(request.getDataSetUUID()) != null) {
            DataSet dataSet = clientDataSetManager.lookupDataSet(request);
            listener.callback(dataSet);
        } else {
            remoteDataSetManager.call(
                    new RemoteCallback<DataSet>() {
                        public void callback(DataSet result) {
                            if (result != null) {
                                listener.callback(result);
                            }
                        }
                    }).lookupDataSet(request);
        }
    }
}
