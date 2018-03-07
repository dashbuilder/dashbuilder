/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataprovider.sql;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetGroupTest;
import org.dashbuilder.dataset.DataSetLookupFactory;
import org.dashbuilder.dataset.DataSetTrimTest;
import org.junit.Test;

import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_AMOUNT;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_CITY;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_DATE;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_DEPARTMENT;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_EMPLOYEE;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_ID;
import static org.fest.assertions.api.Assertions.assertThat;

public class SQLDataSetTrimTest extends SQLDataSetTestBase {

    @Override
    public void testAll() throws Exception {
        testTrim();
    }

    @Test
    public void testTrim() throws Exception {
        DataSetTrimTest subTest = new DataSetTrimTest();
        subTest.testTrim();
        subTest.testDuplicatedColumns();
    }

    @Test
    public void testTotalRowCountNonTrimmedFillingGroupBy() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(DataSetGroupTest.EXPENSE_REPORTS)
                        .group(COLUMN_DEPARTMENT)
                        .column(COLUMN_ID)
                        .column(COLUMN_CITY)
                        .column(COLUMN_DEPARTMENT)
                        .column(COLUMN_EMPLOYEE)
                        .column(COLUMN_DATE)
                        .column(COLUMN_AMOUNT)
                        .rowNumber(10)
                        .rowOffset(0)
                        .buildLookup());
        assertThat(result.getRowCount()).isEqualTo(5);
        assertThat(result.getRowCountNonTrimmed()).isEqualTo(5);

        result = dataSetManager.lookupDataSet(
                DataSetLookupFactory.newDataSetLookupBuilder()
                        .dataset(DataSetGroupTest.EXPENSE_REPORTS)
                        .group(COLUMN_DEPARTMENT)
                        .column(COLUMN_ID)
                        .column(COLUMN_CITY)
                        .column(COLUMN_DEPARTMENT)
                        .column(COLUMN_EMPLOYEE)
                        .column(COLUMN_DATE)
                        .column(COLUMN_AMOUNT)
                        .rowNumber(3)
                        .rowOffset(0)
                        .buildLookup());
        assertThat(result.getRowCount()).isEqualTo(3);
        assertThat(result.getRowCountNonTrimmed()).isEqualTo(5);
    }
}
