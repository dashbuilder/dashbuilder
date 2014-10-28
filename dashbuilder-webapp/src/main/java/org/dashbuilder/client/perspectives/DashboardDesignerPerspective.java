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
package org.dashbuilder.client.perspectives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.dom.client.Document;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorPopup;
import org.dashbuilder.renderer.table.client.TableRenderer;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.dashbuilder.dataset.group.DateIntervalType.MONTH;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.shared.sales.SalesConstants.*;

/**
 * The dashboard composer perspective.
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "DashboardDesignerPerspective")
public class DashboardDesignerPerspective {

    @Inject
    private PlaceManager placeManager;

    @Inject
    DisplayerSettingsJSONMarshaller jsonHelper;

    @Perspective
    public PerspectiveDefinition buildPerspective() {
        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl(MultiListWorkbenchPanelPresenter.class.getName());
        perspective.setName("Designer");
        return perspective;
    }

    @WorkbenchMenu
    public Menus buildMenuBar() {
        return MenuFactory
                .newTopLevelMenu("New displayer")
                .respondsWith(getNewDisplayerCommand())
                .endMenu().build();
    }

    private Command getNewDisplayerCommand() {
        return new Command() {
            public void execute() {
                /* Displayer settings == null => Create a brand new displayer */
                DisplayerEditorPopup displayerEditor = new DisplayerEditorPopup();
                displayerEditor.init(null, new DisplayerEditor.Listener() {

                    public void onClose(DisplayerEditor editor) {
                    }

                    public void onSave(DisplayerEditor editor) {
                        placeManager.goTo(createPlaceRequest(editor.getDisplayerSettings()));
                    }
                });
            }
        };
    }

    private PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonHelper.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String,String>();
        params.put("json", json);
        params.put("edit", "true");
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }
}
