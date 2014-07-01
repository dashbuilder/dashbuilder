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
import static org.dashbuilder.model.dataset.sort.SortOrder.*;

@RunWith(Arquillian.class)
public class DataSetSortTest {

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
    public void testSortByString() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .sort("city", ASCENDING)
                .buildLookup());

        //printDataSet(result);
        assertDataSetValue(result, 0, 1, "Barcelona");
        assertDataSetValue(result, 6, 1, "Brno");
        assertDataSetValue(result, 15, 1, "London");
        assertDataSetValue(result, 22, 1, "Madrid");
        assertDataSetValue(result, 28, 1, "Raleigh");
        assertDataSetValue(result, 41, 1, "Westford");
    }

    @Test
    public void testSortByNumber() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .sort("amount", ASCENDING)
                .buildLookup());

        //printDataSet(result);
        assertDataSetValue(result, 0, 0, "23.00");
        assertDataSetValue(result, 49, 0, "2.00");
    }

    @Test
    public void testSortByDate() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .sort("date", ASCENDING)
                .buildLookup());

        //printDataSet(result);
        assertDataSetValue(result, 0, 0, "50.00");
        assertDataSetValue(result, 49, 0, "1.00");
    }

    @Test
    public void testSortMultiple() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .sort("city", ASCENDING)
                .sort("department", ASCENDING)
                .sort("amount", DESCENDING)
                .buildLookup());

        //printDataSet(result);
        assertDataSetValue(result, 0, 0, "2.00");
        assertDataSetValue(result, 5, 0, "6.00");
        assertDataSetValue(result, 6, 0, "19.00");
        assertDataSetValue(result, 49, 0, "28.00");
    }

    @Test
    public void testGroupandSort() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDSLookup()
                .dataset(EXPENSE_REPORTS)
                .group("department")
                .sum("amount", "total")
                .sort("total", DESCENDING)
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Engineering", "7,650.16"},
                {"Management", "6,017.47"},
                {"Support", "3,345.60"},
                {"Sales", "3,213.53"},
                {"Services", "2,504.50"}
        }, 0);
    }


    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
