/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.sql.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dashbuilder.dataprovider.sql.JDBCUtils;
import org.dashbuilder.dataprovider.sql.dialect.Dialect;

public class Select extends SQLStatement<Select> {

    protected List<Column> columns = new ArrayList<Column>();
    protected String fromSelect = null;
    protected List<Condition> wheres = new ArrayList<Condition>();
    protected List<Column> groupBys = new ArrayList<Column>();
    protected List<SortColumn> orderBys = new ArrayList<SortColumn>();
    protected int limit = -1;
    protected int offset = -1;
    protected boolean offsetPostProcessing = false;
    protected List<String> quotedFields = null;

    public Select(Connection connection, Dialect dialect) {
        super(connection, dialect);
    }

    public boolean isOffsetPostProcessing() {
        return offsetPostProcessing;
    }

    public void setOffsetPostProcessing(boolean offsetPostProcessing) {
        this.offsetPostProcessing = offsetPostProcessing;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public String getFromSelect() {
        return fromSelect;
    }

    public Table getFromTable() {
        return super.getTable();
    }

    public List<Condition> getWheres() {
        return wheres;
    }

    public List<Column> getGroupBys() {
        return groupBys;
    }

    public List<SortColumn> getOrderBys() {
        return orderBys;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public Select columns(Column... cols) {
        for (Column column : cols) {
            columns.add(fix(column));
        }
        return this;
    }

    public Select columns(Collection<Column> cols) {
        for (Column column : cols) {
            columns.add(fix(column));
        }
        return this;
    }

    public Select from(String sql) {
        fromSelect = sql;
        return this;
    }

    public Select from(Table table) {
        return super.table(table);
    }

    public Select where(Condition condition) {
        if (condition != null) {
            if (condition instanceof CoreCondition) {
                fix(((CoreCondition) condition).getColumn());
            }
            wheres.add(condition);
        }
        return this;
    }

    public Select groupBy(Column column) {
        if (column != null) {
            groupBys.add(fix(column));
        }
        return this;
    }

    public Select orderBy(SortColumn... columns) {
        for (SortColumn column : columns) {
            fix(column.getSource());
            orderBys.add(column);
        }
        return this;
    }

    public Select orderBy(List<SortColumn> columns) {
        for (SortColumn column : columns) {
            fix(column.getSource());
            orderBys.add(column);
        }
        return this;
    }

    public Select limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Select offset(int offset) {
        this.offset = offset;
        return this;
    }

    public String getSQL() {
        quotedFields = JDBCUtils.getWordsBetweenQuotes(fromSelect);

        for (Column column : _columnsRefs) {
            String name = column.getName();
            if (quotedFields.contains(name)) {
                name = dialect.getColumnNameQuotedSQL(name);
            } else {
                name = JDBCUtils.fixCase(connection, name);
            }
            column.setName(name);
        }
        fromSelect = fixCase(fromSelect);
        return dialect.getSQL(this);
    }

    @Override
    public String toString() {
        return getSQL();
    }

    // Fetch

    public int fetchCount() throws SQLException {
        String countSql = dialect.getCountQuerySQL(this);
        ResultSet _rs = JDBCUtils.executeQuery(connection, countSql);
        if (_rs.next()) {
            return _rs.getInt(1);
        } else {
            return 0;
        }
    }

    public ResultSet fetch() throws SQLException {
        String sql = getSQL();
        return JDBCUtils.executeQuery(connection, sql);
    }
}
