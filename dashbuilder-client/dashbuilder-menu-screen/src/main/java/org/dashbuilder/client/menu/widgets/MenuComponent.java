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
package org.dashbuilder.client.menu.widgets;

import org.dashbuilder.client.menu.MenuUtils;
import org.dashbuilder.client.menu.json.MenusJSONMarshaller;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.PerspectiveEditorComponent;
import org.dashbuilder.client.resources.i18n.MenusConstants;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Use of EditableWorkbenchMenuBarListener to persist the perspective (and menus)
@ApplicationScoped
public class MenuComponent implements PerspectiveEditorComponent {

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Inject
    protected MenusJSONMarshaller jsonMarshaller;

    @Override
    public String getComponentName() {
        return MenusConstants.INSTANCE.editableWorkbenchMenuBar();
    }

    @Override
    public void createNewInstance() {
        // TODO: Obtain json string for persisted menus.
        final Menus emptyMenus = createEmptyMenus();
        perspectiveEditor.openPlace(createPlaceRequest(emptyMenus));
        perspectiveEditor.saveCurrentPerspective();
    }

    protected PlaceRequest createPlaceRequest(final Menus menus) {
        String json = jsonMarshaller.toJsonString(menus);
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", json);
        return new DefaultPlaceRequest("EditableWorkbenchMenuBar", params);
    }
    
    private Menus createEmptyMenus() {
        final MenuItem homeItem = MenuUtils.createMenuItemCommand("home", "HomePerspective");
        final List<MenuItem> items = new ArrayList<MenuItem>();
        items.add(homeItem);
        return MenuUtils.buildEditableMenusModel(items);
    }
}
