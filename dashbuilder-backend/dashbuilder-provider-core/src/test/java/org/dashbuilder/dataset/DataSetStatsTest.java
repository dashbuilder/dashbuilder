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

import org.dashbuilder.dataset.engine.DataSetOpEngine;
import org.dashbuilder.dataset.index.DataSetIndex;
import org.dashbuilder.dataset.index.stats.DataSetIndexStats;
import org.dashbuilder.dataset.index.stats.SizeEstimator;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetFactory;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.*;

@RunWith(Arquillian.class)
public class DataSetStatsTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String EXPENSE_REPORTS = "expense_reports_dataset";

    @Inject DataSetServices dataSetServices;

    DataSet dataSet;

    @Before
    public void setUp() throws Exception {
        dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        dataSetServices.getDataSetManager().registerDataSet(dataSet);
    }

    @Test
    public void testGroupOpsPerformance() throws Exception {

        // Create & apply two different group operations over the same data set
        DataSetLookup groupByDept1 = DataSetFactory.newLookup()
                .uuid(EXPENSE_REPORTS)
                .group("department", "Department")
                .count("Occurrences")
                .sum("amount", "totalAmount")
                .buildLookup();

        DataSetLookup groupByDept2 = DataSetFactory.newLookup()
                .uuid(EXPENSE_REPORTS)
                .group("department", "Department")
                .avg("amount", "average")
                .buildLookup();

        // Measure the time elapsed
        DataSetManager dataSetManager = dataSetServices.getDataSetManager();
        long begin = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            // Apply the two ops 10 times
            dataSetManager.lookupDataSet(groupByDept1);
            dataSetManager.lookupDataSet(groupByDept2);
        }
        long time = System.nanoTime()-begin;

        // Check out the resulting stats
        DataSetIndex dataSetIndex = dataSetServices.getDataSetIndexRegistry().get(EXPENSE_REPORTS);
        DataSetIndexStats stats = dataSetIndex.getStats();

        System.out.println("Build time=" + ((double) stats.getBuildTime() / 1000000) + " (secs)");
        System.out.println("Reuse time=" + ((double) stats.getReuseTime() / 1000000) + " (secs)");
        System.out.println("Reuse rate=" + stats.getReuseRate());
        System.out.println("Data set size=" + SizeEstimator.formatSize(stats.getDataSetSize()));
        System.out.println("Index size=" + SizeEstimator.formatSize(stats.getIndexSize()));

        assertThat(stats.getBuildTime()).isLessThan(time);
        assertThat(stats.getReuseRate()).isGreaterThan(10);
        assertThat(stats.getIndexSize()).isLessThan(100);

    }
}
