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
package org.dashbuilder.client.gallery;

import com.google.gwt.user.client.ui.SimplePanel;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.DisplayerSettingsEditorImpl;
import org.dashbuilder.displayer.client.DisplayerSettingsEditorListener;
import org.dashbuilder.displayer.client.DisplayerSettingsEditor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.client.DisplayerSettingsJSONSourceViewer;
import org.dashbuilder.displayer.client.DisplayerView;

/**
 * A DisplayerSettings gallery node.
 */
public class GalleryNodeDisplayer extends GalleryNode {

    private DisplayerSettingsEditor displayerSettingsEditor;

    private GalleryEditorType galleryEditorType;
    protected DisplayerSettings displayerSettings;

    public GalleryNodeDisplayer(String name, DisplayerSettings settings) {
        super(name);
        this.displayerSettings = settings;
    }

    public GalleryNodeDisplayer(String name, GalleryEditorType galleryEditorType, DisplayerSettings settings) {
        super(name);
        this.displayerSettings = settings;
        this.galleryEditorType = galleryEditorType;
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public void setDisplayerSettings(DisplayerSettings displayerSettings) {
        this.displayerSettings = displayerSettings;
    }

    protected Widget createWidget() {
        if (galleryEditorType == null) {
            return new DisplayerView(displayerSettings).draw();
        }

        switch (galleryEditorType) {
            case FORM: displayerSettingsEditor = new DisplayerSettingsEditorImpl(); break;
            case JSON: displayerSettingsEditor = new DisplayerSettingsJSONSourceViewer();
        }

        displayerSettingsEditor.setDisplayerSettings( displayerSettings );

        SimplePanel editorPanel = new SimplePanel();
        editorPanel.setWidth("500px");
        editorPanel.add(displayerSettingsEditor);

        final SimplePanel viewerPanel = new SimplePanel();
        viewerPanel.add(new DisplayerView(displayerSettings).draw());

        displayerSettingsEditor.setListener(new DisplayerSettingsEditorListener() {
            public void onDisplayerSettingsChanged(DisplayerSettingsEditor editor) {
                viewerPanel.clear();
                viewerPanel.setWidget(new DisplayerView(displayerSettings).draw());
            }
        });

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(editorPanel);
        panel.add(viewerPanel);
        return panel;
    }
}
