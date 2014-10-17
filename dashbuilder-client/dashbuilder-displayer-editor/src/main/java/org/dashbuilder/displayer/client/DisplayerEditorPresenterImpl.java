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

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.prototypes.DisplayerPrototypes;

@Dependent
public class DisplayerEditorPresenterImpl implements DisplayerEditorPresenter {

    @Inject DisplayerEditorView view;
    @Inject DisplayerPrototypes prototypes;

    private DisplayerEditorListener editorListener = null;
    private DisplayerSettings originalSettings = null;
    private DisplayerSettings currentSettings = null;

    public void init(DisplayerSettings settings, DisplayerEditorListener editorListener) {
        this.originalSettings = settings;
        this.editorListener = editorListener;

        if (settings != null) currentSettings = settings.cloneInstance();
        else currentSettings = DisplayerPrototypes.BAR_CHART_PROTO.cloneInstance();

        view.init(this);
    }

    public boolean isEditing(DisplayerSettings settings) {
        return originalSettings != null && originalSettings.getUUID().equals(settings.getUUID());
    }

    public boolean isCurrentDisplayerReady() {
        return true;
    }

    public DisplayerEditorView getView() {
        return view;
    }

    public DisplayerSettings getOriginalSettings() {
        return originalSettings;
    }

    public DisplayerSettings getCurrentSettings() {
        return currentSettings;
    }

    public void update() {
        if (editorListener != null) {
            editorListener.onDisplayerUpdated(this);
        }
        close();
    }

    public void close() {
        if (editorListener != null) {
            editorListener.onEditorClosed(this);
        }
    }
}
