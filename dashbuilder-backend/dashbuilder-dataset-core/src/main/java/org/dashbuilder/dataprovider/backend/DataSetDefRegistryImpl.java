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
package org.dashbuilder.dataprovider.backend;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * Data set definitions backend registry
 */
@ApplicationScoped
@Service
public class DataSetDefRegistryImpl implements DataSetDefRegistry {

    private Map<String,DataSetDef> dataSetDefMap = new HashMap<String, DataSetDef>();

    public synchronized List<DataSetDef> getSharedDataSetDefs() {
        List<DataSetDef> results = new ArrayList<DataSetDef>();
        for (DataSetDef settings : dataSetDefMap.values()) {
            if (settings.isShared()) {
                results.add(settings);
            }
        }
        return results;
    }

    public synchronized DataSetDef getDataSetDef(String uuid) {
        return dataSetDefMap.get(uuid);
    }

    public synchronized DataSetDef removeDataSetDef(String uuid) {
        return dataSetDefMap.remove(uuid);
    }

    public synchronized void registerDataSetDef(DataSetDef dataSetDef) {
        dataSetDefMap.put(dataSetDef.getUUID(), dataSetDef);
    }
}
