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
package org.dashbuilder.dataset;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.engine.DataSetOpEngine;
import org.dashbuilder.dataset.index.DataSetIndex;
import org.dashbuilder.dataset.index.spi.DataSetIndexRegistry;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetManager;

@ApplicationScoped
public class DataSetManagerImpl implements DataSetManager {

    @Inject protected DataSetServices dataSetServices;
    protected DataSetOpEngine dataSetOpEngine;
    protected DataSetIndexRegistry dataSetIndexRegistry;

    @PostConstruct
    private void init() {
        dataSetOpEngine = dataSetServices.getDataSetOpEngine();
        dataSetIndexRegistry = dataSetServices.getDataSetIndexRegistry();
    }

    public DataSet createDataSet(String uuid) {
        DataSet dataSet = DataSetFactory.newDataSet();
        dataSet.setUUID(uuid);
        return dataSet;
    }

    public DataSet getDataSet(String uuid) throws Exception {
        return fetchDataSet(uuid).getDataSet();
    }

    public void registerDataSet(DataSet dataSet) throws Exception {
        if (dataSet != null) {
            dataSetIndexRegistry.put(dataSet);
        }
    }

    public DataSet refreshDataSet(String uuid) throws Exception {
        dataSetIndexRegistry.remove(uuid);
        return fetchDataSet(uuid).getDataSet();
    }

    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        String uuid = lookup.getDataSetUUID();
        if (StringUtils.isEmpty(uuid)) throw new IllegalArgumentException("Target data set UUID not specified.");

        // Get the target data set
        DataSetIndex dataSetIndex = fetchDataSet(uuid);
        DataSet dataSet = dataSetIndex.getDataSet();

        // Apply the list of operations specified (if any).
        if (!lookup.getOperationList().isEmpty()) {
            dataSet = dataSetOpEngine.execute(dataSetIndex.getDataSet(), lookup.getOperationList());
        }

        // Trim the data set as requested.
        dataSet = dataSet.trim(lookup.getRowOffset(), lookup.getNumberOfRows());
        return dataSet;
    }

    // Internal stuff

    protected DataSetIndex fetchDataSet(String uuid) throws Exception {
        DataSetIndex index = dataSetIndexRegistry.get(uuid);
        if (index != null) return index;

        DataSetIndex result = loadDataSet(uuid);
        if (result != null) return result;

        throw new Exception("Data set not found: " + uuid);
    }

    protected DataSetIndex loadDataSet(String uuid) throws Exception {
        /* TODO: Get the data set from an external provider.
        DataProviderManager dataProviderManager = DataProviderServices.getDataProviderManager();
        DataSet dataSet = dataProviderManager.fetchDataSet(uuid);
        return dataSetIndexer.put(dataSet);
        */

        return null;
    }
}
