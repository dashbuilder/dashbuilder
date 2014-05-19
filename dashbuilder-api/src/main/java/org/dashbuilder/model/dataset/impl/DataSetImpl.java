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
import java.util.ArrayList;

import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetRef;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataSetImpl implements DataSet {

    protected String uuid = null;
    protected List<DataColumn> columns = new ArrayList<DataColumn>();

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
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

    public DataColumn getColumnByIndex(int index) {
        if (columns == null || columns.isEmpty()) return null;
        if (index >= columns.size()) return null;
        return columns.get(index);
    }

    public DataSet addColumn(String id, ColumnType type) {
        return addColumn(id, type, null);
    }

    public DataSet addColumn(String id, ColumnType type, List values) {
        DataColumnImpl c = new DataColumnImpl();
        c.setDataSet(this);
        c.setId(id);
        c.setColumnType(type);
        if (values != null) c.setValues(values);
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
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("The data set has no columns.");
        }
        if (column >= columns.size()) {
            throw new IllegalArgumentException("The column index " + column + " is out of bounds: " + (columns.size()-1));
        }

        DataColumn columnObj = columns.get(column);
        List l = columnObj.getValues();
        if (row > l.size()) {
            throw new IllegalArgumentException("The row index " + row + " is out of bounds: " + (l.size()-1));
        }

        if (row == l.size()) l.add(value);
        l.set(row, value);
        return this;
    }

    public DataSet setValuesAt(int row, Object... values) {
        if (columns == null || columns.isEmpty()) return null;
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            setValueAt(row, i, value);
        }
        return this;
    }

    public DataSet setValues(Object[][] values) {
        if (columns == null || columns.isEmpty()) return null;
        for (int i = 0; i < values.length; i++) {
            Object[] row = values[i];
            for (int j = 0; j < row.length; j++) {
                Object value = row[j];
                setValueAt(i, j, value);
            }
        }
        return this;
    }

    public DataSet trim(int offset, int rows) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset can't be negative: " + offset);
        }
        if (offset >= getRowCount()) {
            throw new IllegalArgumentException("Offset can't be greater than the number of rows: " + offset);
        }
        if (offset == 0 && (rows <= 0 || rows >= this.getRowCount())) {
            return this;
        }

        DataSetImpl other = cloneEmpty();
        for (int i=0; i<columns.size(); i++) {
            DataColumn column = columns.get(i);
            DataColumn colOther = other.columns.get(i);
            List values = column.getValues();
            List valOther = colOther.getValues();
            for (int j=offset; j<values.size() && j<rows; j++) {
                Object value = values.get(j);
                valOther.add(value);
            }
        }
        return other;
    }

    public DataSetImpl cloneEmpty() {
        DataSetImpl other = new DataSetImpl();
        for (int i=0; i<columns.size(); i++) {
            DataColumn column = columns.get(i);
            other.addColumn(column.getId(), column.getColumnType());
        }
        return other;
    }
}
