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

import static org.dashbuilder.dataset.Assertions.*;
import static org.fest.assertions.api.Assertions.*;

public class SQLDataSetDefTest extends SQLDataSetTestBase {

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

        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports_sql.dset");
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        DataSetMetadata metadata = dataSetManager.getDataSetMetadata("expense_reports_sql");
        assertThat(metadata.getNumberOfColumns()).isEqualTo(3);
        assertThat(metadata.getNumberOfRows()).isEqualTo(6);

        DataSet dataSet = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_sql")
                        .filter("amount", FilterFactory.lowerThan(1000))
                        .group("employee")
                        .column("employee")
                        .column("amount", AggregateFunctionType.SUM)
                        .sort("employee", SortOrder.ASCENDING)
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
        assertThat(metadata.getEstimatedSize()).isEqualTo(4300);

        DataSet dataSet = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_columnset")
                        .buildLookup());

        assertThat(dataSet.getColumns().size()).isEqualTo(4);
        assertThat(dataSet.getValueAt(0, 0)).isEqualTo("Engineering");
        assertThat(dataSet.getValueAt(0, 1)).isEqualTo("Roxie Foraker");
        assertThat(dataSet.getValueAt(0, 2)).isEqualTo(120.35d);
        assertThat(dataSet.getValueAt(0, 3).toString()).isEqualTo("2015-12-11 00:00:00.0");
    }

    @Test
    public void testFilters() throws Exception {

        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports_filtered.dset");
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        DataSetMetadata metadata = dataSetManager.getDataSetMetadata("expense_reports_filtered");
        assertThat(metadata.getNumberOfColumns()).isEqualTo(4);
        assertThat(metadata.getEstimatedSize()).isEqualTo(516);

        DataSet dataSet = dataSetManager.lookupDataSet(
                DataSetFactory.newDataSetLookupBuilder()
                        .dataset("expense_reports_filtered")
                        .buildLookup());

        //printDataSet(dataSet);
        assertDataSetValues(dataSet, dataSetFormatter, new String[][]{
                {"Engineering", "Roxie Foraker", "120.35", "12/11/15 00:00"},
                {"Engineering", "Roxie Foraker", "1,100.10", "12/01/15 00:00"},
                {"Engineering", "Roxie Foraker", "900.10", "11/01/15 00:00"},
                {"Services", "Jamie Gilbeau", "340.34", "10/12/15 00:00"},
                {"Services", "Jamie Gilbeau", "300.00", "09/15/15 00:00"},
                {"Services", "Jamie Gilbeau", "152.25", "08/17/15 00:00"}
        }, 0);
    }

    private void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
