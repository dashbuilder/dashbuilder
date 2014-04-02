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

import java.util.Date;
import java.util.List;

import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataSetMetadata;

/**
 * Set of metadata related with the
 */
public class DataSetMetadataImpl implements DataSetMetadata {

    protected String uid;
    protected String parentUid;
    protected int numberOfRows;
    protected int numberOfColumns;
    protected List<String> columnNames;
    protected List<ColumnType> columnTypes;
    protected long estimatedSize;
    protected Date updateDate;

    public String getUID() {
        return uid;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    public String getParentUID() {
        return parentUid;
    }

    public void setParentUID(String parentUid) {
        this.parentUid = parentUid;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<ColumnType> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(List<ColumnType> columnTypes) {
        this.columnTypes = columnTypes;
    }

    public long getEstimatedSize() {
        return estimatedSize;
    }

    public void setEstimatedSize(long estimatedSize) {
        this.estimatedSize = estimatedSize;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    public ColumnType getColumnType(int columnIndex) {
        return columnTypes.get(columnIndex);
    }
}