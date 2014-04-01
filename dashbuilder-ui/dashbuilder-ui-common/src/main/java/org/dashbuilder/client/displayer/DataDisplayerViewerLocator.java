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
 * The locator service for DataDisplayerViewer implementations.
 */
@ApplicationScoped
public class DataDisplayerViewerLocator {

    @Inject SyncBeanManager beanManager;

    /**
     * Get the viewer component for the specified data displayer.
     */
    public DataDisplayerViewer lookupViewer(DataDisplayer target) throws Exception {
        String type = target.getType();
        String lib = target.getRenderer();
        String beanName = lib + "_" + type + "_viewer";

        Collection<IOCBeanDef> beans = beanManager.lookupBeans(beanName);
        if (beans == null || beans.isEmpty()) throw new Exception("No data displayer viewer implementations found for: " + beanName);
        if (beans.size() > 1) throw new Exception("Multiple displayer viewer implementations found for: " + beanName);

        IOCBeanDef beanDef = beans.iterator().next();
        DataDisplayerViewer viewer = (DataDisplayerViewer) beanDef.getInstance();
        viewer.setDataDisplayer(target);
        return viewer;
    }
}