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
package org.dashbuilder.client.menu.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.client.menu.ClientMenuUtils;
import org.dashbuilder.client.menu.exception.MenuExceptionMessages;
import org.dashbuilder.client.menu.json.MenusJSONMarshaller;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.dashbuilder.client.resources.i18n.MenusConstants;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.shared.menu.MenuHandler;
import org.dashbuilder.shared.menu.exception.AbstractMenuException;
import org.dashbuilder.shared.menu.exception.MenuSecurityException;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.PerspectiveChange;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@WorkbenchScreen(identifier = MenuScreen.SCREEN_ID)
@Dependent
public class MenuScreen {

    public static final String SCREEN_ID = "EditableMenuScreen";
    protected PlaceRequest placeRequest;
    protected Menus menus;
    protected MenuScreenListener listener;
    
    @Inject
    MenusJSONMarshaller jsonMarshaller;

    @Inject
    private View view;

    @Inject
    private PlaceManager placeManager;

    @Inject
    protected PerspectiveEditor perspectiveEditor;
    
    @Inject
    protected MenuHandler menuHandler;
    
    @Inject
    protected MenuExceptionMessages menuExceptionMessages;

    @OnStartup
    public void onStartup(final PlaceRequest placeRequest) {
        this.placeRequest = placeRequest;
        init();
    }
    
    private void init() {
        String json = placeRequest.getParameter("json", "");
        if (!StringUtils.isBlank(json)) this.menus = jsonMarshaller.fromJsonString(json);
        if (menus == null) menus = menuHandler.buildEmptyEditableMenusModel();
        menuHandler.setMenus(menus);
        ClientMenuUtils.doDebugLog("MenuScreen - startup: " + json);
        init(menus, menuBarListener);
    }

    @OnOpen
    public void onOpen() {
        if (menus == null) init();
        if (perspectiveEditor.isEditOn()) enableEdition();
        else disableEdition();
    }

    @OnClose
    public void onClose() {
        
    }

    // TODO: Do not show title?
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

        void showError(final String msg);
        
        void clear();

        void enableEdition();

        void disableEdition();

    }
    
    public interface MenuScreenListener {
        void onMenusUpdated(final Menus menus);
    }
    
    // TODO: Move to MenuComponent?
    private final MenuScreenListener menuBarListener = new MenuScreenListener() {
        @Override
        public void onMenusUpdated(final Menus menus) {
            ClientMenuUtils.logMenus(menus);
            ClientMenuUtils.doDebugLog("Saving perspective...");
            perspectiveEditor.updatePlace(placeRequest, createPlaceRequest(menus));
            perspectiveEditor.saveCurrentPerspective();
        }
    };

    protected PlaceRequest createPlaceRequest(final Menus menus) {
        String json = jsonMarshaller.toJsonString(menus);
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", json);
        ClientMenuUtils.doDebugLog("MenuScreen - save: " + json);
        return new DefaultPlaceRequest(SCREEN_ID, params);
    }
    
    interface ViewCallback {
        Collection<PerspectiveActivity> getPerspectiveActivities();
        MenuHandler.MenuItemLocation getItemLocation(final String uuid);
        boolean notHavePermissionToMakeThis(final MenuItem item);
        void removeItem(final String itemUUID);
        void createItemCommand(final String caption, final String activityId);
        void createItemGroup(final String caption);
        void editItemCommand(final String itemUUID, final String caption, final String activityId);
        void editeItemGroup(final String itemUUID, final String caption);
        void moveItem(final String sourceUUID, final String targetUUID, final boolean before);
    }
    
    private final ViewCallback viewCallback = new ViewCallback() {

        @Override
        public Collection<PerspectiveActivity> getPerspectiveActivities() {
            return perspectiveEditor.getPerspectiveActivities();
        }

        @Override
        public MenuHandler.MenuItemLocation getItemLocation(String uuid) {
            return MenuHandler.getItemLocation(menus, uuid);
        }

        @Override
        public boolean notHavePermissionToMakeThis(final MenuItem item) {
            return menuHandler.notHavePermissionToMakeThis(item);
        }

        @Override
        public void removeItem(final String itemUUID) {
            try {
                final  MenuItem  i = menuHandler.removeItem(itemUUID);
                if (i != null) {
                    fireMenusUpdated();
                }
            } catch (final AbstractMenuException e) {
                final String m = menuExceptionMessages.getMessage(e);
                view.showError(m);
            }
        }

        @Override
        public void createItemCommand(final String caption, final String activityId) {
            try {
                final MenuItem item = menuHandler.createMenuItemCommand(caption, activityId);
                if (item != null) {
                    menuHandler.addItem(item);
                    fireMenusUpdated();
                }
            } catch (MenuSecurityException e) {
                view.showError(MenusConstants.INSTANCE.authzFailed());
            }
        }

        @Override
        public void createItemGroup(final String caption) {
            try {
                final MenuItem item = menuHandler.createMenuItemGroup(caption);
                if (item != null) {
                    menuHandler.addItem(item);
                    fireMenusUpdated();
                }
            } catch (MenuSecurityException e) {
                view.showError(MenusConstants.INSTANCE.authzFailed());
            }
        }

        @Override
        public void editItemCommand(final String itemUUID, final String caption, final String activityId) {
            MenuItem newItem = null;
            try {
                newItem = menuHandler.createMenuItemCommand(caption, activityId);
                replaceItem(itemUUID, menus, newItem);    
            } catch (MenuSecurityException e) {
                view.showError(MenusConstants.INSTANCE.authzFailed());
            }
        }

        @Override
        public void editeItemGroup(final String itemUUID, final String caption) {
            try {
                final MenuItem newItem = menuHandler.createMenuItemGroup(caption);
                replaceItem(itemUUID, menus, newItem);
            } catch (MenuSecurityException e) {
                view.showError(MenusConstants.INSTANCE.authzFailed());
            }
        }
        
        private void replaceItem(final String itemUUID, final Menus menus, final MenuItem newItem) {
            if (newItem != null) {
                try {
                    menuHandler.replaceItem(itemUUID, newItem);
                    fireMenusUpdated();
                } catch (MenuSecurityException e) {
                    view.showError(MenusConstants.INSTANCE.authzFailed());
                }
            }
        }

        @Override
        public void moveItem(final String sourceUUID, final String targetUUID, final boolean before) {
            
            try {
                final boolean moved = menuHandler.moveItem(sourceUUID, targetUUID, before);
                if (moved) {
                    fireMenusUpdated();
                }
            } catch (AbstractMenuException e) {
                final String m = menuExceptionMessages.getMessage(e);
                view.showError(m);
            }
        }
    };

    private void buildView() {
        view.build(menus, viewCallback);
    }

    private void fireMenusUpdated() {
        ClientMenuUtils.doDebugLog("fireMenusUpdated");
        if (listener != null) listener.onMenusUpdated(menus);
    }

    public void init( final Menus menus, final MenuScreenListener listener) {
        this.menus = menus;
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
        menus = null;
        listener = null;
        view.clear();
    }

    protected void onPerspectiveEditOn(@Observes final PerspectiveEditOnEvent event) {
        enableEdition();
    }

    protected void onPerspectiveEditOff(@Observes final PerspectiveEditOffEvent event) {
        disableEdition();
    }

    protected void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        disableEdition();
    }
}
