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

import org.dashbuilder.dataprovider.backend.sql.dialect.Dialect;
import org.dashbuilder.dataprovider.backend.sql.model.Column;
import org.dashbuilder.dataprovider.backend.sql.model.CreateTable;
import org.dashbuilder.dataprovider.backend.sql.model.DropTable;
import org.dashbuilder.dataprovider.backend.sql.model.DynamicDateColumn;
import org.dashbuilder.dataprovider.backend.sql.model.FixedDateColumn;
import org.dashbuilder.dataprovider.backend.sql.model.FunctionColumn;
import org.dashbuilder.dataprovider.backend.sql.model.Insert;
import org.dashbuilder.dataprovider.backend.sql.model.Select;
import org.dashbuilder.dataprovider.backend.sql.model.SimpleColumn;
import org.dashbuilder.dataprovider.backend.sql.model.Table;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;

public class SQLFactory {

    public static Select select(Connection connection) {
        Dialect dialect = JDBCUtils.dialect(connection);
        return new Select(connection, dialect);
    }

    public static Insert insert(Connection connection) {
        Dialect dialect = JDBCUtils.dialect(connection);
        return new Insert(connection, dialect);
    }

    public static CreateTable createTable(Connection connection) {
        Dialect dialect = JDBCUtils.dialect(connection);
        return new CreateTable(connection, dialect);
    }

    public static DropTable dropTable(Connection connection) {
        Dialect dialect = JDBCUtils.dialect(connection);
        return new DropTable(connection, dialect);
    }

    public static Table table(String name) {
        return new Table(name);
    }

    public static Table table(String schema, String name) {
        return new Table(schema, name);
    }

    public static Column column(String name) {
        return new SimpleColumn(name);
    }

    public static Column column(String name, ColumnType type, int size) {
        return new SimpleColumn(name, type, size);
    }

    public static Column column(String name, GroupStrategy strategy, DateIntervalType type) {
        if (GroupStrategy.FIXED.equals(strategy)) {
            return new FixedDateColumn(name, type);
        } else {
            return new DynamicDateColumn(name, type);
        }
    }

    public static Column concat(Column... columns) {
        return new FunctionColumn(FunctionColumn.CONCAT, columns);
    }
}
