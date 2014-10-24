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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

public class DisplayerView extends Composite {

    protected DisplayerSettings displayerSettings;
    protected SimplePanel container = new SimplePanel();
    protected Label label = new Label();
    protected Displayer displayer;

    public DisplayerView() {
        initWidget(container);
    }

    public DisplayerView(DisplayerSettings settings) {
        this();
        this.displayerSettings = settings;
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public void setDisplayerSettings(DisplayerSettings displayerSettings) {
        this.displayerSettings = displayerSettings;
    }

    public Displayer getDisplayer() {
        return displayer;
    }

    public Displayer draw() {
        try {
            checkNotNull("displayerSettings", displayerSettings);

            displayer = DisplayerHelper.lookupDisplayer(displayerSettings);

            container.clear();
            container.add( displayer );

            DisplayerHelper.draw( displayer );
        } catch (Exception e) {
            displayMessage(e.getMessage());
        }
        return displayer;
    }

    public Displayer redraw() {
        try {
            checkNotNull("displayerSettings", displayerSettings);
            checkNotNull("displayer", displayer);

            displayer.setDisplayerSettings(displayerSettings);

            DisplayerHelper.redraw( displayer );
        } catch (Exception e) {
            displayMessage(e.getMessage());
        }
        return displayer;
    }

    private void displayMessage(String msg) {
        container.clear();
        container.add(label);
        label.setText(msg);
    }
}
