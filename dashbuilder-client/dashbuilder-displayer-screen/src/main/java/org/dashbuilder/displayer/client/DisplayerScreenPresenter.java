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
package org.dashbuilder.displayer.client;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.uuid.UUIDGenerator;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorPopup;
import org.dashbuilder.displayer.client.widgets.DisplayerView;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

@WorkbenchScreen(identifier = "DisplayerScreen")
@Dependent
public class DisplayerScreenPresenter {

    private DisplayerView displayerView;
    private PerspectiveCoordinator perspectiveCoordinator;
    private DisplayerSettingsJSONMarshaller jsonMarshaller;
    private DisplayerSettings displayerSettings;
    private PlaceManager placeManager;
    private UUIDGenerator uuidGenerator;

    private Menus menu = null;
    private boolean editEnabled = false;
    private boolean cloneEnabled = false;

    @Inject
    public DisplayerScreenPresenter(UUIDGenerator uuidGenerator,
            PlaceManager placeManager,
            DisplayerView displayerView,
            PerspectiveCoordinator perspectiveCoordinator,
            DisplayerSettingsJSONMarshaller jsonMarshaller) {

        this.uuidGenerator = uuidGenerator;
        this.placeManager = placeManager;
        this.displayerView = displayerView;
        this.perspectiveCoordinator = perspectiveCoordinator;
        this.jsonMarshaller = jsonMarshaller;
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest) {
        String json = placeRequest.getParameter("json", "");
        if (!StringUtils.isBlank(json)) this.displayerSettings = jsonMarshaller.fromJsonString(json);
        if (displayerSettings == null) throw new IllegalArgumentException("Displayer settings not found.");

        // Check if display renderer selector component.
        Boolean showRendererSelector = Boolean.parseBoolean(placeRequest.getParameter("showRendererSelector","false"));
        displayerView.setIsShowRendererSelector(showRendererSelector);
        
        // Draw the Displayer.
        if (StringUtils.isBlank(displayerSettings.getUUID())) displayerSettings.setUUID(uuidGenerator.newUuid());
        displayerView.setDisplayerSettings(displayerSettings);
        Displayer displayer = displayerView.draw();
        displayer.refreshOn();

        // Register the Displayer into the coordinator.
        perspectiveCoordinator.addDisplayer(displayer);

        // Check edit mode
        String edit = placeRequest.getParameter("edit", "false");
        String clone = placeRequest.getParameter("clone", "false");
        editEnabled = Boolean.parseBoolean(edit);
        cloneEnabled = Boolean.parseBoolean(clone);
        this.menu = makeMenuBar();
    }

    @OnClose
    public void onClose() {
        this.removeDisplayer();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return displayerSettings.getTitle();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return displayerView;
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return menu;
    }

    private Menus makeMenuBar() {
        if (editEnabled && !cloneEnabled) {
            return MenuFactory
                    .newTopLevelMenu("Edit")
                    .respondsWith(getEditCommand())
                    .endMenu().build();
        }
        if (!editEnabled && cloneEnabled) {
            return MenuFactory
                    .newTopLevelMenu("Clone")
                    .respondsWith(getCloneCommand())
                    .endMenu().build();
        }
        if (editEnabled && cloneEnabled) {
            return MenuFactory
                    .newTopLevelMenu("Edit")
                    .respondsWith(getEditCommand())
                    .endMenu()
                    .newTopLevelMenu("Clone")
                    .respondsWith(getCloneCommand())
                    .endMenu()
                    .build();
        }
        return null;
    }

    private Command getEditCommand() {
        return new Command() {
            public void execute() {
                perspectiveCoordinator.editOn();
                DisplayerEditorPopup displayerEditor =  new DisplayerEditorPopup();
                displayerEditor.init(displayerSettings, new DisplayerEditor.Listener() {

                    public void onClose(DisplayerEditor editor) {
                        perspectiveCoordinator.editOff();
                    }

                    public void onSave(DisplayerEditor editor) {
                        perspectiveCoordinator.editOff();
                        updateDisplayer(editor.getDisplayerSettings());
                    }
                });
            }
        };
    }

    private Command getCloneCommand() {
        return new Command() {
            public void execute() {
                perspectiveCoordinator.editOn();
                DisplayerEditorPopup displayerEditor = new DisplayerEditorPopup();
                DisplayerSettings clonedSettings = displayerSettings.cloneInstance();
                clonedSettings.setUUID(uuidGenerator.newUuid());
                displayerEditor.init(clonedSettings, new DisplayerEditor.Listener() {

                    public void onClose(DisplayerEditor editor) {
                        perspectiveCoordinator.editOff();
                    }

                    public void onSave(DisplayerEditor editor) {
                        perspectiveCoordinator.editOff();
                        placeManager.goTo(createPlaceRequest(editor.getDisplayerSettings()));
                    }
                });
            }
        };
    }

    private void updateDisplayer(DisplayerSettings settings) {
        this.removeDisplayer();

        this.displayerSettings = settings;
        this.displayerView.setDisplayerSettings(settings);
        Displayer newDisplayer = this.displayerView.draw();
        newDisplayer.refreshOn();

        this.perspectiveCoordinator.addDisplayer(newDisplayer);
    }

    protected void removeDisplayer() {
        Displayer displayer = displayerView.getDisplayer();
        perspectiveCoordinator.removeDisplayer(displayer);
        displayer.refreshOff();
        displayer.close();
    }

    private PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonMarshaller.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", json);
        params.put("edit", "true");
        params.put("clone", "true");
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }
}
