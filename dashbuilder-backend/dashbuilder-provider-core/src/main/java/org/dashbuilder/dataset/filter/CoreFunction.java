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
package org.dashbuilder.dataset.filter;

import org.dashbuilder.model.dataset.filter.FilterCoreFunction;
import org.dashbuilder.model.dataset.filter.FilterCoreFunctionType;

public class CoreFunction extends DataSetFunction {

    private FilterCoreFunction coreFunctionFilter = null;

    public CoreFunction(DataSetContext ctx, FilterCoreFunction coreFunctionFilter) {
        super(ctx, coreFunctionFilter);
        this.coreFunctionFilter = coreFunctionFilter;
    }

    public Comparable getCurrentValue() {
        return (Comparable) getDataColumn().getValues().get(getContext().getCurrentRow());
    }

    public Comparable getParameter(int index) {
        return coreFunctionFilter.getParameters().get(index);
    }

    public boolean pass() {
        FilterCoreFunctionType type = coreFunctionFilter.getType();

        if (FilterCoreFunctionType.IS_NULL.equals(type)) {
            return isNull(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_NOT_NULL.equals(type)) {
            return isNotNull(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_EQUALS_TO.equals(type)) {
            return isEqualsTo(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_NOT_EQUALS_TO.equals(type)) {
            return isNotEqualsTo(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_LOWER_THAN.equals(type)) {
            return isLowerThan(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_LOWER_OR_EQUALS_TO.equals(type)) {
            return isLowerThanOrEqualsTo(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_GREATER_THAN.equals(type)) {
            return isGreaterThan(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_GREATER_OR_EQUALS_TO.equals(type)) {
            return isGreaterThanOrEqualsTo(getCurrentValue());
        }
        if (FilterCoreFunctionType.IS_BETWEEN.equals(type)) {
            return isBetween(getCurrentValue());
        }
        throw new IllegalArgumentException("Core function type not supported: " + type);
    }

    public boolean isNull(Comparable value) {
        return value == null;
    }

    public boolean isNotNull(Comparable value) {
        return !isNull(value);
    }

    public boolean isEqualsTo(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return ref.equals(value);
    }

    public boolean isNotEqualsTo(Comparable value) {
        return !isEqualsTo(value);
    }

    public boolean isLowerThan(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) == -1;
    }

    public boolean isLowerThanOrEqualsTo(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) != 1;
    }

    public boolean isGreaterThan(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) == 1;
    }

    public boolean isGreaterThanOrEqualsTo(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) != -1;
    }

    public boolean isBetween(Comparable value) {
        if (isNull(value)) return false;

        Comparable low = getParameter(0);
        Comparable high = getParameter(1);
        if (value.compareTo(low) == -1) return false;
        if (value.compareTo(high) == 1) return false;
        return true;
    }
}
