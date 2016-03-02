/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataset.engine.function;

import java.util.Iterator;
import java.util.List;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.group.AggregateFunctionType;

/**
 * It calculates the max. number of a set of numbers.
 */
public class MaxFunction extends AbstractFunction {

    public MaxFunction() {
        super();
    }

    public AggregateFunctionType getType() {
        return AggregateFunctionType.MAX;
    }

    public double aggregate(List values) {
        if (values == null || values.isEmpty()) return 0;

        // Get the max. value from the collection.
        Number max = null;
        Iterator it = values.iterator();
        while (it.hasNext()) {
            Number n = (Number) it.next();
            if (n == null) continue;
            if (max == null || n.doubleValue() > max.doubleValue()) max = n;
        }
        if (max == null) return 0;
        return round(max.doubleValue(), precission);
    }

    public double aggregate(List values, List<Integer> rows) {
        if (rows == null) return aggregate(values);
        if (rows.isEmpty()) return 0;
        if (values == null || values.isEmpty()) return 0;

        // Get the max. value from the collection.
        Number max = null;
        for (Integer row : rows) {
            Number n = (Number) values.get(row);
            if (n == null) continue;
            if (max == null || n.doubleValue() > max.doubleValue()) max = n;
        }
        if (max == null) return 0;
        return round(max.doubleValue(), precission);
    }
}