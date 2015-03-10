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
package org.dashbuilder.dataset.client.screens;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.dataset.client.widgets.DataSetEditor;
import org.dashbuilder.dataset.client.widgets.DataSetExplorer;
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

@WorkbenchScreen(identifier = "DataSetEditor")
@Dependent
public class DataSetEditorScreenPresenter {

    private DataSetEditor editorWidget;
    
    @OnStartup
    public void onStartup( final PlaceRequest placeRequest) {
        editorWidget = new DataSetEditor();
    }

    @OnClose
    public void onClose() {
        
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Data Set Editor Screen";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return editorWidget;
    }
    
}
