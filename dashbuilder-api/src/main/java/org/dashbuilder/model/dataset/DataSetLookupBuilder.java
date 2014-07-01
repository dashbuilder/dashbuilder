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
package org.dashbuilder.model.dataset;

import org.dashbuilder.model.dataset.filter.ColumnFilter;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.GroupStrategy;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.date.DayOfWeek;
import org.dashbuilder.model.date.Month;

/**
 * A DataSetLookupBuilder allows for the assembly of a DataSetLookup instance (i.e. a DataSet lookup request)
 * in a friendly manner. It allows the issuing of a request to obtain a view over a specific DataSet.
 *
 * <pre>
 *   DataSetFactory.newLookupBuilder()
 *   .dataset("target-dataset-uuid")
 *   .group("department")
 *   .count("id", "occurrences")
 *   .sum("amount", "totalAmount")
 *   .sort("total", "asc")
 *   .buildLookup();
 * </pre>
 *
 */
public interface DataSetLookupBuilder<T extends DataSetLookupBuilder> {

    /**
     * The UUID reference to the source data set.
     */
    T dataset(String uuid);

    /**
     * Set a row offset for the data set.
     * @param offset The row offset for the resulting data set.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T rowOffset(int offset);

    /**
     * Set the number of rows for the data set.
     * @param rows The number of rows for the resulting data set.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T rowNumber(int rows);

    /**
     * Group the data set by one of the columns
     * @param columnId The column identifier of the column to be grouped
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId);

    /**
     * Group the data set by one of the columns, specifying the grouping strategy to use.
     * @param columnId The column identifier of the column to be grouped
     * @param strategy The grouping strategy to use.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     */
    T group(String columnId, GroupStrategy strategy);

    /**
     * Group the data set by one of the columns, of type ColumnType.Date, specifying the size of the date interval
     * by which the column should be grouped. By default the DYNAMIC GroupStrategy will be applied.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param columnId The column identifier of the column to be grouped
     * @param intervalSize The size of the date interval
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, DateIntervalType intervalSize);

    /**
     * Group the data set by one of the columns, of type ColumnType.Date, specifying the size of the date interval
     * by which the column should be grouped. By default the DYNAMIC GroupStrategy will be applied.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param columnId The column identifier of the column to be grouped.
     * @param maxIntervals The maximum number of date intervals that should appear on the graph. The DYNAMIC GroupStrategy
     * implies that if, after grouping, more intervals are generated than the specified amount, a 'greater' DateIntervalType
     * will be applied.
     * For example:
     * <pre>
     *   DataSetFactory.newDSLookup()
     *   .dataset(SALES_OPPS)
     *   .group(CLOSING_DATE, 80, MONTH)
     * </pre>
     * will group the data set by its closing date column, in monthly intervals, up to a maximum 80 months. If this
     * dataset's time-span exceeds this number of months, then the next bigger DateIntervalType (i.e. QUARTER) will be applied.
     * @param intervalSize The size of the date interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, int maxIntervals, DateIntervalType intervalSize);

    /**
     * Group the data set by one of the columns, specifying the size of the interval
     * by which the column should be grouped. By default the DYNAMIC GroupStrategy will be applied.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param columnId The column identifier of the column to be grouped.
     * @param maxIntervals The maximum number of intervals that should appear on the graph. The DYNAMIC GroupStrategy
     * implies that if, after grouping, more intervals are generated than the specified amount, a larger interval
     * will be applied.
     * @param intervalSize The size of the interval, specified as a String.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, int maxIntervals, String intervalSize);

    /**
     * Group the data set by one of the columns, of type ColumnType.Date, specifying the size of the date interval
     * by which the column should be grouped.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param columnId The column identifier of the column to be grouped.
     * @param strategy The GroupStrategy that is to be applied, specified as a String.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param maxIntervals The maximum number of date intervals that should appear on the graph.
     * @param intervalSize The size of the date interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String strategy, int maxIntervals, DateIntervalType intervalSize);

    /**
     * Group the data set by one of the columns, specifying the grouping strategy and the size of the interval
     * by which the column should be grouped.
     * @param columnId The column identifier of the column to be grouped.
     * @param strategy The GroupStrategy that is to be applied, specified as a String.
     * @param maxIntervals The maximum number of intervals that should appear on the graph. The DYNAMIC GroupStrategy
     * implies that if, after grouping, more intervals are generated than the specified amount, a larger interval
     * will be applied.
     * @param intervalSize The size of the interval, specified as a String.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String strategy, int maxIntervals, String intervalSize);

    /**
     * Group the data set by one of the columns, specifying the grouping strategy to use.
     * @param columnId The column identifier of the column to be grouped
     * @param strategy The grouping strategy to use.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param intervalSize The size of the interval, specified as a String.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, GroupStrategy strategy, String intervalSize);

    /**
     * Group the data set by one of the columns, of type ColumnType.Date, specifying the size of the date interval
     * by which the column should be grouped, as well as the GroupStrategy that should be applied.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param columnId The column identifier of the column to be grouped.
     * @param strategy The GroupStrategy to be applied.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param intervalSize The size of the date interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, GroupStrategy strategy, DateIntervalType intervalSize);

    /**
     * Group the data set by one of the columns, specifying the grouping strategy, the maximum allowed amount of intervals,
     * and the size of the interval by which the column should be grouped.
     * @param columnId The column identifier of the column to be grouped.
     * @param strategy The GroupStrategy that is to be applied.
     * @param maxIntervals The maximum number of intervals that should appear on the graph. The DYNAMIC GroupStrategy
     * implies that if, after grouping, more intervals are generated than the specified amount, a larger interval
     * will be applied.
     * @param intervalSize The size of the interval, specified as a String.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, GroupStrategy strategy, int maxIntervals, String intervalSize);

    /**
     * Group the data set by one of the columns. The resulting group will be given the new column identifier.
     * @param columnId The column identifier
     * @param newColumnId The identifier for the group column
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String newColumnId);

    /**
     * Group the data set by one of the columns, of type ColumnType.Date, specifying the size of the date interval
     * by which the column should be grouped. The resulting group will be given the new column identifier.
     * By default the DYNAMIC GroupStrategy will be applied.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param columnId The column identifier of the column to be grouped.
     * @param newColumnId The identifier for the group column
     * @param intervalSize The size of the date interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String newColumnId, DateIntervalType intervalSize);

    /**
     * Group the data set by one of the columns, applying the indicated grouping strategy. The resulting group
     * will be given the new column identifier.
     * @param columnId The column identifier
     * @param newColumnId The identifier for the group column
     * @param strategy The grouping strategy to use, as a String.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     */
    T group(String columnId, String newColumnId, String strategy);

    /**
     * Group the data set by one of the columns, applying the indicated grouping strategy. The resulting group
     * will be given the new column identifier.
     * @param columnId The column identifier
     * @param newColumnId The identifier for the group column.
     * @param strategy The grouping strategy to use, as a String.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     */
    T group(String columnId, String newColumnId, GroupStrategy strategy);

    /**
     * TODO
     * @param columnId
     * @param newColumnId
     * @param strategy
     * @param maxIntervals
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String newColumnId, String strategy, int maxIntervals, String intervalSize);

    /**
     * Group the data set by one of the columns, applying the indicated grouping strategy and specifying the maximum
     * allowed amount of intervals and the size of the interval by which the column should be grouped. The resulting group
     * will be given the new column identifier.
     * @param columnId The column identifier
     * @param newColumnId The identifier for the group column.
     * @param strategy The grouping strategy to use.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param maxIntervals The maximum number of intervals that should appear on the graph. The DYNAMIC GroupStrategy
     * implies that if, after grouping, more intervals are generated than the specified amount, a larger interval
     * will be applied.
     * @param intervalSize The size of the interval, specified as a String.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize);

    /**
     * Group the data set by one of the columns, of type ColumnType.Date, specifying the grouping strategy, the size of
     * the date interval by which the column should be grouped, as well as the maximum of intervals that can appear on the
     * graph.
     * The resulting group will be given the new column identifier.
     * @param columnId The column identifier of the column to be grouped.
     * @param newColumnId The identifier for the group column.
     * @param strategy The GroupStrategy to be applied.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     * @param maxIntervals The maximum number of date intervals that should appear on the graph.
     * @param intervalSize The size of the date interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, DateIntervalType intervalSize);

    /**
     * This call will operate only on a previously grouped data set (i.e. one of the group() methods has been called
     * previously on the data set lookup), and will result in that the grouped column is ordered in ascending order.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T asc();

    /**
     * This call will operate only on a previously grouped data set (i.e. one of the group() methods has been called
     * previously on the data set lookup), and will result in that the grouped column is ordered in descending order.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T desc();

    /**
     * Apply a fixed grouping strategy by the specified date interval, on a previously date-grouped data set.
     *
     * Example:
     * <pre>
     *   DataSetFactory.newDSLookup()
     *   .dataset(SALES_OPPS)
     *   .group(CLOSING_DATE)
     *   .fixed(MONTH).firstMonth(JANUARY)
     * </pre>
     * will group the data set by a column identified by 'CLOSING_DATE', into a fixed monthly interval, starting with
     * January as the first month interval.
     *
     * @param type The size of the date interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T fixed(DateIntervalType type);

    /**
     * This call requires a previously grouped data set with fixed DateIntervalType.DAY_OF_WEEK intervals, i.e. both
     * group() and fixed(DateIntervalType.DAY_OF_WEEK) have to be invoked previously. It will indicate the resulting
     * data set that it has to show the specified day of the week as the first interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @param dayOfWeek The day of the week that should be shown as the graph's first interval.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T firstDay(DayOfWeek dayOfWeek);

    /**
     * This call requires a previously grouped data set with fixed DateIntervalType.MONTH intervals, i.e. both
     * group() and fixed(DateIntervalType.MONTH) have to be invoked previously. It will indicate the resulting
     * data set that it has to show the specified month as the first interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @param month The month that should be shown as the graph's first interval.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T firstMonth(Month month);

    /**
     * This function will group the specified column by its distinct values
     * @param columnId The identifier of the column that is to be grouped.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T distinct(String columnId);

    /**
     * This function will group the specified column by its distinct values. The resulting group will be given the
     * new column identifier.
     * @param columnId The identifier of the column that is to be grouped.
     * @param newColumnId The new identifier for the group column.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T distinct(String columnId, String newColumnId);

    /**
     * This function counts the ocurrences of the values a previously grouped column, and stores them in a new column
     * with the given identifier.
     * @param newColumnId The identifier for the new column.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T count(String newColumnId);

    /**
     * This function will return the minimum value for the specified column.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T min(String columnId);

    /**
     * This function will return the minimum value for the specified column. The result will be stored in a new
     * column with the given identifier.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @param newColumnId The identifier for the new column.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T min(String columnId, String newColumnId);

    /**
     * This function will return the maximum value for the specified column.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T max(String columnId);

    /**
     * This function will return the maximum value for the specified column. The result will be stored in a new
     * column with the given identifier.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @param newColumnId The identifier for the new column.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T max(String columnId, String newColumnId);

    /**
     * This function will calculate the average over the set of values for the specified column.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T avg(String columnId);

    /**
     * This function will calculate the average over the set of values for the specified column. The result will be
     * stored in a new column with the given identifier.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @param newColumnId The identifier for the new column.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T avg(String columnId, String newColumnId);

    /**
     * This function will calculate the sum of the set of values for the specified column.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sum(String columnId);

    /**
     * This function will calculate the sum of the set of values for the specified column. The result will be
     * stored in a new column with the given identifier.
     * @param columnId The identifier of the column over which this aggregate function will be invoked.
     * @param newColumnId The identifier for the new column.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sum(String columnId, String newColumnId);

    /**
     * The function will reduce the generated data set by selecting some of the intervals that were previously generated
     * through a group operation.
     *
     * For example:
     * <pre>
     *   DataSetFactory.newDSLookup()
     *   .dataset(EXPENSE_REPORTS)
     *   .group("department", "Department")
     *   .select("Services", "Engineering", "Support")
     *   .count( "Occurrences" )
     *   .buildLookup());
     * </pre>
     * Will group the expense reports data set by department, select only the "Services", "Engineering" and "Support"
     * intervals, and count how many times each of those occurs respectively.
     * @param intervalNames The interval names that should be preserved in the data set that is being generated.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T select(String... intervalNames);

    /**
     * Filter the data set according to the specified column filters. All column filters will need to explicitly reference
     * the column (through its corresponding identifier) that they need to be applied on.
     *
     * For example:
     * <pre>
     *   DataSetFactory.newDSLookup()
     *   .dataset(EXPENSE_REPORTS)
     *   .filter(AND(
     *               isEqualsTo("department", "Sales"),
     *               OR(
     *                 NOT(isLowerThan("amount", 300)),
     *                 isEqualsTo("city", "Madrid")
     *               )
     *           )
     *   )
     *   .buildLookup());
     * </pre>
     * Will limit the expense reports data set such that for all obtained records, the department will always equal "Sales",
     * and either the amount will not be lower than 300, or the city will be equal to "Madrid".
     * @see org.dashbuilder.model.dataset.filter.ColumnFilter
     * @see org.dashbuilder.model.dataset.filter.FilterFactory
     * @param filter The filters to be applied on the data set's column
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T filter(ColumnFilter... filter);

    /**
     * Filter the data set according to the specified column filters. The specified column identifier allows to specify
     * column filters without explicitly passing in the column identifier with them, they will simply 'inherit'.
     *
     * For example:
     * <pre>
     *   DataSetFactory.newDSLookup()
     *   .dataset(EXPENSE_REPORTS)
     *   .filter("amount",
     *           AND(
     *               isEqualsTo("department", "Sales"),
     *               OR(
     *                 NOT(isLowerThan(300)),
     *                 isEqualsTo("city", "Madrid")
     *               )
     *           )
     *   )
     *   .buildLookup());
     * </pre>
     * Will limit the expense reports data set such that for all obtained records, the department will always equal "Sales",
     * and either the amount will not be lower than 300, or the city will be equal to "Madrid". Since the isLowerThan filter
     * does not reference a column, it implicitly refers to the amount column.
     * @see org.dashbuilder.model.dataset.filter.ColumnFilter
     * @see org.dashbuilder.model.dataset.filter.FilterFactory
     * @param columnId The identifier of the column that the filter array should be applied on
     * @param filter The filters to be applied on the data set's column
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T filter(String columnId, ColumnFilter... filter);

    /**
     * Will apply the specified sort order over the indicated data set column.
     * @param columnId The identifier of the column that should be sorted.
     * @param order The sort order, specified as a String. Accepted values are "asc" and "desc".
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sort(String columnId, String order);

    /**
     * Will apply the specified sort order over the indicated data set column.
     * @param columnId The identifier of the column that should be sorted.
     * @param order The sort order.
     * @see org.dashbuilder.model.dataset.sort.SortOrder
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sort(String columnId, SortOrder order);

    /**
     * @return The DataSetLookup request instance that has been configured.
     */
    DataSetLookup buildLookup();
}
