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
package org.dashbuilder.storage.memory.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dashbuilder.DataProviderServices;
import org.dashbuilder.function.ScalarFunction;
import org.dashbuilder.function.ScalarFunctionManager;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.group.ScalarFunctionType;

/**
 * An interval represent a grouped subset of a data column values.
 */
public class Interval {

    /**
     * A name that identifies the interval and it's different of other intervals belonging to the same group.
     */
    public String name = null;

    /**
     * The row indexes of the values that belong to this interval.
     */
    public List<Integer> rows = new ArrayList<Integer>();

    /**
     * A cache containing all the calculations done within this interval.
     */
    public Map<String, Map<ScalarFunctionType, Double>> scalars = new HashMap<String, Map<ScalarFunctionType, Double>>();

    public Interval(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (name == null) return other == null;
        return name == other || name.equals(other);
    }

    public int hashCode() {
        if (name == null) return 0;
        return name.hashCode();
    }

    public Double calculateScalar(DataColumn rangeColumn, ScalarFunctionType type) {
        // Look into the cache first.
        String columnId = rangeColumn.getId();
        Map<ScalarFunctionType,Double> columnScalars = scalars.get(columnId);
        if (columnScalars == null) scalars.put(columnId, columnScalars = new HashMap<ScalarFunctionType,Double>());
        Double scalar = columnScalars.get(type);
        if (scalar != null) return scalar;

        // Do the scalar calculations.
        ScalarFunctionManager scalarFunctionManager = DataProviderServices.getScalarFunctionManager();
        ScalarFunction function = scalarFunctionManager.getScalarFunctionByCode(type.toString().toLowerCase());
        scalar = function.scalar(rangeColumn.getValues(), rows);

        // Save the result into the cache and return.
        columnScalars.put(type, scalar);
        return scalar;

    }
}
