/**
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

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
     * 
     * Result should be:
     * <ul>
     *     <li>Column 0 -  id=field1 name=field1 type=TEXT </li>
     *     <li>Column 1 -  id=field2 name=field2 type=TEXT </li>
     *     <li>Column 2-   id=field2.raw name=field2.raw type=LABEL </li>
     *     <li>Column 3 -  id=number name=number type=NUMBER </li>
     *     <li>Column 4 -  id=date name=date type=DATE </li>
     * </ul>
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

        Set<DataColumn> expected = new HashSet<DataColumn>(5);
        DataColumn colField1 = new DataColumnImpl("FIELD1", ColumnType.TEXT);
        expected.add(colField1);
        DataColumn colField2 = new DataColumnImpl("FIELD2", ColumnType.TEXT);
        expected.add(colField2);
        DataColumn colField2Raw = new DataColumnImpl("FIELD2.RAW", ColumnType.LABEL);
        expected.add(colField2Raw);
        DataColumn colNumber = new DataColumnImpl("NUMBER", ColumnType.NUMBER);
        expected.add(colNumber);
        DataColumn colDate = new DataColumnImpl("DATE", ColumnType.DATE);
        expected.add(colDate);

        // Columns id assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getId().equals("FIELD2.RAW"));
        Assert.assertTrue(result.getColumnByIndex(1).getId().equals("FIELD2"));
        Assert.assertTrue(result.getColumnByIndex(2).getId().equals("DATE"));
        Assert.assertTrue(result.getColumnByIndex(3).getId().equals("FIELD1"));
        Assert.assertTrue(result.getColumnByIndex(4).getId().equals("NUMBER"));

        // Columns type assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getColumnType().equals(ColumnType.LABEL));
        Assert.assertTrue(result.getColumnByIndex(1).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(2).getColumnType().equals(ColumnType.DATE));
        Assert.assertTrue(result.getColumnByIndex(3).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(4).getColumnType().equals(ColumnType.NUMBER));
    }

}
