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
package org.dashbuilder.client.dataset.impl;

import java.util.List;

import org.dashbuilder.client.dataset.DataColumn;
import org.dashbuilder.client.dataset.DataSet;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataSetImpl implements DataSet {

    protected List<DataColumn> columns;

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
}
