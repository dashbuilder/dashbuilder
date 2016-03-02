/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.sql;

import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.*;

// TODO: Review
@Ignore
public class SQLDataSetCacheTest extends SQLDataSetTestBase {

    @Test
    public void testDataSetNonCached() throws Exception {
        // A non-cached (database held) data set never gets outdated
        _testDataSetCache("noncached", false, 100);
    }

    //@Test
    public void testDataSetStaticCache() throws Exception {
        // A non-synced (static) data set never gets outdated and it always contains the same content
        _testDataSetCache("static", false, 50);
    }

    @Override
    public void testAll() throws Exception {
        testDataSetNonCached();
        testDataSetStaticCache();
    }

    protected void _testDataSetCache(String scenario, boolean outdated, int rows) throws Exception {

        // Register the data set definition
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports_" + scenario + ".dset");
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        // Lookup the dataset (forces the caches to initialize)
        dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_" + scenario)
                        .buildLookup());

        // Insert some extra rows into the database
        populateDbTable();

        // Check if the the data set is outdated
        assertThat(sqlDataSetProvider.isDataSetOutdated(def)).isEqualTo(outdated);

        // Lookup the last database content
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_" + scenario)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(rows);
    }
}
