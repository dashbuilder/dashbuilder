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

import org.junit.Before;
import org.junit.Ignore;

/**
 * <p>Data test for ElasticSearchDataSet.</p>
 * 
 * TODO
 * @since 0.3.0
 */
@Ignore
public class ElasticSearchDataSetCacheTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset";
    protected static final String EL_DATASET_UUID = "expense_reports";
    
    /**
     * Register the dataset used for this test case. 
     */
    @Before
    public void registerDataSet() throws Exception {
        // Register the data set.
        _registerDataSet(EL_EXAMPLE_DATASET_DEF);
    }
    
}
