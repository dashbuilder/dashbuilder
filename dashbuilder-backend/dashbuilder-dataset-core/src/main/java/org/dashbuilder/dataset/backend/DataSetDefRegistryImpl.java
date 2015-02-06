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

import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.events.DataSetStaleEvent;
import org.dashbuilder.dataset.group.TimeFrame;
import org.dashbuilder.scheduler.Scheduler;
import org.dashbuilder.scheduler.SchedulerTask;
import org.slf4j.Logger;

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

    @Inject
    private Event<DataSetStaleEvent> dataSetStaleEvent;

    @Inject
    protected DataSetProviderRegistry dataSetProviderRegistry;

    @Inject
    protected Logger log;

    @Inject
    protected Scheduler scheduler;

    protected Map<String,DataSetDefEntry> dataSetDefMap = new HashMap<String, DataSetDefEntry>();

    protected class DataSetDefEntry extends SchedulerTask {

        DataSetDef def;
        long lastRefreshTime;
        long refreshInMillis;

        DataSetDefEntry(DataSetDef def) {
            this.def = def;
            this.lastRefreshTime = System.currentTimeMillis();
            TimeFrame tf = TimeFrame.parse(def.getRefreshTime());
            this.refreshInMillis = (tf == null ? -1 : tf.toMillis());
        }

        public void schedule() {
            if (refreshInMillis != -1) {
                scheduler.schedule(this, refreshInMillis / 1000);
            }
        }

        public void unschedule() {
            scheduler.unschedule(getKey());
        }

        public boolean isStale() {
            if (refreshInMillis == -1) {
                return false;
            }
            if (!def.isRefreshAlways()) {
                DataSetProvider provider = resolveProvider(def);
                return provider.isDataSetOutdated(def);
            }
            return System.currentTimeMillis() >= lastRefreshTime + refreshInMillis;
        }

         public void stale() {
            lastRefreshTime = System.currentTimeMillis();
            dataSetStaleEvent.fire(new DataSetStaleEvent(def));
        }

        @Override
        public String getDescription() {
            return "DataSetDef refresh task "  + def.getUUID();
        }

        @Override
        public String getKey() {
            return def.getUUID();
        }

        @Override
        public void execute() {
            if (isStale()) {
                stale();
            }
        }
    }

    protected DataSetProvider resolveProvider(DataSetDef dataSetDef) {
        DataSetProviderType type = dataSetDef.getProvider();
        if (type != null) {
            DataSetProvider dataSetProvider = dataSetProviderRegistry.getDataSetProvider(type);
            if (dataSetProvider != null) return dataSetProvider;
        }
        throw new IllegalStateException("DataSetProvider not found: " + dataSetDef.getProvider());
    }

    public synchronized List<DataSetDef> getDataSetDefs(boolean onlyPublic) {
        List<DataSetDef> results = new ArrayList<DataSetDef>();
        for (DataSetDefEntry r : dataSetDefMap.values()) {
            if (!onlyPublic || r.def.isPublic()) {
                results.add(r.def);
            }
        }
        return results;
    }

    public synchronized DataSetDef getDataSetDef(String uuid) {
        DataSetDefEntry record = dataSetDefMap.get(uuid);
        if (record == null) return null;
        return record.def;
    }

    public synchronized DataSetDef removeDataSetDef(String uuid) {
        DataSetDefEntry oldEntry = _removeDataSetDef(uuid);
        if (oldEntry == null) return null;

        // Notify about the removal
        dataSetDefRemovedEvent.fire(new DataSetDefRemovedEvent(oldEntry.def));
        return oldEntry.def;
    }

    public synchronized void registerDataSetDef(DataSetDef newDef) {

        // Register the new entry
        DataSetDefEntry oldEntry = _removeDataSetDef(newDef.getUUID());
        dataSetDefMap.put(newDef.getUUID(), new DataSetDefEntry(newDef));
        DataSetDefEntry newEntry = dataSetDefMap.get(newDef.getUUID());

        // Notify the proper event
        if (oldEntry != null) {
            dataSetDefModifiedEvent.fire(new DataSetDefModifiedEvent(oldEntry.def, newDef));
        } else {
            dataSetDefRegisteredEvent.fire(new DataSetDefRegisteredEvent(newDef));
        }
        // Register the data set into the scheduler
        newEntry.schedule();
    }

    protected DataSetDefEntry _removeDataSetDef(String uuid) {
        DataSetDefEntry oldEntry = dataSetDefMap.remove(uuid);
        if (oldEntry == null) return null;

        // Remove from the scheduler
        oldEntry.unschedule();

        // Make any data set reference stale
        oldEntry.stale();
        return oldEntry;
    }
}
