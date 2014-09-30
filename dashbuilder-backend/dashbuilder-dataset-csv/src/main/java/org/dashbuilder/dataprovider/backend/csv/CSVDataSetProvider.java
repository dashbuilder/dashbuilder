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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.dashbuilder.config.Config;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;

@ApplicationScoped
@Named("csv")
public class CSVDataSetProvider implements DataSetProvider {

    @Inject @Config(";")
    protected String csvSeparatedBy;

    @Inject @Config("\"")
    protected String csvQuoteChar;

    @Inject @Config("\\")
    protected String csvEscapeChar;

    @Inject @Config("MM-dd-yyyy HH:mm:ss")
    protected String csvDatePattern;

    @Inject @Config("#,###.##")
    protected String csvNumberPattern;

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    public DataSetProviderType getType() {
        return DataSetProviderType.CSV;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        // Look first into the static data set provider since CSV data set are statically registered once loaded.
        String uuid = lookup.getDataSetUUID();
        DataSet dataSet = staticDataSetProvider.lookupDataSet(def,
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(uuid)
                        .buildLookup());

        // If not exists or is outdated then load from the CSV file
        CSVDataSetDef csvDef = (CSVDataSetDef) def;
        if (dataSet == null || hasCSVFileChanged(csvDef)) {
            dataSet = loadDataSetFromCSV(csvDef);

            // Make the data set static before return
            staticDataSetProvider.registerDataSet(dataSet);
        }
        return dataSet;
    }

    // Internal implementation stuff

    protected DataSet loadDataSetFromCSV(CSVDataSetDef def) throws Exception {
        return null;
    }


    protected boolean hasCSVFileChanged(CSVDataSetDef def) {
        return false;
    }
}
