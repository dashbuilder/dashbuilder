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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.LogicalExprType;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.sort.SortOrder;

import static org.dashbuilder.dataprovider.backend.sql.SQLFactory.*;

public class DefaultDialect implements Dialect {

    @Override
    public String[] getExcludedColumns() {
        return new String[] {};
    }

    @Override
    public String getColumnSQL(Column column) {

        if (column instanceof FunctionColumn) {
            return getFunctionColumnSQL((FunctionColumn) column);
        }
        if (column instanceof SortColumn) {
            return getSortColumnSQL((SortColumn) column);
        }
        if (column instanceof DynamicDateColumn) {
            return getDynamicDateColumnSQL((DynamicDateColumn) column);
        }
        if (column instanceof FixedDateColumn) {
            return getFixedDateColumnSQL((FixedDateColumn) column);
        }

        String result = getColumnNameSQL(column.getName());
        if (column.getFunctionType() != null) {
            result = getColumnFunctionSQL(result, column.getFunctionType());
        }
        if (!StringUtils.isBlank(column.getAlias())) {;
            if (!result.equals(getColumnNameSQL(column.getAlias()))) {
                result += " " + getColumnAliasSQL(column.getAlias());
            }
        }
        return result;
    }

    @Override
    public String getColumnTypeSQL(Column column) {
        switch (column.getType()) {
            case NUMBER: {
                return "NUMERIC(28,2)";
            }
            case DATE: {
                return "TIMESTAMP";
            }
            default: {
                return "VARCHAR(" + column.getLength() + ")";
            }
        }
    }

    @Override
    public String getTableSQL(Table table) {
        String name = getTableNameSQL(table.getName());
        if (StringUtils.isBlank(table.getSchema())) {
            return name;
        } else{
            return getSchemaNameSQL(table.getSchema()) + "." + name;
        }
    }

    @Override
    public String getTableNameSQL(String name) {
        return "\"" + name + "\"";
    }

    @Override
    public String getSchemaNameSQL(String name) {
        return "\"" + name + "\"";
    }

    @Override
    public String getFunctionColumnSQL(FunctionColumn column) {
        if (FunctionColumn.LOWER.equals(column.getFunction())) {
            return getLowerFunctionSQL(column.getColumns()[0]);
        }
        if (FunctionColumn.CONCAT.equals(column.getFunction())) {
            return getConcatFunctionSQL(column.getColumns());
        }
        if (FunctionColumn.YEAR.equals(column.getFunction())) {
            return getDatePartFunctionSQL("YEAR", column.getColumns()[0]);
        }
        if (FunctionColumn.MONTH.equals(column.getFunction())) {
            return getDatePartFunctionSQL("MONTH", column.getColumns()[0]);
        }
        if (FunctionColumn.DAY.equals(column.getFunction())) {
            return getDatePartFunctionSQL("DAY", column.getColumns()[0]);
        }
        if (FunctionColumn.HOUR.equals(column.getFunction())) {
            return getDatePartFunctionSQL("HOUR", column.getColumns()[0]);
        }
        if (FunctionColumn.MINUTE.equals(column.getFunction())) {
            return getDatePartFunctionSQL("MINUTE", column.getColumns()[0]);
        }
        if (FunctionColumn.SECOND.equals(column.getFunction())) {
            return getDatePartFunctionSQL("SECOND", column.getColumns()[0]);
        }
        throw new IllegalArgumentException("Column function not supported: " + column.getFunction());
    }

    @Override
    public String getLowerFunctionSQL(Column column) {
        String columnSQL = getColumnSQL(column);
        return "LOWER(" + columnSQL + ")";
    }

    @Override
    public String getConcatFunctionSQL(Column[] columns) {
        return getConcatFunctionSQL(columns, "(", ")", " || ");
    }

    public String getConcatFunctionSQL(Column[] columns, String begin, String end, String separator) {
        StringBuilder out = new StringBuilder();
        out.append(begin);
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) out.append(separator);
            Column column = columns[i];
            ColumnType type = column.getType();
            if (ColumnType.LABEL.equals(type) || ColumnType.TEXT.equals(type)) {
                out.append("'").append(column.getName()).append("'");
            } else {
                // Cast needed
                out.append(getColumnCastSQL(column));
            }
        }
        out.append(end);
        return out.toString();
    }

    public String getColumnCastSQL(Column column) {
        String columnSQL = getColumnSQL(column);
        return "CAST(" + columnSQL + " AS VARCHAR)";
    }

    @Override
    public String getDatePartFunctionSQL(String part, Column column) {
        String columnSQL = getColumnSQL(column);
        return "EXTRACT(" + part + " FROM " + columnSQL + ")";
    }

    @Override
    public String getSortColumnSQL(SortColumn sortColumn) {

        Column column = sortColumn.getSource();
        String columnSQL = getColumnSQL(column);

        // Always order by the alias (if any)
        if (!StringUtils.isBlank(column.getAlias())) {
            columnSQL = getColumnNameSQL(column.getAlias());
        }
        return columnSQL + " " + getSortOrderSQL(sortColumn.getOrder());
    }

    @Override
    public String getSortOrderSQL(SortOrder order) {
        if (SortOrder.ASCENDING.equals(order)) {
            return "ASC";
        }
        if (SortOrder.DESCENDING.equals(order)) {
            return "DESC";
        }
        throw new IllegalArgumentException("Sort order not supported: " + order);
    }

    /**
     * The text conversion of a date column is very DB specific.
     * A mechanism combining  concat and extract functions is used by default.
     * Depending on the DB dialect a more polished approach can be used.
     * For instance, <ul>
     * <li>In Oracle and Postgres the 'to_char' function is used.</li>
     * <li>In Mysql, 'date_format'</li>
     * <li>In H2, the 'to_char' function is not used as it's only available since version 1.3.175 and we do need to support older versions.</li>
     * </ul>
     */
    @Override
    public String getDynamicDateColumnSQL(DynamicDateColumn column) {
        Column dateColumn = toChar(column);
        return getColumnSQL(dateColumn);
    }

    public Column toChar(DynamicDateColumn column) {
        Column target = column(column.getName());
        DateIntervalType type = column.getDateType();
        Column SEPARATOR_DATE = column("-", ColumnType.TEXT, 3);
        Column SEPARATOR_EMPTY = column(" ", ColumnType.TEXT, 3);
        Column SEPARATOR_TIME = column(":", ColumnType.TEXT, 3);

        if (DateIntervalType.SECOND.equals(type)) {
            return concat(target.year(), SEPARATOR_DATE,
                    target.month(), SEPARATOR_DATE,
                    target.day(), SEPARATOR_EMPTY,
                    target.hour(), SEPARATOR_TIME,
                    target.minute(), SEPARATOR_TIME,
                    target.second());
        }
        if (DateIntervalType.MINUTE.equals(type)) {
            return concat(target.year(), SEPARATOR_DATE,
                    target.month(), SEPARATOR_DATE,
                    target.day(), SEPARATOR_EMPTY,
                    target.hour(), SEPARATOR_TIME,
                    target.minute());
        }
        if (DateIntervalType.HOUR.equals(type)) {
            return concat(target.year(), SEPARATOR_DATE,
                    target.month(), SEPARATOR_DATE,
                    target.day(), SEPARATOR_EMPTY,
                    target.hour());
        }
        if (DateIntervalType.DAY.equals(type) || DateIntervalType.WEEK.equals(type)) {
            return concat(target.year(), SEPARATOR_DATE,
                    target.month(), SEPARATOR_DATE,
                    target.day());
        }
        if (DateIntervalType.MONTH.equals(type)
                || DateIntervalType.QUARTER.equals(type)) {

            return concat(target.year(), SEPARATOR_DATE,
                    target.month());
        }
        if (DateIntervalType.YEAR.equals(type)
                || DateIntervalType.DECADE.equals(type)
                || DateIntervalType.CENTURY.equals(type)
                || DateIntervalType.MILLENIUM.equals(type)) {

            return target.year();
        }
        throw new IllegalArgumentException("Group '" + target.getName() +
                "' by the given date interval type is not supported: " + type);
    }

    @Override
    public String getFixedDateColumnSQL(FixedDateColumn column) {
        Column target = column(column.getName());
        DateIntervalType type = column.getDateType();
        if (DateIntervalType.SECOND.equals(type)) {
            return getColumnSQL(target.second());
        }
        if (DateIntervalType.MINUTE.equals(type)) {
            return getColumnSQL(target.minute());
        }
        if (DateIntervalType.HOUR.equals(type)) {
            return getColumnSQL(target.hour());
        }
        if (DateIntervalType.DAY_OF_WEEK.equals(type)) {
            return getColumnSQL(target.day());
        }
        if (DateIntervalType.MONTH.equals(type)) {
            return getColumnSQL(target.month());
        }
        if (DateIntervalType.QUARTER.equals(type)) {
            // Emulated using month and converted to quarter during the data set post-processing
            return getColumnSQL(target.month());
        }
        throw new IllegalArgumentException("Interval size '" + type + "' not supported for " +
                "fixed date intervals. The only supported sizes are: " +
                StringUtils.join(DateIntervalType.FIXED_INTERVALS_SUPPORTED, ","));
    }

    @Override
    public String getColumnNameSQL(String name) {
        return "\"" + name + "\"";
    }

    @Override
    public String getColumnAliasSQL(String alias) {
        return "\"" + alias + "\"";
    }

    @Override
    public String getConditionSQL(Condition condition) {
        if (condition instanceof CoreCondition) {
            return getCoreConditionSQL((CoreCondition) condition);
        }
        if (condition instanceof LogicalCondition) {
            return getLogicalConditionSQL((LogicalCondition) condition);
        }
        throw new IllegalArgumentException("Condition type not supported: " + condition);
    }

    @Override
    public String getCoreConditionSQL(CoreCondition condition) {
        String columnSQL = getColumnSQL(condition.getColumn());
        CoreFunctionType type = condition.getFunction();
        Object[] params = condition.getParameters();
        if (CoreFunctionType.IS_NULL.equals(type)) {
            return getIsNullConditionSQL(columnSQL.toString());
        }
        if (CoreFunctionType.NOT_NULL.equals(type)) {
            return getNotNullConditionSQL(columnSQL.toString());
        }
        if (CoreFunctionType.EQUALS_TO.equals(type)) {
            return getIsEqualsToConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.NOT_EQUALS_TO.equals(type)) {
            return getNotEqualsToConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.NOT_EQUALS_TO.equals(type)) {
            return getNotEqualsToConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.LIKE_TO.equals(type)) {
            return getLikeToConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.GREATER_THAN.equals(type)) {
            return getGreaterThanConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.GREATER_OR_EQUALS_TO.equals(type)) {
            return getGreaterOrEqualsConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.LOWER_THAN.equals(type)) {
            return getLowerThanConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.LOWER_OR_EQUALS_TO.equals(type)) {
            return getLowerOrEqualsConditionSQL(columnSQL.toString(), params[0]);
        }
        if (CoreFunctionType.BETWEEN.equals(type)) {
            return getBetweenConditionSQL(columnSQL.toString(), params[0], params[1]);
        }
        throw new IllegalArgumentException("Core condition type not supported: " + type);
    }

    @Override
    public String getNotNullConditionSQL(String column) {
        return column + " IS NOT NULL";
    }

    @Override
    public String getIsNullConditionSQL(String column) {
        return column + " IS NULL";
    }

    @Override
    public String getIsEqualsToConditionSQL(String column, Object param) {
        String paramStr = getParameterSQL(param);
        return column + " = " + paramStr;
    }

    @Override
    public String getNotEqualsToConditionSQL(String column, Object param) {
        String paramStr = getParameterSQL(param);
        return column + " <> " + paramStr;
    }

    @Override
    public String getLikeToConditionSQL(String column, Object param) {
        String paramStr = getParameterSQL(param);
        return column + " LIKE " + paramStr;
    }

    @Override
    public String getGreaterThanConditionSQL(String column, Object param) {
        String paramStr = getParameterSQL(param);
        return column + " > " + paramStr;
    }

    @Override
    public String getGreaterOrEqualsConditionSQL(String column, Object param) {
        String paramStr = getParameterSQL(param);
        return column + " >= " + paramStr;
    }

    @Override
    public String getLowerThanConditionSQL(String column, Object param) {
        String paramStr = getParameterSQL(param);
        return column + " < " + paramStr;
    }

    @Override
    public String getLowerOrEqualsConditionSQL(String column, Object param) {
        String paramStr = getParameterSQL(param);
        return column + " <= " + paramStr;
    }

    @Override
    public String getBetweenConditionSQL(String column, Object from, Object to) {
        String fromStr = getParameterSQL(from);
        String toStr = getParameterSQL(to);
        return column + " BETWEEN " + fromStr + " AND " + toStr;
    }

    @Override
    public String getParameterSQL(Object param) {
        if (param instanceof Number) {
            return getNumberParameterSQL((Number) param);
        }
        if (param instanceof Date) {
            return getDateParameterSQL((Date) param);
        }
        return getStringParameterSQL(param.toString());
    }


    @Override
    public String getNumberParameterSQL(Number param) {
        return param.toString();
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    @Override
    public String getDateParameterSQL(Date param) {
        // timestamp '2015-08-24 13:14:36.615'
        return "TIMESTAMP '" + dateFormat.format(param) + "'";
    }

    @Override
    public String getStringParameterSQL(String param) {
        return "'" + param + "'";
    }

    @Override
    public String getLogicalConditionSQL(LogicalCondition condition) {
        LogicalExprType type = condition.getType();
        Condition[] conditions = condition.getConditions();
        if (LogicalExprType.NOT.equals(type)) {
            return getNotExprConditionSQL(conditions[0]);
        }
        if (LogicalExprType.AND.equals(type)) {
            return getAndExprConditionSQL(conditions[0], conditions[1]);
        }
        if (LogicalExprType.OR.equals(type)) {
            return getOrExprConditionSQL(conditions[0], conditions[1]);
        }
        throw new IllegalArgumentException("Logical condition type not supported: " + type);
    }

    @Override
    public String getNotExprConditionSQL(Condition condition) {
        String conditionSQL = getConditionSQL(condition);
        return "NOT(" + conditionSQL + ")";
    }

    @Override
    public String getAndExprConditionSQL(Condition condition1, Condition condition2) {
        String condition1SQL = getConditionSQL(condition1);
        String condition2SQL = getConditionSQL(condition2);
        return "("  + condition1SQL + " AND " + condition2SQL + ")";
    }

    @Override
    public String getOrExprConditionSQL(Condition condition1, Condition condition2) {
        String condition1SQL = getConditionSQL(condition1);
        String condition2SQL = getConditionSQL(condition2);
        return "("  + condition1SQL + " OR " + condition2SQL + ")";
    }

    @Override
    public String getColumnFunctionSQL(String column, AggregateFunctionType function) {
        switch (function) {
            case SUM: {
                return "SUM(" + column + ")";
            }
            case MAX: {
                return "MAX(" + column + ")";
            }
            case MIN: {
                return "MIN(" + column + ")";
            }
            case AVERAGE: {
                return "AVG(" + column + ")";
            }
            case COUNT: {
                return "COUNT(" + column + ")";
            }
            case DISTINCT: {
                return "COUNT(DISTINCT " + column + ")";
            }
            default: {
                throw new IllegalArgumentException("Function type not valid: " + function);
            }
        }
    }

    @Override
    public String getCountQuerySQL(Select select) {
        return "SELECT "
                + getColumnFunctionSQL("*", AggregateFunctionType.COUNT)
                + " FROM (" + select.getSQL() + ") "
                + getColumnAliasSQL("dbSQL");
    }

    @Override
    public String getSQL(Select select) {
        // Select clause
        StringBuilder sql = new StringBuilder();
        String selectClause = getSelectSQL(select);
        sql.append(selectClause);

        // From clause (inner SQL or table)
        sql.append(" ").append(getFromSQL(select));

        // Where clauses
        List<Condition> wheres = select.getWheres();
        if (!wheres.isEmpty()) {
            sql.append(" ").append(getWhereSQL(select));
        }

        // Group by
        List<Column> groupBys = select.getGroupBys();
        if (!groupBys.isEmpty()) {
            sql.append(" ").append(getGroupBySQL(select));
        }

        // Order by
        List<SortColumn> orderBys = select.getOrderBys();
        if (!orderBys.isEmpty()) {
            sql.append(" ").append(getOrderBySQL(select));
        }

        // Limits
        int limit = select.getLimit();
        int offset = select.getOffset();
        if (limit > 0 || offset > 0) {
            String limitSql = getOffsetLimitSQL(select);
            if (!StringUtils.isBlank(limitSql)) {
                sql.append(limitSql);
            }
        }
        return sql.toString();
    }

    @Override
    public String getSelectSQL(Select select) {
        StringBuilder clause = new StringBuilder();
        clause.append(getSelectStatement(select));

        clause.append(" ");
        if (select.getColumns().isEmpty()) {
            clause.append("*");
        } else {
            boolean first = true;
            for (Column column : select.getColumns()) {
                if (!first) {
                    clause.append(", ");
                }
                String str = getColumnSQL(column);
                clause.append(str);
                first = false;
            }
        }
        return clause.toString();
    }

    @Override
    public String getFromSQL(Select select) {
        String fromSelect = select.getFromSelect();
        Table fromTable = select.getFromTable();
        String from = getFromStatement(select);

        if (fromSelect != null) {
            String alias = getColumnAliasSQL("dbSQL");
            return from  + " (" + fromSelect + ") " + alias;
        }
        else if (fromTable != null ){
            String table = getTableSQL(fromTable);
            return from + " " + table;
        }
        return "";
    }

    @Override
    public String getWhereSQL(Select select) {
        StringBuilder sql = new StringBuilder();
        List<Condition> wheres = select.getWheres();
        boolean first = true;
        for (Condition condition : wheres) {
            if (first) {
                sql.append(getWhereStatement(select)).append(" ");
            } else {
                sql.append(" AND ");
            }
            String str = getConditionSQL(condition);
            sql.append(str);
            first = false;
        }
        return sql.toString();
    }

    @Override
    public String getGroupBySQL(Select select) {
        StringBuilder sql = new StringBuilder();
        List<Column> groupBys = select.getGroupBys();
        boolean first = true;
        for (Column column : groupBys) {
            if (first) {
                sql.append(getGroupByStatement(select)).append(" ");
            } else {
                sql.append(", ");
            }
            String str = getColumnSQL(column);
            sql.append(str);
            first = false;
        }
        return sql.toString();
    }

    @Override
    public String getOrderBySQL(Select select) {
        StringBuilder sql = new StringBuilder();
        List<SortColumn> orderBys = select.getOrderBys();
        boolean first = true;
        for (SortColumn column : orderBys) {
            if (first) {
                sql.append(getOrderByStatement(select)).append(" ");
            } else {
                sql.append(", ");
            }
            String str = getSortColumnSQL(column);
            sql.append(str);
            first = false;
        }
        return sql.toString();
    }

    @Override
    public String getOffsetLimitSQL(Select select) {
        int offset = select.getOffset();
        int limit = select.getLimit();
        StringBuilder out = new StringBuilder();
        if (limit > 0) out.append(" LIMIT ").append(limit);
        if (offset > 0) out.append(" OFFSET ").append(offset);
        return out.toString();
    }

    @Override
    public String getSelectStatement(Select select) {
        return "SELECT";
    }

    @Override
    public String getFromStatement(Select select) {
        return "FROM";
    }

    @Override
    public String getWhereStatement(Select select) {
        return "WHERE";
    }

    @Override
    public String getGroupByStatement(Select select) {
        return "GROUP BY";
    }

    @Override
    public String getOrderByStatement(Select select) {
        return "ORDER BY";
    }
}
