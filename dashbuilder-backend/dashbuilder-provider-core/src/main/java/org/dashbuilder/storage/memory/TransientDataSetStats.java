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
package org.dashbuilder.storage.memory;

import java.util.List;

import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpStats;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.DataSetStats;

/**
 * DataSet statistics for the TransientDataSetStorage implementation.
 */
public class TransientDataSetStats implements DataSetStats {

    private DataSetHolder holder;
    private SizeEstimator sizeEstimator;

    TransientDataSetStats(DataSetHolder holder, SizeEstimator sizeEstimator) {
        this.holder = holder;
        this.sizeEstimator = sizeEstimator;
    }

    public long getBuildTime() {
        return holder.buildTime;
    }

    public int getReuseHits() {
        return holder.reuseHits;
    }

    public DataSetOpStats getOpStats(DataSetOpType type) {
        return holder.opStats.get(type);
    }

    public int sizeOf() {
        int nrows = holder.dataSet.getRowCount();
        if (nrows == 0) return 0;

        List<DataColumn> columns = holder.dataSet.getColumns();
        int ncells = nrows * columns.size();
        int result = ncells * 4;
        for (int i = 0; i < columns.size(); i++) {
            Object firstRowValue = holder.dataSet.getValueAt(0, i);
            if (firstRowValue instanceof String) {
                for (int j = 0; j < nrows; j++) {
                    String stringValue = (String) holder.dataSet.getValueAt(j, i);
                    result += sizeEstimator.sizeOfString(stringValue);
                }
            } else {
                int singleValueSize = sizeEstimator.sizeOf(firstRowValue);
                result += nrows * singleValueSize;
            }
        }
        return result;
    }
}
