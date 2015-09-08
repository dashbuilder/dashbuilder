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
package org.dashbuilder.dataprovider.backend.sql.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dashbuilder.dataprovider.backend.sql.JDBCUtils;
import org.dashbuilder.dataprovider.backend.sql.dialect.Dialect;

public class Insert extends SQLStatement<Insert> {

    protected List<Column> columns = new ArrayList<Column>();
    protected List values = new ArrayList();

    public Insert(Connection connection, Dialect dialect) {
        super(connection, dialect);
    }

    public Insert into(Table table) {
        return super.table(table);
    }

    public Insert set(Column column, Object value) {
        columns.add(column);
        values.add(value);
        return this;
    }

    public String getSQL() {
        StringBuilder sql = new StringBuilder("INSERT INTO ");

        // Table
        if (table != null) {
            sql.append(dialect.getTableSQL(this));
        }

        // Columns
        boolean first = true;
        sql.append(" (");
        for (Column column : columns) {
            if (!first) {
                sql.append(",");
            }
            String str = dialect.getColumnSQL(column);
            sql.append(str);
            first = false;
        }
        sql.append(")");

        // Values
        first = true;
        sql.append(" VALUES (");
        for (Object value : values) {
            if (!first) {
                sql.append(",");
            }
            String str = dialect.getParameterSQL(value);
            sql.append(str);
            first = false;
        }
        sql.append(")");
        return sql.toString();
    }

    @Override
    public String toString() {
        return getSQL();
    }

    public void execute() throws SQLException {
        String sql = getSQL();
        JDBCUtils.execute(connection, sql);
    }
}