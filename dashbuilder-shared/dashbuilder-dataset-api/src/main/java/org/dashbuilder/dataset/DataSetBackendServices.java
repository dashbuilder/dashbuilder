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

import java.util.List;

import org.dashbuilder.dataset.backend.EditDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Data set backend services
 */
@Remote
public interface DataSetBackendServices {

    /**
     * Register a data set definition. 
     * @param definition The data set definition.
     * @return The registered data set definition UUID. If UUID not present on the definition from the argument <code>definition</code>, the UUID will be generated.
     */
    String registerDataSetDef(DataSetDef definition);

    /**
     * Updates a data set definition.
     * @param uuid The UUID of the data set definition to update.
     * @param definition The updated data set definition attributes.
     * @return The registered data set definition UUID.
     */
    String updateDataSetDef(String uuid, DataSetDef definition);
    
    /**
     * Removes a data set definition from the registry. 
     * @param uuid The data set definition identifier.
     */
    void removeDataSetDef(String uuid);
    
    /**
     * Apply a sequence of operations (filter, sort, group, ...) on a remote data set.
     *
     * @return A brand new data set with all the calculations applied.
     */
    DataSet lookupDataSet(DataSetLookup lookup) throws Exception;

    /**
     * Load a data set and apply several operations (filter, sort, group, ...) on top of it for a given definition.
     * Index and cache are not used.
     * @return null, if the data set can be retrieved.
     */
    DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup);

    /**
     * Same as lookupDataSet but only retrieves the metadata of the resulting data set.
     *
     * @return A DataSetMetadata instance containing general information about the data set.
     */
    DataSetMetadata lookupDataSetMetadata(String uuid) throws Exception;

    /**
     * Prepare a DataSetDef for edition.
     *
     * @return A cloned definition and the original column deifnition list.
     */
    EditDataSetDef prepareEdit(String uuid) throws Exception;

    /**
     * Get those public (shareable) data set definition (those with the public flag set to true)
     */
    List<DataSetDef> getPublicDataSetDefs();

    /**
     * Persist the data set definition. 
     * @param dataSetDef The data set definition.
     * @throws Exception
     */
    void persistDataSetDef(final DataSetDef dataSetDef) throws Exception;

    /**
     * Deletes the data set definition. 
     * @param uuid The data set definition UUID.
     * @throws Exception
     */
    void deleteDataSetDef(final String uuid);
    
    /**
     * Export a dataset, specified by a DataSetLookup, to CSV format.
     * @param dataSetLookup The dataSetLookup that defines the dataset to be exported.
     * @return A String with the relative url to the exported data.
     */
    String exportDataSetCSV(DataSetLookup dataSetLookup);

    /**
     * Export a dataset, specified by a DataSetLookup, to Excel format.
     * @param dataSetLookup The dataSetLookup that defines the dataset to be exported.
     * @return A String with the relative url to the exported data.
     */
    String exportDataSetExcel(DataSetLookup dataSetLookup);

    /**
     * Export a dataset to CSV format.
     * @param dataSet The dataset to export.
     * @return A String with the relative url to the exported data.
     */
    String exportDataSetCSV(DataSet dataSet);

    /**
     * Export a dataset to Excel format.
     * @param dataSet The dataset to export.
     * @return A String with the relative url to the exported data.
     */
    String exportDataSetExcel(DataSet dataSet);
}
