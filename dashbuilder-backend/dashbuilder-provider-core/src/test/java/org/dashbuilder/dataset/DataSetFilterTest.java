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

import java.util.Calendar;
import java.util.Date;
import javax.inject.Inject;

import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.Assertions.*;
import static org.dashbuilder.model.dataset.filter.FilterFactory.*;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class DataSetFilterTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String EXPENSE_REPORTS = "expense_reports_dataset";

    @Inject
    DataSetManager dataSetManager;

    protected DataSet dataSet;
    protected DataSetFormatter dataSetFormatter;

    @Before
    public void setUp() throws Exception {
        dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        dataSetManager.registerDataSet(dataSet);
        dataSetFormatter = new DataSetFormatter();
    }

    @Test
    public void testFilterByString() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .filter("city", isEqualsTo("Barcelona"))
                .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(6);
        assertDataSetValue(result, 0, 0, "1.00");
        assertDataSetValue(result, 5, 0, "6.00");
    }

    @Test
    public void testFilterByNumber() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .filter("amount", isBetween(100, 200))
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
        Date date = c.getTime();

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                    .dataset(EXPENSE_REPORTS)
                    .filter("date", isGreaterThan(date))
                    .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(15);
    }

    @Test
    public void testFilterMultiple() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(2015, 0, 0, 0, 0);
        Date date = c.getTime();

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                    .dataset(EXPENSE_REPORTS)
                    .filter("date", isGreaterThan(date))
                    .filter("amount", isLowerOrEqualsTo(120.35))
                    .filter("city", isNotEqualsTo("Barcelona"))
                    .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 1, 0, "10.00");

        // The order of the filter criteria does not alter the result.
        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                    .dataset(EXPENSE_REPORTS)
                    .filter("city", isNotEqualsTo("Barcelona"))
                    .filter("amount", isLowerOrEqualsTo(120.35))
                    .filter("date", isGreaterThan(date))
                    .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 1, 0, "10.00");
    }

    @Test
    public void testANDExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                        .dataset(EXPENSE_REPORTS)
                        .filter("amount", AND(isGreaterThan(100), isLowerThan(150)))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "1.00");
    }

    @Test
    public void testNOTExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                        .dataset(EXPENSE_REPORTS)
                        .filter("amount", NOT(isGreaterThan(100)))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(5);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 4, 0, "30.00");
    }

    @Test
    public void testORExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                        .dataset(EXPENSE_REPORTS)
                        .filter("amount", OR( NOT(isGreaterThan(100)), isGreaterThan(1000) ))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(7);
        assertDataSetValue(result, 0, 0, "2.00");
        assertDataSetValue(result, 6, 0, "30.00");
    }

    @Test
    public void testCombinedExpression() throws Exception {

        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                        .dataset(EXPENSE_REPORTS)
                        .filter("amount", AND(
                                isEqualsTo("department", "Sales"),
                                OR( NOT(isLowerThan(300)), isEqualsTo("city", "Madrid") )))
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(7);
        assertDataSetValue(result, 0, 0, "9.00");
        assertDataSetValue(result, 6, 0, "28.00");
    }


    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
