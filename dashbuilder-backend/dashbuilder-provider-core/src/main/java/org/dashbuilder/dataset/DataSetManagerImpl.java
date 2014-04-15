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
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.storage.spi.DataSetStorage;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.impl.DataSetImpl;

@ApplicationScoped
public class DataSetManagerImpl implements DataSetManager {

    @Inject
    protected DataSetStorage dataSetStorage;

    public DataSet createDataSet(String uuid) {
        DataSetImpl dataSet = new DataSetImpl();
        dataSet.setUUID(uuid);
        return dataSet;
    }

    public DataSet getDataSet(String uuid) throws Exception {
        DataSet result = dataSetStorage.get(uuid);
        if (result == null) result = fetchDataSet(uuid);
        if (result == null) throw new Exception("Data set not found: " + uuid);
        return result;
    }

    public void registerDataSet(DataSet dataSet) throws Exception {
        if (dataSet != null && dataSet.getUUID() != null) {
            dataSetStorage.put(dataSet);
        }
    }

    public DataSet refreshDataSet(String uuid) throws Exception {
        DataSet ds = fetchDataSet(uuid);
        if (ds == null) return dataSetStorage.get(uuid);

        dataSetStorage.remove(uuid);
        dataSetStorage.put(ds);
        return ds;
    }

    public DataSet fetchDataSet(String uuid) throws Exception {
        DataSet result = dataSetStorage.get(uuid);
        if (result != null) return result;

        // Get the data set from an external provider.
        //return dataProviderManager.fetchDataSet(uuid);
        return null;
    }

    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        String uuid = lookup.getDataSetUUID();
        if (StringUtils.isEmpty(uuid)) throw new IllegalArgumentException("Target data set UUID not specified.");

        // Retrieve (load if required) the source data set
        DataSet result = getDataSet(uuid);

        // Apply the list of operations specified.
        for (DataSetOp op : lookup.getOperationList()) {
            result = dataSetStorage.apply(uuid, op);
        }
        // Trim the data set as requested.
        result = result.trim(lookup.getRowOffset(), lookup.getNumberOfRows());
        return result;
    }
}
