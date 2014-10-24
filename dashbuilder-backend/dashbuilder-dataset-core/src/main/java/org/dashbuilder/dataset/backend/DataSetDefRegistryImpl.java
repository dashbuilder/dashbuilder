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
package org.dashbuilder.dataset.backend;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * Data set definitions backend registry
 */
@ApplicationScoped
public class DataSetDefRegistryImpl implements DataSetDefRegistry {

    @Inject
    private Event<DataSetDefModifiedEvent> dataSetDefModifiedEvent;

    @Inject
    private Event<DataSetDefRegisteredEvent> dataSetDefRegisteredEvent;

    @Inject
    private Event<DataSetDefRemovedEvent> dataSetDefRemovedEvent;

    protected Map<String,DataSetDef> dataSetDefMap = new HashMap<String, DataSetDef>();

    public synchronized List<DataSetDef> getSharedDataSetDefs() {
        List<DataSetDef> results = new ArrayList<DataSetDef>();
        for (DataSetDef r : dataSetDefMap.values()) {
            if (r.isShared()) {
                results.add(r);
            }
        }
        return results;
    }

    public synchronized DataSetDef getDataSetDef(String uuid) {
        return dataSetDefMap.get(uuid);
    }

    public synchronized DataSetDef removeDataSetDef(String uuid) {
        DataSetDef oldDef = dataSetDefMap.remove(uuid);
        if (oldDef == null) return null;

        dataSetDefRemovedEvent.fire(new DataSetDefRemovedEvent(oldDef));
        return oldDef;
    }

    public synchronized void registerDataSetDef(DataSetDef newDef) {
        DataSetDef oldDef = dataSetDefMap.get(newDef.getUUID());
        if (oldDef != null) {
            dataSetDefMap.put(newDef.getUUID(), newDef);
            dataSetDefModifiedEvent.fire(new DataSetDefModifiedEvent(oldDef, newDef));
        } else {
            dataSetDefMap.put(newDef.getUUID(), newDef);
            dataSetDefRegisteredEvent.fire(new DataSetDefRegisteredEvent(newDef));
        }
    }
}
