/**
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
package org.dashbuilder.dataset.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.engine.SharedDataSetOpEngine;
import org.dashbuilder.dataset.engine.index.DataSetIndex;

/**
 * Client implementation of a DataSetManager. It hold as map of data sets in memory.
 * It is designed to manipulate not quite big data sets. For big data sets the backend implementation is better,
 */
@ApplicationScoped
public class ClientDataSetManager implements DataSetManager {

    SharedDataSetOpEngine dataSetOpEngine;

    @Inject
    public ClientDataSetManager(SharedDataSetOpEngine dataSetOpEngine) {
        this.dataSetOpEngine = dataSetOpEngine;
    }

    public DataSet createDataSet(String uuid) {
        DataSet dataSet = DataSetFactory.newEmptyDataSet();
        dataSet.setUUID(uuid);
        return dataSet;
    }

    public DataSet getDataSet(String uuid) {
        DataSetIndex index = dataSetOpEngine.getIndexRegistry().get(uuid);
        if (index == null) return null;

        return index.getDataSet();
    }

    public void registerDataSet(DataSet dataSet) {
        if (dataSet != null) {
            dataSetOpEngine.getIndexRegistry().put(dataSet);
        }
    }

    public DataSet removeDataSet(String uuid) {
        DataSetIndex index = dataSetOpEngine.getIndexRegistry().remove(uuid);
        if (index == null) return null;
        return index.getDataSet();
    }

    public DataSet lookupDataSet(DataSetLookup lookup) {
        String uuid = lookup.getDataSetUUID();
        if (StringUtils.isEmpty(uuid)) return null;

        // Get the target data set
        DataSetIndex dataSetIndex = dataSetOpEngine.getIndexRegistry().get(uuid);
        if (dataSetIndex == null) return null;
        DataSet dataSet = dataSetIndex.getDataSet();

        // Apply the list of operations specified (if any).
        if (!lookup.getOperationList().isEmpty()) {
            dataSet = dataSetOpEngine.execute(uuid, lookup.getOperationList());
        }

        // Trim the data set as requested.
        dataSet = dataSet.trim(lookup.getRowOffset(), lookup.getNumberOfRows());
        return dataSet;
    }

    public DataSet[] lookupDataSets(DataSetLookup[] lookup) {
        DataSet[] result = new DataSet[lookup.length];
        for (int i = 0; i < lookup.length; i++) {
            result[i] = lookupDataSet(lookup[i]);
        }
        return result;
    }

    public DataSetMetadata getDataSetMetadata(String uuid) {
        DataSetLookup lookup = new DataSetLookup(uuid);
        DataSet dataSet = lookupDataSet(lookup);
        if (dataSet == null) return null;

        return dataSet.getMetadata();
    }
}
