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

import javax.enterprise.inject.Specializes;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.ds.PGSimpleDataSource;

@Specializes
public class DataSourceLocatorMock extends SQLDataSourceLocatorImpl {

    public static String TYPE = "h2";
    public static String URL = "jdbc:h2:mem:test;DATABASE_TO_UPPER=FALSE";
    public static String SERVER = null;
    public static String DB = null;
    public static int PORT = 0;
    public static String USER = null;
    public static String PASSWORD = null;

    public DataSource lookup(SQLDataSetDef def) throws NamingException {
        if (TYPE.equals("mysql")) {
            MysqlDataSource ds = new MysqlDataSource();
            ds.setURL(URL);
            if (USER != null) ds.setUser(USER);
            if (PASSWORD != null) ds.setPassword(PASSWORD);
            return ds;
        }
        else if (TYPE.equals("postgres")) {
            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setServerName(SERVER);
            ds.setDatabaseName(DB);
            ds.setPortNumber(PORT);
            if (USER != null) ds.setUser(USER);
            if (PASSWORD != null) ds.setPassword(PASSWORD);
            return ds;
        }
        else {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL(URL);
            if (USER != null) ds.setUser(USER);
            if (PASSWORD != null) ds.setPassword(PASSWORD);
            return ds;
        }
    }
}
