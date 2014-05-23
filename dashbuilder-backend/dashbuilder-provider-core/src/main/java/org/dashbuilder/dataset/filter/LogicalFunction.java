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

import java.util.List;
import java.util.ArrayList;

import org.dashbuilder.model.dataset.filter.FilterLogicalExpr;
import org.dashbuilder.model.dataset.filter.LogicalOperatorType;

public class LogicalFunction extends DataSetFunction {

    private FilterLogicalExpr logicalFunctionFilter;
    private List<DataSetFunction> functionTerms = new ArrayList<DataSetFunction>();

    public LogicalFunction(DataSetContext ctx, FilterLogicalExpr filter) {
        super(ctx, filter);
        this.logicalFunctionFilter = filter;
    }

    public LogicalFunction addFunctionTerm(DataSetFunction functionTerm) {
        functionTerms.add(functionTerm);
        return this;
    }

    public boolean pass() {
        LogicalOperatorType type = logicalFunctionFilter.getLogicalOperator();

        if (LogicalOperatorType.NOT.equals(type)) {
            for (DataSetFunction term : functionTerms) {
                boolean termOk = term.pass();
                if (termOk) return false;
            }
            return true;
        }
        if (LogicalOperatorType.AND.equals(type)) {
            for (DataSetFunction term : functionTerms) {
                boolean termOk = term.pass();
                if (!termOk) return false;
            }
            return true;
        }
        if (LogicalOperatorType.OR.equals(type)) {
            for (DataSetFunction term : functionTerms) {
                boolean termOk = term.pass();
                if (termOk) return true;
            }
            return false;
        }
        throw new IllegalArgumentException("Logical operator not supported: " + type);
    }
}
