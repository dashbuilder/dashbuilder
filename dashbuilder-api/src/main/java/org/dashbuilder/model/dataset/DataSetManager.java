/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.model.dataset;

/**
 * Main interface for accessing data sets.
 */
public interface DataSetManager {

    /**
     * Create a brand new data set instance.
     */
    DataSet createDataSet(String uuid);

    /**
     * Retrieve (load if required) a data set.
     */
    DataSet getDataSet(String uuid) throws Exception;

    /**
     * Registers the specified data set instance.
     */
    void registerDataSet(DataSet dataSet) throws Exception;

    /**
     * Discard any active operation and ensure the most up to date data is loaded and returned.
     */
    DataSet refreshDataSet(String uuid) throws Exception;

    /**
     * Load a data set and apply several operations (filter, sort, group, ...) on it.
     */
    DataSet lookupDataSet(DataSetLookup lookup) throws Exception;
}
