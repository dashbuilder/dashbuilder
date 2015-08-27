/**
 * Copyright (C) 2015 JBoss Inc
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
package org.dashbuilder.dataprovider.backend.sql.model;

import java.util.Collection;

import org.dashbuilder.dataset.filter.LogicalExprType;

public class LogicalCondition extends Condition {

    protected LogicalExprType type;
    protected Condition[] conditions;

    public LogicalCondition(LogicalExprType type, Collection<Condition> conditionList) {
        this.type = type;
        conditions = conditionList.toArray(new Condition[0]);
    }

    public LogicalCondition(LogicalExprType type, Condition... conditions) {
        this.type = type;
        this.conditions = conditions;
    }

    public LogicalExprType getType() {
        return type;
    }

    public Condition[] getConditions() {
        return conditions;
    }
}
