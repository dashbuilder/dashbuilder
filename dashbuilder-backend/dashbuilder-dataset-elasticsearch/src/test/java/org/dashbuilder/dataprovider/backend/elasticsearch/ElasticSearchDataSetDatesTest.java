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
package org.dashbuilder.dataprovider.backend.elasticsearch;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.sort.SortOrder;
import org.junit.Before;
import org.junit.Test;

import static org.dashbuilder.dataset.Assertions.assertDataSetValues;
import static org.dashbuilder.dataset.filter.FilterFactory.timeFrame;
import static org.dashbuilder.dataset.group.AggregateFunctionType.COUNT;
import static org.dashbuilder.dataset.group.AggregateFunctionType.SUM;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * <p>Data test for date types for ElasticSearchDataSet. It tests grouping and filtering with date related fields.</p>
 * <p>It uses as source dataset: <code>org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset</code></p>
 *
 * @since 0.3.0
 */
public class ElasticSearchDataSetDatesTest extends ElasticSearchDataSetTestBase {

    protected static final String EL_EXAMPLE_DATASET_DEF = "org/dashbuilder/dataprovider/backend/elasticsearch/expensereports.dset";
    protected static final String EL_DATASET_UUID = "expense_reports";

    /**
     * Register the dataset used for this test case. 
     */
    @Before
    public void registerDataSet() throws Exception {
        // Register the data set.
        _registerDataSet(EL_EXAMPLE_DATASET_DEF);
    }

    @Test
    public void testGroupByYearDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).dynamic(YEAR, true)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"2009", "13.00", "6,126.13"},
                {"2010", "11.00", "5,252.96"},
                {"2011", "11.00", "6,515.38"},
                {"2012", "15.00", "7,336.69"}
        }, 0);
    }


    @Test
    public void testGroupByMonthDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).dynamic(99, MONTH, true)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(48);
        assertThat(result.getValueAt(0, 0)).isEqualTo("2009-01");
        assertThat(result.getValueAt(0, 1)).isEqualTo(1d);
        assertThat(result.getValueAt(0, 2)).isEqualTo(921.9000244140625d);
        assertThat(result.getValueAt(1, 0)).isEqualTo("2009-02");
        assertThat(result.getValueAt(1, 1)).isEqualTo(1.0d);
        assertThat(result.getValueAt(1, 2)).isEqualTo(700.6599731445312d);
        assertThat(result.getValueAt(47, 0)).isEqualTo("2012-12");
        assertThat(result.getValueAt(47, 1)).isEqualTo(2.0d);
        assertThat(result.getValueAt(47, 2)).isEqualTo(1220.4499740600586d);
    }

    @Test
    public void testGroupByDayDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).dynamic(9999, DAY, true)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .sort(EL_EXAMPLE_COLUMN_ID, SortOrder.ASCENDING)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(1438);
        assertThat(result.getValueAt(0, 0)).isEqualTo("2009-01-04");
    }

    // Time frame function.
    @Test
    public void testTimeFrameFunction() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .filter(EL_EXAMPLE_COLUMN_DATE, timeFrame("10second"))
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(0);
    }
}
