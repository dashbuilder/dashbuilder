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
import org.dashbuilder.displayer.events.DisplayerEditedEvent;
import org.dashbuilder.displayer.events.DisplayerUpdatedEvent;
import org.dashbuilder.displayer.events.DisplayerClosedEvent;
import org.dashbuilder.displayer.events.DisplayerOnFocusEvent;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@WorkbenchPopup(identifier = "DisplayerEditor")
@Dependent
public class DisplayerEditorPopup {

    @Inject private DisplayerEditorPresenter displayerEditorPresenter;

    @Inject private DisplayerSettingsJSONMarshaller jsonMarshaller;

    @Inject Event<DisplayerUpdatedEvent> displayerUpdatedEvent;

    @Inject private PlaceManager placeManager;

    private PlaceRequest placeRequest;

    private DisplayerSettings displayerSettings;

    @WorkbenchPartTitle
    public String getTitle() {
        if (displayerSettings == null) return "New Displayer";
        return "Displayer Editor";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return displayerEditorPresenter.getView();
    }

    @OnStartup
    public void init(PlaceRequest placeRequest) {
        this.placeRequest = placeRequest;
        String json = placeRequest.getParameter("json", "");

        if (!StringUtils.isBlank(json)) {
            // Edit existing displayer
            init(jsonMarshaller.fromJsonString(json));
        } else {
            // Create a brand new one
            init((DisplayerSettings) null);
        }
    }

    private void init(DisplayerSettings settings) {
        displayerSettings = settings;
        displayerEditorPresenter.init(settings, new DisplayerEditorListener() {

            public void onEditorClosed(DisplayerEditorPresenter editor) {
                placeManager.forceClosePlace(placeRequest);
            }

            public void onDisplayerUpdated(DisplayerEditorPresenter editor) {
                displayerUpdatedEvent.fire(new DisplayerUpdatedEvent(editor.getOriginalSettings(), editor.getCurrentSettings()));
            }
        });
    }

    // Listen to DisplayerPresenter events

    private void onDisplayerFocusEvent(@Observes DisplayerOnFocusEvent event) {
        checkNotNull("event", event);
        checkNotNull("settings", event.getDisplayerSettings());

        // Only change current settings if it was in edition mode already.
        init(event.getDisplayerSettings());
    }

    private void onDisplayerClosedEvent(@Observes DisplayerClosedEvent event) {
        checkNotNull("event", event);
        checkNotNull("settings", event.getDisplayerSettings());

        if (displayerEditorPresenter.isEditing(event.getDisplayerSettings())) {
            displayerEditorPresenter.close();
        }
    }
}
