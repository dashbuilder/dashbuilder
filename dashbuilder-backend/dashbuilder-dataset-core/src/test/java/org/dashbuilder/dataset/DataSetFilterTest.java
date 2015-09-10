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
package org.dashbuilder.dataset;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Inject;

import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.ExpenseReportsData.*;
import static org.dashbuilder.dataset.Assertions.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class DataSetFilterTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String EXPENSE_REPORTS = "expense_reports";

    @Inject
    public DataSetManager dataSetManager;

    @Inject
    public DataSetFormatter dataSetFormatter;

    @Before
    public void setUp() throws Exception {
        DataSet dataSet = ExpenseReportsData.INSTANCE.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        dataSetManager.registerDataSet(dataSet);
        dataSetFormatter = new DataSetFormatter();
    }

    @Test
    public void testFilterByString() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(COLUMN_CITY, equalsTo("Barcelona"))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(6);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 5, 0, "6.00");
    }

    @Test
    public void testFilterByNumber() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .filter(COLUMN_AMOUNT, between(100, 200))
                .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(5);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "6.00");
        assertDataSetValue(result, 2, 0, "10.00");
        assertDataSetValue(result, 3, 0, "17.00");
        assertDataSetValue(result, 4, 0, "33.00");
    }

    @Test
    public void testFilterByDate() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(2015, 0, 0, 0, 0);
        Timestamp date = new Timestamp(c.getTime().getTime());

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                    .dataset(EXPENSE_REPORTS)
                    .filter(COLUMN_DATE, greaterThan(new Timestamp(date.getTime())))
                    .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(15);
    }

    @Test
    public void testFilterUntilToday() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                    .dataset(EXPENSE_REPORTS)
                    .filter(COLUMN_DATE, timeFrame("10second"))
                    .buildLookup());

        //assertThat(result.getRowCount()).isEqualTo(0);
    }

    @Test
    public void testFilterMultiple() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(2015, 0, 0, 0, 0);
        Timestamp date = new Timestamp(c.getTime().getTime());

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                    .dataset(EXPENSE_REPORTS)
                    .filter(COLUMN_DATE, greaterThan(date))
                    .filter(COLUMN_AMOUNT, lowerOrEqualsTo(120.35))
                    .filter(COLUMN_CITY, notEqualsTo("Barcelona"))
                    .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 1, 0, "10.00");

        // The order of the filter criteria does not alter the result.
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                    .dataset(EXPENSE_REPORTS)
                    .filter(COLUMN_CITY, notEqualsTo("Barcelona"))
                    .filter(COLUMN_AMOUNT, lowerOrEqualsTo(120.35))
                    .filter(COLUMN_DATE, greaterThan(date))
                    .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 1, 0, "10.00");
    }

    @Test
    public void testANDExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(COLUMN_AMOUNT, AND(greaterThan(100), lowerThan(150)))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "1.00");
    }

    @Test
    public void testNOTExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(COLUMN_AMOUNT, NOT(greaterThan(100)))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(5);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 4, 0, "30.00");
    }

    @Test
    public void testORExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(COLUMN_AMOUNT, OR(NOT(greaterThan(100)), greaterThan(1000), equalsTo(COLUMN_ID, 1)))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(8);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 1, 0, "2.00");
        assertDataSetValue(result, 7, 0, "30.00");
    }

    @Test
    public void testORExpressionMultilple() throws Exception {

        List<Comparable> cities = new ArrayList<Comparable>();
        for(String city : new String[] {"Barcelona", "London", "Madrid"}){
            cities.add(city);
        }

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(equalsTo(COLUMN_CITY, cities))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(19);
    }

    @Test
    public void testCombinedExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(COLUMN_AMOUNT, AND(
                                equalsTo(COLUMN_DEPARTMENT, "Sales"),
                                OR(NOT(lowerThan(300)), equalsTo(COLUMN_CITY, "Madrid"))))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(7);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 6, 0, "28.00");
    }

    @Test
    public void testCombinedExpression2() throws Exception {

        List<Comparable> cities = new ArrayList<Comparable>();
        for(String city : new String[] {"Barcelona", "London", "Madrid"}){
            cities.add(city);
        }

        List<ColumnFilter> condList = new  ArrayList<ColumnFilter>();
        for(String employee : new String[] {"Roxie Foraker", "Patricia J. Behr"}){
            condList.add(equalsTo(COLUMN_EMPLOYEE, employee));

        }

        ColumnFilter filter1 = equalsTo(COLUMN_CITY, cities);
        ColumnFilter filter2 = AND(OR(condList), equalsTo(COLUMN_DEPARTMENT, "Engineering"));
        ColumnFilter filter3 = equalsTo(COLUMN_DEPARTMENT, "Services");

        DataSetLookupBuilder builder = DataSetFactory.newDataSetLookupBuilder();
        builder.dataset(EXPENSE_REPORTS);
        builder.filter(AND(filter1, OR(filter2, filter3)));
        builder.column(COLUMN_ID);
        builder.column(COLUMN_CITY);
        builder.column(COLUMN_DEPARTMENT);
        builder.column(COLUMN_EMPLOYEE);
        builder.column(COLUMN_AMOUNT);
        builder.column(COLUMN_DATE);

        //  (CITY = Barcelona, London, Madrid
        //     AND (((EMPLOYEE = Roxie Foraker OR EMPLOYEE = Patricia J. Behr) AND DEPARTMENT = Engineering)
        //            OR DEPARTMENT = Services))
        DataSet result = dataSetManager.lookupDataSet(builder.buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
            {"1.00", "Barcelona", "Engineering", "Roxie Foraker", "120.35", "12/11/15 12:00"},
            {"2.00", "Barcelona", "Engineering", "Roxie Foraker", "1,100.10", "12/01/15 12:00"},
            {"3.00", "Barcelona", "Engineering", "Roxie Foraker", "900.10", "11/01/15 12:00"},
            {"4.00", "Barcelona", "Services", "Jamie Gilbeau", "340.34", "10/12/15 12:00"},
            {"5.00", "Barcelona", "Services", "Jamie Gilbeau", "300.00", "09/15/15 12:00"},
            {"6.00", "Barcelona", "Services", "Jamie Gilbeau", "152.25", "08/17/15 12:00"},
            {"7.00", "Madrid", "Services", "Roxie Foraker", "800.80", "07/01/15 12:00"},
            {"8.00", "Madrid", "Services", "Roxie Foraker", "911.11", "06/01/15 12:00"}
        }, 0);
    }

    @Test
    public void testLikeOperatorCaseSensitive() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(likeTo(COLUMN_CITY, "Bar%"))
                        .column(COLUMN_ID)
                        .sort(COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(6);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 5, 0, "6.00");

        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(likeTo(COLUMN_CITY, "%L%", true /* Case sensitive */))
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(7);
    }

    @Test
    public void testLikeOperatorNonCaseSensitive() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(likeTo(COLUMN_CITY, "Bar%"))
                        .column(COLUMN_ID)
                        .sort(COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(6);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 5, 0, "6.00");

        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .filter(likeTo(COLUMN_CITY, "%L%", false /* Case un-sensitive */))
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(26);
    }

    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
