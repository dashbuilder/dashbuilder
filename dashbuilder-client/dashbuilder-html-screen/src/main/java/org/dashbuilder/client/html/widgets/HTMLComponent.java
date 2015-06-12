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
package org.dashbuilder.client.html.widgets;

import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.PerspectiveEditorComponent;
import org.dashbuilder.client.resources.i18n.HTMLScreenConstants;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class HTMLComponent implements PerspectiveEditorComponent {

    @Inject
    protected PerspectiveEditor perspectiveEditor;

    @Override
    public String getComponentName() {
        return HTMLScreenConstants.INSTANCE.htmlScreen();
    }

    @Override
    public void createNewInstance() {
        perspectiveEditor.openPlace(createPlaceRequest());
        perspectiveEditor.saveCurrentPerspective();
    }

    protected PlaceRequest createPlaceRequest() {
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", "");
        return new DefaultPlaceRequest(HTMLScreen.SCREEN_ID, params);
    }
    
}
