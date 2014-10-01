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
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataprovider.backend.StaticDataSetProvider;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;

@ApplicationScoped
@Named("csv")
public class CSVDataSetProvider implements DataSetProvider {

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    public DataSetProviderType getType() {
        return DataSetProviderType.CSV;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        // Look first into the static data set provider since CSV data set are statically registered once loaded.
        DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);

        // If not exists or is outdated then load from the CSV file
        CSVDataSetDef csvDef = (CSVDataSetDef) def;
        if (dataSet == null || hasCSVFileChanged(dataSet, csvDef)) {
            dataSet = loadDataSetFromCSV(csvDef);
            dataSet.setUUID(def.getUUID());

            // Make the data set static before return
            staticDataSetProvider.registerDataSet(dataSet);

        }
        // Always do the lookup on the statically registered data set.
        return staticDataSetProvider.lookupDataSet(def, lookup);
    }

    // Internal implementation stuff

    protected DataSet loadDataSetFromCSV(CSVDataSetDef def) throws Exception {
        CSVParser csvParser = new CSVParser(def);
        return csvParser.load();
    }

    protected boolean hasCSVFileChanged(DataSet dataSet, CSVDataSetDef def) {
        if (StringUtils.isBlank(def.getFilePath())) return false;

        File f = new File(def.getFilePath());
        return f.lastModified() > dataSet.getCreationDate().getTime();
    }
}
