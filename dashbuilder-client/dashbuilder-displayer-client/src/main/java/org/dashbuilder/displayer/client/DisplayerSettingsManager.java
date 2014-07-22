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

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.displayer.DisplayerSettings;

@ApplicationScoped
public class DisplayerSettingsManager {

    private List<DisplayerSettings> displayerSettingsList = new ArrayList<DisplayerSettings>();

    public List<DisplayerSettings> getAllDisplayerSettings() {
        return displayerSettingsList;
    }

    public DisplayerSettings addDisplayerSettings(DisplayerSettings displayerSettings) {
        displayerSettingsList.add(displayerSettings);
        return displayerSettings;
    }

    public DisplayerSettings getDisplayerSettings(String uuid) {
        for (DisplayerSettings settings : displayerSettingsList) {
            if (settings.getUUID().equals(uuid)){
                return settings;
            }
        }
        return null;
    }
}
