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

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.events.DisplayerSettingsChangedEvent;
import org.dashbuilder.displayer.events.DisplayerSettingsOnCloseEvent;
import org.dashbuilder.displayer.events.DisplayerSettingsOnEditEvent;
import org.dashbuilder.displayer.events.DisplayerSettingsOnFocusEvent;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@WorkbenchScreen(identifier = "DisplayerSettingsEditor")
@Dependent
public class DisplayerSettingsEditorPresenter {

    private DisplayerSettings displayerSettings;

    @Inject private DisplayerSettingsJSONMarshaller jsonMarshaller;

    @Inject private DisplayerSettingsEditorForm settingsEditor;

    @Inject private Event<DisplayerSettingsChangedEvent> settingsChangedEvent;

    @Inject private PlaceManager placeManager;

    private PlaceRequest placeRequest;

    @OnStartup
    public void onStartup(PlaceRequest placeRequest) {
        this.placeRequest = placeRequest;
        String json = placeRequest.getParameter("json", "");
        if (!StringUtils.isBlank(json)) {
            setDisplayerSettings(jsonMarshaller.fromJsonString(json));
        }
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Displayer Settings";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return settingsEditor;
    }

    @DefaultPosition
    public Position getDefaultPosition() {
        return CompassPosition.EAST;
    }

    public void setDisplayerSettings(DisplayerSettings displayerSettings) {
        this.displayerSettings = displayerSettings;

        settingsEditor.setDisplayerSettings(displayerSettings);
        settingsEditor.setListener(new DisplayerSettingsEditorListener() {
            public void onDisplayerSettingsChanged(DisplayerSettingsEditor editor) {
                settingsChangedEvent.fire(new DisplayerSettingsChangedEvent(editor.getDisplayerSettings()));
            }
        });
    }

    private void onDisplayerSettingsEditEvent(@Observes DisplayerSettingsOnEditEvent event) {
        checkNotNull("event", event);
        checkNotNull("settings", event.getDisplayerSettings());

        setDisplayerSettings(event.getDisplayerSettings());
    }

    private void onDisplayerSettingsFocusEvent(@Observes DisplayerSettingsOnFocusEvent event) {
        checkNotNull("event", event);
        checkNotNull("settings", event.getDisplayerSettings());

        // Only change current settings if it was in edition mode already.
        if (displayerSettings != null) {
            setDisplayerSettings(event.getDisplayerSettings());
        }
    }

    private void onDisplayerSettingsCloseEvent(@Observes DisplayerSettingsOnCloseEvent event) {
        checkNotNull("event", event);
        checkNotNull("settings", event.getDisplayerSettings());

        if (displayerSettings.getUUID().equals(event.getDisplayerSettings().getUUID())) {
            displayerSettings = null;
            placeManager.forceClosePlace(placeRequest);
        }
    }
}
