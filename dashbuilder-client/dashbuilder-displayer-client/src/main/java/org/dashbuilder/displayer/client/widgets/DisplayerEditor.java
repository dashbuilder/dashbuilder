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
package org.dashbuilder.displayer.client.widgets;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;

@Dependent
public class DisplayerEditor implements
        DisplayerTypeSelector.Presenter,
        DisplayerSettingsEditorForm.Presenter {

    DisplayerEditorView view;
    DisplayerPrototypes prototypes;

    private DisplayerEditorListener editorListener = null;
    private DisplayerSettings originalSettings = null;
    private DisplayerSettings currentSettings = null;
    private boolean brandNewDisplayer = true;

    public DisplayerEditor() {
        this.prototypes = DisplayerPrototypes.get();
        this.view = new DisplayerEditorView(
                new DisplayerTypeSelector(),
                new DisplayerSettingsEditorForm());
    }

    @Inject
    public DisplayerEditor(
            DisplayerEditorView view,
            DisplayerPrototypes prototypes) {

        this.view = view;
        this.prototypes = prototypes;
    }

    public void init(DisplayerSettings settings, DisplayerEditorListener editorListener) {

        this.originalSettings = settings;
        this.editorListener = editorListener;

        if (settings != null) {
            brandNewDisplayer = false;
            currentSettings = settings.cloneInstance();
            view.init(this);
            view.disableTypeSelection();
            view.gotoDisplaySettings();
        } else {
            brandNewDisplayer = true;
            currentSettings = prototypes.getProto(DisplayerType.BARCHART).cloneInstance();
            view.init(this);
            view.gotoTypeSelection();
        }
    }

    public void displayerSettingsChanged(DisplayerSettings settings) {
        currentSettings = settings;
        view.showDisplayer();
    }

    public void changeDisplayerType(DisplayerType type) {
        // Rest the current settings
        currentSettings = prototypes.getProto(type).cloneInstance();

        // Show the new displayer
        view.showDisplayer();
    }

    public boolean isEditing(DisplayerSettings settings) {
        return originalSettings != null && originalSettings.getUUID().equals(settings.getUUID());
    }

    public boolean isCurrentDisplayerReady() {
        return true;
    }

    public boolean isBrandNewDisplayer() {
        return brandNewDisplayer;
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

    public void save() {
        if (editorListener != null) {
            editorListener.onDisplayerSaved(this);
        }
    }

    public void close() {
        if (editorListener != null) {
            editorListener.onEditorClosed(this);
        }
    }
}
