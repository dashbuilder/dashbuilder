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
 * <p>Data test for ElasticSearchDataSet column definitions.</p>
 *  
 * <p>Test dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset</code></p>
 * <ul>
 *     <li>Uses column provided from EL index mapping (allColumns flag is set to true)</li>
 * </ul>
 *
 * <p>Test dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns.dset</code></p>
 * <ul>
 *     <li>Modify column type for city column as TEXT (not as LABEL, by default, allColumns flag is set to true)</li>
 * </ul>
 *
 * <p>Test dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns2.dset</code></p>
 * <ul>
 *     <li>Defined custom columns: id (NUMBER), employee (TEXT), city (TEXT), amount (NUMBER)</li>
 * </ul> 
 *
 * <p>Test dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns-error.dset</code></p>
 * <ul>
 *     <li>Try to override employee column type to label and it's not allowed (as employee field is type analyzed string)</li>
 * </ul> 
 * 
 * @since 0.3.0
 */
public class ElasticSearchDataSetCustomColumnsTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_ALL_COLUMNS_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-allcolumns.dset";
    protected static final String EL_DATASET_ALL_COLUMNS_UUID = "expense_reports_allcolumns";
    protected static final String EL_EXAMPLE_CUSTOM_COLUMNS_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns.dset";
    protected static final String EL_DATASET_CUSTOM_COLUMNS_UUID = "expense_reports_custom_columns";
    protected static final String EL_EXAMPLE_CUSTOM_COLUMNS2_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns2.dset";
    protected static final String EL_DATASET_CUSTOM_COLUMNS2_UUID = "expense_reports_custom_columns2";
    protected static final String EL_EXAMPLE_BAD_COLUMNS_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-custom-columns-error.dset";
    protected static final String EL_DATASET_BAD_COLUMNS_UUID = "expense_reports_custom_columns_error";
    
    /**
     * Register the dataset used for this test case. 
     */
    @Before
    public void registerDataSet() throws Exception {
        // Register the data sets.
        _registerDataSet(EL_EXAMPLE_ALL_COLUMNS_DATASET_DEF);
        _registerDataSet(EL_EXAMPLE_CUSTOM_COLUMNS_DATASET_DEF);
        _registerDataSet(EL_EXAMPLE_CUSTOM_COLUMNS2_DATASET_DEF);
        _registerDataSet(EL_EXAMPLE_BAD_COLUMNS_DATASET_DEF);
    }

    /**
     * **********************************************************************************************************************************************************************************************
     * COLUMNS TESING.
     * **********************************************************************************************************************************************************************************************
     */

    /**
     * Test retrieving all columns from index mapping (no columns defined in def and allColumns flag is set to true)
     */
    @Test
    public void testAllColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_ALL_COLUMNS_UUID)
                        .buildLookup());

        // Columns size assertion.
        Assert.assertNotNull(result.getColumns());
        Assert.assertTrue(result.getColumns().size() == 6);

        // Columns id assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getId().equals(EL_EXAMPLE_COLUMN_AMOUNT));
        Assert.assertTrue(result.getColumnByIndex(1).getId().equals(EL_EXAMPLE_COLUMN_CITY));
        Assert.assertTrue(result.getColumnByIndex(2).getId().equals(EL_EXAMPLE_COLUMN_DATE));
        Assert.assertTrue(result.getColumnByIndex(3).getId().equals(EL_EXAMPLE_COLUMN_DEPT));
        Assert.assertTrue(result.getColumnByIndex(4).getId().equals(EL_EXAMPLE_COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(5).getId().equals(EL_EXAMPLE_COLUMN_ID));

        // Columns name assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getName().equals(EL_EXAMPLE_COLUMN_AMOUNT));
        Assert.assertTrue(result.getColumnByIndex(1).getName().equals(EL_EXAMPLE_COLUMN_CITY));
        Assert.assertTrue(result.getColumnByIndex(2).getName().equals(EL_EXAMPLE_COLUMN_DATE));
        Assert.assertTrue(result.getColumnByIndex(3).getName().equals(EL_EXAMPLE_COLUMN_DEPT));
        Assert.assertTrue(result.getColumnByIndex(4).getName().equals(EL_EXAMPLE_COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(5).getName().equals(EL_EXAMPLE_COLUMN_ID));


        // Columns type assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getColumnType().equals(ColumnType.NUMBER));
        Assert.assertTrue(result.getColumnByIndex(1).getColumnType().equals(ColumnType.LABEL));
        Assert.assertTrue(result.getColumnByIndex(2).getColumnType().equals(ColumnType.DATE));
        Assert.assertTrue(result.getColumnByIndex(3).getColumnType().equals(ColumnType.LABEL));
        Assert.assertTrue(result.getColumnByIndex(4).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(5).getColumnType().equals(ColumnType.NUMBER));
    }


    /**
     * Test retrieving all columns from index mapping and overriding one (a column is  defined in def and allColumns flag is set to true)
     */
    @Test
    public void testCustomColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CUSTOM_COLUMNS_UUID)
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        // Columns size assertion.
        Assert.assertNotNull(result.getColumns());
        Assert.assertTrue(result.getColumns().size() == 6);

        // Columns id assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getId().equals(EL_EXAMPLE_COLUMN_AMOUNT));
        Assert.assertTrue(result.getColumnByIndex(1).getId().equals(EL_EXAMPLE_COLUMN_CITY));
        Assert.assertTrue(result.getColumnByIndex(2).getId().equals(EL_EXAMPLE_COLUMN_DATE));
        Assert.assertTrue(result.getColumnByIndex(3).getId().equals(EL_EXAMPLE_COLUMN_DEPT));
        Assert.assertTrue(result.getColumnByIndex(4).getId().equals(EL_EXAMPLE_COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(5).getId().equals(EL_EXAMPLE_COLUMN_ID));

        // Columns name assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getName().equals(EL_EXAMPLE_COLUMN_AMOUNT));
        Assert.assertTrue(result.getColumnByIndex(1).getName().equals(EL_EXAMPLE_COLUMN_CITY));
        Assert.assertTrue(result.getColumnByIndex(2).getName().equals(EL_EXAMPLE_COLUMN_DATE));
        Assert.assertTrue(result.getColumnByIndex(3).getName().equals(EL_EXAMPLE_COLUMN_DEPT));
        Assert.assertTrue(result.getColumnByIndex(4).getName().equals(EL_EXAMPLE_COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(5).getName().equals(EL_EXAMPLE_COLUMN_ID));


        // Columns type assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getColumnType().equals(ColumnType.NUMBER));
        Assert.assertTrue(result.getColumnByIndex(1).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(2).getColumnType().equals(ColumnType.DATE));
        Assert.assertTrue(result.getColumnByIndex(3).getColumnType().equals(ColumnType.LABEL));
        Assert.assertTrue(result.getColumnByIndex(4).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(5).getColumnType().equals(ColumnType.NUMBER));
    }

    /**
     * Test using column defined in def (allColumns flag is set to false)
     */
    @Test
    public void testGivenColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CUSTOM_COLUMNS2_UUID)
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

    @Test(expected = RuntimeException.class)
    public void testSortingWithNonExstingColumn() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CUSTOM_COLUMNS_UUID)
                        .sort("mycolumn", SortOrder.DESCENDING)
                        .buildLookup());
    }

    @Test(expected = RuntimeException.class)
    public void testSortingWithNonDefinedColumn() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CUSTOM_COLUMNS2_UUID)
                        .sort(EL_EXAMPLE_COLUMN_DEPT, SortOrder.DESCENDING)
                        .buildLookup());
    }



    /**
     * Test columns as this dataset defintion contains custom definitions.
     * An exception must be thrown due to cannot change employee column type to label, as it's an anaylzed string in the EL index mapping.
     */
    @Test(expected = RuntimeException.class)
    public void testColumnsBadDefined() throws Exception {
        dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_BAD_COLUMNS_UUID)
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
    }
}
