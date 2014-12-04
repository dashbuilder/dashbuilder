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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.engine.SharedDataSetOpEngine;
import org.dashbuilder.dataset.engine.index.DataSetIndex;
import org.dashbuilder.dataset.events.DataSetBackendRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetBackendRemovedEvent;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

/**
 * DataSetProvider implementation for static (in-memory) data sets.
 * <p>It's been designed with several goals in mind:
 * <ul>
 *     <li>To provide a highly reusable data set cache.</li>
 *     <li>To index almost every operation performed over a data set.</li>
 *     <li>Multiple clients requesting the same data set operations will benefit from the indexing/caching services provided.</li>
 * </ul>
 * </p>
 */
@ApplicationScoped
@Named("static")
public class StaticDataSetProvider implements DataSetProvider {

    @Inject
    private SharedDataSetOpEngine dataSetOpEngine;

    @Inject
    private Event<DataSetBackendRegisteredEvent> dataSetRegisteredEvent;

    @Inject
    private Event<DataSetBackendRemovedEvent> dataSetRemovedEvent;

    public DataSetProviderType getType() {
        return DataSetProviderType.STATIC;
    }

    public DataSetMetadata getDataSetMetadata(DataSetDef def) {
        DataSet dataSet = lookupDataSet(def, null);
        if (dataSet == null) return null;
        return dataSet.getMetadata();
    }

    public void registerDataSet(DataSet dataSet) {
        dataSetOpEngine.getIndexRegistry().put(dataSet);

        // Fire an event
        dataSetRegisteredEvent.fire(new DataSetBackendRegisteredEvent(dataSet.getMetadata()));
    }

    public DataSet removeDataSet(String uuid) {
        DataSetIndex index = dataSetOpEngine.getIndexRegistry().remove(uuid);
        if (index == null) return null;

        // Fire an event before return
        DataSet dataSet = index.getDataSet();
        dataSetRemovedEvent.fire(new DataSetBackendRemovedEvent(dataSet.getMetadata()));
        return dataSet;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) {
        String uuid = def.getUUID();
        if (StringUtils.isEmpty(uuid)) return null;

        // Get the target data set
        DataSetIndex dataSetIndex = dataSetOpEngine.getIndexRegistry().get(uuid);
        if (dataSetIndex == null) return null;
        DataSet dataSet = dataSetIndex.getDataSet();
        if (lookup == null) return dataSet;

        // Apply the list of operations specified (if any).
        if (!lookup.getOperationList().isEmpty()) {
            dataSet = dataSetOpEngine.execute(dataSet, lookup.getOperationList());
        }

        // Trim the data set as requested.
        dataSet = dataSet.trim(lookup.getRowOffset(), lookup.getNumberOfRows());
        return dataSet;
    }

    public boolean isDataSetOutdated(DataSetDef def) {
        return false;
    }

    // Listen to changes on the data set definition registry

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);
        checkNotNull("event", event.getOldDataSetDef());
        checkNotNull("event", event.getNewDataSetDef());

        DataSetDef oldDef = event.getOldDataSetDef();
        if (DataSetProviderType.STATIC.equals(oldDef.getProvider())) {
            String uuid = event.getOldDataSetDef().getUUID();
            removeDataSet(uuid);
        }
        DataSetDef newDef = event.getNewDataSetDef();
        if (DataSetProviderType.STATIC.equals(newDef.getProvider())) {
            String uuid = event.getNewDataSetDef().getUUID();
            registerDataSet(newDef.getDataSet());
        }
    }
}
