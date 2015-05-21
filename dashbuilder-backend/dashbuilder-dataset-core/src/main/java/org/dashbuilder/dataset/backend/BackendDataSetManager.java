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
package org.dashbuilder.dataset.backend;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.config.Config;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.exception.DataSetLookupException;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.StaticDataSetDef;
import org.slf4j.Logger;

/**
 * Backend implementation of the DataSetManager interface. It provides an uniform interface to the data set
 * registration and lookup services on top of the data set provider interface.
 */
@ApplicationScoped
public class BackendDataSetManager implements DataSetManager {

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    protected DataSetProviderRegistry dataSetProviderRegistry;

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    @Inject @Config("true")
    protected boolean pushEnabled = false;

    @Inject @Config("1024")
    protected int pushMaxSize = 2048;

    @Inject
    protected Logger log;

    public DataSet createDataSet(String uuid) {
        DataSet dataSet = DataSetFactory.newEmptyDataSet();
        dataSet.setUUID(uuid);
        return dataSet;
    }

    public DataSet getDataSet(String uuid) {
        try {
            DataSetDef dataSetDef = dataSetDefRegistry.getDataSetDef(uuid);
            if (dataSetDef == null) throw new RuntimeException("Data set not found: " + uuid);

            // Fetch the specified data set
            return resolveProvider(dataSetDef)
                    .lookupDataSet(dataSetDef, null);
        } catch (Exception e) {
            throw new RuntimeException("Can't fetch the specified data set: " + uuid, e);
        }
    }

    public void registerDataSet(DataSet dataSet) {
        if (dataSet != null) {
            StaticDataSetDef def = new StaticDataSetDef();
            def.setUUID(dataSet.getUUID());
            def.setName(dataSet.getUUID());
            def.setDataSet(dataSet);
            def.setPushEnabled(pushEnabled);
            def.setPushMaxSize(pushMaxSize);
            dataSetDefRegistry.registerDataSetDef(def);

            // Register the data set after the definition. It's mandatory to do this right after since
            // the registerDataSetDef will delete any old existing data set matching the given UUID.
            staticDataSetProvider.registerDataSet(dataSet);
        }
    }

    public DataSet removeDataSet(String uuid) {
        if (StringUtils.isBlank(uuid)) return null;

        dataSetDefRegistry.removeDataSetDef(uuid);
        return staticDataSetProvider.removeDataSet(uuid);
    }

    public DataSet lookupDataSet(DataSetLookup lookup) {
        String uuid = lookup.getDataSetUUID();
        if (StringUtils.isBlank(uuid)) return null;

        DataSetDef dataSetDef = dataSetDefRegistry.getDataSetDef(uuid);
        if (dataSetDef == null) throw new RuntimeException("Data set not found: " + uuid);

        try {
            return resolveProvider(dataSetDef)
                    .lookupDataSet(dataSetDef, lookup);
        } catch (Exception e) {
            throw new DataSetLookupException(uuid, "Can't lookup on specified data set: " + lookup.getDataSetUUID(), e);
        }
    }

    public DataSet[] lookupDataSets(DataSetLookup[] lookup) {
        DataSet[] result = new DataSet[lookup.length];
        for (int i = 0; i < lookup.length; i++) {
            result[i] = lookupDataSet(lookup[i]);
        }
        return result;
    }

    public DataSetMetadata getDataSetMetadata(String uuid) {
        if (StringUtils.isBlank(uuid)) return null;

        DataSetDef dataSetDef = dataSetDefRegistry.getDataSetDef(uuid);
        if (dataSetDef == null) throw new RuntimeException("Data set not found: " + uuid);

        try {
            return resolveProvider(dataSetDef)
                    .getDataSetMetadata(dataSetDef);
        } catch (Exception e) {
            throw new DataSetLookupException(uuid, "Can't get metadata on specified data set: " + uuid, e);
        }
    }

    public DataSetProvider resolveProvider(DataSetDef dataSetDef) {
        // Get the target data set provider
        DataSetProviderType type = dataSetDef.getProvider();
        if (type != null) {
            DataSetProvider dataSetProvider = dataSetProviderRegistry.getDataSetProvider(type);
            if (dataSetProvider != null) return dataSetProvider;
        }

        // If no provider is defined then return the static one
        return staticDataSetProvider;
    }
}
