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

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.sort.SortOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.dashbuilder.dataset.Assertions.assertDataSetValue;
import static org.dashbuilder.dataset.filter.FilterFactory.isEqualsTo;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * <p>Data test for ElasticSearchDataSet.</p>
 * <p>It uses as source dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns-error.dset</code></p>
 * <p>The column employee is defined as label, but it's an analyzed string in the index mapping, so this situation cannot be done.:</p>
 * 
 * @since 0.3.0
 */
public class ElasticSearchDataSetCustomColumnsErrorTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns-error.dset";
    protected static final String EL_DATASET_UUID = "expense_reports_custom_columns_error";
    
    /**
     * Register the dataset used for this test case. 
     */
    @Before
    public void registerDataSet() throws Exception {
        // Register the data set.
        _registerDataSet(EL_EXAMPLE_DATASET_DEF);
    }

    /**
     * **********************************************************************************************************************************************************************************************
     * COLUMNS TESING.
     * **********************************************************************************************************************************************************************************************
     */
    
    
    /**
     * Test columns as this dataset defintion contains custom definitions.
     * An exception must be thrown due to cannot change employee column type to label, as it's an anaylzed string in the EL index mapping.
     */
    @Test(expected = RuntimeException.class)
    public void testColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
    }

}
