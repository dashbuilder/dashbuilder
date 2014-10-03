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
package org.dashbuilder.dataset.impl;

import java.util.List;
import java.util.ArrayList;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataSetMetadataImpl implements DataSetMetadata {

    protected DataSetDef definition;
    protected String uuid;
    protected int numberOfRows;
    protected int numberOfColumns;
    protected List<String> columnIds = new ArrayList<String>();
    protected List<ColumnType> columnTypes = new ArrayList<ColumnType>();
    protected int estimatedSize;

    public DataSetMetadataImpl() {
    }

    public DataSetMetadataImpl(DataSetImpl dataSet) {
        this.definition = dataSet.getDefinition();
        this.uuid = dataSet.uuid;
        this.numberOfRows = dataSet.getRowCount();
        this.estimatedSize = (int) dataSet.getEstimatedSize() / 1000;
        this.numberOfColumns = dataSet.getColumns().size();
        for (DataColumn column : dataSet.getColumns()) {
            columnIds.add(column.getId());
            columnTypes.add(column.getColumnType());
        }
    }

    public String getUUID() {
        return uuid;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public int getEstimatedSize() {
        return estimatedSize;
    }

    public String getColumnId(int columnIndex) {
        return columnIds.get(columnIndex);
    }

    public ColumnType getColumnType(int columnIndex) {
        return columnTypes.get(columnIndex);
    }

    public DataSetDef getDefinition() {
        return definition;
    }

    public void setDefinition(DataSetDef definition) {
        this.definition = definition;
    }
}