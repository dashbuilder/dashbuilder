/**
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
package org.dashbuilder.dataprovider.backend.sql;

import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.sort.SortOrder;
import org.junit.Test;

import static org.dashbuilder.dataset.ExpenseReportsData.*;
import static org.dashbuilder.dataset.Assertions.*;
import static org.fest.assertions.api.Assertions.*;

public class SQLDataSetDefTest extends SQLDataSetTestBase {

    @Override
    public void testAll() throws Exception {
        if (!testSettings.isMonetDB()) {
            testAllColumns();
        }
        testSQLDataSet();
        testColumnSet();
        testFilters();
    }

    @Test
    public void testAllColumns() throws Exception {

        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports_allcolumns.dset");
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        DataSetMetadata metadata = dataSetManager.getDataSetMetadata("expense_reports_allcolumns");
        assertThat(metadata.getNumberOfColumns()).isEqualTo(6);
        assertThat(metadata.getEstimatedSize()).isEqualTo(6350);
    }

    @Test
    public void testSQLDataSet() throws Exception {

        String testDsetFile = testSettings.getExpenseReportsSqlDsetFile();
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource(testDsetFile);
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        DataSetMetadata metadata = dataSetManager.getDataSetMetadata("expense_reports_sql");
        assertThat(metadata.getNumberOfColumns()).isEqualTo(3);
        assertThat(metadata.getNumberOfRows()).isEqualTo(6);

        DataSet dataSet = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_sql")
                        .filter(COLUMN_AMOUNT, FilterFactory.lowerThan(1000))
                        .group(COLUMN_EMPLOYEE)
                        .column(COLUMN_EMPLOYEE)
                        .column(COLUMN_AMOUNT, AggregateFunctionType.SUM)
                        .sort(COLUMN_EMPLOYEE, SortOrder.ASCENDING)
                        .buildLookup());

        //printDataSet(dataSet);
        assertDataSetValues(dataSet, dataSetFormatter, new String[][]{
                {"Jamie Gilbeau", "792.59"},
                {"Roxie Foraker", "1,020.45"}
        }, 0);
    }

    @Test
    public void testColumnSet() throws Exception {

        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports_columnset.dset");
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        DataSetMetadata metadata = dataSetManager.getDataSetMetadata("expense_reports_columnset");
        assertThat(metadata.getNumberOfColumns()).isEqualTo(4);
        if (!testSettings.isMonetDB()) {
            assertThat(metadata.getEstimatedSize()).isEqualTo(4300);
        }

        DataSet dataSet = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_columnset")
                        .buildLookup());

        assertThat(dataSet.getColumns().size()).isEqualTo(4);
        assertThat(dataSet.getValueAt(0, 0)).isEqualTo("Engineering");
        assertThat(dataSet.getValueAt(0, 1)).isEqualTo("Roxie Foraker");
        assertThat(dataSet.getValueAt(0, 2)).isEqualTo(120.35d);
        assertThat(dataSetFormatter.formatValueAt(dataSet, 0, 3)).isEqualTo("12/11/15 12:00");
    }

    @Test
    public void testFilters() throws Exception {

        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports_filtered.dset");
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        DataSetMetadata metadata = dataSetManager.getDataSetMetadata("expense_reports_filtered");
        assertThat(metadata.getNumberOfColumns()).isEqualTo(4);
        if (!testSettings.isMonetDB()) {
            assertThat(metadata.getEstimatedSize()).isEqualTo(516);
        }

        DataSet dataSet = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_filtered")
                        .group(COLUMN_DEPARTMENT)
                        .column(COLUMN_DEPARTMENT)
                        .column(COLUMN_EMPLOYEE)
                        .column(COLUMN_AMOUNT, AggregateFunctionType.SUM)
                        .sort(COLUMN_DEPARTMENT, SortOrder.DESCENDING)
                        .buildLookup());

        //printDataSet(dataSet);
        assertDataSetValues(dataSet, dataSetFormatter, new String[][]{
                {"Services", "Jamie Gilbeau", "792.59"},
                {"Engineering", "Roxie Foraker", "2,120.55"}
        }, 0);

        dataSet = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_filtered")
                        .filter(COLUMN_ID, FilterFactory.lowerThan(4))
                        .group(COLUMN_DEPARTMENT)
                        .column(COLUMN_DEPARTMENT)
                        .column(COLUMN_EMPLOYEE)
                        .column(COLUMN_AMOUNT, AggregateFunctionType.SUM)
                        .buildLookup());

        //printDataSet(dataSet);
        assertDataSetValues(dataSet, dataSetFormatter, new String[][]{
                {"Engineering", "Roxie Foraker", "2,120.55"}
        }, 0);
    }
}
