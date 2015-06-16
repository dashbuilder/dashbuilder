/**
 * Copyright (C) 2015 JBoss Inc
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
package org.dashbuilder.dataset.service;

import java.util.List;

import org.dashbuilder.dataset.backend.EditDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Services for the handling of data set definitions
 */
@Remote
public interface DataSetDefServices {

    /**
     * Get those public (shareable) data set definition (those with the "public" flag set to true)
     */
    List<DataSetDef> getPublicDataSetDefs();

    /**
     * Register a data set definition.
     * @param definition The data set definition.
     * @return The registered data set definition UUID. If UUID not present on the definition from the argument <code>definition</code>, the UUID will be generated.
     */
    String registerDataSetDef(DataSetDef definition);

    /**
     * Removes a data set definition from the registry.
     * @param uuid The data set definition identifier.
     */
    void removeDataSetDef(String uuid);

    /**
     * Prepare a DataSetDef for edition.
     *
     * @return A cloned definition and the original column definition list.
     */
    EditDataSetDef prepareEdit(String uuid) throws Exception;
}
