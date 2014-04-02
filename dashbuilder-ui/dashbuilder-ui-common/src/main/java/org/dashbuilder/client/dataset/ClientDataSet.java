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
package org.dashbuilder.client.dataset;

import java.util.List;

import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;

public class ClientDataSet implements DataSet {

    protected ClientDataSet parent;

    public String getUUID() {
        return Integer.toString(this.hashCode());
    }

    public DataSet getParent() {
        return parent;
    }

    public void setParent(ClientDataSet parent) {
        this.parent = parent;
    }

    public List<DataColumn> getColumns() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public DataColumn getColumnById(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public DataSet addColumn(String name, ColumnType type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public int getRowCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Object getValueAt(int row, int column) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public DataSet setValueAt(int row, int column, Object value) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
