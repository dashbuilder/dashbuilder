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

import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetLookupBuilder;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpStats;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.DataSetStats;
import org.dashbuilder.storage.memory.SizeEstimator;
import org.dashbuilder.storage.memory.TransientDataSetStorage;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.*;

import static org.dashbuilder.model.dataset.group.ScalarFunctionType.*;

@RunWith(Arquillian.class)
public class TransientDataSetStorageTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String EXPENSE_REPORTS = "expense_reports_dataset";

    @Inject
    TransientDataSetStorage storage;

    @Before
    public void setUp() throws Exception {
        DataSet dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        storage.put(dataSet);
    }

    @Test
    public void testGroupOpsPerformance() throws Exception {
        // Create & apply two different group operations over the same data set
        DataSetOp groupByDept1 = new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .group("department", "Department")
                .count("Occurrences")
                .sum("amount", "totalAmount")
                .build().getOperationList().get(0);

        DataSetOp groupByDept2 = new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .group("department", "Department")
                .avg("amount", "average")
                .build().getOperationList().get(0);

        // Measure the time elapsed
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            // Apply the two ops 10 times
            storage.apply(EXPENSE_REPORTS, groupByDept1);
            storage.apply(EXPENSE_REPORTS, groupByDept2);
        }
        long time = System.currentTimeMillis()-begin;

        // Check out the resulting stats
        DataSetStats stats = storage.stats(EXPENSE_REPORTS);
        System.out.println("Build time=" + stats.getBuildTime());
        System.out.println("Reuse hits=" + stats.getReuseHits());
        System.out.println("Size=" + SizeEstimator.formatSize(stats.sizeOf()));
        assertThat(stats.getReuseHits()).isEqualTo(20);

        // Data set group op is performed only two times, the rest are reuse hits
        DataSetOpStats groupStats = stats.getOpStats(DataSetOpType.GROUP);
        System.out.println("Group  (" + groupStats.toString() + ")");
        int groupOps = groupStats.getNumberOfOps();
        int groupHits = groupStats.getReuseHits();
        long groupTime = groupOps*groupStats.getAverageTime();
        assertThat(groupOps).isEqualTo(2);
        assertThat(groupHits).isEqualTo(18);
        assertThat(groupTime).isLessThanOrEqualTo(time);

    }
}
