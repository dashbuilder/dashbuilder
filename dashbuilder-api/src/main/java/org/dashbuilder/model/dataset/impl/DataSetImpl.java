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
package org.dashbuilder.model.dataset.impl;

import java.util.List;

import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataSetImpl implements DataSet {

    protected String uid;
    protected DataSetImpl parent;
    protected List<DataColumn> columns;

    public String getUID() {
        return uid;
    }

    public DataSet getParent() {
        return parent;
    }

    public List<DataColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DataColumn> columns) {
        this.columns = columns;
    }

    public DataColumn getColumnById(String id) {
        for (DataColumn column : columns) {
            if (column.getId().equals(id)) return column;
        }
        return null;
    }

    public DataSet addColumn(String name, ColumnType type) {
        DataColumnImpl c = new DataColumnImpl();
        c.setName(name);
        c.setColumnType(type);
        columns.add(c);
        return this;
    }

    public boolean isEmpty() {
        return getRowCount() == 0;
    }

    public int getRowCount() {
        if (columns == null || columns.isEmpty()) {
            return 0;
        }
        return columns.get(0).getValues().size();
    }

    public Object getValueAt(int row, int column) {
        if (columns == null || columns.isEmpty()) return null;
        if (column >= columns.size()) return null;
        if (row >= getRowCount()) return null;

        DataColumn columnObj = columns.get(column);
        return columnObj.getValues().get(row);
    }

    public DataSet setValueAt(int row, int column, Object value) {
        if (columns == null || columns.isEmpty()) return null;
        if (column >= columns.size()) return null;
        DataColumn columnObj = columns.get(column);
        List l = columnObj.getValues();
        if (row > l.size()) return null;
        if (row == l.size()) l.add(value);
        l.set(row, value);
        return this;

    }
}
