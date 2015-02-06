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

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * <p>Data test for ElasticSearchDataSet.</p>
 * <p>It uses as source dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns.dset</code></p>
 * <p>This dataset definition defines:</p>
 * <ul>
 *     <li>Id, employee, city & amount columns.</li>
 *     <li>Modify column type for city column as TEXT (not as LABEL, by default).</li>
 * </ul>
 * 
 * @since 0.3.0
 */
public class ElasticSearchDataSetCustomColumnsTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns.dset";
    protected static final String EL_DATASET_UUID = "expense_reports_custom_columns";
    
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
     * 
     * Result should be:
     * <ul>
     *     <li>Column 0 -  id=id name=id type=NUMBER </li>
     *     <li>Column 1 -  id=employee name=employee type=LABEL </li>
     *     <li>Column 2 -  id=city name=city type=TEXT </li>
     *     <li>Column 3 -  id=amount name=amount type=NUMBER </li>
     * </ul>
     */
    @Test
    public void testColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        // Columns size assertion.
        Assert.assertNotNull(result.getColumns());
        Assert.assertTrue(result.getColumns().size() == 4);

        // Columns id assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getId().equals(EL_EXAMPLE_COLUMN_ID));
        Assert.assertTrue(result.getColumnByIndex(1).getId().equals(EL_EXAMPLE_COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(2).getId().equals(EL_EXAMPLE_COLUMN_CITY));
        Assert.assertTrue(result.getColumnByIndex(3).getId().equals(EL_EXAMPLE_COLUMN_AMOUNT));

        // Columns name assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getName().equals(EL_EXAMPLE_COLUMN_ID));
        Assert.assertTrue(result.getColumnByIndex(1).getName().equals(EL_EXAMPLE_COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(2).getName().equals(EL_EXAMPLE_COLUMN_CITY));
        Assert.assertTrue(result.getColumnByIndex(3).getName().equals(EL_EXAMPLE_COLUMN_AMOUNT));

        // Columns type assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getColumnType().equals(ColumnType.NUMBER));
        Assert.assertTrue(result.getColumnByIndex(1).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(2).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(3).getColumnType().equals(ColumnType.NUMBER));
    }

    /**
     * **********************************************************************************************************************************************************************************************
     * LOOKUP TESTING.
     * **********************************************************************************************************************************************************************************************
     */

    /**
     * Most basic test.
     */
    @Test
    public void testDefaultLookup() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(50);
        // Test id column.
        assertThat(result.getValueAt(0, 0)).isEqualTo(1d);
        assertThat(result.getValueAt(49, 0)).isEqualTo(50d);

        // Test row 0 values.
        assertThat(result.getValueAt(0, 1)).isEqualTo(EL_EXAMPLE_EMP_ROXIE);
        assertThat(result.getValueAt(0, 2)).isEqualTo(EL_EXAMPLE_CITY_BARCELONA);
        assertThat(result.getValueAt(0, 3)).isEqualTo(120.35d);

        // Test row 1 values.
        assertThat(result.getValueAt(1, 0)).isEqualTo(2d);
        assertThat(result.getValueAt(1, 1)).isEqualTo(EL_EXAMPLE_EMP_ROXIE);
        assertThat(result.getValueAt(1, 2)).isEqualTo(EL_EXAMPLE_CITY_BARCELONA);
        assertThat(result.getValueAt(1, 3)).isEqualTo(1100.1d);

        // Test row 8 values.
        assertThat(result.getValueAt(8, 0)).isEqualTo(9d);
        assertThat(result.getValueAt(8, 1)).isEqualTo(EL_EXAMPLE_EMP_NITA);
        assertThat(result.getValueAt(8, 2)).isEqualTo(EL_EXAMPLE_CITY_MADRID);
        assertThat(result.getValueAt(8, 3)).isEqualTo(75.75d);

        // Test row 30 values.
        assertThat(result.getValueAt(30, 0)).isEqualTo(31d);
        assertThat(result.getValueAt(30, 1)).isEqualTo(EL_EXAMPLE_EMP_HANNA);
        assertThat(result.getValueAt(30, 2)).isEqualTo(EL_EXAMPLE_CITY_RALEIGH);
        assertThat(result.getValueAt(30, 3)).isEqualTo(234.34d);

        // Test row 46 values.
        assertThat(result.getValueAt(46, 0)).isEqualTo(47d);
        assertThat(result.getValueAt(46, 1)).isEqualTo(EL_EXAMPLE_EMP_PATRICIA);
        assertThat(result.getValueAt(46, 2)).isEqualTo(EL_EXAMPLE_CITY_LONDON);
        assertThat(result.getValueAt(46, 3)).isEqualTo(565.56d);
    }

    /**
     * Most basic test. Apply descendant sorting.
     */
    @Test
    public void testDefaultLookupWithSorting() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.DESCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(50);
        assertThat(result.getValueAt(0, 0)).isEqualTo(50d);
        assertThat(result.getValueAt(49, 0)).isEqualTo(1d);
    }

    @Test(expected = RuntimeException.class)
    public void testSortingWithNonExstingColumn() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort("mycolumn", SortOrder.DESCENDING)
                        .buildLookup());
    }

    @Test(expected = RuntimeException.class)
    public void testSortingWithNonDefinedColumn() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(EL_EXAMPLE_COLUMN_DEPT, SortOrder.DESCENDING)
                        .buildLookup());
    }

   
}
