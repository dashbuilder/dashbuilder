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
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.DataSetDefValidationCallback;
import org.dashbuilder.client.widgets.dataset.editor.widgets.DataSetEditorView;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.backend.EditDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.editor.client.resources.i18n.DataSetAuthoringConstants;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.service.DataSetDefVfsServices;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.ext.editor.commons.client.BaseEditor;
import org.uberfire.ext.editor.commons.client.BaseEditorView;
import org.uberfire.ext.editor.commons.client.file.SaveOperationService;
import org.uberfire.ext.editor.commons.service.support.SupportsCopy;
import org.uberfire.ext.editor.commons.service.support.SupportsDelete;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.workbench.events.NotificationEvent.NotificationType.*;
import static org.uberfire.ext.editor.commons.client.menu.MenuItems.*;

@Dependent
@WorkbenchEditor(identifier = "DataSetDefEditor", supportedTypes = {DataSetDefType.class}, priority = Integer.MAX_VALUE)
public class DataSetDefEditorPresenter extends BaseEditor {

    public interface View extends BaseEditorView, IsWidget {
        void startEdit(EditDataSetDef editDataSetDef);
        void checkValid(DataSetDefValidationCallback callback);
        void showError(String message);
        void showError(ClientRuntimeError error);
    }

    @Inject
    Caller<DataSetDefVfsServices> services;

    @Inject
    PlaceManager placeManager;

    @Inject
    DataSetDefType resourceType;

    View view;
    DataSetDef model;

    @Inject
    public DataSetDefEditorPresenter(final View view) {
        super(view);
        this.view = view;
    }

    @OnStartup
    public void onStartup(final ObservablePath path, final PlaceRequest place) {
        init(path,
                place,
                resourceType,
                true,
                false,
                VALIDATE,
                SAVE,
                COPY,
                DELETE);
    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return super.getTitle();
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        return buildTitle();
    }

    protected String buildTitle() {
        if (model == null) {
            return DataSetAuthoringConstants.INSTANCE.editorTitleGeneric();
        } else {
            String type = DataSetEditorView.getProviderName(model.getProvider());
            return DataSetAuthoringConstants.INSTANCE.editorTitle(model.getName(), type);
        }
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    @WorkbenchPartView
    public Widget getWidget() {
        return view.asWidget();
    }

    @OnMayClose
    public boolean onMayClose() {
        return super.mayClose(getCurrentModelHash());
    }

    @Override
    protected void loadContent() {
        try {
            services.call(loadCallback, errorCallback).load(versionRecordManager.getCurrentPath());
        } catch (Exception e) {
            view.showError(new ClientRuntimeError(e));
        }
    }

    @Override
    protected Command onValidate() {
        return new Command() {
            @Override
            public void execute() {
                view.checkValid(new DataSetDefValidationCallback () {
                    @Override
                    public void valid(DataSetDef dataSetDef, DataSet dataSet) {
                        notification.fire(new NotificationEvent(DataSetAuthoringConstants.INSTANCE.validationOk(), SUCCESS));
                    }
                    @Override
                    public void invalid(ClientRuntimeError error) {
                        notification.fire(new NotificationEvent(DataSetAuthoringConstants.INSTANCE.validationFailed(), ERROR));
                    }
                });
            }
        };
    }

    @Override
    protected void save() {
        view.checkValid(new DataSetDefValidationCallback () {
            @Override
            public void valid(DataSetDef dataSetDef, DataSet dataSet) {
                _save();
            }
            @Override
            public void invalid(ClientRuntimeError error) {
            }
        });
    }

    protected void _save() {
        new SaveOperationService().save(versionRecordManager.getCurrentPath(),
                new ParameterizedCommand<String>() {
                    @Override public void execute(final String commitMessage) {
                        model.setPublic(true);
                        model.setAllColumnsEnabled(false);
                        services.call(getSaveSuccessCallback(getCurrentModelHash()), errorCallback)
                                .save(model, commitMessage);
                    }
                }
        );
        concurrentUpdateSessionInfo = null;
    }

    public int getCurrentModelHash() {
        if (model == null) return 0;
        return model.getUUID().hashCode();
    }

    RemoteCallback<EditDataSetDef> loadCallback = new RemoteCallback<EditDataSetDef>() {
        public void callback(final EditDataSetDef result) {
            if (result == null) {
                view.hideBusyIndicator();
                view.showError(DataSetAuthoringConstants.INSTANCE.dataSetNotFound());
            } else {
                model = result.getDefinition();
                setOriginalHash(getCurrentModelHash());

                changeTitleNotification.fire(new ChangeTitleWidgetEvent(place, buildTitle()));

                view.hideBusyIndicator();
                view.startEdit(result);
            }
        }
    };

    ErrorCallback<Message> errorCallback = new ErrorCallback<Message>() {
        @Override
        public boolean error(Message message, Throwable throwable) {
            view.hideBusyIndicator();
            view.showError(new ClientRuntimeError(throwable));
            return false;
        }
    };

    protected Caller<? extends SupportsDelete> getDeleteServiceCaller() {
        return services;
    }

    protected Caller<? extends SupportsCopy> getCopyServiceCaller() {
        return services;
    }

    private void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        placeManager.closePlace(place);
    }
}
