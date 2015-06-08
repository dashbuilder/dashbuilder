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
package org.dashbuilder.client.perspective.editor.widgets.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.json.MenusJSONMarshaller;
import org.dashbuilder.client.perspective.editor.menu.MenuUtils;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DisplayerSettings;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@WorkbenchScreen(identifier = "EditableWorkbenchMenuBar")
@Dependent
public class EditableWorkbenchMenuBarPresenter {
    
    protected PlaceRequest placeRequest;
    protected Menus menus;
    protected EditableWorkbenchMenuBarListener listener;
    
    @Inject
    MenusJSONMarshaller jsonMarshaller;

    @Inject
    private View view;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private AuthorizationManager authzManager;

    @Inject
    private User identity;

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @OnStartup
    public void onStartup(final PlaceRequest placeRequest) {
        this.placeRequest = placeRequest;
        String json = placeRequest.getParameter("json", "");
        if (!StringUtils.isBlank(json)) this.menus = jsonMarshaller.fromJsonString(json);
        if (menus == null) throw new IllegalArgumentException("Menus not found");
        GWT.log("EditableWorkbenchMenuBarPresenter - startup: " + json);
        enableEdition();
        // TODO: Move menuBarListener to EditableWorkbenchMenuBarComponent.
        init(menus, menuBarListener);
    }

    @OnClose
    public void onClose() {
        clear();
    }

    // TODO: Editable title.
    @WorkbenchPartTitle
    public String getTitle() {
        return "My Title";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return this.view;
    }


    public interface View
            extends
            IsWidget {

        void build(final Menus menus, final ViewCallback callback);

        void clear();

        void enableEdition();

        void disableEdition();

    }
    
    public interface EditableWorkbenchMenuBarListener {
        void onMenusUpdated(final Menus menus);
    }
    
    // TODO: Move to EditableWorkbenchMenuBarComponent.
    private final EditableWorkbenchMenuBarListener menuBarListener = new EditableWorkbenchMenuBarListener() {
        @Override
        public void onMenusUpdated(final Menus menus) {
            MenuUtils.logMenus(menus);
            GWT.log("Saving perspective...");
            perspectiveEditor.updatePlace(placeRequest, createPlaceRequest(menus));
            perspectiveEditor.saveCurrentPerspective();
        }
    };

    protected PlaceRequest createPlaceRequest(final Menus menus) {
        String json = jsonMarshaller.toJsonString(menus);
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", json);
        GWT.log("EditableWorkbenchMenuBarPresenter - save: " + json);
        return new DefaultPlaceRequest("EditableWorkbenchMenuBar", params);
    }
    
    interface ViewCallback {
        Collection<PerspectiveActivity> getPerspectiveActivities();
        boolean notHavePermissionToMakeThis( final MenuItem item );
        void removeItem(final String itemUUID);
        void createItem(final String caption, final String activityId);
        void moveItem(final String sourceUUID, final String targetUUID,final boolean before);
    }
    
    private final ViewCallback viewCallback = new ViewCallback() {

        @Override
        public Collection<PerspectiveActivity> getPerspectiveActivities() {
            return perspectiveEditor.getPerspectiveActivities();
        }

        @Override
        public boolean notHavePermissionToMakeThis(final MenuItem item) {
            return !authzManager.authorize( item, identity );
        }

        @Override
        public void removeItem(final String itemUUID) {
            MenuUtils.removeItem(menus, itemUUID);
            fireMenusUpdated();
            buildView();
        }

        @Override
        public void createItem(final String caption, final String activityId) {
            final MenuItem item = MenuUtils.createMenuItemCommand(caption, activityId);
            if (item != null) {
                MenuUtils.addItem(menus, item);
                fireMenusUpdated();
                buildView();
            }
        }

        @Override
        public void moveItem(final String sourceUUID, final String targetUUID, final boolean before) {
            MenuUtils.moveItem(menus, sourceUUID, targetUUID, before);
            fireMenusUpdated();
            buildView();
        }
    };

    private void buildView() {
        view.build(menus, viewCallback);
    }

    private void fireMenusUpdated() {
        if (listener != null) listener.onMenusUpdated(menus);
    }

    public void init( final Menus menus, final EditableWorkbenchMenuBarListener listener) {
        this.menus = MenuUtils.buildEditableMenusModel(menus.getItems());
        this.listener = listener;
        buildView();
    }

    public void enableEdition() {
        view.enableEdition();        
    }

    public void disableEdition() {
        view.disableEdition();
    }
    
    public void clear() {
        view.clear();
    }
    
}
