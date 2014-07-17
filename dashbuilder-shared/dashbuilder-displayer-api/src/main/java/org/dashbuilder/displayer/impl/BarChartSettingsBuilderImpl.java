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
package org.dashbuilder.displayer.impl;

import org.dashbuilder.displayer.BarChartSettingsBuilder;
import org.dashbuilder.displayer.BarChartDisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettings;

public class BarChartSettingsBuilderImpl extends AbstractXAxisChartSettingsBuilder<BarChartSettingsBuilderImpl> implements BarChartSettingsBuilder<BarChartSettingsBuilderImpl> {

    protected DisplayerSettings createDisplayerSettings() {
        return new BarChartDisplayerSettings();
    }

    public BarChartSettingsBuilderImpl set3d(boolean b) {
        BarChartDisplayerSettings d = (BarChartDisplayerSettings ) displayerSettings;
        d.set3d(b);
        return this;
    }

    public BarChartSettingsBuilderImpl horizontal() {
        BarChartDisplayerSettings d = (BarChartDisplayerSettings ) displayerSettings;
        d.setHorizontal(true);
        return this;
    }

    public BarChartSettingsBuilderImpl vertical() {
        BarChartDisplayerSettings d = (BarChartDisplayerSettings ) displayerSettings;
        d.setHorizontal(false);
        return this;
    }
}
