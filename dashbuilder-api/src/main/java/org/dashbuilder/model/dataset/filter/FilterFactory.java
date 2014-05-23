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

    public static FilterColumn isNull() {
        return isNull(null);
    }

    public static FilterColumn isNull(String columnId) {
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_NULL);
    }

    public static FilterColumn isNotNull() {
        return isNotNull(null);
    }

    public static FilterColumn isNotNull(String columnId) {
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_NOT_NULL);
    }

    public static FilterColumn isEqualsTo(Comparable allowedValue) {
        return isEqualsTo(null, allowedValue);
    }

    public static FilterColumn isEqualsTo(String columnId, Comparable allowedValue) {
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_EQUALS_TO, allowedValue);
    }

    public static FilterColumn isNotEqualsTo(Comparable allowedValue) {
        return isNotEqualsTo(null, allowedValue);
    }

    public static FilterColumn isNotEqualsTo(String columnId, Comparable allowedValue) {
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_NOT_EQUALS_TO, allowedValue);
    }

    public static FilterColumn isLowerThan(Comparable ref) {
        return isLowerThan(null, ref);
    }

    public static FilterColumn isLowerThan(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_LOWER_THAN, ref);
    }

    public static FilterColumn isLowerOrEqualsTo(Comparable ref) {
        return isLowerOrEqualsTo(null, ref);
    }

    public static FilterColumn isLowerOrEqualsTo(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_LOWER_OR_EQUALS_TO, ref);
    }

    public static FilterColumn isGreaterThan(Comparable ref) {
        return isGreaterThan(null, ref);
    }

    public static FilterColumn isGreaterThan(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_GREATER_THAN, ref);
    }

    public static FilterColumn isGreaterOrEqualsTo(Comparable ref) {
        return isGreaterOrEqualsTo(null, ref);
    }

    public static FilterColumn isGreaterOrEqualsTo(String columnId, Comparable ref) {
        if (ref instanceof Number) ref = ((Number) ref).doubleValue();
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_GREATER_OR_EQUALS_TO, ref);
    }

    public static FilterColumn isBetween(Comparable low, Comparable high) {
        return isBetween(null, low, high);
    }

    public static FilterColumn isBetween(String columnId, Comparable low, Comparable high) {
        if (low instanceof Number) low = ((Number) low).doubleValue();
        if (high instanceof Number) high = ((Number) high).doubleValue();
        return new FilterCoreFunction(columnId, FilterCoreFunctionType.IS_BETWEEN, low, high);
    }

    // Boolean operators

    public static FilterColumn AND(FilterColumn... filters) {
        return AND(null, filters);
    }

    public static FilterColumn AND(String columnId, FilterColumn... filters) {
        return new FilterLogicalExpr(columnId, LogicalOperatorType.AND, filters);
    }

    public static FilterColumn OR(FilterColumn... filters) {
        return OR(null, filters);
    }

    public static FilterColumn OR(String columnId, FilterColumn... filters) {
        return new FilterLogicalExpr(columnId, LogicalOperatorType.OR, filters);
    }

    public static FilterColumn NOT(FilterColumn... filters) {
        return NOT(null, filters);
    }

    public static FilterColumn NOT(String columnId, FilterColumn... filters) {
        return new FilterLogicalExpr(columnId, LogicalOperatorType.NOT, filters);
    }

    // Custom filter

    public static FilterColumn function(String columnId, FilterFunction function) {
        return new FilterCustomFunction(columnId, function);
    }
}
