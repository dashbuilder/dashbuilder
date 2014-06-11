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
package org.dashbuilder.client.dataset;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.model.dataset.DataSetMetadata;

/**
 * Client implementation of a DataSetManager. It hold as map of data sets in memory.
 * It is designed to manipulate not quite big data sets. For big data sets the backend implementation is better,
 */
@ApplicationScoped
public class ClientDataSetManager implements DataSetManager {

    private Map<String,DataSet> dataSetMap = new HashMap<String,DataSet>();

    public DataSet createDataSet(String uuid) {
        DataSet dataSet = DataSetFactory.newDataSet();
        dataSet.setUUID(uuid);
        return dataSet;
    }

    public DataSet getDataSet(String uuid) throws Exception {
        if (!dataSetMap.containsKey(uuid)) return null;
        return dataSetMap.get(uuid);
    }

    public void registerDataSet(DataSet dataSet) {
        if (dataSet != null) {
            String uuid = dataSet.getUUID();
            if (uuid == null || uuid.trim().length() == 0) {
                uuid = "dataset_" + dataSet.hashCode();
                dataSet.setUUID(uuid);
            }
            dataSetMap.put(dataSet.getUUID(), dataSet);
        }
    }

    public DataSetMetadata getDataSetMetadata(String uuid) throws Exception {
        DataSet dataSet = getDataSet(uuid);
        if (dataSet == null) return null;

        return dataSet.getMetadata();
    }

    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        DataSet dataSet = getDataSet(lookup.getDataSetUUID());
        if (dataSet == null) return null;

        // Apply the list of operations specified (if any).
        if (!lookup.getOperationList().isEmpty()) {
            // TODO: Share the backend DataSetOpEngine
        }

        // Trim the data set as requested.
        dataSet = dataSet.trim(lookup.getRowOffset(), lookup.getNumberOfRows());
        return dataSet;
    }

    public DataSet[] lookupDataSets(DataSetLookup[] lookup) throws Exception {
        DataSet[] result = new DataSet[lookup.length];
        for (int i = 0; i < lookup.length; i++) {
            result[i] = lookupDataSet(lookup[i]);
        }
        return result;
    }

    public DataSetMetadata lookupDataSetMetadata(DataSetLookup lookup) throws Exception {
        DataSet dataSet = lookupDataSet(lookup);
        if (dataSet == null) return null;

        return dataSet.getMetadata();
    }
}
