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
package org.dashbuilder.client.editor;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.kie.uberfire.perspective.editor.client.api.ExternalPerspectiveEditorComponent;

@Dependent
public class DisplayerPerspectiveEditorComponent implements ExternalPerspectiveEditorComponent {

    private DisplayerEditor editor;
    private DisplayerSettingsJSONMarshaller jsonMarshaller;

    @PostConstruct
    public void setup() {
        editor = new DisplayerEditor();
        jsonMarshaller = new DisplayerSettingsJSONMarshaller();
    }

    @Override
    public void setup(String placeName, Map<String,String> parameters) {
        String json = parameters.get("json");
        DisplayerSettings settings = jsonMarshaller.fromJsonString(json);
        editor.init(settings, null);
    }

    @Override
    public String getPlaceName() {
        return "Displayer";
    }

    @Override
    public Map<String,String> getParametersMap() {
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", jsonMarshaller.toJsonString(editor.getDisplayerSettings()));
        return params;
    }

    @Override
    public IsWidget getConfig() {
        return editor;
    }

    @Override
    public IsWidget getPreview( Map<String, String> parameters ) {
        return null;
    }
}
