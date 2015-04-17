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
package org.dashbuilder.renderer.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.RendererLibLocator;
import org.dashbuilder.renderer.client.metric.MetricDisplayer;
import org.dashbuilder.renderer.client.selector.SelectorDisplayer;
import org.dashbuilder.renderer.client.table.TableDisplayer;

/**
 * Default renderer
 */
@ApplicationScoped
@Named(DefaultRenderer.UUID + "_renderer")
public class DefaultRenderer extends AbstractRendererLibrary {

    public static final String UUID = "Default";

    @PostConstruct
    private void init() {
        RendererLibLocator.get().registerRenderer(DisplayerType.TABLE, UUID, false);
        RendererLibLocator.get().registerRenderer(DisplayerType.SELECTOR, UUID, true);
        RendererLibLocator.get().registerRenderer(DisplayerType.METRIC, UUID, true);
    }

    @Override
    public String getUUID() {
        return UUID;
    }

    // TODO complete
    @Override
    public DisplayerType.DisplayerSubType[] getSupportedDisplayerSubtypes(DisplayerType displayerType) {
        return null;
    }

    @Override
    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        DisplayerType type = displayerSettings.getType();
        if (DisplayerType.TABLE.equals(type)) return new TableDisplayer();
        if (DisplayerType.SELECTOR.equals(type)) return new SelectorDisplayer();
        if (DisplayerType.METRIC.equals(type)) return new MetricDisplayer();

        return null;
    }
}
