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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.client.dataset.ClientDataSetManager;
import org.dashbuilder.client.dataset.DataSetManagerProxy;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetRef;

/**
 * The locator service for DataSetHandler implementations.
 */
@ApplicationScoped
public class DataSetHandlerLocator {

    @Inject DataSetManagerProxy dataSetManagerProxy;
    @Inject ClientDataSetManager clientDataSetManager;

    /**
     * Get the operation handler component for the specified data set reference.
     */
    public DataSetHandler lookupHandler(DataSetRef ref) {
        if (ref instanceof DataSet) {
            DataSet dataSet = (DataSet) ref;
            clientDataSetManager.registerDataSet(dataSet);
            DataSetLookup lookup = new DataSetLookup(dataSet.getUUID());
            return new DataSetHandlerImpl(dataSetManagerProxy, lookup);
        }
        if (ref instanceof DataSetLookup) {
            return new DataSetHandlerImpl(dataSetManagerProxy, (DataSetLookup) ref);
        }
        throw new IllegalArgumentException("DataSetRef implementation not supported: " + ref.getClass().getName());
    }
}