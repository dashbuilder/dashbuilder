/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;

import org.dashbuilder.dataset.DataSetLookupFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.dashbuilder.dataset.ExpenseReportsData.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;

/**
 * TODO: Add suppport for void/empty arguments filter calls
 */
@Ignore
public class ElasticSearchEmptyArgumentsTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset";
    protected static final String EL_DATASET_UUID = "expense_reports";

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Register the data set definition for expense reports index.
        _registerDataSet(EL_EXAMPLE_DATASET_DEF);
    }

    /**
     * When a function does not receive an expected argument(s),
     * the function must be ruled out from the lookup call.
     *
     * See https://issues.jboss.org/browse/DASHBUILDE-90
     */
    @Test
    public void testEmptyArguments() throws Exception {
        assertEquals(dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(equalsTo(COLUMN_CITY, (Comparable) null))
                        .buildLookup()).getRowCount(), 0);

        assertEquals(dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(equalsTo(COLUMN_CITY, Arrays.asList()))
                        .buildLookup()).getRowCount(), 50);

        assertEquals(dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(notEqualsTo(COLUMN_CITY, null))
                        .buildLookup()).getRowCount(), 50);

        assertEquals(dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(between(COLUMN_AMOUNT, null, null))
                        .buildLookup()).getRowCount(), 50);

        assertEquals(dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(in(COLUMN_CITY, null))
                        .buildLookup()).getRowCount(), 50);

        assertEquals(dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(in(COLUMN_CITY, Arrays.asList()))
                        .buildLookup()).getRowCount(), 50);

        assertEquals(dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(notIn(COLUMN_CITY, Arrays.asList()))
                        .buildLookup()).getRowCount(), 50);
    }
}
