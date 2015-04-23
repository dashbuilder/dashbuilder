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
package org.dashbuilder.dataset.editor.client.screens;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.DataSetExplorer;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.*;
import org.dashbuilder.client.widgets.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetDefRemoveCallback;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @since 0.3.0
 */
@WorkbenchScreen(identifier = "DataSetExplorer")
@Dependent
public class DataSetExplorerScreenPresenter {

    
    private Menus menu = null;

    @Inject
    DataSetExplorer explorerWidget;

    @Inject
    Event<NewDataSetEvent> newDataSetEvent;

    @Inject
    Event<EditDataSetEvent> editDataSetEvent;

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest) {
        explorerWidget.addEditDataSetEventHandler(new EditDataSetEventHandler() {
            @Override
            public void onEditDataSet(EditDataSetEvent event) {
                editDataSet(event);
            }
        });
        explorerWidget.addDeleteDataSetEventHandler(new DeleteDataSetEventHandler() {
            @Override
            public void onDeleteDataSet(DeleteDataSetEvent event) {
                deleteDataSet(event);
            }
        });
        this.menu = makeMenuBar();
    }

    @OnClose
    public void onClose() {
        // TODO: Close editor widget.
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return DataSetExplorerConstants.INSTANCE.title();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return explorerWidget;
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return menu;
    }

    private Menus makeMenuBar() {
        return MenuFactory
                .newTopLevelMenu(DataSetExplorerConstants.INSTANCE.newDataSet())
                .respondsWith(getNewCommand())
                .endMenu()
                .build();
    }

    private Command getNewCommand() {
        return new Command() {
            public void execute() {
                newDataSet();
            }
        };
    }
    
    void newDataSet() {
        NewDataSetEvent event = new NewDataSetEvent();
        newDataSetEvent.fire(event);
    }
    
    void editDataSet(EditDataSetEvent event) {
        editDataSetEvent.fire(event);
    }

    void deleteDataSet(DeleteDataSetEvent event) {
        deleteDataSet(event.getUuid());
    }
    
    public void deleteDataSet(String uuid) {
        DataSetClientServices.get().removeDataSetDef(uuid, new DataSetDefRemoveCallback() {
            @Override
            public void success() {
                // TODO
            }

            @Override
            public boolean onError(DataSetClientServiceError error) {
                // TODO
                return false;
            }
        });
    }
}
