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

import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
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

    @Inject ClientDataSetManager clientDataSetManager;

    /**
     * Get the displayer component for the specified data displayer (with no data set attached).
     */
    public Displayer lookupDisplayer(DisplayerSettings target) {
        RendererLibrary renderer = RendererManager.get().getRendererForDisplayer(target);
        Displayer displayer = renderer.lookupDisplayer(target);
        if (displayer == null) {
            String rendererUuid = target.getRenderer();
            if (StringUtils.isBlank(rendererUuid)) throw new RuntimeException(CommonConstants.INSTANCE.displayerlocator_default_renderer_undeclared(target.getType().toString()));
            throw new RuntimeException(CommonConstants.INSTANCE.displayerlocator_unsupported_displayer_renderer(target.getType().toString(), rendererUuid));
        }
        displayer.setDisplayerSettings( target );

        // Check if a DataSet has been set instead of a DataSetLookup.
        DataSetLookup dataSetLookup = target.getDataSetLookup();
        if (target.getDataSet() != null) {
            DataSet dataSet = target.getDataSet();
            clientDataSetManager.registerDataSet(dataSet);
            dataSetLookup = new DataSetLookup(dataSet.getUUID());
        }

        DataSetHandler handler = new DataSetHandlerImpl(dataSetLookup);
        displayer.setDataSetHandler(handler);
        return displayer;
    }
}