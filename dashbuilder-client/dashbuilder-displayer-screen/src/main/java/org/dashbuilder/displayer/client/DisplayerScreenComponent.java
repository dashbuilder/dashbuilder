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
package org.dashbuilder.displayer.client;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.PerspectiveEditorComponent;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorPopup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

@ApplicationScoped
public class DisplayerScreenComponent implements PerspectiveEditorComponent {

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Inject
    protected DisplayerSettingsJSONMarshaller jsonMarshaller;

    @Override
    public String getComponentName() {
        return "Displayer";
    }

    @Override
    public void createNewInstance() {
        /* Displayer settings == null => Create a brand new displayer */
        DisplayerEditorPopup displayerEditor = new DisplayerEditorPopup();
        displayerEditor.init(null, new DisplayerEditor.Listener() {

            public void onClose(final DisplayerEditor editor) {
            }

            public void onSave(final DisplayerEditor editor) {
                perspectiveEditor.openPlace(createPlaceRequest(editor.getDisplayerSettings()));
                perspectiveEditor.saveCurrentPerspective();
            }
        });
    }

    protected PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonMarshaller.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", json);
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }
}
