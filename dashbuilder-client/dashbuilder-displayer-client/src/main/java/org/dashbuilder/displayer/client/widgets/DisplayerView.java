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

import com.google.gwt.user.client.ui.*;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

public class DisplayerView extends Composite {

    private static final String RENDERER_SELECTOR_WIDTH = "300px";
    
    protected DisplayerSettings displayerSettings;
    protected Panel container = new FlowPanel();
    protected Label label = new Label();
    protected Displayer displayer;
    protected Boolean isShowRendererSelector = false;

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

    public void setIsShowRendererSelector(Boolean isShowRendererSelector) {
        this.isShowRendererSelector = isShowRendererSelector;
    }

    public Displayer getDisplayer() {
        return displayer;
    }

    public Displayer draw() {
        try {
            checkNotNull("displayerSettings", displayerSettings);
            
            // Lookup the displayer.
            displayer = DisplayerHelper.lookupDisplayer(displayerSettings);
            DisplayerHelper.draw(displayer);

            // Add the displayer into a container
            container.clear();
            final FlowPanel displayerContainer = new FlowPanel();
            displayerContainer.add(displayer);

            // Add the renderer selector (if enabled)
            if (isShowRendererSelector) {
                RendererSelector rendererSelector = new RendererSelector(displayerSettings, RendererSelector.SelectorType.TAB, new RendererSelector.RendererSelectorEventHandler() {

                    public void onRendererSelected(RendererSelector.RendererSelectorEvent event) {
                        displayerSettings.setRenderer(event.getRenderer());
                        displayer = DisplayerHelper.lookupDisplayer(displayerSettings);
                        DisplayerHelper.draw(displayer);

                        displayerContainer.clear();
                        displayerContainer.add(displayer);
                    }
                });
                
                rendererSelector.setWidth(RENDERER_SELECTOR_WIDTH);
                container.add(rendererSelector);
            }
            container.add(displayerContainer);
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
