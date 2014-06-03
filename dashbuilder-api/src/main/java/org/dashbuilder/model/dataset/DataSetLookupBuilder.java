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
package org.dashbuilder.model.dataset;

import org.dashbuilder.model.dataset.filter.ColumnFilter;
import org.dashbuilder.model.dataset.group.DateIntervalType;
import org.dashbuilder.model.dataset.group.GroupStrategy;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.date.DayOfWeek;
import org.dashbuilder.model.date.Month;

/**
 * It allows for the building of DataSetLookup instances in a friendly manner.
 *
 * <pre>
     DataSetFactory.newLookupBuilder()
     .uuid("target-dataset-uuid")
     .group("department")
     .count("id", "occurrences")
     .sum("amount", "totalAmount")
     .sort("total", "asc")
     .buildLookup();
 </pre>

 */
public interface DataSetLookupBuilder<T extends DataSetLookupBuilder> {

    T uuid(String uuid);
    T rowOffset(int offset);
    T rowNumber(int rows);

    T group(String columnId);
    T group(String columnId, String newColumnId);
    T group(String columnId, GroupStrategy strategy);
    T group(String columnId, DateIntervalType intervalSize);
    T group(String columnId, int maxIntervals, DateIntervalType intervalSize);
    T group(String columnId, int maxIntervals, String intervalSize);
    T group(String columnId, String strategy, int maxIntervals, DateIntervalType intervalSize);
    T group(String columnId, String strategy, int maxIntervals, String intervalSize);
    T group(String columnId, GroupStrategy strategy, String intervalSize);
    T group(String columnId, GroupStrategy strategy, DateIntervalType intervalSize);
    T group(String columnId, GroupStrategy strategy, int maxIntervals, String intervalSize);
    T group(String columnId, String newColumnId, String strategy);
    T group(String columnId, String newColumnId, GroupStrategy strategy);
    T group(String columnId, String newColumnId, String strategy, int maxIntervals, String intervalSize);
    T group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, String intervalSize);
    T group(String columnId, String newColumnId, GroupStrategy strategy, int maxIntervals, DateIntervalType intervalSize);

    T fixed(DateIntervalType type);
    T fixed(DateIntervalType type, boolean ascending);
    T firstDay(DayOfWeek dayOfWeek);
    T firstMonth(Month month);

    T distinct(String columnId);
    T distinct(String columnId, String newColumnId);
    T count(String newColumnId);
    T min(String columnId);
    T min(String columnId, String newColumnId);
    T max(String columnId);
    T max(String columnId, String newColumnId);
    T avg(String columnId);
    T avg(String columnId, String newColumnId);
    T sum(String columnId);
    T sum(String columnId, String newColumnId);

    T select(String... intervalNames);

    T filter(ColumnFilter... filter);
    T filter(String columnId, ColumnFilter... filter);

    T sort(String columnId, String order);
    T sort(String columnId, SortOrder order);

    DataSetLookup buildLookup();
}
