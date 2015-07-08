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

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.widgets.dataset.editor.widgets.events.EditDataSetEvent;
import org.dashbuilder.client.widgets.dataset.editor.widgets.explorer.DataSetExplorer;
import org.dashbuilder.client.widgets.resources.i18n.DataSetExplorerConstants;
import org.dashbuilder.dataset.service.DataSetDefVfsServices;
import org.jboss.errai.common.client.api.Caller;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

/**
 * @since 0.3.0
 */
@WorkbenchScreen(identifier = "DataSetDefExplorer")
@Dependent
public class DataSetDefExplorerScreen {

    
    private Menus menu = null;

    @Inject
    PlaceManager placeManager;

    @Inject
    Caller<DataSetDefVfsServices> services;

    @Inject
    Event<NotificationEvent> notification;

    @Inject
    DataSetExplorer explorerWidget;

    @OnStartup
    public void onStartup(PlaceRequest placeRequest) {
        this.menu = makeMenuBar();
        explorerWidget.show();
    }

    @OnClose
    public void onClose() {
        
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
        placeManager.goTo("DataSetDefWizard");
    }
    
    private void onEditDataSetEvent(@Observes EditDataSetEvent event) {
        checkNotNull("event", event);
        placeManager.goTo(new PathPlaceRequest(event.getDef().getVfsPath()));
    }
}
