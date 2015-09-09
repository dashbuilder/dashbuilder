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
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * <p>Data test for ElasticSearchDataSet.</p>
 * // TODO: Review - second document insertion not working... 
 * @since 0.3.0
 */
@Ignore
public class ElasticSearchDataSetCacheTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASETS_ROOT = "org/dashbuilder/dataprovider/backend/elasticsearch/";
    protected static final String EL_DATASET_UUID = "expense_reports";
    
    @Inject
    ElasticSearchDataSetProvider elasticSearchDataSetProvider;

    @Test
    public void testDataSetNonCached() throws Exception {
        // A non-cached data set never gets outdated
        _testDataSetCache(null, false, 52);
    }

    //@Test
    public void testDataSetStaticCache() throws Exception {
        // A non-synced (static) data set never gets outdated and it always contains the same content
        _testDataSetCache("static_cache", false, 50);
    }

    protected void _testDataSetCache(String scenario, boolean outdated, int rows) throws Exception {

        // Register the data set definition
        String fileName = scenario != null ? "expensereports-" + scenario + ".dset" : "expensereports.dset";
        ElasticSearchDataSetDef def = _registerDataSet(EL_EXAMPLE_DATASETS_ROOT + fileName);
        
        // Lookup the dataset (forces the caches to initialize)
        String dataSetUUID = scenario != null ? EL_DATASET_UUID + "_" + scenario : EL_DATASET_UUID;
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(dataSetUUID)
                        .buildLookup());

        // Insert some extra rows into the database
        populateELServer(EL_EXAMPLE_MORE_DATA);

        // Check if the the data set is outdated
        assertThat(elasticSearchDataSetProvider.isDataSetOutdated(def)).isEqualTo(outdated);

        // Lookup the last database content
        DataSet result2 = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(dataSetUUID)
                        .rowNumber(100)
                        .buildLookup());

        assertThat(result2.getRowCount()).isEqualTo(rows);
    }
}
