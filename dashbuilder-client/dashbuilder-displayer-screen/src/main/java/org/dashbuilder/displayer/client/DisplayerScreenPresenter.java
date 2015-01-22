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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorPopup;
import org.dashbuilder.displayer.client.widgets.DisplayerView;
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

@WorkbenchScreen(identifier = "DisplayerScreen")
@Dependent
public class DisplayerScreenPresenter {

    private DisplayerView displayerView;
    private PerspectiveCoordinator perspectiveCoordinator;
    private DisplayerSettingsJSONMarshaller jsonMarshaller;
    private DisplayerSettings displayerSettings;
    private Menus menu = null;
    private boolean editEnabled = false;

    @Inject
    public DisplayerScreenPresenter(DisplayerView displayerView,
            PerspectiveCoordinator perspectiveCoordinator,
            DisplayerSettingsJSONMarshaller jsonMarshaller) {
        this.displayerView = displayerView;
        this.perspectiveCoordinator = perspectiveCoordinator;
        this.jsonMarshaller = jsonMarshaller;
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest) {
        String json = placeRequest.getParameter("json", "");
        if (!StringUtils.isBlank(json)) this.displayerSettings = jsonMarshaller.fromJsonString(json);
        if (displayerSettings == null) throw new IllegalArgumentException("Displayer settings not found.");

        // Draw the Displayer.
        if (StringUtils.isBlank(displayerSettings.getUUID())) displayerSettings.setUUID(Document.get().createUniqueId());
        displayerView.setDisplayerSettings(displayerSettings);
        Displayer displayer = displayerView.draw();

        // Register the Displayer into the coordinator.
        perspectiveCoordinator.addDisplayer(displayer);

        // Check edit mode
        String edit = placeRequest.getParameter("edit", "false");
        editEnabled = Boolean.parseBoolean(edit);
        if (editEnabled) this.menu = makeMenuBar();
    }

    @OnClose
    public void onClose() {
        Displayer displayer = displayerView.getDisplayer();
        perspectiveCoordinator.removeDisplayer(displayer);
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
        return MenuFactory
                .newTopLevelMenu("Edit")
                .respondsWith(getEditCommand())
                .endMenu().build();
    }

    private Command getEditCommand() {
        return new Command() {
            public void execute() {
                DisplayerEditorPopup displayerEditor =  new DisplayerEditorPopup();
                displayerEditor.init(displayerSettings, new DisplayerEditor.Listener() {

                    public void onClose(DisplayerEditor editor) {
                    }

                    public void onSave(DisplayerEditor editor) {
                        updateDisplayer(editor.getDisplayerSettings());
                    }
                });
            }
        };
    }

    private void updateDisplayer(DisplayerSettings settings) {
        this.displayerSettings = settings;
        this.displayerView.setDisplayerSettings(settings);

        Displayer oldDisplayer = this.displayerView.getDisplayer();
        Displayer newDisplayer = this.displayerView.draw();

        this.perspectiveCoordinator.removeDisplayer(oldDisplayer);
        this.perspectiveCoordinator.addDisplayer(newDisplayer);
    }
}
