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

import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.Assertions.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.fest.assertions.api.Assertions.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

@RunWith(Arquillian.class)
public class DataSetGroupTest {

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
    }

    @Test
    public void testDataSetFunctions() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .column(COUNT, "#items")
                .column("amount", MIN)
                .column("amount", MAX)
                .column("amount", AVERAGE)
                .column("amount", SUM)
                .column("city", DISTINCT)
                .buildLookup());

        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"50.00", "1.10", "1,100.10", "454.63", "22,731.26", "6.00"}
        }, 0);
    }

    @Test
    public void testGroupByLabelDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("department")
                .column("department", "Department")
                .column(COUNT, "Occurrences")
                .column("amount", MIN, "min")
                .column("amount", MAX, "max")
                .column("amount", AVERAGE, "average")
                .column("amount", SUM, "total")
                .sort("department", SortOrder.ASCENDING)
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Engineering", "19.00", "1.10", "1,100.10", "402.64", "7,650.16"},
                {"Management", "11.00", "43.03", "992.20", "547.04", "6,017.47"},
                {"Sales", "8.00", "75.75", "995.30", "401.69", "3,213.53"},
                {"Services", "5.00", "152.25", "911.11", "500.90", "2,504.50"},
                {"Support", "7.00", "300.01", "1,001.90", "477.94", "3,345.60"}
        }, 0);
    }

    @Test
    public void testGroupByYearDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").dynamic(YEAR, true)
                .column("date", "Period")
                .column(COUNT, "Occurrences")
                .column("amount", SUM, "totalAmount")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"2012", "13.00", "6,126.13"},
                {"2013", "11.00", "5,252.96"},
                {"2014", "11.00", "4,015.48"},
                {"2015", "15.00", "7,336.69"}
        }, 0);
    }

    @Test
    public void testGroupByMonthDynamic() throws Exception {
        DataSet result = lookupGroupByMonthDynamic(true);

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(48);
        assertThat(result.getValueAt(0, 0)).isEqualTo("2012-01");
    }

    @Test
    public void testGroupByMonthDynamicNonEmpty() throws Exception {
        DataSet result = lookupGroupByMonthDynamic(false);

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(37);
        assertThat(result.getValueAt(0, 0)).isEqualTo("2012-01");
    }

    public DataSet lookupGroupByMonthDynamic(boolean emptyIntervals) throws Exception {
        return dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(EXPENSE_REPORTS)
                        .group("date").dynamic(99, MONTH, emptyIntervals)
                        .column("date", "Period")
                        .column("employee", "Employee")
                        .column(COUNT, "Occurrences")
                        .column("amount", SUM, "totalAmount")
                        .buildLookup());
    }


    @Test
    public void testGroupByDayDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").dynamic(9999, DAY_OF_WEEK, true)
                .column("date", "Period")
                .column(COUNT, "Occurrences")
                .column("amount", SUM, "totalAmount")
                .buildLookup());

        //printDataSet(result);
        assertThat(result.getRowCount()).isEqualTo(1438);
        assertThat(result.getValueAt(0, 0)).isEqualTo("2012-01-04");
    }

    @Test
    public void testGroupByMonthFixed() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").fixed(MONTH, true)
                .column("date", "Period")
                .column(COUNT, "Occurrences")
                .column("amount", SUM, "totalAmount")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"1", "3.00", "2,324.20"},
                {"2", "6.00", "2,885.57"},
                {"3", "5.00", "1,012.55"},
                {"4", "3.00", "1,061.06"},
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
    public void testGroupByMonthFirstMonth() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").fixed(MONTH, true).firstMonth(Month.NOVEMBER)
                .column("date", "Period")
                .column(COUNT, "Occurrences")
                .column("amount", SUM, "totalAmount")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"11", "3.00", "1,443.75"},
                {"12", "4.00", "2,520.88"},
                {"1", "3.00", "2,324.20"},
                {"2", "6.00", "2,885.57"},
                {"3", "5.00", "1,012.55"},
                {"4", "3.00", "1,061.06"},
                {"5", "5.00", "2,503.34"},
                {"6", "9.00", "4,113.87"},
                {"7", "4.00", "2,354.04"},
                {"8", "2.00", "452.25"},
                {"9", "3.00", "693.35"},
                {"10", "3.00", "1,366.40"}
        }, 0);
    }

    @Test
    public void testGroupByMonthReverse() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").fixed(MONTH, true).desc()
                .column("date", "Period")
                .column(COUNT, "Occurrences")
                .column("amount", SUM, "totalAmount")
                .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"12", "4.00", "2,520.88"},
                {"11", "3.00", "1,443.75"},
                {"10", "3.00", "1,366.40"},
                {"9", "3.00", "693.35"},
                {"8", "2.00", "452.25"},
                {"7", "4.00", "2,354.04"},
                {"6", "9.00", "4,113.87"},
                {"5", "5.00", "2,503.34"},
                {"4", "3.00", "1,061.06"},
                {"3", "5.00", "1,012.55"},
                {"2", "6.00", "2,885.57"},
                {"1", "3.00", "2,324.20"}
        }, 0);
    }

    @Test
    public void testGroupByMonthFirstMonthReverse() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
            DataSetFactory.newDataSetLookupBuilder()
            .dataset(EXPENSE_REPORTS)
            .group("date").fixed(MONTH, true).desc().firstMonth(Month.MARCH)
            .column("date", "Period")
            .column(COUNT, "Occurrences")
            .column("amount", SUM, "totalAmount")
            .buildLookup());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"3", "5.00", "1,012.55"},
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
                {"4", "3.00", "1,061.06"}
        }, 0);
    }

    @Test
    public void testFixedIntervalsSupported() throws Exception {
        for (DateIntervalType type : DateIntervalType.values()) {
            try {
                DataSetFactory.newDataSetLookupBuilder().group("date").fixed(type, true);
                if (!DateIntervalType.FIXED_INTERVALS_SUPPORTED.contains(type)) {
                    fail("Missing exception on a not supported fixed interval: " + type);
                }
            } catch (Exception e) {
                if (DateIntervalType.FIXED_INTERVALS_SUPPORTED.contains(type)) {
                    fail("Exception on a supported fixed interval: " + type);
                }
            }
        }
    }

    @Test
    public void testFirstDayOfWeekOk() throws Exception {
        DataSetFactory.newDataSetLookupBuilder()
            .group("date")
            .fixed(DAY_OF_WEEK, true)
            .firstDay(DayOfWeek.MONDAY);
    }

    @Test
    public void testFirstDayOfWeekNok() throws Exception {
        try {
            DataSetFactory.newDataSetLookupBuilder()
                .group("date")
                .fixed(QUARTER, true)
                .firstDay(DayOfWeek.MONDAY);
            fail("firstDayOfWeek required a DAY_OF_WEEK fixed domain.");
        } catch (Exception e) {
            // Expected.
        }
    }

    @Test
    public void testFirstDayOfMonthOk() throws Exception {
        DataSetFactory.newDataSetLookupBuilder()
            .group("date")
            .fixed(MONTH, true)
            .firstMonth(Month.APRIL);
    }

    @Test
    public void testFirstDayOfMonthNok() throws Exception {
        try {
            DataSetFactory.newDataSetLookupBuilder()
                .group("date")
                .fixed(QUARTER, true)
                .firstMonth(Month.APRIL);
            fail("firstDayOfWeek required a DAY_OF_WEEK fixed domain.");
        } catch (Exception e) {
            // Expected.
        }
    }

    @Test
    public void testGroupByWeek() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").fixed(DAY_OF_WEEK, true).firstDay(DayOfWeek.MONDAY)
                .column("date", "Period")
                .column(COUNT, "Occurrences")
                .column("amount", SUM, "totalAmount")
                .buildLookup().cloneInstance());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"2", "10.00", "3,904.17"},
                {"3", "8.00", "4,525.69"},
                {"4", "7.00", "4,303.14"},
                {"5", "4.00", "1,021.95"},
                {"6", "8.00", "3,099.08"},
                {"7", "5.00", "2,012.05"},
                {"1", "8.00", "3,865.18"}
        }, 0);
    }

    @Test
    public void testGroupByQuarter() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                .dataset(EXPENSE_REPORTS)
                .group("date").fixed(QUARTER, true)
                .column("date", "Period")
                .column(COUNT, "Occurrences")
                .column("amount", SUM, "totalAmount")
                .buildLookup().cloneInstance());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"1", "14.00", "6,222.32"},
                {"2", "17.00", "7,678.27"},
                {"3", "9.00", "3,499.64"},
                {"4", "10.00", "5,331.03"}
        }, 0);
    }

    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
