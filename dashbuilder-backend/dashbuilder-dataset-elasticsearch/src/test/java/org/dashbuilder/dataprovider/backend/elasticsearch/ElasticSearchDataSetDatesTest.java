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
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
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

    
    @Test
    public void testGroupByMonthFixed() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(MONTH, true)
                        .column(EL_EXAMPLE_COLUMN_DATE, EL_EXAMPLE_COLUMN_DATE)
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup());

        printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"1", "3.00", "2,324.20"},
                {"2", "6.00", "2,885.57"},
                {"3", "5.00", "2,413.45"},
                {"4", "3.00", "2,160.06"},
                {"5", "5.00", "2,503.34"},
                {"6", "9.00", "4,113.87"},
                {"7", "4.00", "2,354.04"},
                {"8", "2.00", "452.25"},
                {"9", "3.00", "693.35"},
                {"10", "3.00", "1,366.40"},
                {"11", "3.00", "1,443.75"},
                {"12", "4.00", "2,520.88"}
        }, 0);
    }

    @Test
    public void testGroupByMonthReverse() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(MONTH, true).desc()
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup());

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"12", "4.00", "2,520.88"},
                {"11", "3.00", "1,443.75"},
                {"10", "3.00", "1,366.40"},
                {"9", "3.00", "693.35"},
                {"8", "2.00", "452.25"},
                {"7", "4.00", "2,354.04"},
                {"6", "9.00", "4,113.87"},
                {"5", "5.00", "2,503.34"},
                {"4", "3.00", "2,160.06"},
                {"3", "5.00", "2,413.45"},
                {"2", "6.00", "2,885.57"},
                {"1", "3.00", "2,324.20"}
        }, 0);
    }

    @Test
    public void testGroupByMonthFirstMonth() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(MONTH, true).firstMonth(Month.NOVEMBER)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup());

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"11", "3.00", "1,443.75"},
                {"12", "4.00", "2,520.88"},
                {"1", "3.00", "2,324.20"},
                {"2", "6.00", "2,885.57"},
                {"3", "5.00", "2,413.45"},
                {"4", "3.00", "2,160.06"},
                {"5", "5.00", "2,503.34"},
                {"6", "9.00", "4,113.87"},
                {"7", "4.00", "2,354.04"},
                {"8", "2.00", "452.25"},
                {"9", "3.00", "693.35"},
                {"10", "3.00", "1,366.40"}
        }, 0);
    }

    @Test
    public void testGroupByMonthFirstMonthReverse() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(MONTH, true).desc().firstMonth(Month.MARCH)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup());

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"3", "5.00", "2,413.45"},
                {"2", "6.00", "2,885.57"},
                {"1", "3.00", "2,324.20"},
                {"12", "4.00", "2,520.88"},
                {"11", "3.00", "1,443.75"},
                {"10", "3.00", "1,366.40"},
                {"9", "3.00", "693.35"},
                {"8", "2.00", "452.25"},
                {"7", "4.00", "2,354.04"},
                {"6", "9.00", "4,113.87"},
                {"5", "5.00", "2,503.34"},
                {"4", "3.00", "2,160.06"}
        }, 0);
    }

    @Test
    public void testGroupByWeekFirstDayMonday() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(DAY_OF_WEEK, true).firstDay(DayOfWeek.MONDAY)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup().cloneInstance());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"2", "6.00", "2,278.07"},
                {"3", "7.00", "3,932.06"},
                {"4", "7.00", "2,965.08"},
                {"5", "5.00", "2,759.12"},
                {"6", "12.00", "5,170.74"},
                {"7", "6.00", "3,880.54"},
                {"1", "7.00", "4,245.55"}
        }, 0);
    }

    @Test
    public void testGroupByWeekFirstDayMondayDesc() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(DAY_OF_WEEK, true).firstDay(DayOfWeek.MONDAY).desc()
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup().cloneInstance());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"2", "6.00", "2,278.07"},
                {"1", "7.00", "4,245.55"},
                {"7", "6.00", "3,880.54"},
                {"6", "12.00", "5,170.74"},
                {"5", "5.00", "2,759.12"},
                {"4", "7.00", "2,965.08"},
                {"3", "7.00", "3,932.06"}
        }, 0);
    }

    @Test
    public void testGroupByWeekFirstDaySunday() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(DAY_OF_WEEK, true).firstDay(DayOfWeek.SUNDAY)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup().cloneInstance());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"1", "7.00", "4,245.55"},
                {"2", "6.00", "2,278.07"},
                {"3", "7.00", "3,932.06"},
                {"4", "7.00", "2,965.08"},
                {"5", "5.00", "2,759.12"},
                {"6", "12.00", "5,170.74"},
                {"7", "6.00", "3,880.54"}
        }, 0);
    }

    @Test
    public void testGroupByQuarter() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EL_DATASET_UUID)
                        .group(EL_EXAMPLE_COLUMN_DATE).fixed(QUARTER, true)
                        .column(EL_EXAMPLE_COLUMN_DATE, "Period")
                        .column(COUNT, "Occurrences")
                        .column(EL_EXAMPLE_COLUMN_AMOUNT, SUM, "totalAmount")
                        .buildLookup().cloneInstance());

        printDataSet(result);
        
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"1", "14.00", "7,623.22"},
                {"2", "17.00", "8,777.27"},
                {"3", "9.00", "3,499.64"},
                {"4", "10.00", "5,331.03"}
        }, 0);
    }
}
