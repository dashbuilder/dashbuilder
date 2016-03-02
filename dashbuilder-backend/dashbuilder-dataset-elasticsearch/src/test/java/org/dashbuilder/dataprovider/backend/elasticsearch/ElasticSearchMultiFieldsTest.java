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
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.sort.SortOrder;
import org.elasticsearch.common.util.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * <p>Data test for the multi-fields feature..</p>
 * <p>It uses as source dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/multifields.dset</code></p>
 *
 * @since 0.4.0
 */
public class ElasticSearchMultiFieldsTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_MULTIFIELDS_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/multifields.dset";
    protected static final String EL__MULTIFIELDS_UUID = "multifields";
    
    /**
     * Register the data set used for this test case. 
     */
    @Before
    public void registerDataSet() throws Exception {
        super.setUp();

        // Register the data set definition for multifields index.
        _registerDataSet(EL_MULTIFIELDS_DEF);

    }

    /**
     * Test resulting columns.
     */
    @Test
    public void testColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL__MULTIFIELDS_UUID)
                        .sort("FIELD2.RAW", SortOrder.DESCENDING)
                        .buildLookup());

        // Columns size assertion.
        Assert.assertNotNull(result.getColumns());
        Assert.assertTrue(result.getColumns().size() == 5);

        List<DataColumn> expected = new ArrayList<DataColumn>(5);
        DataColumn colField2 = new DataColumnImpl("FIELD2", ColumnType.TEXT);
        expected.add(colField2);
        DataColumn colDate = new DataColumnImpl("DATE", ColumnType.DATE);
        expected.add(colDate);
        DataColumn colField2Raw = new DataColumnImpl("FIELD2.RAW", ColumnType.LABEL);
        expected.add(colField2Raw);
        DataColumn colNumber = new DataColumnImpl("NUMBER", ColumnType.NUMBER);
        expected.add(colNumber);
        DataColumn colField1 = new DataColumnImpl("FIELD1", ColumnType.TEXT);
        expected.add(colField1);
        
        // As we are not providing any default column order neither in the lookup or in the dset defintiion, 
        // just check columns that all columns are present and are of the given types.
        Assert.assertTrue(result.getColumns().size() == 5);
        Assert.assertTrue(result.getColumns().contains(colField1));
        Assert.assertTrue(result.getColumns().contains(colField2));
        Assert.assertTrue(result.getColumns().contains(colField2Raw));
        Assert.assertTrue(result.getColumns().contains(colDate));
        Assert.assertTrue(result.getColumns().contains(colNumber));
        
    }
    

}
