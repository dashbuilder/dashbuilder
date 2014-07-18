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

import javax.inject.Inject;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.Assertions.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(Arquillian.class)
public class DataSetNestedGroupTest {

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

    /* @Test
      TODO: public void testMultipleYearSplit() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .group("date").fixed(MONTH)
                .sum("total")
                .group("date", YEAR)
                .sum("total per year")
                .buildLookup());

        printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Barcelona", "6.00", "120.35", "1,100.10", "485.52", "2,913.14"},
                {"Madrid", "2.00", "800.80", "911.11", "855.95", "1,711.91"},
                {"Brno", "4.00", "159.01", "800.24", "364.86", "1,459.45"},
                {"Westford", "5.00", "1.10", "600.34", "265.29", "1,326.43"},
                {"Raleigh", "4.00", "209.55", "401.40", "284.38", "1,137.53"},
                {"London", "3.00", "333.45", "868.45", "535.40", "1,606.20"}
        }, 0);
    }*/

    @Test
    public void testGroupSelectionFilter() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                        .dataset(EXPENSE_REPORTS)
                        .filter("amount", FilterFactory.isGreaterThan(500))
                        .group("department").select("Engineering")
                        .group("city").select("Westford")
                        .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(1);
        assertDataSetValue(result, 0, 0, "26.00");
    }

    @Test
    public void testNestedGroupFromMultipleSelection() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .group("department", "Department").select("Services", "Engineering")
                .group("city", "City")
                .count("Occurrences")
                .min("amount", "min")
                .max("amount", "max")
                .avg("amount", "average")
                .sum("amount", "total")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Barcelona", "6.00", "120.35", "1,100.10", "485.52", "2,913.14"},
                {"Madrid", "2.00", "800.80", "911.11", "855.95", "1,711.91"},
                {"Brno", "4.00", "159.01", "800.24", "364.86", "1,459.45"},
                {"Westford", "5.00", "1.10", "600.34", "265.29", "1,326.43"},
                {"Raleigh", "4.00", "209.55", "401.40", "284.38", "1,137.53"},
                {"London", "3.00", "333.45", "868.45", "535.40", "1,606.20"}
        }, 0);
    }

    @Test
    public void testNestedGroupRequiresSelection() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .group("department", "Department")
                .group("city", "city")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Engineering"},
                {"Services"},
                {"Sales"},
                {"Support"},
                {"Management"}
        }, 0);
    }

    @Test
    public void testNoResultsSelection() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                        .dataset(EXPENSE_REPORTS)
                        .group("employee").select("Jerri Preble")
                        .group("department").select("Engineering")
                        .group("city").select("Westford")
                        .group("date").fixed(DateIntervalType.MONTH)
                        .sum("amount", "total")
                        .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"JANUARY", "0.00"},
                {"FEBRUARY", "0.00"},
                {"MARCH", "0.00"},
                {"APRIL", "0.00"},
                {"MAY", "0.00"},
                {"JUNE", "0.00"},
                {"JULY", "0.00"},
                {"AUGUST", "0.00"},
                {"SEPTEMBER", "0.00"},
                {"OCTOBER", "0.00"},
                {"NOVEMBER", "0.00"},
                {"DECEMBER", "0.00"}
        }, 0);
    }

    @Test
    public void testThreeNestedLevels() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                        .dataset(EXPENSE_REPORTS)
                        .group("department").select("Services", "Engineering")
                        .group("city").select("Madrid", "Barcelona")
                        .group("date").fixed(DateIntervalType.MONTH)
                        .sum("amount", "total")
                        .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"JANUARY", "0.00"},
                {"FEBRUARY", "0.00"},
                {"MARCH", "0.00"},
                {"APRIL", "0.00"},
                {"MAY", "0.00"},
                {"JUNE", "911.11"},
                {"JULY", "800.80"},
                {"AUGUST", "152.25"},
                {"SEPTEMBER", "300.00"},
                {"OCTOBER", "340.34"},
                {"NOVEMBER", "900.10"},
                {"DECEMBER", "1,220.45"}
        }, 0);
    }


    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
