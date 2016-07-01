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
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.ExpenseReportsData;
import org.dashbuilder.dataset.group.DateIntervalPattern;
import org.dashbuilder.dataset.sort.SortOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.dashbuilder.dataset.Assertions.assertDataSetValue;
import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.COUNT;
import static org.dashbuilder.dataset.group.AggregateFunctionType.MIN;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * <p>Data test for ElasticSearchDataSet.</p>
 * <p>It uses as source dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset</code></p>
 *
 * @since 0.3.0
 */
public class ElasticSearchDataSetTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset";
    protected static final String EL_EXAMPLE_CSENSITIVE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports-csensitive.dset";
    protected static final String EL_DATASET_UUID = "expense_reports";
    protected static final String EL_DATASET_CSENSITIVE_UUID = "expense_reports_csensitive";
    private static final String DATE_FORMAT = "yyyy-MM-dd Z";
    
    /**
     * Register the data set used for this test case. 
     */
    @Before
    public void registerDataSet() throws Exception {
        super.setUp();

        // Register the data set definition for expense reports index.
        _registerDataSet(EL_EXAMPLE_DATASET_DEF);

        // Register the data set definition for expense reports case sensitive index.
        _registerDataSet(EL_EXAMPLE_CSENSITIVE_DATASET_DEF);
    }

    /**
     * **********************************************************************************************************************************************************************************************
     * COLUMNS TESTING.
     * **********************************************************************************************************************************************************************************************
     */
    
    
    /**
     * Test columns when dataset definition does not contain any column definition.
     *
     * Result should be:
     * <ul>
     *     <li>Column 0 -  id=id name=id type=NUMBER </li>
     *     <li>Column 1 -  id=amount name=amount type=NUMBER </li>
     *     <li>Column 2-  id=department name=department type=LABEL </li>
     *     <li>Column 3 -  id=employee name=employee type=LABEL </li>
     *     <li>Column 4 -  id=date name=date type=DATE </li>
     *     <li>Column 5 -  id=city name=city type=LABEL </li>
     * </ul>
     */
    @Test
    public void testColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        // Columns size assertion.
        Assert.assertNotNull(result.getColumns());
        Assert.assertTrue(result.getColumns().size() == 6);

        // Columns id assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getId().equals(ExpenseReportsData.COLUMN_ID));
        Assert.assertTrue(result.getColumnByIndex(1).getId().equals(ExpenseReportsData.COLUMN_AMOUNT));
        Assert.assertTrue(result.getColumnByIndex(2).getId().equals(ExpenseReportsData.COLUMN_DEPARTMENT));
        Assert.assertTrue(result.getColumnByIndex(3).getId().equals(ExpenseReportsData.COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(4).getId().equals(ExpenseReportsData.COLUMN_DATE));
        Assert.assertTrue(result.getColumnByIndex(5).getId().equals(ExpenseReportsData.COLUMN_CITY));

        // Columns type assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getColumnType().equals(ColumnType.NUMBER));
        Assert.assertTrue(result.getColumnByIndex(1).getColumnType().equals(ColumnType.NUMBER));
        Assert.assertTrue(result.getColumnByIndex(2).getColumnType().equals(ColumnType.LABEL));
        Assert.assertTrue(result.getColumnByIndex(3).getColumnType().equals(ColumnType.TEXT));
        Assert.assertTrue(result.getColumnByIndex(4).getColumnType().equals(ColumnType.DATE));
        Assert.assertTrue(result.getColumnByIndex(5).getColumnType().equals(ColumnType.LABEL));
    }


    /**
     * **********************************************************************************************************************************************************************************************
     * LOOKUP TESTING.
     * **********************************************************************************************************************************************************************************************
     */


    /**
     * No operations.
     */
    @Test
    public void testBasicLookup() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .buildLookup());
        
        assertThat(result.getRowCount()).isEqualTo(50);
    }
    
    /**
     * Just use a sort operation.
     */
    @Test
    public void testDefaultLookup() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(50);
        // Test id column.
        assertThat(result.getValueAt(0, 0)).isEqualTo(1d);
        assertThat(result.getValueAt(49, 0)).isEqualTo(50d);
        
        // Test row 0 values.
        assertThat(result.getValueAt(0, 1)).isEqualTo(120.35);
        assertThat(result.getValueAt(0, 2)).isEqualTo(EL_EXAMPLE_DEPT_ENGINEERING);
        assertThat(result.getValueAt(0, 3)).isEqualTo(EL_EXAMPLE_EMP_ROXIE);
        Date date = new SimpleDateFormat(DATE_FORMAT).parse("2015-12-11 -0900");
        assertThat(result.getValueAt(0, 4)).isEqualTo(date);
        assertThat(result.getValueAt(0, 5)).isEqualTo(EL_EXAMPLE_CITY_BARCELONA);

        // Test row 1 values.
        assertThat(result.getValueAt(1, 0)).isEqualTo(2d);
        assertThat(result.getValueAt(1, 1)).isEqualTo(1100.10);
        assertThat(result.getValueAt(1, 2)).isEqualTo(EL_EXAMPLE_DEPT_ENGINEERING);
        assertThat(result.getValueAt(1, 3)).isEqualTo(EL_EXAMPLE_EMP_ROXIE);
        date = new SimpleDateFormat(DATE_FORMAT).parse("2015-12-01 -0900");
        assertThat(result.getValueAt(1, 4)).isEqualTo(date);
        assertThat(result.getValueAt(1, 5)).isEqualTo(EL_EXAMPLE_CITY_BARCELONA);

        // Test row 8 values.
        assertThat(result.getValueAt(8, 0)).isEqualTo(9d);
        assertThat(result.getValueAt(8, 1)).isEqualTo(75.75d);
        assertThat(result.getValueAt(8, 2)).isEqualTo(EL_EXAMPLE_DEPT_SALES);
        assertThat(result.getValueAt(8, 3)).isEqualTo(EL_EXAMPLE_EMP_NITA);
        date = new SimpleDateFormat(DATE_FORMAT).parse("2015-05-11 -0900");
        assertThat(result.getValueAt(8, 4)).isEqualTo(date);
        assertThat(result.getValueAt(8, 5)).isEqualTo(EL_EXAMPLE_CITY_MADRID);

        // Test row 30 values.
        assertThat(result.getValueAt(30, 0)).isEqualTo(31d);
        assertThat(result.getValueAt(30, 1)).isEqualTo(234.34);
        assertThat(result.getValueAt(30, 2)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(30, 3)).isEqualTo(EL_EXAMPLE_EMP_HANNA);
        date = new SimpleDateFormat(DATE_FORMAT).parse("2013-09-01 -0900");
        assertThat(result.getValueAt(30, 4)).isEqualTo(date);
        assertThat(result.getValueAt(30, 5)).isEqualTo(EL_EXAMPLE_CITY_RALEIGH);

        // Test row 46 values.
        assertThat(result.getValueAt(46, 0)).isEqualTo(47d);
        assertThat(result.getValueAt(46, 1)).isEqualTo(565.56);
        assertThat(result.getValueAt(46, 2)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(46, 3)).isEqualTo(EL_EXAMPLE_EMP_PATRICIA);
        date = new SimpleDateFormat(DATE_FORMAT).parse("2012-04-14 -0900");
        assertThat(result.getValueAt(46, 4)).isEqualTo(date);
        assertThat(result.getValueAt(46, 5)).isEqualTo(EL_EXAMPLE_CITY_LONDON);
    }

    /**
     * Most basic test. Apply descendent sorting.
     */
    @Test
    public void testDefaultLookupWithSorting() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                         .sort(ExpenseReportsData.COLUMN_ID, SortOrder.DESCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(50);
        assertThat(result.getValueAt(0, 0)).isEqualTo(50d);
        assertThat(result.getValueAt(49, 0)).isEqualTo(1d);
    }

    // Sorting by non existing column.
    @Test(expected = RuntimeException.class)
    public void testSortingWithNonExistingColumn() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort("mycolumn", SortOrder.DESCENDING)
                        .buildLookup());
    }

    /**
     * Test trimming.
     */
    @Test
    public void testLookupTrim() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .rowNumber(10)
                        .rowOffset(40)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(10);
        assertThat(result.getValueAt(0, 0)).isEqualTo(41d);
        assertThat(result.getValueAt(9, 0)).isEqualTo(50d);

        // Test row 6 values.
        assertThat(result.getValueAt(6, 0)).isEqualTo(47d);
        assertThat(result.getValueAt(6, 1)).isEqualTo(565.56);
        assertThat(result.getValueAt(6, 2)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(6, 3)).isEqualTo(EL_EXAMPLE_EMP_PATRICIA);
        Date date = new SimpleDateFormat(DATE_FORMAT).parse("2012-04-14 -0900");
        assertThat(result.getValueAt(6, 4)).isEqualTo(date);
        assertThat(result.getValueAt(6, 5)).isEqualTo(EL_EXAMPLE_CITY_LONDON);
    }


    /**
     * ****************************************************************************************************
     * SPECIAL GROUP OPS TESTING.
     * ****************************************************************************************************

    /**
     * Aggregating by a non grouped column with no aggregation function is not allowed.
     * An RuntimeException must be thrown.
     * @throws Exception
     * 
     */
    @Test(expected = RuntimeException.class)
    public void testAggregationByNoFunctionColumn() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .column(ExpenseReportsData.COLUMN_DEPARTMENT, "Department")
                        .column(COUNT, "#items")
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
    }

    @Test
    public void testAggregationWithColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(ExpenseReportsData.COLUMN_DEPARTMENT)
                        .column(ExpenseReportsData.COLUMN_DEPARTMENT, "Department")
                        .column(COUNT, "#items")
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(5);
        assertThat(result.getValueAt(0, 0)).isEqualTo(EL_EXAMPLE_DEPT_ENGINEERING);
        assertThat(result.getValueAt(0, 1)).isEqualTo(19d);
        assertThat(result.getValueAt(1, 0)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(1, 1)).isEqualTo(11d);
        assertThat(result.getValueAt(2, 0)).isEqualTo(EL_EXAMPLE_DEPT_SALES);
        assertThat(result.getValueAt(2, 1)).isEqualTo(8d);
        assertThat(result.getValueAt(3, 0)).isEqualTo(EL_EXAMPLE_DEPT_SERVICES);
        assertThat(result.getValueAt(3, 1)).isEqualTo(5d);
        assertThat(result.getValueAt(4, 0)).isEqualTo(EL_EXAMPLE_DEPT_SUPPORT);
        assertThat(result.getValueAt(4, 1)).isEqualTo(7d);
    }

    @Test(expected = RuntimeException.class)
    public void testAggregationFunctionByNonExistingColumn() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(ExpenseReportsData.COLUMN_DEPARTMENT)
                        .column(ExpenseReportsData.COLUMN_DEPARTMENT, "Department")
                        .column(COUNT, "Occurrences")
                        .column("mycolumn", MIN, "min")
                        .buildLookup());
    }

    /**
     * **********************************************************************************************************************************************************************************************
     * FILTER TESTING.
     * **********************************************************************************************************************************************************************************************
     */
    
    @Test
    public void testFilterEqualsByStringNotAnalyzed() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(6);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 5, 0, "6.00");
    }

    @Test
    public void testFilterEqualsByStringAnalyzed() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, equalsTo(EL_EXAMPLE_EMP_NITA))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(4);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 3, 0, "12.00");
    }

    @Test
    public void testFilterLikeToByStringAnalyzedAndCaseSensitive() throws Exception {

        // Default analyzer for field (it applies lower-cased terms, so it's not case sensitive). As the pattern given in this lookup is not lower cased, result will be empty.
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "Jul%", true))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(0);

        // Default analyzer for field (lowecased analyzer). The lower-cased pattern value works.
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "jul%", true))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
        
        assertThat(result.getRowCount()).isEqualTo(4);


        // Custom analyzer for field (the example field it's case sensitive as it's analyzed using the custom tokenizer analyzer).
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CSENSITIVE_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "Jul%", true))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(4);

        // Custom analyzer for field (the example field it's case sensitive as it's analyzed using the custom tokenizer analyzer).
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CSENSITIVE_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "jul%", true))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(0);
    }

    @Test
    public void testFilterLikeToByStringAnalyzedAndCaseUnSensitive() throws Exception {
        
        // Default analyzer for field (it applies lower-cased terms, so it's not case sensitive).
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "Jul%", false))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(4);

        // Default analyzer for field (lowecased analyzer). It returns same results as previous lookup as the field is analyzed by the default ELS analyzer.
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "jul%", false))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(4);


        // Custom analyzer for field is always for case sensitive filters, so this case will return empty results. 
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CSENSITIVE_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "Jul%", false))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(0);

        // Custom analyzer for field is always for case sensitive filters, so this case will return empty results.
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_CSENSITIVE_UUID)
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, likeTo(ExpenseReportsData.COLUMN_EMPLOYEE, "jul%", false))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(0);
        
    }

    @Test
    public void testFilterLikeToByStringNotAnalyzedAndCaseSensitive() throws Exception {

        // Case sensitive
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_DEPARTMENT, likeTo(ExpenseReportsData.COLUMN_DEPARTMENT, "Sal%", true))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(8);
    }

    // Cannot apply filtering with not analyzed columns using case un-sensitive.
    @Test(expected = RuntimeException.class)
    public void testFilterLikeToByStringNotAnalyzedAndCaseUnSensitive() throws Exception {
        // Case un-sensitive
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_DEPARTMENT, likeTo(ExpenseReportsData.COLUMN_DEPARTMENT, "Sal%", false))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
        
    }
    
    @Test
    public void testFilterMultiple() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_AMOUNT, lowerOrEqualsTo(120.35))
                        .filter(ExpenseReportsData.COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "1.00");

        // The order of the filter criteria does not alter the result.
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(ExpenseReportsData.COLUMN_AMOUNT, lowerOrEqualsTo(120.35))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "1.00");
    }

    @Test
    public void testFilterMultiple2() throws Exception {
       
        // The order of the filter criteria does not alter the result.
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_CITY, notEqualsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(ExpenseReportsData.COLUMN_AMOUNT, greaterOrEqualsTo(1000))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "15.00");
    }

    @Test(expected = RuntimeException.class)
    public void testFilterByNonExistingColumn() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter("myfield", greaterOrEqualsTo(1000))
                        .buildLookup());
    }

    @Test
    public void testANDExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_AMOUNT, AND(greaterThan(100), lowerThan(150)))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "1.00");
    }

    @Test
    public void testORExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_AMOUNT, OR(lowerThan(200), greaterThan(1000)))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(11);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "2.00");
        assertDataSetValue(result, 2, 0, "6.00");
        assertDataSetValue(result, 3, 0, "9.00");
        assertDataSetValue(result, 4, 0, "10.00");
        assertDataSetValue(result, 5, 0, "15.00");
        assertDataSetValue(result, 6, 0, "17.00");
        assertDataSetValue(result, 7, 0, "23.00");
        assertDataSetValue(result, 8, 0, "24.00");
        assertDataSetValue(result, 9, 0, "30.00");
        assertDataSetValue(result, 10, 0, "33.00");
    }

    @Test
    public void testNOTExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_AMOUNT, NOT(greaterThan(200)))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(9);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "6.00");
        assertDataSetValue(result, 2, 0, "9.00");
        assertDataSetValue(result, 3, 0, "10.00");
        assertDataSetValue(result, 4, 0, "17.00");
        assertDataSetValue(result, 5, 0, "23.00");
        assertDataSetValue(result, 6, 0, "24.00");
        assertDataSetValue(result, 7, 0, "30.00");
        assertDataSetValue(result, 8, 0, "33.00");
    }

    @Test
    public void testCombinedExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_AMOUNT, AND(
                                equalsTo(ExpenseReportsData.COLUMN_DEPARTMENT, EL_EXAMPLE_DEPT_SALES),
                                OR(NOT(lowerThan(300)), equalsTo(ExpenseReportsData.COLUMN_CITY, EL_EXAMPLE_CITY_MADRID))))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(7);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 6, 0, "28.00");
    }

    /**
     * **********************************************************************************************************************************************************************************************
     * FILTER EXPRESSIONS COMBINING QUERIES AND FILTERS.
     * Consider:
     * - When filtering by string not_analyzed fields -> translated into EL filters.
     * - When filtering by string analyzed fields -> translated into EL queries.
     * So:
     * - When AND logical expressions are mixed in same filter between analyzed/not_analyzed fields -> use of El queries + filters (implicitly is an AND expression,as the result of the query will be filtered)
     * - When OR logical expressions are mixed in same filter between analyzed/not_analyzed fields -> use of El queries + filters, but we need to achieve an exclusion instead of an inclusion.
     * **********************************************************************************************************************************************************************************************
    */
    
    /** 
     * This test is testing the AND case for analyzed / not_analyzed columns.
     * @throws Exception
     */
    @Test
    public void testFilterByAnalyzedAndNonAnalyzedColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, equalsTo(EL_EXAMPLE_EMP_JAMIE))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(3);
        assertDataSetValue(result, 0, 0, "4.00");
        assertDataSetValue(result, 1, 0, "5.00");
        assertDataSetValue(result, 2, 0, "6.00");
        
    }

    /**
     * This test is testing the AND case for non-analyzed columns.
     * @throws Exception
     */
    @Test
    public void testFilterByNonAnalyzedAndNonAnalyzedColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(ExpenseReportsData.COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(ExpenseReportsData.COLUMN_DEPARTMENT, equalsTo(EL_EXAMPLE_DEPT_ENGINEERING))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(3);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "2.00");
        assertDataSetValue(result, 2, 0, "3.00");

    }


    /**
     * **********************************************************************************************************************************************************************************************
     * AGGREGATIONS FILTERING
     * **********************************************************************************************************************************************************************************************
     */

    @Test
    public void testAggregationAndFilterByAnalyzedField() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .column(COUNT, "#items")
                        .filter(ExpenseReportsData.COLUMN_EMPLOYEE, equalsTo(EL_EXAMPLE_EMP_ROXIE))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValueAt(0, 0)).isEqualTo(5d);
    }

    @Test
    public void testAggregationAndFilterByNonAnalyzedField() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .column(COUNT, "#items")
                        .filter(ExpenseReportsData.COLUMN_DEPARTMENT, equalsTo(EL_EXAMPLE_DEPT_ENGINEERING))
                        .sort(ExpenseReportsData.COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
        
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValueAt(0, 0)).isEqualTo(19d);
    }

}
