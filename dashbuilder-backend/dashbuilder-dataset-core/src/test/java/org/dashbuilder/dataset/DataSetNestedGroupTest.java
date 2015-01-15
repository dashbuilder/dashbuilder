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
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

@RunWith(Arquillian.class)
public class DataSetNestedGroupTest {

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
        DataSet dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        dataSetManager.registerDataSet(dataSet);
        dataSetFormatter = new DataSetFormatter();
    }

/*
     @Test
     public void testMultipleYearSplit() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").fixed(DateIntervalType.MONTH)
                .column("amount", SUM, "total")
                .column("date", "amount", SUM, "total in {date}", DateIntervalType.YEAR)
                .buildLookup());

        printDataSet(result);
    }
*/

    @Test
    public void testGroupSelectionFilter() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
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
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("department", "Department").select("Services", "Engineering")
                .group("city", "City")
                .column("city")
                .column(COUNT, "Occurrences")
                .column("amount", MIN, "min")
                .column("amount", MAX, "max")
                .column("amount", AVERAGE, "average")
                .column("amount", SUM, "total")
                .sort("city", "asc")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Barcelona", "6.00", "120.35", "1,100.10", "485.52", "2,913.14"},
                {"Brno", "4.00", "159.01", "800.24", "364.86", "1,459.45"},
                {"London", "3.00", "333.45", "868.45", "535.40", "1,606.20"},
                {"Madrid", "2.00", "800.80", "911.11", "855.96", "1,711.91"},
                {"Raleigh", "4.00", "209.55", "401.40", "284.38", "1,137.53"},
                {"Westford", "5.00", "1.10", "600.34", "265.29", "1,326.43"}
        }, 0);
    }

    @Test
    public void testNestedGroupRequiresSelection() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("department", "Department")
                .column("department")
                .group("city", "city")
                .sort("department", "asc")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Engineering"},
                {"Management"},
                {"Sales"},
                {"Services"},
                {"Support"}
        }, 0);
    }

    @Test
    public void testNoResultsSelection() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("employee").select("Jerri Preble")
                .group("department").select("Engineering")
                .group("city").select("Westford")
                .group("date").fixed(DateIntervalType.MONTH, true)
                .column("date")
                .column("amount", SUM, "total")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"1", "0.00"},
                {"2", "0.00"},
                {"3", "0.00"},
                {"4", "0.00"},
                {"5", "0.00"},
                {"6", "0.00"},
                {"7", "0.00"},
                {"8", "0.00"},
                {"9", "0.00"},
                {"10", "0.00"},
                {"11", "0.00"},
                {"12", "0.00"}
        }, 0);
    }

    @Test
    public void testThreeNestedLevels() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("department").select("Services", "Engineering")
                .group("city").select("Madrid", "Barcelona")
                .group("date").fixed(DateIntervalType.MONTH, true)
                .column("date")
                .column("amount", SUM, "total")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"1", "0.00"},
                {"2", "0.00"},
                {"3", "0.00"},
                {"4", "0.00"},
                {"5", "0.00"},
                {"6", "911.11"},
                {"7", "800.80"},
                {"8", "152.25"},
                {"9", "300.00"},
                {"10", "340.34"},
                {"11", "900.10"},
                {"12", "1,220.45"}
        }, 0);
    }

    @Test
    public void testGroupJoin() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("department")
                .group("city").select("Barcelona", "Brno").join()
                .group("date", "month").fixed(DateIntervalType.MONTH, true).join()
                .column("department")
                .column("city")
                .column("month")
                .column("amount", SUM, "total")
                .buildLookup());

        //printDataSet(result);
    }

    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
