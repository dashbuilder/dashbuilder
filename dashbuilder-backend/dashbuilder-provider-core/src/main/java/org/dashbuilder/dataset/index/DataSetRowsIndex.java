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
package org.dashbuilder.dataset.index;

import java.util.List;

import org.dashbuilder.dataset.index.stats.SizeEstimator;

/**
 * A DataSet index node holding a row sub set.
 */
public class DataSetRowsIndex extends DataSetIndexNode {

    List<Integer> rows = null;

    DataSetRowsIndex(DataSetIndexNode parent, List<Integer> rows) {
        super(parent, 0);
        this.rows = rows;
    }

    DataSetRowsIndex(List<Integer> rows) {
        this(null, rows);
    }

    public List<Integer> getRows() {
        return rows;
    }

    public long getEstimatedSize() {
        long result = super.getEstimatedSize();
        if (rows != null) {
            result += rows.size() * SizeEstimator.sizeOfInteger;
        }
        return result;
    }
}

