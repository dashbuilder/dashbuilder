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
package org.dashbuilder.dataprovider;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.def.DataSetDef;

/**
 * This service provider interface is designed to provide access to different data set storage implementations with
 * the main goal of providing a unified interface for the data set fetch & lookup operations.
 */
public interface DataSetProvider {

    /**
     * The type of the provider.
     */
    DataSetProviderType getType();

    /**
     * Fetch a data set and optionally apply several operations (filter, sort, group, ...) on top of it.
     *
     * @param def The data set definition lookup request
     * @param lookup The lookup request over the data set
     * @return The resulting data set instance
     */
    DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception;
}
