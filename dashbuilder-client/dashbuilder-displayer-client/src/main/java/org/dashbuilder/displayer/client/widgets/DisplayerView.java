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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.AbstractDisplayerListener;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.DisplayerListener;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

public class DisplayerView extends Composite {

    private static final String RENDERER_SELECTOR_WIDTH = "300px";

    protected DisplayerSettings displayerSettings;
    protected Panel container = new FlowPanel();
    protected Label label = new Label();
    protected Displayer displayer;
    protected Boolean isShowRendererSelector = false;
    protected DisplayerError errorWidget = new DisplayerError();
    protected boolean error = true;

    DisplayerListener displayerListener = new AbstractDisplayerListener() {
        public void onDraw(Displayer displayer) {
            if (error) {
                show();
            }
        }
        public void onRedraw(Displayer displayer) {
            if (error) {
                show();
            }
        }
        public void onError(Displayer displayer, ClientRuntimeError error) {
            error(error);
        }
    };

    public DisplayerView() {
        initWidget(container);
    }

    public DisplayerView(DisplayerSettings settings) {
        this();
        setDisplayerSettings(settings);
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public void setDisplayerSettings(DisplayerSettings displayerSettings) {
        this.displayerSettings = displayerSettings;
        init();
    }

    public void setIsShowRendererSelector(Boolean isShowRendererSelector) {
        this.isShowRendererSelector = isShowRendererSelector;
    }

    public Displayer getDisplayer() {
        return displayer;
    }

    public void init() {
        try {
            // Lookup the displayer
            checkNotNull("displayerSettings", displayerSettings);
            this.displayer = DisplayerHelper.lookupDisplayer(displayerSettings);
            this.displayer.addListener(displayerListener);

            // Make the displayer visible
            show();
        }
        catch (Exception e) {
            error(new ClientRuntimeError(e));
        }
    }

    protected void show() {
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
                    displayer.draw();

                    displayerContainer.clear();
                    displayerContainer.add(displayer);
                }
            });

            rendererSelector.setWidth(RENDERER_SELECTOR_WIDTH);
            container.add(rendererSelector);
        }
        container.add(displayerContainer);
        error = false;
    }

    public Displayer draw() {
        try {
            // Draw the displayer
            displayer.draw();
        }
        catch (Exception e) {
            error(new ClientRuntimeError(e));
        }
        return displayer;
    }

    public Displayer redraw() {
        try {
            checkNotNull("displayerSettings", displayerSettings);
            checkNotNull("displayer", displayer);

            displayer.setDisplayerSettings(displayerSettings);
            displayer.redraw();
        } catch (Exception e) {
            error(new ClientRuntimeError(e));
        }
        return displayer;
    }

    public void error(ClientRuntimeError e) {
        container.clear();
        container.add(errorWidget);
        errorWidget.show(e.getMessage(), e.getCause());

        error = true;
        GWT.log(e.getMessage(), e.getThrowable());
    }
}
