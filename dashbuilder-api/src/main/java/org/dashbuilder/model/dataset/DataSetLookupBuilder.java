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
     * @param columnId The column identifier
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId);

    /**
     * Group the data set by one of the columns, specifying the grouping strategy to use.
     * @param columnId The column identifier
     * @param strategy The grouping strategy to use.
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     * @see org.dashbuilder.model.dataset.group.GroupStrategy
     */
    T group(String columnId, GroupStrategy strategy);

    /**
     * TODO
     * @param columnId
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, DateIntervalType intervalSize);

    /**
     * TODO
     * @param columnId
     * @param maxIntervals
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, int maxIntervals, DateIntervalType intervalSize);

    /**
     * TODO
     * @param columnId
     * @param maxIntervals
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, int maxIntervals, String intervalSize);

    /**
     * TODO
     * @param columnId
     * @param strategy
     * @param maxIntervals
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String strategy, int maxIntervals, DateIntervalType intervalSize);

    /**
     * TODO
     * @param columnId
     * @param strategy
     * @param maxIntervals
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String strategy, int maxIntervals, String intervalSize);

    /**
     * TODO
     * @param columnId
     * @param strategy
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, GroupStrategy strategy, String intervalSize);

    /**
     * TODO
     * @param columnId
     * @param strategy
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, GroupStrategy strategy, DateIntervalType intervalSize);

    /**
     * TODO
     * @param columnId
     * @param strategy
     * @param maxIntervals
     * @param intervalSize
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
     * TODO
     * @param columnId
     * @param newColumnId
     * @param intervalSize
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
     * @param newColumnId The identifier for the group column
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
     * TODO
     * @param columnId
     * @param newColumnId
     * @param strategy
     * @param maxIntervals
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize);

    /**
     * TODO
     * @param columnId
     * @param newColumnId
     * @param strategy
     * @param maxIntervals
     * @param intervalSize
     * @return The DataSetLookupBuilder instance that is being used to configure a DataSetLookup request.
     */
    T group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, DateIntervalType intervalSize);

    /**
     * TODO
     * @param type
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
