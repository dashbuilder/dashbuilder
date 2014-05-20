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

import org.dashbuilder.dataset.index.DataSetGroupIndex;
import org.dashbuilder.dataset.index.DataSetIndex;
import org.dashbuilder.dataset.index.DataSetIndexElement;
import org.dashbuilder.dataset.index.DataSetScalarIndex;
import org.dashbuilder.dataset.index.DataSetSortIndex;
import org.dashbuilder.dataset.index.visitor.DataSetIndexVisitor;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.DataSet;

/**
 * A DataSetIndex stats
 */
public class DataSetIndexStatsImpl implements DataSetIndexStats, DataSetIndexVisitor {

    private DataSetIndex index;
    private transient long buildTime = 0;
    private transient long reuseTime = 0;
    private transient long indexSize = 0;
    private transient int numberOfGroupOps = 0;
    private transient int numberOfSortOps = 0;
    private transient int numberOfScalarOps = 0;
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

        if (element instanceof DataSetGroupIndex) {
            numberOfGroupOps++;
        }
        if (element instanceof DataSetSortIndex) {
            numberOfSortOps++;
        }
        if (element instanceof DataSetScalarIndex) {
            numberOfScalarOps++;
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

    public int getNumberOfGroupOps() {
        return numberOfGroupOps;
    }

    public int getNumberOfSortOps() {
        return numberOfSortOps;
    }

    public int getNumberOfScalarOps() {
        return numberOfScalarOps;
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

    public String toString() {
        return toString(" ");
    }

    public String toString(String sep) {
        StringBuilder out = new StringBuilder();
        out.append("Data set size=").append(SizeEstimator.formatSize(getDataSetSize())).append(sep);
        out.append("Index size=").append(SizeEstimator.formatSize(getIndexSize())).append(sep);
        out.append("Build time=").append(((double) getBuildTime() / 1000000)).append(" (secs)").append(sep);
        out.append("Reuse time=").append(((double) getReuseTime() / 1000000)).append(" (secs)").append(sep);
        out.append("Reuse rate=").append(getReuseRate()).append(sep);
        out.append("#Group ops=").append(getNumberOfGroupOps()).append(sep);
        out.append("#Sort ops=").append(getNumberOfSortOps()).append(sep);
        out.append("#Scalar ops=").append(getNumberOfScalarOps()).append(sep);
        return out.toString();
    }
}

