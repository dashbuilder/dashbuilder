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
package org.dashbuilder.dataset;

import java.util.List;
import java.util.ArrayList;

/**
 * A set of constraints over the structure of a DataSet instance.
 */
public class DataSetConstraints<T> {

    public static final int ERROR_COLUMN_TYPE = 100;
    public static final int ERROR_COLUMN_NUMBER = 101;

    protected ColumnType[] columnTypes = null;
    protected List<ColumnType[]> alternativeTypes = new ArrayList<ColumnType[]>();
    protected int minColumns = -1;
    protected int maxColumns = -1;

    public ColumnType[] getColumnTypes() {
        return columnTypes;
    }

    public T setColumnTypes(ColumnType[] columns) {
        _checkSizes(minColumns, maxColumns, columns);
        this.columnTypes = columns;
        return (T) this;
    }

    public T setAlternativeTypes(ColumnType[]... columns) {
        for (ColumnType[] types : columns) {
            _checkSizes(minColumns, maxColumns, types);
            this.alternativeTypes.add(types);
        }
        return (T) this;
    }

    public List<ColumnType[]> getAlternativeTypes() {
        return alternativeTypes;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public T setMaxColumns(int maxColumns) {
        _checkSizes(minColumns, maxColumns, columnTypes);
        this.maxColumns = maxColumns;
        return (T) this;
    }

    public int getMinColumns() {
        return minColumns;
    }

    public T setMinColumns(int minColumns) {
        _checkSizes(minColumns, maxColumns, columnTypes);
        this.minColumns = minColumns;
        return (T) this;
    }

    private void _checkSizes(int min, int max, ColumnType[] types) {
        if (min == 0) {
            throw new IllegalArgumentException("Minimum data set columns must be greater or equals than 1. Actual=" + min);
        }
        if (max == 0) {
            throw new IllegalArgumentException("Maximum data set columns must be greater or equals than 1. Actual=" + min);
        }
        if (min != -1 & max != -1 && min > max) {
            throw new IllegalArgumentException("Min=" + min + " data set columns cannot be greater than the max=" + max);
        }
        if (types != null) {
            if (min != -1 && types.length < min) {
                throw new IllegalArgumentException("columnTypes is smaller than " + min);
            }
            if (max != -1 && types.length > max) {
                throw new IllegalArgumentException("columnTypes is greater than " + max);
            }
        }
    }

    public ValidationError check(DataSet dataSet) {

        if (minColumns != -1 && dataSet.getColumns().size() < minColumns) {
            return new ValidationError(ERROR_COLUMN_NUMBER);
        }
        if (maxColumns != -1 && dataSet.getColumns().size() > maxColumns) {
            return new ValidationError(ERROR_COLUMN_NUMBER);
        }
        ValidationError error = null;
        if (columnTypes != null) {
            error = checkTypes(dataSet, columnTypes);
            if (error != null) {
                for (ColumnType[] _types : alternativeTypes) {
                    error = checkTypes(dataSet, _types);
                }
            }
        }
        return error;
    }

    private ValidationError checkTypes(DataSet dataSet, ColumnType[] types) {
        for (int i = 0; i < dataSet.getColumns().size(); i++) {
            ColumnType columnType = dataSet.getColumnByIndex(i).getColumnType();
            if (i < types.length && !columnType.equals(types[i])) {
                return new ValidationError(ERROR_COLUMN_TYPE, i);
            }
        }
        return null;
    }

    public class ValidationError {
        int code = -1;
        List parameters = new ArrayList();

        public ValidationError(int code, Object... params) {
            this.code = code;
            this.parameters.add(params);
        }

        public int getCode() {
            return code;
        }

        public List getParameters() {
            return parameters;
        }
    }
}