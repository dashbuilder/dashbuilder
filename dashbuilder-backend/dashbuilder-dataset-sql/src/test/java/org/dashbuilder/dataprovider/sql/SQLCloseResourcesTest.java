/**
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.sql;

import org.dashbuilder.dataprovider.sql.dialect.Dialect;
import org.dashbuilder.dataprovider.sql.model.Column;
import org.dashbuilder.dataprovider.sql.model.Select;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupFactory;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JDBCUtils.class)
public class SQLCloseResourcesTest {

    @Mock
    Connection connection;

    @Mock
    Statement statement;

    @Mock
    ResultSet resultSet;

    @Mock
    Dialect dialect;

    @Mock
    Select select;

    @Mock
    SQLDataSourceLocator dataSourceLocator;

    @Mock
    DataSource dataSource;

    @Mock
    ResultSetMetaData metaData;

    SQLDataSetProvider sqlDataSetProvider;
    ResultSetHandler resultSetHandler;
    SQLDataSetDef dataSetDef = new SQLDataSetDef();

    @Before
    public void setUp() throws Exception {
        resultSetHandler = new ResultSetHandler(resultSet, statement);
        dataSetDef.setDataSource("test");
        dataSetDef.setDbSQL("test");

        sqlDataSetProvider = SQLDataSetProvider.get();
        sqlDataSetProvider.setDataSourceLocator(dataSourceLocator);
        when(dataSourceLocator.lookup(any(SQLDataSetDef.class))).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        PowerMockito.mockStatic(JDBCUtils.class);
        when(JDBCUtils.dialect(connection)).thenReturn(dialect);
        when(JDBCUtils.executeQuery(any(Connection.class), anyString())).thenReturn(resultSetHandler);
    }

    @Test
    public void testGetColumns() throws Exception {
        sqlDataSetProvider._getColumns(dataSetDef, connection);
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    public void testLookup() throws Exception {
        DataSetLookup dataSetLookup = DataSetLookupFactory.newDataSetLookupBuilder()
                .column("column1")
                .buildLookup();

        when(JDBCUtils.getColumns(resultSet, null)).thenReturn(Arrays.asList(new Column("column1")));
        sqlDataSetProvider.lookupDataSet(dataSetDef, dataSetLookup);
        verify(resultSet, times(3)).close();
        verify(statement, times(3)).close();
        verify(connection).close();
    }
}
