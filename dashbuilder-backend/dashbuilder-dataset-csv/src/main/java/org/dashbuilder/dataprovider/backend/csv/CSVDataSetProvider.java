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

        // If not exists or is outdated then load from the CSV file
        CSVDataSetDef csvDef = (CSVDataSetDef) def;
        CSVParser csvParser = new CSVParser(csvDef);
        File csvFile = csvParser.getCSVFile();
        if (dataSet == null || hasCSVFileChanged(dataSet, csvFile)) {
            dataSet = csvParser.load();
            dataSet.setUUID(def.getUUID());
            dataSet.setDefinition(def);

            // Make the data set static before return
            staticDataSetProvider.registerDataSet(dataSet);
        }
        try {
            // Always do the lookup on the statically registered data set.
            dataSet = staticDataSetProvider.lookupDataSet(def, lookup);
        } finally {
            // Remove transient data sets
            // f.i: for those definitions created from the data set editor UI
            if (def.getUUID() != null && dataSetDefRegistry.getDataSetDef(def.getUUID()) == null) {
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
        try {
            CSVDataSetDef csvDef = (CSVDataSetDef) def;
            File csvFile = new CSVParser(csvDef).getCSVFile();
            return hasCSVFileChanged(dataSet, csvFile);
        } catch (Exception e) {
            log.error("Problems reading CSV file: " + def, e);
            return false;
        }
    }

    protected boolean hasCSVFileChanged(DataSet dataSet, File csvFile) {
        return csvFile != null && csvFile.lastModified() > dataSet.getCreationDate().getTime();
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
