/*
 * Copyright 2012 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dashbuilder.client.html.widgets;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.dashbuilder.client.resources.i18n.HTMLScreenConstants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.PerspectiveChange;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@WorkbenchScreen(identifier = HTMLScreen.SCREEN_ID)
@Dependent
public class HTMLScreen {

    public static final String SCREEN_ID = "HTMLScreen";
    protected PlaceRequest placeRequest;
    
    @Inject
    private View view;

    @Inject
    private PlaceManager placeManager;

    @Inject
    protected PerspectiveEditor perspectiveEditor;
    
    @OnStartup
    public void onStartup(final PlaceRequest placeRequest) {
        this.placeRequest = placeRequest;
        init();
    }
    
    private void init() {
        String json = placeRequest.getParameter("json", "");
        view.show();
    }

    @OnOpen
    public void onOpen() {
        view.show();
    }

    @OnClose
    public void onClose() {
        
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return HTMLScreenConstants.INSTANCE.htmlScreen();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return this.view;
    }


    public interface View
            extends
            IsWidget {

        void show();

    }
    
    protected void onPerspectiveEditOn(@Observes final PerspectiveEditOnEvent event) {
        // TODO
    }

    protected void onPerspectiveEditOff(@Observes final PerspectiveEditOffEvent event) {
        // TODO
    }

    protected void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        // TODO
    }
}
