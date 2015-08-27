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
package org.dashbuilder.dataprovider.backend.sql.dialect;

import java.util.Date;

import org.dashbuilder.dataprovider.backend.sql.model.Column;
import org.dashbuilder.dataprovider.backend.sql.model.Condition;
import org.dashbuilder.dataprovider.backend.sql.model.CoreCondition;
import org.dashbuilder.dataprovider.backend.sql.model.DynamicDateColumn;
import org.dashbuilder.dataprovider.backend.sql.model.FixedDateColumn;
import org.dashbuilder.dataprovider.backend.sql.model.FunctionColumn;
import org.dashbuilder.dataprovider.backend.sql.model.LogicalCondition;
import org.dashbuilder.dataprovider.backend.sql.model.Select;
import org.dashbuilder.dataprovider.backend.sql.model.SortColumn;
import org.dashbuilder.dataprovider.backend.sql.model.Table;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.sort.SortOrder;

public interface Dialect {

    String getCountQuerySQL(Select select);

    String getSQL(Select select);

    String getSelectSQL(Select select);

    String getFromSQL(Select select);

    String getWhereSQL(Select select);

    String getGroupBySQL(Select select);

    String getOrderBySQL(Select select);

    String getOffsetLimitSQL(Select select);

    String getSelectStatement(Select select);

    String getFromStatement(Select select);

    String getWhereStatement(Select select);

    String getGroupByStatement(Select select);

    String getOrderByStatement(Select select);

    String getColumnSQL(Column column);

    String getColumnTypeSQL(Column column);

    String[] getExcludedColumns();

    String getTableSQL(Table table);

    String getTableNameSQL(String name);

    String getSchemaNameSQL(String name);

    String getFunctionColumnSQL(FunctionColumn column);

    String getLowerFunctionSQL(Column column);

    String getConcatFunctionSQL(Column[] columns);

    String getDatePartFunctionSQL(String part, Column column);

    String getSortColumnSQL(SortColumn column);

    String getSortOrderSQL(SortOrder order);

    String getDynamicDateColumnSQL(DynamicDateColumn column);

    String getFixedDateColumnSQL(FixedDateColumn column);

    String getColumnNameSQL(String name);

    String getColumnAliasSQL(String alias);

    String getConditionSQL(Condition condition);

    String getCoreConditionSQL(CoreCondition condition);

    String getNotNullConditionSQL(String column);

    String getIsNullConditionSQL(String column);

    String getIsEqualsToConditionSQL(String column, Object param);

    String getNotEqualsToConditionSQL(String column, Object param);

    String getLikeToConditionSQL(String column, Object param);

    String getGreaterThanConditionSQL(String column, Object param);

    String getGreaterOrEqualsConditionSQL(String column, Object param);

    String getLowerThanConditionSQL(String column, Object param);

    String getLowerOrEqualsConditionSQL(String column, Object param);

    String getBetweenConditionSQL(String column, Object from, Object to);

    String getParameterSQL(Object param);

    String getNumberParameterSQL(Number param);

    String getDateParameterSQL(Date param);

    String getStringParameterSQL(String param);

    String getLogicalConditionSQL(LogicalCondition condition);

    String getNotExprConditionSQL(Condition condition);

    String getAndExprConditionSQL(Condition condition1, Condition condition2);

    String getOrExprConditionSQL(Condition condition1, Condition condition2);

    String getColumnFunctionSQL(String column, AggregateFunctionType function);
}
