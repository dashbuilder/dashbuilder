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
package org.dashbuilder.dataset.backend;

import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * DataSetLookup from/to JSON utilities
 */
public class DataSetLookupJSONMarshaller {

    // Filter ops settings
    public static final String FILTER_COLUMN = "column";
    public static final String FILTER_FUNCTION = "function";
    public static final String FILTER_ARGS = "args";

    BackendDataSetValueFormatter valueFormatter = new BackendDataSetValueFormatter();

    public DataSetFilter fromJsonFilterOps(JSONArray array) throws Exception {
        DataSetFilter dataSetFilter = new DataSetFilter();
        for (int i=0; i<array.length(); i++) {
            JSONObject filter = array.getJSONObject(i);
            String column = filter.has(FILTER_COLUMN) ? filter.getString(FILTER_COLUMN) : null;
            String function = filter.has(FILTER_FUNCTION) ? filter.getString(FILTER_FUNCTION) : null;
            if (StringUtils.isBlank(column)) throw new IllegalArgumentException("Missing function column");
            if (StringUtils.isBlank(function)) throw new IllegalArgumentException("Missing function: " + column);
            CoreFunctionType functionType = CoreFunctionType.getByName(function);
            if (functionType == null) throw new IllegalArgumentException("Function does not exist: " + function);
            CoreFunctionFilter columnFilter = new CoreFunctionFilter(column, functionType);
            dataSetFilter.addFilterColumn(columnFilter);

            JSONArray argsArray = filter.getJSONArray(FILTER_ARGS);
            for (int j=0; j<argsArray.length(); j++) {
                String str = argsArray.getString(j);
                if (StringUtils.isBlank(str)) throw new IllegalArgumentException("Empty filter function argument: " + function);
                Object arg  = valueFormatter.parseValue(str);
                columnFilter.getParameters().add(arg);
            }
        }
        return dataSetFilter;
    }
    
    public JSONArray toJson(final DataSetFilter filter) throws Exception {
        JSONArray result = null;
        
        final List<ColumnFilter> columnFilters = filter.getColumnFilterList();
        if (columnFilters != null && !columnFilters.isEmpty()) {
            for (final ColumnFilter columnFilter : columnFilters) {
                if (columnFilter instanceof CoreFunctionFilter) {
                    final CoreFunctionFilter coreFunctionFilter = (CoreFunctionFilter) columnFilter;
                    if (result == null) result = new JSONArray();
                    final JSONObject filterObj = new JSONObject();
                    final String columnId = columnFilter.getColumnId();
                    filterObj.put(FILTER_COLUMN, columnId);
                    final CoreFunctionType type = coreFunctionFilter.getType();
                    filterObj.put(FILTER_FUNCTION, type.name().toUpperCase());
                    final List arguments = coreFunctionFilter.getParameters();
                    if (arguments != null && !arguments.isEmpty()) {
                        final JSONArray args = new JSONArray();
                        for (final Object value : arguments) {
                            final String s = valueFormatter.formatValue(value);
                            args.put(s);
                        }
                        filterObj.put(FILTER_ARGS, args);
                    }
                    result.put(filterObj);
                    
                }
            }
        }
        return result;
    }
}
