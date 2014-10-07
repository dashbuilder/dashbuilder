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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

/**
 * The locator service for those installed RendererLibrary implementations.
 */
@ApplicationScoped
public class RendererLibLocator {

    public static RendererLibLocator get() {
        Collection<IOCBeanDef<RendererLibLocator>> beans = IOC.getBeanManager().lookupBeans(RendererLibLocator.class);
        IOCBeanDef<RendererLibLocator> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }

    @Inject SyncBeanManager beanManager;

    private Map<DisplayerType,String> defaultRenderers = new HashMap<DisplayerType,String>();

    @PostConstruct
    private void init() {
        // Set the default renderer lib for each displayer type.
        setDefaultRenderer(DisplayerType.BARCHART, "google");
        setDefaultRenderer(DisplayerType.PIECHART, "google");
        setDefaultRenderer(DisplayerType.AREACHART, "google");
        setDefaultRenderer(DisplayerType.LINECHART, "google");
        setDefaultRenderer(DisplayerType.BUBBLECHART, "google");
        setDefaultRenderer(DisplayerType.METERCHART, "google");
        setDefaultRenderer(DisplayerType.MAP, "google");
        setDefaultRenderer(DisplayerType.TABLE, "google");
        setDefaultRenderer(DisplayerType.SELECTOR, "selector");
    }

    public void setDefaultRenderer(DisplayerType displayerType, String rendererName) {
        defaultRenderers.put(displayerType, rendererName);
    }

    public String getDefaultRenderer(DisplayerType displayerType) {
        return defaultRenderers.get(displayerType);
    }

    public RendererLibrary lookupRenderer(DisplayerSettings target) {
        // Get the renderer specified for the displayer.
        String renderer = target.getRenderer();

        // If none then get the default renderer specified.
        if (renderer == null) renderer = getDefaultRenderer(target.getType());
        return lookupRenderer(renderer);
    }

    public RendererLibrary lookupRenderer(String renderer) {

        // Take the google renderer as the default.
        if (renderer == null) renderer = "google";

        // Lookup the renderer library.
        String beanName = renderer + "_renderer";
        Collection<IOCBeanDef> beans = beanManager.lookupBeans(beanName);
        if (beans == null || beans.isEmpty()) throw new RuntimeException(renderer + " renderer not found.");
        if (beans.size() > 1) throw new RuntimeException("Multiple renderer implementations found for: " + renderer);

        IOCBeanDef beanDef = beans.iterator().next();
        return (RendererLibrary) beanDef.getInstance();
    }
}