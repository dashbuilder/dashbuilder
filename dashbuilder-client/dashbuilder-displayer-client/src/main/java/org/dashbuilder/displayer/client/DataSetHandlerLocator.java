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

import org.dashbuilder.dataset.client.ClientDataSetManager;
import org.dashbuilder.dataset.client.DataSetLookupClient;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetRef;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

/**
 * The locator service for DataSetHandler implementations.
 */
@ApplicationScoped
public class DataSetHandlerLocator {

    public static DataSetHandlerLocator get() {
        Collection<IOCBeanDef<DataSetHandlerLocator>> beans = IOC.getBeanManager().lookupBeans(DataSetHandlerLocator.class);
        IOCBeanDef<DataSetHandlerLocator> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }

    @Inject DataSetLookupClient dataSetLookupClient;
    @Inject ClientDataSetManager clientDataSetManager;

    /**
     * Get the operation handler component for the specified data set reference.
     */
    public DataSetHandler lookupHandler(DataSetRef ref) {
        if (ref instanceof DataSet) {
            DataSet dataSet = (DataSet) ref;
            clientDataSetManager.registerDataSet(dataSet);
            DataSetLookup lookup = new DataSetLookup(dataSet.getUUID());
            return new DataSetHandlerImpl(dataSetLookupClient, lookup);
        }
        if (ref instanceof DataSetLookup) {
            return new DataSetHandlerImpl(dataSetLookupClient, (DataSetLookup) ref);
        }
        throw new IllegalArgumentException("DataSetRef implementation not supported: " + ref.getClass().getName());
    }
}