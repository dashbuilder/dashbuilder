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

    protected List<SortColumn> sortColumnList = new ArrayList<SortColumn>();

    public DataSetOpType getType() {
        return DataSetOpType.SORT;
    }

    public void addSortColumn(SortColumn... sortColumns) {
        for (SortColumn sortColumn : sortColumns) {
            sortColumnList.add(sortColumn);
        }
    }

    public List<SortColumn> getSortColumnList() {
        return sortColumnList;
    }

    public boolean equals(Object obj) {
        try {
            DataSetSort other = (DataSetSort) obj;
            if (sortColumnList.size() != other.sortColumnList.size()) return false;
            for (int i = 0; i < sortColumnList.size(); i++) {
                SortColumn el = sortColumnList.get(i);
                SortColumn otherEl = other.sortColumnList.get(i);
                if (!el.equals(otherEl)) return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
