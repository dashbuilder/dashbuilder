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
package org.dashbuilder.storage;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.dashbuilder.storage.spi.DataSetStorage;
import org.uberfire.commons.services.cdi.Startup;

/**
 * Holds a list of the installed DataSetStorage implementations.
 */
@ApplicationScoped
@Startup
public class DataSetStorages {

    @Inject
    protected Instance<DataSetStorage> storageInstances;

    protected List<DataSetStorage> storageList = new ArrayList<DataSetStorage>();

    @PostConstruct
    private void init() {
        for (DataSetStorage storage : storageInstances) {
            storageList.add(storage);
        }
    }
}
