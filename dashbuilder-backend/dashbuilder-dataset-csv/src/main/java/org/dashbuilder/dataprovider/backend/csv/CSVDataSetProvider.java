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
package org.dashbuilder.dataprovider.backend.csv;

import java.io.File;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.events.DataSetStaleEvent;
import org.slf4j.Logger;

@ApplicationScoped
@Named("csv")
public class CSVDataSetProvider implements DataSetProvider {

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    protected CSVFileStorage csvStorage;

    @Inject
    protected Logger log;

    public DataSetProviderType getType() {
        return DataSetProviderType.CSV;
    }

    public DataSetMetadata getDataSetMetadata(DataSetDef def) throws Exception {
        DataSet dataSet = lookupDataSet(def, null);
        if (dataSet == null) return null;
        return dataSet.getMetadata();
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        // Look first into the static data set provider since CSV data set are statically registered once loaded.
        DataSet dataSet = staticDataSetProvider.lookupDataSet(def.getUUID(), null);

        // If the lookup request is in test mode or the data set not exists or is outdated then load from the CSV file
        CSVDataSetDef csvDef = (CSVDataSetDef) def;
        if ((lookup != null && lookup.testMode()) || dataSet == null || hasCSVFileChanged(dataSet, csvDef)) {
            CSVParser csvParser = new CSVParser(csvDef, csvStorage);
            dataSet = csvParser.load();
            dataSet.setUUID(def.getUUID());
            dataSet.setDefinition(def);

            // Register the CSV data set available into the static provider
            staticDataSetProvider.registerDataSet(dataSet);
        }
        try {
            // Always do the lookup on the statically registered data set.
            dataSet = staticDataSetProvider.lookupDataSet(def, lookup);
        } finally {
            if (lookup != null && lookup.testMode()) {
                // In test mode remove the data set form cache
                staticDataSetProvider.removeDataSet(def.getUUID());
            }
        }
        // Return the lookup results
        return dataSet;
    }

    public boolean isDataSetOutdated(DataSetDef def) {
        // If no data set is registered then no way for having stale data.
        DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);
        if (dataSet == null) return false;

        // Check if the CSV file has changed.
        return hasCSVFileChanged(dataSet, (CSVDataSetDef) def);
    }

    protected boolean hasCSVFileChanged(DataSet dataSet, CSVDataSetDef def) {
        if (StringUtils.isBlank(def.getFilePath())) {
            return false;
        }
        File f = new File(def.getFilePath());
        return f.exists() && f.lastModified() > dataSet.getCreationDate().getTime();
    }

    // Listen to changes on the data set definition registry

    protected void onDataSetStaleEvent(@Observes DataSetStaleEvent event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.CSV.equals(def.getProvider())) {
            remove(def.getUUID());
        }
    }

    protected void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent  event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.CSV.equals(def.getProvider())) {
            remove(def.getUUID());
        }
    }

    private void remove(final String uuid) {
        staticDataSetProvider.removeDataSet(uuid);
    }
}
