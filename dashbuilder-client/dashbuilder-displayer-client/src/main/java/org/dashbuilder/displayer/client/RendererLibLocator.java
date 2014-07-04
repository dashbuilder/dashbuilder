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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.DataDisplayer;
import org.dashbuilder.displayer.DataDisplayerType;
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

    private Map<DataDisplayerType,String> defaultRenderers = new HashMap<DataDisplayerType,String>();

    public void setDefaultRenderer(DataDisplayerType displayerType, String rendererName) {
        defaultRenderers.put(displayerType, rendererName);
    }

    public String getDefaultRenderer(DataDisplayerType displayerType) {
        return defaultRenderers.get(displayerType);
    }

    public RendererLibrary lookupRenderer(DataDisplayer target) {
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