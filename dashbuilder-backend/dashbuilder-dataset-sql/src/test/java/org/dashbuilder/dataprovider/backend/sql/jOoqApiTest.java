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

import java.math.BigDecimal;
import java.sql.Connection;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.RawDataSetSamples;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.*;
import static org.jooq.impl.DSL.*;

public class jOoqApiTest {

    Connection conn;
    Table EXPENSES = table("expense_reports");
    Field ID = field("ID", SQLDataType.INTEGER);
    Field CITY = field("CITY", SQLDataType.VARCHAR.length(50));
    Field DEPT = field("DEPARTMENT", SQLDataType.VARCHAR.length(50));
    Field EMPLOYEE = field("EMPLOYEE", SQLDataType.VARCHAR.length(50));
    Field DATE = field("DATE", SQLDataType.DATE);
    Field AMOUNT = field("AMOUNT", SQLDataType.FLOAT);

    public static final String CREATE_TABLE = "CREATE TABLE expense_reports (\n" +
            "  id INTEGER NOT NULL,\n" +
            "  city VARCHAR(50),\n" +
            "  department VARCHAR(50),\n" +
            "  employee VARCHAR(50),\n" +
            "  date TIMESTAMP,\n" +
            "  amount NUMERIC(28,2),\n" +
            "  PRIMARY KEY(id)\n" +
            ")";

    @Before
    public void setUp() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        conn = ds.getConnection();

        // Create the table
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
    public void testDefaultSchema() throws Exception {
        ResultQuery ctx = DSL.using(conn)
                .select(fieldByName("dashbuilder", ID.getName()))
                .from(tableByName("dashbuilder", EXPENSES.getName()));

        String sql = ctx.getSQL();
        assertThat(sql).isEqualTo("select \"dashbuilder\".\"ID\" from \"dashbuilder\".\"table\"");
    }

    @Test
    public void testSelectColumn() throws Exception {
        Result result= DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(ID)
                .from(EXPENSES)
                .fetch();
        assertThat(result.getValue(0, ID)).isEqualTo(new Integer(1));
        assertThat(result.getValue(49, ID)).isEqualTo(new Integer(50));
    }

    @Test
    public void testNestedSelect() throws Exception {
        Result result= DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(ID)
                .from("(select * from " + EXPENSES + ")")
                .fetch();
        assertThat(result.getValue(0, ID)).isEqualTo(new Integer(1));
        assertThat(result.getValue(49, ID)).isEqualTo(new Integer(50));
    }

    @Test
    public void testSelectAllColumns() throws Exception {
        Result result= DSL.using(conn, new Settings().withRenderFormatted(true))
                .selectFrom(EXPENSES)
                .fetch();
        assertThat(result.getValue(0, ID)).isEqualTo(new Integer(1));
        assertThat(result.getValue(49, ID)).isEqualTo(new Integer(50));
    }

    @Test
    public void testGroupMultiple() throws Exception {
        ResultQuery ctx = DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(DEPT, EMPLOYEE, DEPT.count(), AMOUNT.sum())
                .from(EXPENSES)
                .groupBy(DEPT, EMPLOYEE);

        String sql = ctx.getSQL();
        Result result= ctx.fetch();
        assertThat(result.size()).isEqualTo(16);
    }


    @Test
    public void testGroupByMonthDynamic() throws Exception {
        ResultQuery ctx = DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(concat(year(DATE), field("'_'"), month(DATE)), DEPT.count(), AMOUNT.sum())
                .from(EXPENSES)
                .groupBy(DATE).orderBy(DATE.asc());

        String sql = ctx.getSQL();
        Result result= ctx.fetch();
        //printResult(result);
        assertThat(result.getValue(0, 0)).isEqualTo("2012_1");
        assertThat(result.getValue(47, 0)).isEqualTo("2015_12");
        assertThat(result.size()).isEqualTo(48);
    }

    @Test
    public void testAggregateFunction() throws Exception {
        ResultQuery ctx = DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(ID.count(), AMOUNT.sum())
                .from(EXPENSES);

        String sql = ctx.getSQL();
        Result result= ctx.fetch();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getValue(0, 0)).isEqualTo(new Integer(50));
        assertThat(((BigDecimal) result.getValue(0, 1)).longValue()).isEqualTo(new Long(22731));
    }

    @Test
    public void testGroupByLabel() throws Exception {
        ResultQuery ctx = DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(ID.count(), AMOUNT.sum(), DEPT)
                .from(EXPENSES)
                .groupBy(DEPT);

        String sql = ctx.getSQL();
        Result result= ctx.fetch();
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.getValue(0, 0)).isEqualTo(new Integer(11));
        assertThat(((BigDecimal) result.getValue(0, 1)).longValue()).isEqualTo(new Long(6017));
        assertThat(result.getValue(0, 2)).isEqualTo("Management");
    }

    @Test
    public void testGroupByYear() throws Exception {
        ResultQuery ctx = DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(year(DATE), AMOUNT.sum())
                .from(EXPENSES)
                .groupBy(year(DATE));

        String sql = ctx.getSQL();
        Result result= ctx.fetch();
        assertThat(result.size()).isEqualTo(4);
        assertThat(result.getValue(0, 0)).isEqualTo(2012);
        assertThat(result.getValue(1, 0)).isEqualTo(2013);
        assertThat(result.getValue(2, 0)).isEqualTo(2014);
        assertThat(result.getValue(3, 0)).isEqualTo(2015);
        assertThat(((BigDecimal) result.getValue(0, 1)).longValue()).isEqualTo(6126L);
        assertThat(((BigDecimal) result.getValue(1, 1)).longValue()).isEqualTo(5252L);
        assertThat(((BigDecimal) result.getValue(2, 1)).longValue()).isEqualTo(4015L);
        assertThat(((BigDecimal) result.getValue(3, 1)).longValue()).isEqualTo(7336L);
    }

    @Test
    public void testGroupByMonth() throws Exception {
        ResultQuery ctx = DSL.using(conn, new Settings().withRenderFormatted(true))
                .select(month(DATE), AMOUNT.sum())
                .from(EXPENSES)
                .groupBy(month(DATE));

        String sql = ctx.getSQL();
        Result result= ctx.fetch();
        assertThat(result.size()).isEqualTo(12);
        assertThat(result.getValue(0, 0)).isEqualTo(1);
        assertThat(result.getValue(1, 0)).isEqualTo(2);
        assertThat(result.getValue(2, 0)).isEqualTo(3);
        assertThat(result.getValue(3, 0)).isEqualTo(4);
        assertThat(result.getValue(4, 0)).isEqualTo(5);
        assertThat(result.getValue(5, 0)).isEqualTo(6);
        assertThat(result.getValue(6, 0)).isEqualTo(7);
        assertThat(result.getValue(7, 0)).isEqualTo(8);
        assertThat(result.getValue(8, 0)).isEqualTo(9);
        assertThat(result.getValue(9, 0)).isEqualTo(10);
        assertThat(result.getValue(10, 0)).isEqualTo(11);
        assertThat(result.getValue(11, 0)).isEqualTo(12);
        assertThat(((BigDecimal) result.getValue(0, 1)).longValue()).isEqualTo(2324L);
        assertThat(((BigDecimal) result.getValue(1, 1)).longValue()).isEqualTo(2885L);
        assertThat(((BigDecimal) result.getValue(2, 1)).longValue()).isEqualTo(1012L);
        assertThat(((BigDecimal) result.getValue(3, 1)).longValue()).isEqualTo(1061L);
        assertThat(((BigDecimal) result.getValue(4, 1)).longValue()).isEqualTo(2503L);
        assertThat(((BigDecimal) result.getValue(5, 1)).longValue()).isEqualTo(4113L);
        assertThat(((BigDecimal) result.getValue(6, 1)).longValue()).isEqualTo(2354L);
        assertThat(((BigDecimal) result.getValue(7, 1)).longValue()).isEqualTo(452L);
        assertThat(((BigDecimal) result.getValue(8, 1)).longValue()).isEqualTo(693L);
        assertThat(((BigDecimal) result.getValue(9, 1)).longValue()).isEqualTo(1366L);
        assertThat(((BigDecimal) result.getValue(10, 1)).longValue()).isEqualTo(1443L);
        assertThat(((BigDecimal) result.getValue(11, 1)).longValue()).isEqualTo(2520L);
    }

    public void printResult(Result rs) {
        for (int i=0; i<rs.getValues(0).size(); i++) {
            System.out.println("");
            try {
                for (int j=0;; j++) {
                    if (j > 0) System.out.print(", ");
                    System.out.print(rs.getValue(i, j));

                }
            } catch (Exception e) {
                continue;
            }
        }
    }
}
