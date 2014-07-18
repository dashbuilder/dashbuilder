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
import org.dashbuilder.displayer.client.DisplayerSettingsEditorListener;
import org.dashbuilder.displayer.client.DisplayerSettingsEditorLocator;
import org.dashbuilder.displayer.client.DisplayerSettingsEditor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.kpi.client.KPIViewer;
import org.dashbuilder.kpi.KPI;

/**
 * A KPI gallery node.
 */
public class GalleryNodeKPI extends GalleryNode {

    protected KPI kpi;
    protected boolean editEnabled = false;

    public GalleryNodeKPI(String name, KPI kpi) {
        super(name);
        this.kpi = kpi;
    }

    public GalleryNodeKPI(String name, boolean editEnabled, KPI kpi) {
        super(name);
        this.kpi = kpi;
        this.editEnabled = editEnabled;
    }

    public KPI getKpi() {
        return kpi;
    }

    public void setKpi(KPI kpi) {
        this.kpi = kpi;
    }

    public boolean isEditEnabled() {
        return editEnabled;
    }

    public void setEditEnabled(boolean editEnabled) {
        this.editEnabled = editEnabled;
    }

    protected Widget createWidget() {
        if (!isEditEnabled()) {
            return new KPIViewer(kpi).draw();
        }

        DisplayerSettingsEditor settingsEditor = DisplayerSettingsEditorLocator.get().lookupSettingsEditor(kpi.getDisplayerSettings());

        SimplePanel editorPanel = new SimplePanel();
        editorPanel.setWidth("500px");
        editorPanel.add(settingsEditor);

        final SimplePanel viewerPanel = new SimplePanel();
        viewerPanel.add(new KPIViewer( kpi ).draw());

        settingsEditor.setListener(new DisplayerSettingsEditorListener() {
            public void onDisplayerSettingChanged(DisplayerSettingsEditor editor) {
                viewerPanel.clear();
                viewerPanel.setWidget( new KPIViewer( kpi ).draw() );
            }
        });

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(editorPanel);
        panel.add(viewerPanel);
        return panel;
    }
}
