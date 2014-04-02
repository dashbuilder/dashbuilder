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
    DataSet createDataSet();

    /**
     * Get the current data set for the given provider.
     * The data set returned might vary depending whether there are active filters applied on the provider or not.
     */
    DataSet getDataSet(String uid) throws Exception;

    /**
     * Registers the specified DataSet instance.
     */
    void registerDataSet(String uid, DataSet dataSet) throws Exception;

    /**
     * Discard any active filter and ensure the most up to date data is loaded and returned.
     */
    DataSet refreshDataSet(String uid) throws Exception;

    /**
     * Apply a filter on the specified data set.
     *
     * @param ops The list of operations to apply to the specified data set.
     */
    DataSet transformDataSet(String uid, DataSetOperation... ops) throws Exception;
}
