/**
 * Copyright (C) 2015 JBoss Inc
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataprovider.backend.sql.dialect.DB2Dialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.DefaultDialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.Dialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.H2Dialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.MySQLDialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.OracleDialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.OracleLegacyDialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.PostgresDialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.SQLServerDialect;
import org.dashbuilder.dataprovider.backend.sql.dialect.SybaseASEDialect;
import org.dashbuilder.dataprovider.backend.sql.model.Column;
import org.dashbuilder.dataset.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dashbuilder.dataprovider.backend.sql.SQLFactory.*;

public class JDBCUtils {

    public static final Dialect DEFAULT = new DefaultDialect();
    public static final Dialect H2 = new H2Dialect();
    public static final Dialect MYSQL = new MySQLDialect();
    public static final Dialect POSTGRES = new PostgresDialect();
    public static final Dialect ORACLE = new OracleDialect();
    public static final Dialect ORACLE_LEGACY = new OracleLegacyDialect();
    public static final Dialect SQLSERVER = new SQLServerDialect();
    public static final Dialect DB2 = new DB2Dialect();
    public static final Dialect SYBASE_ASE = new SybaseASEDialect();

    private static final Logger log = LoggerFactory.getLogger(JDBCUtils.class);

    public static void execute(Connection connection, String sql) throws SQLException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(sql);
            }
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            log.error(sql);
            throw e;
        }
    }

    public static ResultSet executeQuery(Connection connection, String sql) throws SQLException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(sql);
            }
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            log.error(sql);
            throw e;
        }
    }

    public static Dialect dialect(Connection connection) {
        try {
            DatabaseMetaData m = connection.getMetaData();
            String url = m.getURL();
            if (!StringUtils.isBlank(url)) {
                return dialect(url, m.getDatabaseMajorVersion());
            }
            String dbName = m.getDatabaseProductName();
            return dialect(dbName.toLowerCase());
        }
        catch (SQLException e) {
            e.printStackTrace();
            return DEFAULT;
        }
    }

    public static Dialect dialect(String url, int majorVersion) {

        if (url.contains(":h2:")) {
            return H2;
        }
        if (url.contains(":mysql:")) {
            return MYSQL;
        }
        if (url.contains(":postgresql:")) {
            return POSTGRES;
        }
        if (url.contains(":oracle:")) {
            if (majorVersion < 12) {
                return ORACLE_LEGACY;
            } else {
                return ORACLE;
            }
        }
        if (url.contains(":sqlserver:")) {
            return SQLSERVER;
        }
        if (url.contains(":db2:")) {
            return DB2;
        }
        if (url.contains(":sybase:")) {
            return SYBASE_ASE;
        }
        return DEFAULT;
    }

    public static Dialect dialect(String dbName) {
        if (dbName.contains("h2")) {
            return H2;
        }
        if (dbName.contains("mysql")) {
            return MYSQL;
        }
        if (dbName.contains("postgre") || dbName.contains("enterprisedb")) {
            return POSTGRES;
        }
        if (dbName.contains("oracle")) {
            return ORACLE;
        }
        if (dbName.contains("microsoft") || dbName.contains("sqlserver") || dbName.contains("sql server")) {
            return SQLSERVER;
        }
        if (dbName.contains("db2")) {
            return DB2;
        }
        if (dbName.contains("ase") || dbName.contains("adaptive")) {
            return SYBASE_ASE;
        }
        return DEFAULT;
    }

    public static String getTableName(Connection conn, String dbSchema, String tableNamePattern) {
        try {
            ResultSet rs = conn.getMetaData().getTables(null, dbSchema, tableNamePattern, null);
            if (rs.next()) {
                // Table name found as is
                return tableNamePattern;
            } else {
                rs = conn.getMetaData().getTables(null, dbSchema, tableNamePattern.toUpperCase(), null);
                if (rs.next()) {
                    // Table name found in upper case
                    return tableNamePattern.toUpperCase();
                } else {
                    rs = conn.getMetaData().getTables(null, dbSchema, tableNamePattern.toLowerCase(), null);
                    if (rs.next()) {
                        // Table name found in lower case
                        return tableNamePattern.toLowerCase();
                    } else {
                        return tableNamePattern;
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving table name from database metadata: " + tableNamePattern, e);
            return tableNamePattern;
        }
    }

    public static List<Column> getColumns(ResultSet resultSet, String[] exclude) throws SQLException {
        List<Column> columnList = new ArrayList<Column>();
        List<String> columnExcluded = exclude == null ? new ArrayList<String>() : Arrays.asList(exclude);

        ResultSetMetaData meta = resultSet.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String name = meta.getColumnName(i);
            String alias = meta.getColumnLabel(i);

            if (!columnExcluded.contains(name) && !columnExcluded.contains(alias)) {
                ColumnType type = JDBCUtils.calculateType(meta.getColumnType(i));
                int size = meta.getColumnDisplaySize(i);

                Column column = column(name, type, size).as(alias);
                columnList.add(column);
            }
        }
        return columnList;
    }

    public static ColumnType calculateType(int sqlDataType) {
        switch (sqlDataType) {

            // Category-like columns.
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.BIT:
            case Types.BOOLEAN: {
                return ColumnType.LABEL;
            }

            // Text-like columns.
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR: {
                return ColumnType.TEXT;
            }

            // Number-like columns.
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
            case Types.REAL:
            case Types.SMALLINT: {
                return ColumnType.NUMBER;
            }

            // Date-like columns.
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP: {
                return ColumnType.DATE;
            }

            /*case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.NULL:
            case Types.OTHER:
            case Types.JAVA_OBJECT:
            case Types.DISTINCT:
            case Types.STRUCT:
            case Types.ARRAY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.REF:
            case Types.ROWID:
            case Types.SQLXML:
            case Types.DATALINK:*/

            // Unsupported (see above) types are treated as a text values.
            default: {
                return ColumnType.TEXT;
            }
        }
    }
}
