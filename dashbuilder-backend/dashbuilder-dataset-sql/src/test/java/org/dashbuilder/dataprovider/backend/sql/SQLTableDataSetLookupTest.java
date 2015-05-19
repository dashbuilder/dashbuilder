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
package org.dashbuilder.dataprovider.backend.sql;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetFilterTest;
import org.dashbuilder.dataset.DataSetGroupTest;
import org.dashbuilder.dataset.DataSetNestedGroupTest;
import org.junit.Test;

import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.fest.assertions.api.Assertions.*;

public class SQLTableDataSetLookupTest extends SQLDataSetTestBase {

    @Test
    public void testDataSetTrim() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(DataSetGroupTest.EXPENSE_REPORTS)
                        .rowNumber(10)
                        .rowOffset(40)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(10);
        assertThat(result.getValueAt(0, 0)).isEqualTo(41d);
        assertThat(result.getValueAt(9, 0)).isEqualTo(50d);

        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(DataSetGroupTest.EXPENSE_REPORTS)
                        .group(DEPT.getName())
                        .column(DEPT.getName())
                        .column(AMOUNT.getName(), SUM)
                        .rowNumber(3)
                        .rowOffset(0)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(3);
        assertThat(result.getRowCountNonTrimmed()).isEqualTo(5);

        result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(DataSetGroupTest.EXPENSE_REPORTS)
                        .filter(CITY.getName(), equalsTo("Barcelona"))
                        .rowNumber(3)
                        .rowOffset(0)
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(3);
        assertThat(result.getRowCountNonTrimmed()).isEqualTo(6);
    }

    @Test
    public void testDataSetColumns() throws Exception {
        DataSet result = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset(DataSetGroupTest.EXPENSE_REPORTS)
                        .column(CITY.getName(), "City")
                        .column(DEPT.getName(), "Department")
                        .column(EMPLOYEE.getName(), "Employee")
                        .column(AMOUNT.getName(), "Amount")
                        .buildLookup());

        assertThat(result.getRowCount()).isEqualTo(50);
        assertThat(result.getColumnByIndex(0).getId()).isEqualTo("City");
        assertThat(result.getColumnByIndex(1).getId()).isEqualTo("Department");
        assertThat(result.getColumnByIndex(2).getId()).isEqualTo("Employee");
        assertThat(result.getColumnByIndex(3).getId()).isEqualTo("Amount");
        assertThat(result.getColumnByIndex(0).getColumnType()).isEqualTo(ColumnType.LABEL);
        assertThat(result.getColumnByIndex(1).getColumnType()).isEqualTo(ColumnType.LABEL);
        assertThat(result.getColumnByIndex(2).getColumnType()).isEqualTo(ColumnType.LABEL);
        assertThat(result.getColumnByIndex(3).getColumnType()).isEqualTo(ColumnType.NUMBER);
        assertThat(result.getValueAt(0, 0)).isEqualTo("Barcelona");
        assertThat(result.getValueAt(0, 1)).isEqualTo("Engineering");
        assertThat(result.getValueAt(0, 2)).isEqualTo("Roxie Foraker");
        assertThat(result.getValueAt(0, 3)).isEqualTo(120.35d);
    }

    @Test
    public void testDataSetGroup() throws Exception {
        DataSetGroupTest subTest = new DataSetGroupTest();
        subTest.dataSetManager = dataSetManager;
        subTest.dataSetFormatter = dataSetFormatter;
        subTest.testDataSetFunctions();
        subTest.testGroupByLabelDynamic();
        subTest.testGroupByYearDynamic();
        subTest.testGroupByMonthDynamic();
        subTest.testGroupByMonthDynamicNonEmpty();
        subTest.testGroupByDayDynamic();
        // TODO: Not supported by DB subTest.testGroupByWeek();
        subTest.testGroupByMonthReverse();
        subTest.testGroupByMonthFixed();
        subTest.testGroupByMonthFirstMonth();
        subTest.testGroupByMonthFirstMonthReverse();
        subTest.testGroupByQuarter();
    }

    @Test
    public void testDataSetNestedGroup() throws Exception {
        DataSetNestedGroupTest subTest = new DataSetNestedGroupTest();
        subTest.dataSetManager = dataSetManager;
        subTest.dataSetFormatter = dataSetFormatter;
        subTest.testGroupSelectionFilter();
        subTest.testNestedGroupFromMultipleSelection();
        subTest.testNestedGroupRequiresSelection();
        subTest.testThreeNestedLevels();
        subTest.testNoResultsSelection();
    }

    @Test
    public void testDataSetFilter() throws Exception {
        DataSetFilterTest subTest = new DataSetFilterTest();
        subTest.dataSetManager = dataSetManager;
        subTest.dataSetFormatter = dataSetFormatter;
        subTest.testFilterByString();
        subTest.testFilterByDate();
        subTest.testFilterByNumber();
        subTest.testFilterMultiple();
        subTest.testFilterUntilToday();
        subTest.testANDExpression();
        subTest.testNOTExpression();
        subTest.testORExpression();
        subTest.testCombinedExpression();
    }
}
