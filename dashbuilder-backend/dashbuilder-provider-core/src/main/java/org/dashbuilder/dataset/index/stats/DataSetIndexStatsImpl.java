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
package org.dashbuilder.dataset.index.stats;

import java.util.List;

import org.dashbuilder.dataset.index.DataSetIndex;
import org.dashbuilder.dataset.index.DataSetIndexElement;
import org.dashbuilder.dataset.index.DataSetIndexNode;
import org.dashbuilder.dataset.index.visitor.DataSetIndexVisitor;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;

/**
 * A DataSetIndex stats
 */
public class DataSetIndexStatsImpl implements DataSetIndexStats, DataSetIndexVisitor {

    private DataSetIndex index;
    private transient long buildTime;
    private transient long reuseTime;
    private transient long indexSize;
    private transient DataSetIndexElement longestBuild;
    private transient DataSetIndexElement shortestBuild;
    private transient DataSetIndexElement lessReused;
    private transient DataSetIndexElement mostReused;

    public DataSetIndexStatsImpl(DataSetIndex index) {
        this.index = index;
        index.acceptVisitor(this);
    }

    public void visit(DataSetIndexElement element) {
        buildTime += element.getBuildTime();
        reuseTime += element.getReuseTime();
        indexSize += element.getEstimatedSize();

        if (longestBuild == null || element.getBuildTime() > longestBuild.getBuildTime()) {
            longestBuild = element;
        }
        if (shortestBuild == null || element.getBuildTime() > shortestBuild.getBuildTime()) {
            shortestBuild = element;
        }
        if (lessReused == null || element.getReuseHits() > lessReused.getReuseHits()) {
            lessReused = element;
        }
        if (mostReused == null || element.getReuseHits() > mostReused.getReuseHits()) {
            mostReused = element;
        }
    }

    public double getReuseRate() {
        if (buildTime == 0) return 0;
        return reuseTime/buildTime;
    }

    public long getBuildTime() {
        return buildTime;
    }

    public long getReuseTime() {
        return reuseTime;
    }

    public DataSetIndexElement getLongestBuild() {
        return longestBuild;
    }

    public DataSetIndexElement getShortestBuild() {
        return shortestBuild;
    }

    public DataSetIndexElement getLessReused() {
        return lessReused;
    }

    public DataSetIndexElement getMostReused() {
        return mostReused;
    }

    public long getIndexSize() {
        return indexSize;
    }

    public long getDataSetSize() {
        DataSet dataSet = index.getDataSet();
        int nrows = dataSet.getRowCount();
        if (nrows == 0) return 0;

        List<DataColumn> columns = dataSet.getColumns();
        int ncells = nrows * columns.size();
        int result = ncells * 4;
        for (int i = 0; i < columns.size(); i++) {
            Object firstRowValue = dataSet.getValueAt(0, i);
            if (firstRowValue instanceof String) {
                for (int j = 0; j < nrows; j++) {
                    String stringValue = (String) dataSet.getValueAt(j, i);
                    result += SizeEstimator.sizeOfString(stringValue);
                }
            } else {
                int singleValueSize = SizeEstimator.sizeOf(firstRowValue);
                result += nrows * singleValueSize;
            }
        }
        return result;
    }
}

