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
import org.dashbuilder.dataset.backend.date.DateUtils;
import org.dashbuilder.dataset.group.DateIntervalPattern;
import org.dashbuilder.dataset.sort.SortOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.dashbuilder.dataset.Assertions.assertDataSetValue;
import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * <p>Data test for ElasticSearchDataSet.</p>
 * <p>It uses as source dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset</code></p>
 *
 * @since 0.3.0
 */
public class ElasticSearchDataSetTest extends ElasticSearchDataSetTestBase {

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
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        // Columns size assertion.
        Assert.assertNotNull(result.getColumns());
        Assert.assertTrue(result.getColumns().size() == 6);

        // Columns id assertion.
        Assert.assertTrue(result.getColumnByIndex(0).getId().equals(EL_EXAMPLE_COLUMN_ID));
        Assert.assertTrue(result.getColumnByIndex(1).getId().equals(EL_EXAMPLE_COLUMN_AMOUNT));
        Assert.assertTrue(result.getColumnByIndex(2).getId().equals(EL_EXAMPLE_COLUMN_DEPT));
        Assert.assertTrue(result.getColumnByIndex(3).getId().equals(EL_EXAMPLE_COLUMN_EMPLOYEE));
        Assert.assertTrue(result.getColumnByIndex(4).getId().equals(EL_EXAMPLE_COLUMN_DATE));
        Assert.assertTrue(result.getColumnByIndex(5).getId().equals(EL_EXAMPLE_COLUMN_CITY));

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
        assertThat(result.getValueAt(0, 1)).isEqualTo(120.35d);
        assertThat(result.getValueAt(0, 2)).isEqualTo(EL_EXAMPLE_DEPT_ENGINEERING);
        assertThat(result.getValueAt(0, 3)).isEqualTo(EL_EXAMPLE_EMP_ROXIE);
        Date date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2012-12-11");
        assertThat(result.getValueAt(0, 4)).isEqualTo(date);
        assertThat(result.getValueAt(0, 5)).isEqualTo(EL_EXAMPLE_CITY_BARCELONA);

        // Test row 1 values.
        assertThat(result.getValueAt(1, 0)).isEqualTo(2d);
        assertThat(result.getValueAt(1, 1)).isEqualTo(1100.1d);
        assertThat(result.getValueAt(1, 2)).isEqualTo(EL_EXAMPLE_DEPT_ENGINEERING);
        assertThat(result.getValueAt(1, 3)).isEqualTo(EL_EXAMPLE_EMP_ROXIE);
        date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2012-12-01");
        assertThat(result.getValueAt(1, 4)).isEqualTo(date);
        assertThat(result.getValueAt(1, 5)).isEqualTo(EL_EXAMPLE_CITY_BARCELONA);

        // Test row 8 values.
        assertThat(result.getValueAt(8, 0)).isEqualTo(9d);
        assertThat(result.getValueAt(8, 1)).isEqualTo(75.75d);
        assertThat(result.getValueAt(8, 2)).isEqualTo(EL_EXAMPLE_DEPT_SALES);
        assertThat(result.getValueAt(8, 3)).isEqualTo(EL_EXAMPLE_EMP_NITA);
        date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2012-05-11");
        assertThat(result.getValueAt(8, 4)).isEqualTo(date);
        assertThat(result.getValueAt(8, 5)).isEqualTo(EL_EXAMPLE_CITY_MADRID);

        // Test row 30 values.
        assertThat(result.getValueAt(30, 0)).isEqualTo(31d);
        assertThat(result.getValueAt(30, 1)).isEqualTo(234.34d);
        assertThat(result.getValueAt(30, 2)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(30, 3)).isEqualTo(EL_EXAMPLE_EMP_HANNA);
        date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2010-09-01");
        assertThat(result.getValueAt(30, 4)).isEqualTo(date);
        assertThat(result.getValueAt(30, 5)).isEqualTo(EL_EXAMPLE_CITY_RALEIGH);

        // Test row 46 values.
        assertThat(result.getValueAt(46, 0)).isEqualTo(47d);
        assertThat(result.getValueAt(46, 1)).isEqualTo(565.56d);
        assertThat(result.getValueAt(46, 2)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(46, 3)).isEqualTo(EL_EXAMPLE_EMP_PATRICIA);
        date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2009-04-14");
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
                         .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.DESCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(50);
        assertThat(result.getValueAt(0, 0)).isEqualTo(50d);
        assertThat(result.getValueAt(49, 0)).isEqualTo(1d);
    }

    // Sorting by non existing column.
    @Test(expected = RuntimeException.class)
    public void testSortingWithNonExstingColumn() throws Exception {
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
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .rowNumber(10)
                        .rowOffset(40)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(10);
        assertThat(result.getValueAt(0, 0)).isEqualTo(41d);
        assertThat(result.getValueAt(9, 0)).isEqualTo(50d);

        // Test row 6 values.
        assertThat(result.getValueAt(6, 0)).isEqualTo(47d);
        assertThat(result.getValueAt(6, 1)).isEqualTo(565.56d);
        assertThat(result.getValueAt(6, 2)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(6, 3)).isEqualTo(EL_EXAMPLE_EMP_PATRICIA);
        Date date = new SimpleDateFormat(DateIntervalPattern.DAY).parse("2009-04-14");
        assertThat(result.getValueAt(6, 4)).isEqualTo(date);
        assertThat(result.getValueAt(6, 5)).isEqualTo(EL_EXAMPLE_CITY_LONDON);
    }


    /**
     * **********************************************************************************************************************************************************************************************
     * AGGREGATIONS TESTING.
     * **********************************************************************************************************************************************************************************************
     */

    /**
     * <p>Test group (aggregation) functions.</p>
     *
     * <p>Results should be:</p>
     * <ul>
     *     <li>column 0 - count = 50</li>
     *     <li>column1 - amount min - 43.029998779296875</li>
     *     <li>column2 - amount max - 1402.300048828125</li>
     *     <li>column3 - amount avg - 504.6232014465332</li>
     *     <li>column4 - amount sum - 25231.16007232666</li>
     *     <li>column5 - city distinct - 6</li>
     * </ul>
     *
     *
     */
    @Test
    public void testGroupFunctions() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .column(COUNT, "#items")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, MIN, "min")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, MAX, "max")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, AVERAGE, "avg")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "sum")
                        .column(EL_EXAMPLE_COLUMN_CITY, DISTINCT, "distinct")
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValueAt(0, 0)).isEqualTo(50d);
        assertThat(result.getValueAt(0, 1)).isEqualTo(43.029998779296875d);
        assertThat(result.getValueAt(0, 2)).isEqualTo(1402.300048828125d);
        assertThat(result.getValueAt(0, 3)).isEqualTo(504.6232014465332d);
        assertThat(result.getValueAt(0, 4)).isEqualTo(25231.16007232666d);
        assertThat(result.getValueAt(0, 5)).isEqualTo(6d);
    }
    
    /**
     * <p>Test group (aggregation) functions.</p>
     * 
     * <p>Results should be:</p>
     * <ul>
     *     <li>column 0 - count = 50</li>
     *     <li>column1 - amount min - 43.029998779296875</li>
     *     <li>column2 - amount max - 1402.300048828125</li>
     *     <li>column3 - amount avg - 504.6232014465332</li>
     *     <li>column4 - amount sum - 25231.16007232666</li>
     *     <li>column5 - city distinct - 6</li>
     * </ul>
     * 
     * <p>This test differs from the previous one <code>testGroupFunctions</code> by not specifying resulting column identifiers for aggregated columns (it should be generated by the engine).</p>
     */
    @Test
    public void testGroupFunctionsWithAggregatedColumnsWithNoResultingColumnId() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .column(COUNT, "#items")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, MIN)
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, MAX)
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, AVERAGE)
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM)
                        .column(EL_EXAMPLE_COLUMN_CITY, DISTINCT)
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValueAt(0, 0)).isEqualTo(50d);
        assertThat(result.getValueAt(0, 1)).isEqualTo(43.029998779296875d);
        assertThat(result.getValueAt(0, 2)).isEqualTo(1402.300048828125d);
        assertThat(result.getValueAt(0, 3)).isEqualTo(504.6232014465332d);
        assertThat(result.getValueAt(0, 4)).isEqualTo(25231.16007232666d);
        assertThat(result.getValueAt(0, 5)).isEqualTo(6d);
    }

    @Test
    public void testGroupByLabelDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DEPT)
                        .column(EL_EXAMPLE_COLUMN_DEPT, "Department")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, MIN, "min")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, MAX, "max")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, AVERAGE, "average")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "total")
                        .sort(EL_EXAMPLE_COLUMN_AMOUNT, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(5);
        
        assertThat(result.getValueAt(0, 0)).isEqualTo(EL_EXAMPLE_DEPT_ENGINEERING);
        assertThat(result.getValueAt(0, 1)).isEqualTo(19.0d);
        assertThat(result.getValueAt(0, 2)).isEqualTo(120.3499984741211d);
        assertThat(result.getValueAt(0, 3)).isEqualTo(1402.300048828125d);
        assertThat(result.getValueAt(0, 4)).isEqualTo(534.2136836804842d);
        assertThat(result.getValueAt(0, 5)).isEqualTo(10150.0599899292d);

        assertThat(result.getValueAt(1, 0)).isEqualTo(EL_EXAMPLE_DEPT_MANAGEMENT);
        assertThat(result.getValueAt(1, 1)).isEqualTo(11.0d);
        assertThat(result.getValueAt(1, 2)).isEqualTo(43.029998779296875d);
        assertThat(result.getValueAt(1, 3)).isEqualTo(992.2000122070312d);
        assertThat(result.getValueAt(1, 4)).isEqualTo(547.0427315451882d);
        assertThat(result.getValueAt(1, 5)).isEqualTo(6017.47004699707d);

        assertThat(result.getValueAt(2, 0)).isEqualTo(EL_EXAMPLE_DEPT_SALES);
        assertThat(result.getValueAt(2, 1)).isEqualTo(8.0d);
        assertThat(result.getValueAt(2, 2)).isEqualTo(75.75d);
        assertThat(result.getValueAt(2, 3)).isEqualTo(995.2999877929688d);
        assertThat(result.getValueAt(2, 4)).isEqualTo(401.6912536621094d);
        assertThat(result.getValueAt(2, 5)).isEqualTo(3213.530029296875d);

        assertThat(result.getValueAt(3, 0)).isEqualTo(EL_EXAMPLE_DEPT_SERVICES);
        assertThat(result.getValueAt(3, 1)).isEqualTo(5.0d);
        assertThat(result.getValueAt(3, 2)).isEqualTo(152.25d);
        assertThat(result.getValueAt(3, 3)).isEqualTo(911.1099853515625d);
        assertThat(result.getValueAt(3, 4)).isEqualTo(500.8999938964844d);
        assertThat(result.getValueAt(3, 5)).isEqualTo(2504.499969482422d);

        assertThat(result.getValueAt(4, 0)).isEqualTo(EL_EXAMPLE_DEPT_SUPPORT);
        assertThat(result.getValueAt(4, 1)).isEqualTo(7.0d);
        assertThat(result.getValueAt(4, 2)).isEqualTo(300.010009765625d);
        assertThat(result.getValueAt(4, 3)).isEqualTo(1001.9000244140625d);
        assertThat(result.getValueAt(4, 4)).isEqualTo(477.942862374442d);
        assertThat(result.getValueAt(4, 5)).isEqualTo(3345.6000366210938d);

    }

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
                        .column(EL_EXAMPLE_COLUMN_DEPT, "Department")
                        .column(COUNT, "#items")
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
    }

    @Test
    public void testAggregationWithColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DEPT)
                        .column(EL_EXAMPLE_COLUMN_DEPT, "Department")
                        .column(COUNT, "#items")
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
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
    public void testAggregationGroupByNonExistingColumn() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group("mycolumn")
                        .buildLookup());
    }

    @Test(expected = RuntimeException.class)
    public void testAggregationFunctionByNonExistingColumn() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DEPT)
                        .column(EL_EXAMPLE_COLUMN_DEPT, "Department")
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
    public void testFilterByStringNotAnalyzed() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(6);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 5, 0, "6.00");
    }

    @Test
    public void testFilterByStringAnalyzed() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_EMPLOYEE, equalsTo(EL_EXAMPLE_EMP_NITA))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(4);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 3, 0, "12.00");
    }
    
    @Test
    public void testFilterByNumber() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, between(100, 200))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(4);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "6.00");
        assertDataSetValue(result, 2, 0, "17.00");
        assertDataSetValue(result, 3, 0, "33.00");
    }

    @Test
    public void testFilterByDate() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(2012, 0, 0, 0, 0);
        Timestamp date = new Timestamp(c.getTime().getTime());

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_DATE, greaterThan(new Timestamp(date.getTime())))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(15);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "2.00");
        assertDataSetValue(result, 2, 0, "3.00");
        assertDataSetValue(result, 3, 0, "4.00");
        assertDataSetValue(result, 4, 0, "5.00");
        assertDataSetValue(result, 5, 0, "6.00");
        assertDataSetValue(result, 6, 0, "7.00");
        assertDataSetValue(result, 7, 0, "8.00");
        assertDataSetValue(result, 8, 0, "9.00");
        assertDataSetValue(result, 9, 0, "10.00");
        assertDataSetValue(result, 10, 0, "11.00");
        assertDataSetValue(result, 11, 0, "12.00");
        assertDataSetValue(result, 12, 0, "13.00");
        assertDataSetValue(result, 13, 0, "14.00");
        assertDataSetValue(result, 14, 0, "15.00");
    }

    @Test
    public void testFilterMultipleByDate() throws Exception {
        // Date column filtering and other filters.
        Calendar c = Calendar.getInstance();
        c.set(2010, 0, 0, 0, 0);
        Timestamp date = new Timestamp(c.getTime().getTime());

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_DATE, lowerThan(new Timestamp(date.getTime())))
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, greaterThan(500))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(4);
        assertDataSetValue(result, 0, 0, "45.00");
        assertDataSetValue(result, 1, 0, "47.00");
        assertDataSetValue(result, 2, 0, "49.00");
        assertDataSetValue(result, 3, 0, "50.00");
    }

    @Test
    public void testFilterMultiple() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, lowerOrEqualsTo(120.35))
                        .filter(EL_EXAMPLE_COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "1.00");

        // The order of the filter criteria does not alter the result.
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, lowerOrEqualsTo(120.35))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
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
                        .filter(EL_EXAMPLE_COLUMN_CITY, notEqualsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, greaterOrEqualsTo(1000))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(3);
        assertDataSetValue(result, 0, 0, "15.00");
        assertDataSetValue(result, 1, 0, "23.00");
        assertDataSetValue(result, 2, 0, "24.00");
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
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, AND(greaterThan(100), lowerThan(150)))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "1.00");
    }

    @Test
    public void testORExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, OR(lowerThan(200), greaterThan(1000)))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
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
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, NOT(greaterThan(200)))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(7);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "6.00");
        assertDataSetValue(result, 2, 0, "9.00");
        assertDataSetValue(result, 3, 0, "10.00");
        assertDataSetValue(result, 4, 0, "17.00");
        assertDataSetValue(result, 5, 0, "30.00");
        assertDataSetValue(result, 6, 0, "33.00");
    }

    @Test
    public void testCombinedExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_AMOUNT, AND(
                                equalsTo(EL_EXAMPLE_COLUMN_DEPT, EL_EXAMPLE_DEPT_SALES),
                                OR(NOT(lowerThan(300)), equalsTo(EL_EXAMPLE_COLUMN_CITY, EL_EXAMPLE_CITY_MADRID))))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
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
                        .filter(EL_EXAMPLE_COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(EL_EXAMPLE_COLUMN_EMPLOYEE, equalsTo(EL_EXAMPLE_EMP_JAMIE))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
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
                        .filter(EL_EXAMPLE_COLUMN_CITY, equalsTo(EL_EXAMPLE_CITY_BARCELONA))
                        .filter(EL_EXAMPLE_COLUMN_DEPT, equalsTo(EL_EXAMPLE_DEPT_ENGINEERING))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
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
                        .filter(EL_EXAMPLE_COLUMN_EMPLOYEE, equalsTo(EL_EXAMPLE_EMP_ROXIE))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
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
                        .filter(EL_EXAMPLE_COLUMN_DEPT, equalsTo(EL_EXAMPLE_DEPT_ENGINEERING))
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());
        
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValueAt(0, 0)).isEqualTo(19d);
    }

}
