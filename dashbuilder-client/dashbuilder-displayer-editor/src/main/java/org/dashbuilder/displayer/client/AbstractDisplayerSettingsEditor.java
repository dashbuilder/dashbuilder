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

import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.displayer.DisplayerSettings;

/**
 * Base class for implementing data displayer editor widgets.
 */
public abstract class AbstractDisplayerSettingsEditor<T extends DisplayerSettings> extends Composite implements DisplayerSettingsEditor<T> {

    protected T displayerSettings;
    protected DisplayerSettingsEditorListener listener;

    public T getDisplayerSettings() {
        return displayerSettings;
    }

    public void setDisplayerSettings(T displayerSettings) {
        this.displayerSettings = displayerSettings;
    }

    public DisplayerSettingsEditorListener getListener() {
        return listener;
    }

    public void setListener(DisplayerSettingsEditorListener listener) {
        this.listener = listener;
    }

    protected void notifyChanges() {
        listener.onDisplayerSettingsChanged(this);
    }
}