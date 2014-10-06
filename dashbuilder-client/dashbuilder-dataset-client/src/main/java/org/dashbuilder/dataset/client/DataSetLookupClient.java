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
package org.dashbuilder.dataset.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.DataSetLookupService;
import org.dashbuilder.dataset.client.resources.i18n.DataSetConstants;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetBackendRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetModifiedEvent;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.uberfire.workbench.events.NotificationEvent;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;
import static org.uberfire.workbench.events.NotificationEvent.NotificationType.*;

/**
 * Proxy interface to the list of available DataSetManager implementations.
 * It hides to client widgets where the data sets are stored and how they are fetched and processed.
 */
@ApplicationScoped
public class DataSetLookupClient {

    public static DataSetLookupClient get() {
        Collection<IOCBeanDef<DataSetLookupClient>> beans = IOC.getBeanManager().lookupBeans(DataSetLookupClient.class);
        IOCBeanDef<DataSetLookupClient> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }

    @Inject
    private ClientDataSetManager clientDataSetManager;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    private Event<DataSetModifiedEvent> dataSetModifiedEvent;

    /**
     * The service caller used to lookup data sets from the backend.
     */
    private Caller<DataSetLookupService> dataSetLookupService = null;

    /**
     * A cache of DataSetMetadata instances
     */
    private Map<DataSetLookup,DataSetMetadata> remoteMetadataMap = new HashMap<DataSetLookup,DataSetMetadata>();

    /**
     * If enabled then remote data set can be pushed to clients.
     */
    private boolean pushRemoteDataSetEnabled = true;

    /**
     * It holds a set of data set push requests in progress.
     */
    private Map<String,DataSetPushHandler> pushRequestMap = new HashMap<String,DataSetPushHandler>();

    public boolean isPushRemoteDataSetEnabled() {
        return pushRemoteDataSetEnabled;
    }

    /**
     * Enable/disable the ability to push remote data sets from server.
     */
    public void setPushRemoteDataSetEnabled(boolean pushRemoteDataSetEnabled) {
        this.pushRemoteDataSetEnabled = pushRemoteDataSetEnabled;
    }

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
            if (remoteMetadataMap.containsKey(request)) {
                listener.callback(remoteMetadataMap.get(request));
            } else {
                dataSetLookupService.call(
                    new RemoteCallback<DataSetMetadata>() {
                    public void callback(DataSetMetadata result) {
                        if (result == null) listener.notFound();
                        else {
                            remoteMetadataMap.put(request, result);
                            listener.callback(result);
                        }
                    }}).lookupDataSetMetadata(request);
            }
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

        // Look always into the client data set manager.
        if (clientDataSetManager.getDataSet(request.getDataSetUUID()) != null) {
            DataSet dataSet = clientDataSetManager.lookupDataSet(request);
            listener.callback(dataSet);
        }
        // If the data set is not in client, then look up remotely (only if the remote access is available).
        else if (dataSetLookupService != null) {

            // First of all, get the target data set estimated size.
            final DataSetLookup lookupSourceDataSet = new DataSetLookup(request.getDataSetUUID());
            fetchMetadata(lookupSourceDataSet, new DataSetMetadataCallback() {
                public void callback(DataSetMetadata metatada) {

                    // Push the data set to client if and only if the push feature is enabled, the data set is
                    // pushable & the data set is smaller than the max push size defined.
                    DataSetDef dsetDef = metatada.getDefinition();
                    boolean isPushable = dsetDef != null && dsetDef.isPushEnabled() && metatada.getEstimatedSize() < dsetDef.getMaxPushSize();
                    if (pushRemoteDataSetEnabled && isPushable) {

                        // Check if a push is already in progress.
                        // (This is necessary in order to avoid repeating multiple push requests over the same data set).
                        DataSetPushHandler pushHandler = pushRequestMap.get(request.getDataSetUUID());
                        if (pushHandler == null) {
                            // Create a push handler.
                            pushHandler = new DataSetPushHandler(metatada);

                            // Send the lookup request to the server...
                            _lookupDataSet(lookupSourceDataSet, pushHandler);
                        }
                        // Register the lookup request into the current handler.
                        pushHandler.registerLookup(request, listener);
                    }
                    // Lookup the remote data set otherwise.
                    else {
                        _lookupDataSet(request, listener);
                    }
                }
                // Data set metadata not found
                public void notFound() {
                    listener.notFound();
                }
            });
        }
        // Data set not found on client.
        else {
            listener.notFound();
        }
    }

    private void _lookupDataSet(DataSetLookup request, final DataSetReadyCallback listener) {
        try {
            dataSetLookupService.call(
                new RemoteCallback<DataSet>() {
                    public void callback(DataSet result) {
                        if (result == null) listener.notFound();
                        else listener.callback(result);
                    }}).lookupDataSet(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class DataSetPushHandler implements DataSetReadyCallback {

        private DataSetMetadata dataSetMetadata = null;
        private List<DataSetLookupListenerPair> listenerList = new ArrayList<DataSetLookupListenerPair>();

        private DataSetPushHandler(DataSetMetadata metadata) {
            this.dataSetMetadata = metadata;

            pushRequestMap.put(dataSetMetadata.getUUID(), this);
            notification.fire(
                    new NotificationEvent( DataSetConstants.INSTANCE.dsLookupClient_loadingFromServer( Integer.toString( metadata.getEstimatedSize() ) ), INFO)
            );
        }

        public void registerLookup(DataSetLookup lookup, DataSetReadyCallback listener) {
            listenerList.add(new DataSetLookupListenerPair(lookup, listener));
        }

        public void callback(DataSet dataSet) {
            pushRequestMap.remove(dataSetMetadata.getUUID());

            clientDataSetManager.registerDataSet(dataSet);
            notification.fire(new NotificationEvent( DataSetConstants.INSTANCE.dsLookupClient_successfullyLoaded(), SUCCESS));

            for (DataSetLookupListenerPair pair : listenerList) {
                DataSet result = clientDataSetManager.lookupDataSet(pair.lookup);
                pair.listener.callback(result);
            }
        }

        public void notFound() {
            pushRequestMap.remove(dataSetMetadata.getUUID());

            notification.fire(new NotificationEvent(DataSetConstants.INSTANCE.dsLookupClient_dsNotFoundInServer(), ERROR));
            for (DataSetLookupListenerPair pair : listenerList) {
                pair.listener.notFound();
            }
        }
    }

    private class DataSetLookupListenerPair {

        DataSetLookup lookup;
        DataSetReadyCallback listener;

        private DataSetLookupListenerPair(DataSetLookup lookup, DataSetReadyCallback listener) {
            this.lookup = lookup;
            this.listener = listener;
        }
    }

    // Catch backend events

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);
        String uuid = event.getNewDataSetDef().getUUID();

        // Remove any stale data existing on the client.
        // This will force next lookup requests to push a refreshed data set.
        clientDataSetManager.removeDataSet(uuid);

        // If a data set has been updated on the sever then fire an event.
        dataSetModifiedEvent.fire(new DataSetModifiedEvent(uuid));
    }

    private void onDataSetRegistered(@Observes DataSetBackendRegisteredEvent event) {
        checkNotNull("event", event);
        String uuid = event.getDataSetMetadata().getUUID();

        // Remove any stale data existing on the client.
        // This will force next lookup requests to push a refreshed data set.
        DataSet clientDataSet = clientDataSetManager.removeDataSet(uuid);

        // If the dataset already existed on client then an update event is fired.
        if (clientDataSet != null) {
            dataSetModifiedEvent.fire(new DataSetModifiedEvent(uuid));
        }
    }
}
