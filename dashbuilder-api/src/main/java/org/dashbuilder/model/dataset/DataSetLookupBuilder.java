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
     * January as the first interval.
     *
     * @param type The size of the date interval.
     * @see org.dashbuilder.model.dataset.group.DateIntervalType
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T fixed(DateIntervalType type);

    /**
     * TODO
     * @param type
     * @param ascending
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T fixed(DateIntervalType type, boolean ascending);

    /**
     * TODO
     * @param dayOfWeek
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T firstDay(DayOfWeek dayOfWeek);

    /**
     * TODO
     * @param month
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T firstMonth(Month month);

    /**
     * TODO
     * @param columnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T distinct(String columnId);

    /**
     * TODO
     * @param columnId
     * @param newColumnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T distinct(String columnId, String newColumnId);

    /**
     * TODO
     * @param newColumnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T count(String newColumnId);

    /**
     * TODO
     * @param columnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T min(String columnId);

    /**
     * TODO
     * @param columnId
     * @param newColumnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T min(String columnId, String newColumnId);

    /**
     * TODO
     * @param columnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T max(String columnId);

    /**
     * TODO
     * @param columnId
     * @param newColumnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T max(String columnId, String newColumnId);

    /**
     * TODO
     * @param columnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T avg(String columnId);

    /**
     * TODO
     * @param columnId
     * @param newColumnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T avg(String columnId, String newColumnId);

    /**
     * TODO
     * @param columnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sum(String columnId);

    /**
     * TODO
     * @param columnId
     * @param newColumnId
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sum(String columnId, String newColumnId);

    /**
     * TODO
     * @param intervalNames
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T select(String... intervalNames);

    /**
     * TODO
     * @param filter
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T filter(ColumnFilter... filter);

    /**
     * TODO
     * @param columnId
     * @param filter
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T filter(String columnId, ColumnFilter... filter);

    /**
     * TODO
     * @param columnId
     * @param order
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sort(String columnId, String order);

    /**
     * TODO
     * @param columnId
     * @param order
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T sort(String columnId, SortOrder order);

    /**
     * @return The DataSetLookup request instance that has been configured.
     */
    DataSetLookup buildLookup();
}
