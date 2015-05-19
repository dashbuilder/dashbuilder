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
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFormatter;
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
import org.junit.runner.RunWith;

import static org.jooq.impl.DSL.*;

@RunWith(Arquillian.class)
public class SQLDataSetTestBase {

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

    String expenseReportsDsetFile = "expenseReports.dset";

    public static final String CREATE_TABLE = "CREATE TABLE expense_reports (\n" +
            "  id INTEGER NOT NULL,\n" +
            "  city VARCHAR(50),\n" +
            "  department VARCHAR(50),\n" +
            "  employee VARCHAR(50),\n" +
            "  date TIMESTAMP,\n" +
            "  amount NUMERIC(28,2),\n" +
            "  PRIMARY KEY(id)\n" +
            ")";

    public static final String DROP_TABLE = "DROP TABLE expense_reports";

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

        // H2 in-memory
        DataSourceLocatorMock.TYPE = "h2";
        DataSourceLocatorMock.URL = "jdbc:h2:mem:test;DATABASE_TO_UPPER=FALSE";

        // H2 local
        //DataSourceLocatorMock.TYPE = "h2";
        //DataSourceLocatorMock.URL = "jdbc:h2:~/test;DATABASE_TO_UPPER=FALSE";

        // MySQL
        //DataSourceLocatorMock.TYPE = "mysql";
        //DataSourceLocatorMock.URL = "jdbc:mysql://localhost:3306/dashbuilder";
        //DataSourceLocatorMock.USER = "root";

        // Postgres
        //DataSourceLocatorMock.TYPE = "postgres";
        //DataSourceLocatorMock.SERVER = "localhost";
        //DataSourceLocatorMock.DB = "dashbuilder";
        //DataSourceLocatorMock.PORT = 5432;
        //DataSourceLocatorMock.USER = "dashbuilder";
        //DataSourceLocatorMock.PASSWORD = "dashbuilder";

        // Register the SQL data set
        URL fileURL = Thread.currentThread().getContextClassLoader().getResource(expenseReportsDsetFile);
        String json = IOUtils.toString(fileURL);
        SQLDataSetDef def = (SQLDataSetDef) jsonMarshaller.fromJson(json);
        dataSetDefRegistry.registerDataSetDef(def);

        // Get a data source connection
        DataSource dataSource = dataSourceLocator.lookup(def);
        conn = dataSource.getConnection();

        // Create the expense reports table
        using(conn).execute(CREATE_TABLE);

        // Populate the table
        populateDbTable();
    }

    @After
    public void tearDown() throws Exception {
        // Drop the expense reports table
        using(conn).execute(DROP_TABLE);

        conn.close();
    }

    protected void populateDbTable() throws Exception {
        int rowCount = using(conn).fetchCount(EXPENSES);

        DataSet dataSet = RawDataSetSamples.EXPENSE_REPORTS.toDataSet();
        for (int i = 0; i < dataSet.getRowCount(); i++) {
            int id = ((Number) dataSet.getValueAt(i, 0)).intValue();
            using(conn).insertInto(EXPENSES)
                    .set(ID,  rowCount + id)
                    .set(CITY, dataSet.getValueAt(i, 1))
                    .set(DEPT, dataSet.getValueAt(i, 2))
                    .set(EMPLOYEE, dataSet.getValueAt(i, 3))
                    .set(DATE, dataSet.getValueAt(i, 4))
                    .set(AMOUNT, dataSet.getValueAt(i, 5))
                    .execute();
        }
    }
}
