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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.engine.BackendDataSetOpEngine;
import org.dashbuilder.dataset.engine.index.DataSetIndex;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;

/**
 * Backend implementation of the DataSetManager interface. It's been designed with several goals in mind:
 * <ul>
 *     <li>To provide a highly reusable data set cache.</li>
 *     <li>To index almost every operation performed over a data set.</li>
 *     <li>Multiple clients requesting the same data set operations will benefit from the indexing/caching services provided.</li>
 * </ul>
 */
@ApplicationScoped
public class BackendDataSetManager implements DataSetManager {

    @Inject
    protected BackendDataSetOpEngine dataSetOpEngine;

    public DataSet createDataSet(String uuid) {
        DataSet dataSet = DataSetFactory.newDataSet();
        dataSet.setUUID(uuid);
        return dataSet;
    }

    public DataSet getDataSet(String uuid) {
        DataSetIndex index = fetchDataSet(uuid);
        if (index == null) return null;

        return index.getDataSet();
    }

    public DataSetMetadata getDataSetMetadata(String uuid) {
        DataSet dataSet = getDataSet(uuid);
        if (dataSet == null) return null;

        return dataSet.getMetadata();
    }

    public void registerDataSet(DataSet dataSet) {
        if (dataSet != null) {
            dataSetOpEngine.getIndexRegistry().put(dataSet);
        }
    }

    public DataSet refreshDataSet(String uuid) {
        dataSetOpEngine.getIndexRegistry().remove(uuid);
        DataSetIndex index = fetchDataSet(uuid);
        if (index == null) return null;

        return index.getDataSet();
    }

    public DataSet lookupDataSet(DataSetLookup lookup) {
        String uuid = lookup.getDataSetUUID();
        if (StringUtils.isEmpty(uuid)) return null;

        // Get the target data set
        DataSetIndex dataSetIndex = fetchDataSet(uuid);
        if (dataSetIndex == null) return null;
        DataSet dataSet = dataSetIndex.getDataSet();

        // Apply the list of operations specified (if any).
        if (!lookup.getOperationList().isEmpty()) {
            dataSet = dataSetOpEngine.execute(dataSetIndex.getDataSet(), lookup.getOperationList());
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

    public DataSetMetadata lookupDataSetMetadata(DataSetLookup lookup) {
        DataSet dataSet = lookupDataSet(lookup);
        if (dataSet == null) return null;

        return dataSet.getMetadata();
    }

    // Internal stuff

    protected DataSetIndex fetchDataSet(String uuid) {
        DataSetIndex index = dataSetOpEngine.getIndexRegistry().get(uuid);
        if (index != null) return index;

        return loadDataSet(uuid);
    }

    protected DataSetIndex loadDataSet(String uuid) {
        /* TODO: Get the data set from an external provider.
        DataProviderManager dataProviderManager = DataProviderServices.getDataProviderManager();
        DataSet dataSet = dataProviderManager.fetchDataSet(uuid);
        return dataSetIndexer.put(dataSet);
        */

        return null;
    }
}
