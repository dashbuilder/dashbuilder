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
import java.sql.Connection;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetFilterTest;
import org.dashbuilder.dataset.DataSetFormatter;
import org.dashbuilder.dataset.DataSetGroupTest;
import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.RawDataSetSamples;
import org.dashbuilder.dataset.backend.DataSetDefJSONMarshaller;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.test.ShrinkWrapHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dashbuilder.dataset.Assertions.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.fest.assertions.api.Assertions.*;
import static org.jooq.impl.DSL.*;

@RunWith(Arquillian.class)
public class SQLDataSetGroupTest {

    @Deployment
    public static Archive<?> createTestArchive()  {
        return ShrinkWrapHelper.createJavaArchive()
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    DataSetManager dataSetManager;

    @Inject
    DataSetFormatter dataSetFormatter;

    @Inject
    DataSetDefRegistry dataSetDefRegistry;

    @Inject
    DataSetDefJSONMarshaller jsonMarshaller;

    @Inject
    SQLDataSourceLocator dataSourceLocator;

    public static final String CREATE_TABLE = "CREATE TABLE expense_reports (\n" +
            "  id INTEGER NOT NULL,\n" +
            "  city VARCHAR(50),\n" +
            "  department VARCHAR(50),\n" +
            "  employee VARCHAR(50),\n" +
            "  date TIMESTAMP,\n" +
            "  amount NUMERIC(28,2),\n" +
            "  PRIMARY KEY(id)\n" +
            ")";

    Connection conn;
    Table EXPENSES = table("expense_reports");
    Field ID = field("id", SQLDataType.INTEGER);
    Field CITY = field("city", SQLDataType.VARCHAR.length(50));
    Field DEPT = field("department", SQLDataType.VARCHAR.length(50));
    Field EMPLOYEE = field("employee", SQLDataType.VARCHAR.length(50));
    Field DATE = field("date", SQLDataType.DATE);
    Field AMOUNT = field("amount", SQLDataType.FLOAT);

    @Before
    public void setUp() throws Exception {

        // Register the SQL based data set
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource("expenseReports.dset");
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        // Get a data source connection
        DataSource dataSource = dataSourceLocator.lookup(def);
        conn = dataSource.getConnection();

        // Create the expense reports table
        using(conn).execute(CREATE_TABLE);

        // Populate the table
        DataSet dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        for (int i = 0; i < dataSet.getRowCount(); i++) {
            using(conn).insertInto(EXPENSES)
                    .set(ID, dataSet.getValueAt(i, 0))
                    .set(CITY, dataSet.getValueAt(i, 1))
                    .set(DEPT, dataSet.getValueAt(i, 2))
                    .set(EMPLOYEE, dataSet.getValueAt(i, 3))
                    .set(DATE, dataSet.getValueAt(i, 4))
                    .set(AMOUNT, dataSet.getValueAt(i, 5))
                    .execute();
        }
    }

    @After
    public void tearDown() throws Exception {
        conn.close();
    }

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
                        .filter(CITY.getName(), isEqualsTo("Barcelona"))
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
        assertThat(result.getColumnByIndex(0).getName()).isEqualTo("City");
        assertThat(result.getColumnByIndex(1).getName()).isEqualTo("Department");
        assertThat(result.getColumnByIndex(2).getName()).isEqualTo("Employee");
        assertThat(result.getColumnByIndex(3).getName()).isEqualTo("Amount");
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
    }
}
