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
package org.dashbuilder.client.displayer;

import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.model.displayer.DataDisplayer;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

/**
 * The locator service for DataViewer implementations.
 */
@ApplicationScoped
public class DataViewerLocator {

    @Inject SyncBeanManager beanManager;

    public RendererLibrary lookupRenderer(String renderer) {
        if (renderer == null) renderer = "google";
        String beanName = renderer + "_renderer";

        Collection<IOCBeanDef> beans = beanManager.lookupBeans(beanName);
        if (beans == null || beans.isEmpty()) throw new RuntimeException(renderer + " renderer not found.");
        if (beans.size() > 1) throw new RuntimeException("Multiple renderer implementations found for: " + renderer);

        IOCBeanDef beanDef = beans.iterator().next();
        return (RendererLibrary) beanDef.getInstance();
    }

    /**
     * Get the viewer component for the specified data displayer.
     */
    public DataViewer lookupViewer(DataDisplayer target) {
        RendererLibrary renderer = lookupRenderer(target.getRenderer());
        DataViewer viewer = renderer.lookupViewer(target);
        if (viewer == null) throw new RuntimeException(target.getType() + " displayer not supported in " + target.getRenderer() + " renderer.");

        viewer.setDataDisplayer(target);
        return viewer;
    }
}