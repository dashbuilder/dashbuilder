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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A logical expression based filter definition.
 */
@Portable
public class FilterLogicalExpr extends FilterColumn {

    protected LogicalOperatorType logicalOperator = LogicalOperatorType.AND;
    protected List<FilterColumn> logicalTerms = new ArrayList<FilterColumn>();

    public FilterLogicalExpr() {
    }

    public FilterLogicalExpr(String columnId, LogicalOperatorType operator, FilterColumn... terms) {
        super(columnId);
        this.logicalOperator = operator;
        setLogicalTerms(terms);
    }

    public void setColumnId(String columnId) {
        String oldColumnId = getColumnId();
        super.setColumnId(columnId);

        // Ensure children column refs are synced with its parent.
        for (FilterColumn childFunction : logicalTerms) {
            String childColumnId = childFunction.getColumnId();
            if (childColumnId == null || childColumnId.equals(oldColumnId)) {
                childFunction.setColumnId(columnId);
            }
        }
    }

    public LogicalOperatorType getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperatorType logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public List<FilterColumn> getLogicalTerms() {
        return logicalTerms;
    }

    public void setLogicalTerms(List<FilterColumn> logicalTerms) {
        this.logicalTerms = logicalTerms;
    }

    public void addLogicalTerm(FilterColumn logicalTerm) {
        // Functions with no column reference inherit the column from its parent
        String childColumnId = logicalTerm.getColumnId();
        if (childColumnId == null) {
            logicalTerm.setColumnId(this.getColumnId());
        }
        this.logicalTerms.add(logicalTerm);
    }

    public void setLogicalTerms(FilterColumn... logicalTerms) {
        this.logicalTerms.clear();
        for (FilterColumn term : logicalTerms) {
            addLogicalTerm(term);
        }
    }

    public boolean equals(Object obj) {
        try {
            FilterLogicalExpr other = (FilterLogicalExpr) obj;
            if (!super.equals(other)) return false;

            if (logicalOperator != null && !logicalOperator.equals(other.logicalOperator)) return false;
            if (logicalTerms.size() != other.logicalTerms.size()) return false;
            for (FilterColumn fc : logicalTerms) {
                if (!other.logicalTerms.contains(fc)) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(logicalOperator).append(" (");
        for (int i=0; i<logicalTerms.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(logicalTerms.get(i).toString());
        }
        out.append(")");
        return out.toString();
    }
}
