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
     */
    void registerDataSetDef(DataSetDef definition);

    /**
     * Removes a data set definition. 
     * @param definition The data set definition.
     */
    void removeDataSetDef(DataSetDef definition);
    
    /**
     * Apply a sequence of operations (filter, sort, group, ...) on a remote data set.
     *
     * @return A brand new data set with all the calculations applied.
     */
    DataSet lookupDataSet(DataSetLookup lookup) throws Exception;

    /**
     * Same as lookupDataSet but only retrieves the metadata of the resulting data set.
     *
     * @return A DataSetMetadata instance containing general information about the data set.
     */
    DataSetMetadata lookupDataSetMetadata(String uuid) throws Exception;

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
}
