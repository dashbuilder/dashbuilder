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
package org.dashbuilder.service;

import java.util.List;
import java.util.ArrayList;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Interface for requesting access to data sets stored in the backend.
 */
@Portable
public class DataSetLookupOptions {

    protected int rowOffset = 0;
    protected int numberOfRows = 100;
    protected List<String> columnNames = new ArrayList<String>();

    public int getRowOffset() {
        return rowOffset;
    }

    public void setRowOffset(int rowOffset) {
        this.rowOffset = rowOffset;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public void setColumnNames(String... columnNames) {
        this.columnNames.clear();
        for (String columnName : columnNames) {
            this.columnNames.add(columnName);
        }
    }
}
