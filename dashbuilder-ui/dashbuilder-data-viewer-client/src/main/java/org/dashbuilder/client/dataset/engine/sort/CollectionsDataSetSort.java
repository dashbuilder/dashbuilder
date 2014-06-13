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
package org.dashbuilder.client.dataset.engine.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.client.dataset.engine.DataSetHandler;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.sort.ColumnSort;

/**
 * A basic sort algorithm takes relies on the default <tt>Collections.sort()</tt> implementation.
 */
@ApplicationScoped
public class CollectionsDataSetSort implements DataSetSortAlgorithm {

    public List<Integer> sort(DataSetHandler ctx, List<ColumnSort> columnSortList) {
        DataSet dataSet = ctx.getDataSet();

        // Create the comparator.
        DataSetRowComparator comparator = new DataSetRowComparator();
        for (ColumnSort columnSort : columnSortList) {
            DataColumn column = dataSet.getColumnById(columnSort.getColumnId());
            if (column == null) throw new IllegalArgumentException("Column not found in data set: " + columnSort.getColumnId());

            comparator.criteria(column, columnSort.getOrder());
        }
        // Create the row number list to sort.
        List<Integer> rows = new ArrayList<Integer>();
        if (ctx != null && ctx.getRows() != null) {
            rows.addAll(ctx.getRows());
        } else {
            for (int i=0; i<dataSet.getRowCount(); i++) {
                rows.add(i);
            }
        }
        // Sort the row numbers.
        Collections.sort(rows, comparator);
        return rows;
    }
}
