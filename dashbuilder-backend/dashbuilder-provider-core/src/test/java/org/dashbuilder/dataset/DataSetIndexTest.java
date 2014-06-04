/**
 * Copyright (C) 2012 JBoss Inc
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

import org.dashbuilder.dataset.index.DataSetIndex;
import org.dashbuilder.dataset.index.spi.DataSetIndexRegistry;
import org.dashbuilder.dataset.index.stats.DataSetIndexStats;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.*;
import static org.dashbuilder.model.dataset.filter.FilterFactory.*;

@RunWith(Arquillian.class)
public class DataSetIndexTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String EXPENSE_REPORTS = "expense_reports_dataset";

    /**
     * Group by department and count occurrences
     */
    DataSetLookup groupByDeptAndCount = DataSetFactory.newDSLookup()
            .uuid(EXPENSE_REPORTS)
            .group("department", "Department")
            .count("occurrences")
            .buildLookup();

    /**
     * Group by department and sum the amount
     */
    DataSetLookup groupByDeptAndSum = DataSetFactory.newDSLookup()
            .uuid(EXPENSE_REPORTS)
            .group("department", "Department")
            .avg("amount")
            .buildLookup();

    /**
     * Filter by city & department
     */
    DataSetLookup filterByCityAndDept = DataSetFactory.newDSLookup()
            .uuid(EXPENSE_REPORTS)
            .filter("city", isEqualsTo("Barcelona"))
            .filter("department", isEqualsTo("Engineering"))
            .buildLookup();

    /**
     * Sort by amount in ascending order
     */
    DataSetLookup sortByAmountAsc = DataSetFactory.newDSLookup()
            .uuid(EXPENSE_REPORTS)
            .sort("amount", "asc")
            .buildLookup();

    /**
     * Sort by amount in descending order
     */
    DataSetLookup sortByAmountDesc = DataSetFactory.newDSLookup()
            .uuid(EXPENSE_REPORTS)
            .sort("amount", "desc")
            .buildLookup();

    @Inject DataSetServices dataSetServices;
    DataSetManager dataSetManager;
    DataSetIndexRegistry dataSetIndexRegistry;
    DataSet dataSet;

    @Before
    public void setUp() throws Exception {
        dataSetManager = dataSetServices.getDataSetManager();
        dataSetIndexRegistry = dataSetServices.getDataSetIndexRegistry();
        dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        dataSetManager.registerDataSet(dataSet);
    }

    @Test
    public void testGroupPerformance() throws Exception {

        // Apply two different group operations and measure the elapsed time.
        long begin = System.nanoTime();
        int lookupTimes = 1000;
        for (int i = 0; i < lookupTimes; i++) {
            dataSetManager.lookupDataSet(groupByDeptAndCount);
            dataSetManager.lookupDataSet(groupByDeptAndSum);
        }
        long time = System.nanoTime()-begin;

        // Check out the resulting stats
        DataSetIndex dataSetIndex = dataSetIndexRegistry.get(EXPENSE_REPORTS);
        DataSetIndexStats stats = dataSetIndex.getStats();
        DataSet dataSet = dataSetIndex.getDataSet();
        System.out.println(stats.toString("\n"));

        // Assert the reuse of group operations and aggregate calculations is working.
        assertThat(stats.getNumberOfGroupOps()).isEqualTo(1);
        assertThat(stats.getNumberOfAggFunctions()).isEqualTo(10);

        // The build time should be shorter than the overall lookup time.
        assertThat(stats.getBuildTime()).isLessThan(time);

        // The reuse rate must reflect the number of times the lookups are being reused.
        assertThat(stats.getReuseRate()).isGreaterThanOrEqualTo(lookupTimes-1);

        // The index size must not be greater than the 20% of the dataset's size
        assertThat(stats.getIndexSize()).isLessThan(dataSet.getEstimatedSize()/5);
    }

    @Test
    public void testFilterPerformance() throws Exception {
        // Apply a filter operation and measure the elapsed time.
        long begin = System.nanoTime();
        int lookupTimes = 1000;
        for (int i = 0; i < lookupTimes; i++) {
            dataSetManager.lookupDataSet(filterByCityAndDept);
        }
        long time = System.nanoTime()-begin;

        // Check out the resulting stats
        DataSetIndex dataSetIndex = dataSetIndexRegistry.get(EXPENSE_REPORTS);
        DataSetIndexStats stats = dataSetIndex.getStats();
        DataSet dataSet = dataSetIndex.getDataSet();

        System.out.println(stats.toString("\n"));

        // Assert reuse is working.
        assertThat(stats.getNumberOfFilterOps()).isEqualTo(2);

        // The build time should be shorter than the overall lookup time.
        assertThat(stats.getBuildTime()).isLessThan(time);

        // The reuse rate must reflect the number of times the lookups are being reused.
        assertThat(stats.getReuseRate()).isGreaterThanOrEqualTo(lookupTimes-1);

        // The index size must not be greater than the 20% of the dataset's size
        assertThat(stats.getIndexSize()).isLessThan(dataSet.getEstimatedSize()/5);
    }

    @Test
    public void testSortPerformance() throws Exception {

        // Apply the same sort operation several times and measure the elapsed time.
        long begin = System.nanoTime();
        int lookupTimes = 1000;
        for (int i = 0; i < lookupTimes; i++) {
            dataSetManager.lookupDataSet(sortByAmountAsc);
            dataSetManager.lookupDataSet(sortByAmountDesc);
        }
        long time = System.nanoTime()-begin;

        // Check out the resulting stats
        DataSetIndex dataSetIndex = dataSetIndexRegistry.get(EXPENSE_REPORTS);
        DataSetIndexStats stats = dataSetIndex.getStats();
        DataSet dataSet = dataSetIndex.getDataSet();

        System.out.println(stats.toString("\n"));

        // Assert the reuse of sort operations is working.
        assertThat(stats.getNumberOfSortOps()).isEqualTo(2);

        // The build time should be shorter than the overall lookup time.
        assertThat(stats.getBuildTime()).isLessThan(time);

        // The reuse rate must reflect the number of times the lookups are being reused.
        assertThat(stats.getReuseRate()).isGreaterThanOrEqualTo(lookupTimes - 1);

        // The index size must not be greater than the 20% of the dataset's size
        assertThat(stats.getIndexSize()).isLessThan(dataSet.getEstimatedSize()/5);
    }
}
