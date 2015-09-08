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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dashbuilder.dataprovider.backend.sql.JDBCUtils;
import org.dashbuilder.dataprovider.backend.sql.dialect.Dialect;

public class Select extends SQLStatement<Select> {

    protected List<Column> columns = new ArrayList<Column>();
    protected String fromSelect = null;
    protected List<Condition> wheres = new ArrayList<Condition>();
    protected List<Column> groupBys = new ArrayList<Column>();
    protected List<SortColumn> orderBys = new ArrayList<SortColumn>();
    protected int limit = -1;
    protected int offset = -1;
    protected boolean offsetPostProcessing = false;

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
            columns.add(column);
        }
        return this;
    }

    public Select columns(Collection<Column> cols) {
        columns.addAll(cols);
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
        wheres.add(condition);
        return this;
    }

    public Select groupBy(Column column) {
        groupBys.add(column);
        return this;
    }

    public Select orderBy(SortColumn... columns) {
        for (SortColumn column : columns) {
            orderBys.add(column);
        }
        return this;
    }

    public Select orderBy(List<SortColumn> columns) {
        orderBys.addAll(columns);
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
