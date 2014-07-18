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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.DataSetRef;
import org.dashbuilder.displayer.DisplayerSettings;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

/**
 * The locator service for Displayer implementations.
 */
@ApplicationScoped
public class DisplayerLocator {

    public static DisplayerLocator get() {
        Collection<IOCBeanDef<DisplayerLocator>> beans = IOC.getBeanManager().lookupBeans(DisplayerLocator.class);
        IOCBeanDef<DisplayerLocator> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }

    @Inject DataSetHandlerLocator handlerLocator;

    /**
     * Get the displayer component for the specified data displayer (with no data set attached).
     */
    public Displayer lookupDisplayer(DisplayerSettings target) {
        RendererLibrary renderer = RendererLibLocator.get().lookupRenderer(target);
        Displayer displayer = renderer.lookupDisplayer(target);
        if (displayer == null) throw new RuntimeException(target.getType() + " displayer not supported in " + target.getRenderer() + " renderer.");

        displayer.setDisplayerSettings( target );
        return displayer;
    }

    /**
     * Get the displayer component for the specified data displayer and attach it to the specified data set ref.
     */
    public Displayer lookupDisplayer(DataSetRef dataSetRef, DisplayerSettings target) {
        Displayer displayer = lookupDisplayer(target);
        DataSetHandler handler = handlerLocator.lookupHandler(dataSetRef);
        displayer.setDataSetHandler(handler);
        return displayer;
    }
}