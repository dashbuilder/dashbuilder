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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.events.DisplayerEditedEvent;
import org.dashbuilder.displayer.events.DisplayerClosedEvent;
import org.dashbuilder.displayer.events.DisplayerOnFocusEvent;
import org.dashbuilder.displayer.events.DisplayerUpdatedEvent;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnFocus;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@WorkbenchScreen(identifier = "DisplayerScreen")
@Dependent
public class DisplayerPresenter {

    private DisplayerView displayerView;
    private PerspectiveCoordinator perspectiveCoordinator;
    private DisplayerSettingsJSONMarshaller jsonMarshaller;
    private PlaceManager placeManager;
    private DisplayerSettings displayerSettings;
    private Event<DisplayerOnFocusEvent> displayerOnFocusEvent;
    private Event<DisplayerEditedEvent> displayerOnEditEvent;
    private Event<DisplayerClosedEvent> displayerOnCloseEvent;
    private Menus menu = null;
    private boolean editEnabled = false;

    @Inject
    public DisplayerPresenter(DisplayerView displayerView,
            PerspectiveCoordinator perspectiveCoordinator,
            DisplayerSettingsJSONMarshaller jsonMarshaller,
            Event<DisplayerOnFocusEvent> displayerOnFocusEvent,
            Event<DisplayerEditedEvent> displayerOnEditEvent,
            Event<DisplayerClosedEvent> displayerOnCloseEvent,
            PlaceManager placeManager) {
        this.displayerView = displayerView;
        this.perspectiveCoordinator = perspectiveCoordinator;
        this.jsonMarshaller = jsonMarshaller;
        this.placeManager = placeManager;
        this.displayerOnFocusEvent = displayerOnFocusEvent;
        this.displayerOnEditEvent = displayerOnEditEvent;
        this.displayerOnCloseEvent = displayerOnCloseEvent;
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

    @WorkbenchPartTitle
    public String getTitle() {
        return displayerSettings.getTitle() + (editEnabled ? " editor" : "");
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return displayerView;
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return menu;
    }

    @OnFocus
    public void onFocus() {
        displayerOnFocusEvent.fire(new DisplayerOnFocusEvent(displayerSettings));
    }

    @OnClose
    public void onClose() {
        displayerOnCloseEvent.fire(new DisplayerClosedEvent(displayerSettings));
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
                placeManager.goTo("DisplayerEditor");
                displayerOnEditEvent.fire(new DisplayerEditedEvent(displayerSettings));
            }
        };
    }

    /**
     * Be aware of changes in the displayer settings.
     */
    private void onDisplayerUpdated(@Observes DisplayerUpdatedEvent event) {
        checkNotNull("event", event);
        checkNotNull("settings", event.getModifiedSettings());

        DisplayerSettings settings = event.getModifiedSettings();
        if (displayerSettings != null && settings.getUUID().equals(displayerSettings.getUUID())) {
            Displayer oldDisplayer = this.displayerView.getDisplayer();
            this.displayerSettings = settings;
            this.displayerView.setDisplayerSettings(displayerSettings);
            Displayer newDisplayer = this.displayerView.draw();

            this.perspectiveCoordinator.removeDisplayer(oldDisplayer);
            this.perspectiveCoordinator.addDisplayer(newDisplayer);
        }
    }
}
