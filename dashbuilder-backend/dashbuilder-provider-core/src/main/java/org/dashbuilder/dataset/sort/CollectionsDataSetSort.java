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
package org.dashbuilder.dataset.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.sort.DataSetSortAlgorithm;
import org.dashbuilder.model.dataset.sort.ColumnSort;

/**
 * A basic sort algorithm takes relies on the default <tt>Collections.sort()</tt> implementation.
 */
@ApplicationScoped
public class CollectionsDataSetSort implements DataSetSortAlgorithm {


    public List<Integer> sort(DataSet dataSet, List<ColumnSort> columnSortList) {
        // Create the comparator.
        DataSetRowComparator comparator = new DataSetRowComparator();
        for (ColumnSort columnSort : columnSortList) {
            DataColumn column = dataSet.getColumnById(columnSort.getColumnId());
            comparator.criteria(column, columnSort.getOrder());
        }
        // Create the row number list to sort.
        List<Integer> rows = new ArrayList<Integer>();
        for (int i=0; i<dataSet.getRowCount(); i++) {
            rows.add(i);
        }
        // Sort the row numbers.
        Collections.sort(rows, comparator);
        return rows;
    }
}
