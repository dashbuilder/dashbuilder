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
package org.dashbuilder.client.screens;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.DisplayerView;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnStartup;

import static org.dashbuilder.dataset.date.Month.*;

@ApplicationScoped
@WorkbenchScreen(identifier = "StaticChartScreen")
public class StaticChartScreen {

    public static final DisplayerSettings displayerSettings =
            DisplayerSettingsFactory.newLineChartSettings()
            .dataset("static_chart_sample")
            .title("Sales Evolution Per Year")
            .column("Month")
            .column("Sales in 2013")
            .column("Sales in 2014")
            .column("Sales in 2015")
            .dataset(DataSetFactory.newDataSetBuilder()
                .label("month")
                .number("2012")
                .number("2013")
                .number("2014")
                .row(JANUARY, 1000d, 2000d, 3000d)
                .row(FEBRUARY, 1400d, 2300d, 2000d)
                .row(MARCH, 1300d, 2000d, 1400d)
                .row(APRIL, 900d, 2100d, 1500d)
                .row(MAY, 1300d, 2300d, 1600d)
                .row(JUNE, 1010d, 2000d, 1500d)
                .row(JULY, 1050d, 2400d, 3000d)
                .row(AUGUST, 2300d, 2000d, 3200d)
                .row(SEPTEMBER, 1900d, 2700d, 3000d)
                .row(OCTOBER, 1200d, 2200d, 3100d)
                .row(NOVEMBER, 1400d, 2100d, 3100d)
                .row(DECEMBER, 1100d, 2100d, 4200d)
                .buildDataSet())
            .buildSettings();

    DisplayerView displayerView;

    @OnStartup
    public void init() {
        DisplayerView displayerView = new DisplayerView(displayerSettings);
        displayerView.draw();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Static Chart";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return displayerView;
    }
}