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
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.model.dataset.group.DomainStrategy;
import org.dashbuilder.model.date.DayOfWeek;
import org.dashbuilder.model.date.Month;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.Assertions.*;
import static org.dashbuilder.model.dataset.group.ScalarFunctionType.*;
import static org.dashbuilder.model.dataset.group.DateIntervalType.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(Arquillian.class)
public class DataSetGroupTest {

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
    public void testGroupByLabelDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .domain("department", "Department")
                .range("id", "Occurrences", COUNT)
                .range("amount", "totalAmount", SUM)
                .build());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][] {
                {"Engineering", "19.00", "7,650.16"},
                {"Services", "5.00", "2,504.50"},
                {"Sales", "8.00", "3,213.53"},
                {"Support", "7.00", "3,345.60"},
                {"Management", "11.00", "6,017.47"}
        }, 0);
    }

    @Test
    public void testGroupByDateDynamic() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .domain("date", "Period", DomainStrategy.DYNAMIC, 10, "year")
                .range("id", "Occurrences", COUNT)
                .range("amount", "totalAmount", SUM)
                .build());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"2012", "13.00", "6,126.13"},
                {"2013", "11.00", "5,252.96"},
                {"2014", "11.00", "4,015.48"},
                {"2015", "15.00", "7,336.69"}
        }, 0);
    }

    @Test
    public void testGroupByYear() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .domain("date", "Period").fixed(MONTH, true)
                .range("id", "Occurrences", COUNT)
                .range("amount", "totalAmount", SUM)
                .build());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"JANUARY", "3.00", "2,324.20"},
                {"FEBRUARY", "6.00", "2,885.57"},
                {"MARCH", "5.00", "1,012.55"},
                {"APRIL", "3.00", "1,061.06"},
                {"MAY", "5.00", "2,503.34"},
                {"JUNE", "9.00", "4,113.87"},
                {"JULY", "4.00", "2,354.04"},
                {"AUGUST", "2.00", "452.25"},
                {"SEPTEMBER", "3.00", "693.35"},
                {"OCTOBER", "3.00", "1,366.40"},
                {"NOVEMBER", "3.00", "1,443.75"},
                {"DECEMBER", "4.00", "2,520.88"}
        }, 0);
    }

    @Test
    public void testGroupByYearReverse() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .domain("date", "Period").fixed(MONTH, false)
                .range("id", "Occurrences", COUNT)
                .range("amount", "totalAmount", SUM)
                .build());

        //printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"DECEMBER", "4.00", "2,520.88"},
                {"NOVEMBER", "3.00", "1,443.75"},
                {"OCTOBER", "3.00", "1,366.40"},
                {"SEPTEMBER", "3.00", "693.35"},
                {"AUGUST", "2.00", "452.25"},
                {"JULY", "4.00", "2,354.04"},
                {"JUNE", "9.00", "4,113.87"},
                {"MAY", "5.00", "2,503.34"},
                {"APRIL", "3.00", "1,061.06"},
                {"MARCH", "5.00", "1,012.55"},
                {"FEBRUARY", "6.00", "2,885.57"},
                {"JANUARY", "3.00", "2,324.20"}
        }, 0);
    }

    @Test
    public void testFirstDayOfWeekOk() throws Exception {
        new DataSetLookupBuilder()
                .domain("date").fixed(DAY_OF_WEEK).firstDayOfWeek(DayOfWeek.MONDAY);
    }

    @Test
    public void testFirstDayOfWeekNok() throws Exception {
        try {
            new DataSetLookupBuilder()
                    .domain("date").fixed(QUARTER).firstDayOfWeek(DayOfWeek.MONDAY);
            fail("firstDayOfWeek required a DAY_OF_WEEK fixed domain.");
        } catch (Exception e) {
            // Expected.
        }
    }

    @Test
    public void testFirstDayOfMonthOk() throws Exception {
        new DataSetLookupBuilder()
                .domain("date").fixed(MONTH).firstMonthOfYear(Month.APRIL);
    }

    @Test
    public void testFirstDayOfMonthNok() throws Exception {
        try {
            new DataSetLookupBuilder()
                    .domain("date").fixed(QUARTER).firstMonthOfYear(Month.APRIL);
            fail("firstDayOfWeek required a DAY_OF_WEEK fixed domain.");
        } catch (Exception e) {
            // Expected.
        }
    }

    @Test
    public void testGroupByWeek() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .domain("date", "Period").fixed(DAY_OF_WEEK).firstDayOfWeek(DayOfWeek.MONDAY)
                .range("id", "Occurrences", COUNT)
                .range("amount", "totalAmount", SUM)
                .build());

        ////printDataSet(result);
        assertDataSetValues(result, dataSetFormatter, new String[][]{
                {"MONDAY", "10.00", "3,904.17"},
                {"TUESDAY", "8.00", "4,525.69"},
                {"WEDNESDAY", "7.00", "4,303.14"},
                {"THURSDAY", "4.00", "1,021.95"},
                {"FRIDAY", "8.00", "3,099.08"},
                {"SATURDAY", "5.00", "2,012.05"},
                {"SUNDAY", "8.00", "3,865.18"}
        }, 0);
    }

    @Test
    public void testGroupByQuarter() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(new DataSetLookupBuilder()
                .uuid(EXPENSE_REPORTS)
                .domain("date", "Period").fixed(QUARTER)
                .range("id", "Occurrences", COUNT)
                .range("amount", "totalAmount", SUM)
                .build());

        printDataSet(result);
        /*assertDataSetValues(result, dataSetFormatter, new String[][]{
        }, 0);*/
    }

    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
