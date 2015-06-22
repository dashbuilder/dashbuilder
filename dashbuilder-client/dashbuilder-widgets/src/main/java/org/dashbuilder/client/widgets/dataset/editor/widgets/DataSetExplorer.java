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
package org.dashbuilder.client.widgets.dataset.editor.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.EditDataSetEventHandler;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

/**
 * <p>Data Set Explorer widget</p>
 * @since 0.3.0 
 */
@Dependent
public class DataSetExplorer implements IsWidget {

    public interface View extends IsWidget, HasHandlers {
        void set(Collection<DataSetDef> dataSetDefs);

        boolean add(final DataSetDef dataSetDef);

        void remove(final DataSetDef dataSetDef);

        boolean update(final DataSetDef oldDataSetDef, DataSetDef newDataSetDef);

        void show(final ShowDataSetDefCallback callback);

        void clear();
        
        void showError(String type, String message, String cause);

        HandlerRegistration addEditDataSetEventHandler(final EditDataSetEventHandler handler);
    }

    public interface ShowDataSetDefCallback {

        void getMetadata(final DataSetDef def, final DataSetMetadataCallback callback);

        boolean isShowBackendCache(final DataSetDef def);
    }

    DataSetClientServices clientServices;
    View view;

    @Inject
    public DataSetExplorer(DataSetClientServices clientServices) {
        this.view = new DataSetExplorerView();
        this.clientServices = clientServices;
        init();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    private void init() {
        clientServices.getPublicDataSetDefs(new RemoteCallback<List<DataSetDef>>() {
            public void callback(List<DataSetDef> dataSetDefs) {
                view.set(dataSetDefs);
                view.show(showDataSetDefCallback);
            }
        });
    }

    // Be aware of data set lifecycle events

    private void onDataSetDefRegisteredEvent(@Observes DataSetDefRegisteredEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getDataSetDef();
        if (def != null && def.isPublic()) {
            view.add(event.getDataSetDef());
            view.show(showDataSetDefCallback);
        }
    }

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);

        final DataSetDef def = event.getNewDataSetDef();
        if (def != null && def.isPublic()) {
            view.update(event.getOldDataSetDef(), event.getNewDataSetDef());
            view.show(showDataSetDefCallback);
        }
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        checkNotNull("event", event);

        view.remove(event.getDataSetDef());
        view.show(showDataSetDefCallback);
    }

    private final ShowDataSetDefCallback showDataSetDefCallback = new ShowDataSetDefCallback() {
        @Override
        public void getMetadata(final DataSetDef def, final DataSetMetadataCallback callback) {
            try {
                DataSetClientServices.get().fetchMetadata(def.getUUID(), callback);
            } catch (Exception e) {
                showError(e);
            }
        }

        @Override
        public boolean isShowBackendCache(DataSetDef def) {
            return def != null && def.getProvider() != null 
                    && ( !DataSetProviderType.BEAN.equals(def.getProvider()) 
                    && !DataSetProviderType.CSV.equals(def.getProvider() ));
        }
    };

    public void showError(final DataSetClientServiceError error) {
        final String type = error.getThrowable() != null ? error.getThrowable().getClass().getName() : null;
        final String message = error.getThrowable() != null ? error.getThrowable().getMessage() : error.getMessage().toString();
        final String cause = error.getThrowable() != null && error.getThrowable().getCause() != null ? error.getThrowable().getCause().getMessage() : null;
        showError(type, message, cause);
    }
    
    private void showError(final Exception e) {
        showError(null, e.getMessage(), null);
    }

    private void showError(final String type, final String message, final String cause) {
        if (type != null) GWT.log("Error type: " + type);
        if (message != null) GWT.log("Error message: " + message);
        if (cause != null) GWT.log("Error cause: " + cause);
        view.showError(type, message, cause);
    }
    
    // **************** EVENT HANDLER REGISTRATIONS ****************************

    public HandlerRegistration addEditDataSetEventHandler(EditDataSetEventHandler handler) {
        return view.addEditDataSetEventHandler(handler);
    }
}
