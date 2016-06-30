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

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.date.Month;
import org.junit.Before;
import org.junit.Test;

import static org.dashbuilder.dataset.Assertions.assertDataSetValues;
import static org.dashbuilder.dataset.group.AggregateFunctionType.COUNT;
import static org.dashbuilder.dataset.group.AggregateFunctionType.SUM;
import static org.dashbuilder.dataset.group.DateIntervalType.MONTH;

/**
 * @since 0.4.0
 */
public class ElasticSearchEmptyIntervalsTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_EMPTYINTERVALS_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/emptyIntervals.dset";
    protected static final String EL_DATASET_EMPTYINTERVALS_UUID = "emptyIntervals";

    /**
     * Register the dataset used for this test case. 
     */
    @Before
    public void registerDataSet() throws Exception {
        super.setUp();

        // Register the data set.
        _registerDataSet(EL_EXAMPLE_EMPTYINTERVALS_DEF);
    }

    @Test
    public void testNonEmptyIntervals() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_EMPTYINTERVALS_UUID)
                        .group("DATE").fixed(MONTH, false).firstMonth(Month.JANUARY)
                        .column("DATE", "Period")
                        .column(COUNT, "Occurrences")
                        .column("NUMBER", SUM, "total")
                        .buildLookup());

        // printDataSet(result);

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"1", "1.00", "1.00"},
                {"2", "1.00", "1.00"},
                {"3", "2.00", "2.00"},
                {"4", "1.00", "1.00"},
                {"5", "1.00", "1.00"},
                {"6", "1.00", "1.00"},
                {"7", "1.00", "1.00"},
                {"8", "1.00", "1.00"},
                {"10", "1.00", "1.00"},
                {"11", "1.00", "1.00"}
        }, 0);
    }

    @Test
    public void testEmptyIntervals() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_EMPTYINTERVALS_UUID)
                        .group("DATE").fixed(MONTH, true).firstMonth(Month.JANUARY)
                        .column("DATE", "Period")
                        .column(COUNT, "Occurrences")
                        .column("NUMBER", SUM, "total")
                        .buildLookup());

        // printDataSet(result);

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"1", "1.00", "1.00"},
                {"2", "1.00", "1.00"},
                {"3", "2.00", "2.00"},
                {"4", "1.00", "1.00"},
                {"5", "1.00", "1.00"},
                {"6", "1.00", "1.00"},
                {"7", "1.00", "1.00"},
                {"8", "1.00", "1.00"},
                {"9", "0.00", "0.00"},
                {"10", "1.00", "1.00"},
                {"11", "1.00", "1.00"},
                {"12", "0.00", "0.00"}
        }, 0);
    }

    @Test
    public void testEmptyIntervalsUsingFirstMonth() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_EMPTYINTERVALS_UUID)
                        .group("DATE").fixed(MONTH, true).firstMonth(Month.MARCH)
                        .column("DATE", "Period")
                        .column(COUNT, "Occurrences")
                        .column("NUMBER", SUM, "total")
                        .buildLookup());

        // printDataSet(result);

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"3", "2.00", "2.00"},
                {"4", "1.00", "1.00"},
                {"5", "1.00", "1.00"},
                {"6", "1.00", "1.00"},
                {"7", "1.00", "1.00"},
                {"8", "1.00", "1.00"},
                {"9", "0.00", "0.00"},
                {"10", "1.00", "1.00"},
                {"11", "1.00", "1.00"},
                {"12", "0.00", "0.00"},
                {"1", "1.00", "1.00"},
                {"2", "1.00", "1.00"}
        }, 0);
    }
    
}
