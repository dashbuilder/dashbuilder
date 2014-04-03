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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.dashbuilder.event.DataSetReadyEvent;
import org.dashbuilder.model.dataset.DataLookup;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.service.DataSetService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

public class ClientDataSetManager {

    @Inject
    private Caller<DataSetService> dataSetService;

    @Inject
    private Event<DataSetReadyEvent> dataSetReadyEvent;


    public DataSet createDataSet(String uuid) {
        ClientDataSet dataSet = new ClientDataSet();
        return dataSet;
    }

    public DataSet getDataSet(String uuid) throws Exception {
        dataSetService.call(
                new RemoteCallback<DataSetMetadata>() {
                    public void callback(DataSetMetadata result) {

                    }
                }).getDataSetMetadata(uuid);
        return null;
    }

    public void registerDataSet(DataSet dataSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataSet refreshDataSet(String uuid) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void lookupDataSet(final DataLookup request) {
        dataSetService.call(
            new RemoteCallback<DataSet>() {
                public void callback(DataSet result) {
                    DataSetReadyEvent event = new DataSetReadyEvent(request, result);
                    dataSetReadyEvent.fire(event);
                }
            }).lookupDataSet(request);
    }
}
