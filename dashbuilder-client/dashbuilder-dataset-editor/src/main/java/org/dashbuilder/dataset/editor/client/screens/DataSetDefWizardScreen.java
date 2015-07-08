/**
 * Copyright (C) 2015 JBoss Inc
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
package org.dashbuilder.dataset.editor.client.screens;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.DataSetEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.SaveDataSetEvent;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.UpdateDataSetEvent;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.editor.client.resources.i18n.DataSetAuthoringConstants;
import org.dashbuilder.dataset.service.DataSetDefVfsServices;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.editor.commons.client.file.SavePopUp;
import org.uberfire.ext.widgets.common.client.common.BusyPopup;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;

@WorkbenchScreen(identifier = "DataSetDefWizard")
@Dependent
public class DataSetDefWizardScreen {

    @Inject
    DataSetEditor dataSetEditor;

    @Inject
    Caller<DataSetDefVfsServices> services;

    @Inject
    Event<NotificationEvent> notification;

    @Inject
    PlaceManager placeManager;

    PlaceRequest placeRequest;

    @WorkbenchPartTitle
    public String getTitle() {
        return DataSetAuthoringConstants.INSTANCE.creationWizardTitle();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return dataSetEditor;
    }

    @OnStartup
    public void init(PlaceRequest placeRequest) {
        this.placeRequest = placeRequest;
        dataSetEditor.newDataSetDef();
    }

    protected void save(final DataSetDef dataSetDef) {
        new SavePopUp(new ParameterizedCommand<String>() {
            @Override public void execute(String message) {
                BusyPopup.showMessage(DataSetAuthoringConstants.INSTANCE.saving());
                services.call(saveSuccessCallback, errorCallback)
                        .save(dataSetDef, message);
            }
        }).show();
    }

    RemoteCallback<Path> saveSuccessCallback = new RemoteCallback<Path>() {
        @Override public void callback(Path path) {
            BusyPopup.close();
            notification.fire(new NotificationEvent(DataSetAuthoringConstants.INSTANCE.savedOk()));
            placeManager.closePlace(placeRequest);
        }
    };

    ErrorCallback<Message> errorCallback = new ErrorCallback<Message>() {
        @Override public boolean error(Message message, Throwable throwable) {
            BusyPopup.close();
            dataSetEditor.showError(new ClientRuntimeError(throwable));
            return false;
        }
    };

    void onDataSetSave(@Observes final SaveDataSetEvent saveDataSetEvent) {
        if (saveDataSetEvent.getContext().equals(dataSetEditor)) {
            save(saveDataSetEvent.getDef());
        }
    }

    void onDataSetUpdate(@Observes final UpdateDataSetEvent updateDataSetEvent) {
        if (updateDataSetEvent.getContext().equals(dataSetEditor)) {
            // Update the def UUID to the original one before saving.
            final DataSetDef def = updateDataSetEvent.getDef();
            def.setUUID(updateDataSetEvent.getUuid());
            save(def);
        }
    }
}
