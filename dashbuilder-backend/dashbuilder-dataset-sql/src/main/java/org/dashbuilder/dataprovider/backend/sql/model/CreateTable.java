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

import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataprovider.backend.sql.JDBCUtils;
import org.dashbuilder.dataprovider.backend.sql.dialect.Dialect;

public class CreateTable extends SQLStatement<CreateTable> {

    protected List<Column> columns = new ArrayList<Column>();
    protected List<Column> primaryKeys = new ArrayList<Column>();

    public CreateTable(Connection connection, Dialect dialect) {
        super(connection, dialect);
    }

    public String getTableName() {
        return table.getName();
    }

    public CreateTable columns(Column... cols) {
        for (Column column : cols) {
            columns.add(column);
        }
        return this;
    }

    public CreateTable primaryKey(Column... cols) {
        for (Column column : cols) {
            primaryKeys.add(column);
        }
        return this;
    }

    public String getSQL() {
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        List<String> pkeys = new ArrayList<String>();
        String tname = dialect.getTableSQL(this);
        sql.append(tname);

        // Columns
        boolean first = true;
        sql.append(" (\n");
        for (Column column : columns) {
            if (!first) {
                sql.append(",\n");
            }
            String name = dialect.getColumnNameSQL(column.getName());
            String type = dialect.getColumnTypeSQL(column);
            sql.append(" ").append(name).append(" ").append(type);
            if (primaryKeys.contains(column)) {
                sql.append(" NOT NULL");
                pkeys.add(name);
            }
            first = false;
        }
        if (!primaryKeys.isEmpty()) {
            sql.append(",\n");
            sql.append(" PRIMARY KEY(");
            sql.append(StringUtils.join(pkeys, ","));
            sql.append(")\n");
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