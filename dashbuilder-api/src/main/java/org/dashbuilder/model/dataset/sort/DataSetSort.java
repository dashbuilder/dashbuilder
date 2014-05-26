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
package org.dashbuilder.model.dataset.sort;

import java.util.List;
import java.util.ArrayList;

import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A data set sort operation definition
 */
@Portable
public class DataSetSort implements DataSetOp {

    protected List<ColumnSort> columnSortList = new ArrayList<ColumnSort>();

    public DataSetOpType getType() {
        return DataSetOpType.SORT;
    }

    public void addSortColumn(ColumnSort... columnSorts) {
        for (ColumnSort columnSort : columnSorts) {
            columnSortList.add(columnSort);
        }
    }

    public List<ColumnSort> getColumnSortList() {
        return columnSortList;
    }

    /**
     * Clone this sort operation.
     */
    public DataSetSort cloneSortOp() {
        DataSetSort newSortOp = new DataSetSort();
        for (ColumnSort columnSort : getColumnSortList()) {
            ColumnSort newColumnSort = new ColumnSort();
            newColumnSort.setColumnId(columnSort.getColumnId());
            newColumnSort.setOrder(columnSort.getOrder());
            newSortOp.addSortColumn(newColumnSort);
        }
        return newSortOp;
    }

    /**
     * Invert the sort order if this sort operation.
     */
    public DataSetSort invertOrder() {
        for (ColumnSort columnSort : getColumnSortList()) {
            SortOrder order = columnSort.getOrder();
            if (SortOrder.ASCENDING.equals(order)) columnSort.setOrder(SortOrder.DESCENDING);
            else if (SortOrder.DESCENDING.equals(order)) columnSort.setOrder(SortOrder.ASCENDING);
        }
        return this;
    }

    public boolean equals(Object obj) {
        try {
            DataSetSort other = (DataSetSort) obj;
            if (columnSortList.size() != other.columnSortList.size()) return false;
            for (int i = 0; i < columnSortList.size(); i++) {
                ColumnSort el = columnSortList.get(i);
                ColumnSort otherEl = other.columnSortList.get(i);
                if (!el.equals(otherEl)) return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
