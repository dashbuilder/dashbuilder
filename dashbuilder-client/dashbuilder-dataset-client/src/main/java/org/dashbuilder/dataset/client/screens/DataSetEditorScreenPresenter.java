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
import org.dashbuilder.dataset.client.widgets.events.EditDataSetEvent;
import org.dashbuilder.dataset.client.widgets.events.NewDataSetEvent;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;

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

    public void onNewDataSet(@Observes NewDataSetEvent event) {
        editorWidget.newDataSet(event.getUuid());
    }
    
    public void onEditDataSet(@Observes EditDataSetEvent event) {
        try {
            editorWidget.editDataSet(event.getUuid());
        } catch (Exception e) {
            error("DataSetEditorScreenPresenter#onEditDataSet - Cannot edit dataset with uuid [" + event.getUuid() + "].", e);
        }
    }

    // TODO: Display message to user.
    private void error(String message, Exception e) {
        GWT.log(message + "\n Excetion: " + e.getMessage());
    }

    
}
