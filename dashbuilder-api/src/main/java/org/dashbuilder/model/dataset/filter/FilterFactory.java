/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.model.dataset.filter;

/**
 * A factory of filter functions
 */
public class FilterFactory {

    // Core filter functions

    public static ColumnFilter isNull() {
        return isNull(null);
    }

    public static ColumnFilter isNull(String columnId) {
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_NULL);
    }

    public static ColumnFilter isNotNull() {
        return isNotNull(null);
    }

    public static ColumnFilter isNotNull(String columnId) {
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_NOT_NULL);
    }

    public static ColumnFilter isEqualsTo(Comparable allowedValue) {
        return isEqualsTo(null, allowedValue);
    }

    public static ColumnFilter isEqualsTo(String columnId, Comparable allowedValue) {
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_EQUALS_TO, allowedValue);
    }

    public static ColumnFilter isNotEqualsTo(Comparable allowedValue) {
        return isNotEqualsTo(null, allowedValue);
    }

    public static ColumnFilter isNotEqualsTo(String columnId, Comparable allowedValue) {
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_NOT_EQUALS_TO, allowedValue);
    }

    public static ColumnFilter isLowerThan(Comparable ref) {
        return isLowerThan(null, ref);
    }

    public static ColumnFilter isLowerThan(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_LOWER_THAN, ref);
    }

    public static ColumnFilter isLowerOrEqualsTo(Comparable ref) {
        return isLowerOrEqualsTo(null, ref);
    }

    public static ColumnFilter isLowerOrEqualsTo(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_LOWER_OR_EQUALS_TO, ref);
    }

    public static ColumnFilter isGreaterThan(Comparable ref) {
        return isGreaterThan(null, ref);
    }

    public static ColumnFilter isGreaterThan(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_GREATER_THAN, ref);
    }

    public static ColumnFilter isGreaterOrEqualsTo(Comparable ref) {
        return isGreaterOrEqualsTo(null, ref);
    }

    public static ColumnFilter isGreaterOrEqualsTo(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_GREATER_OR_EQUALS_TO, ref);
    }

    public static ColumnFilter isBetween(Comparable low, Comparable high) {
        return isBetween(null, low, high);
    }

    public static ColumnFilter isBetween(String columnId, Comparable low, Comparable high) {
        if (low instanceof Number) low = ((Number) low).doubleValue();
        if (high instanceof Number) high = ((Number) high).doubleValue();
        return new CoreFunctionFilter(columnId, CoreFunctionType.IS_BETWEEN, low, high);
    }

    // Boolean operators

    public static ColumnFilter AND(ColumnFilter... filters) {
        return AND(null, filters);
    }

    public static ColumnFilter AND(String columnId, ColumnFilter... filters) {
        return new LogicalExprFilter(columnId, LogicalExprType.AND, filters);
    }

    public static ColumnFilter OR(ColumnFilter... filters) {
        return OR(null, filters);
    }

    public static ColumnFilter OR(String columnId, ColumnFilter... filters) {
        return new LogicalExprFilter(columnId, LogicalExprType.OR, filters);
    }

    public static ColumnFilter NOT(ColumnFilter... filters) {
        return NOT(null, filters);
    }

    public static ColumnFilter NOT(String columnId, ColumnFilter... filters) {
        return new LogicalExprFilter(columnId, LogicalExprType.NOT, filters);
    }

    // Custom filter

    public static ColumnFilter function(String columnId, FilterFunction function) {
        return new CustomFunctionFilter(columnId, function);
    }
}
