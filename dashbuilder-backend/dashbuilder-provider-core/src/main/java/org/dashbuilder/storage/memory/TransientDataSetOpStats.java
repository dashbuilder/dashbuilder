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

import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.DataSetOpStats;

/**
 * DataSetOp statistics for the TransientDataSetStorage implementation.
 */
public class TransientDataSetOpStats implements DataSetOpStats {

    int numberOfOps = 0;
    int reuseHits = 0;
    long averageTime = 0;
    DataSetHolder longestOp = null;
    DataSetHolder shortestOp = null;

    void update(DataSetHolder op) {
        averageTime = (averageTime*numberOfOps + op.buildTime) / ++numberOfOps;
        if (longestOp == null || op.buildTime > longestOp.buildTime) longestOp = op;
        if (shortestOp == null || op.buildTime <= shortestOp.buildTime) shortestOp = op;
    }

    public int getNumberOfOps() {
        return numberOfOps;
    }

    public int getReuseHits() {
        return reuseHits;
    }

    public long getAverageTime() {
        return averageTime;
    }

    public long getLongestTime() {
        if (longestOp == null) return 0;
        return longestOp.buildTime;
    }

    public long getShortestTime() {
        if (shortestOp == null) return 0;
        return shortestOp.buildTime;
    }

    public DataSetOp getLongestOp() {
        if (longestOp == null) return null;
        return longestOp.op;
    }

    public DataSetOp getShortestOp() {
        if (shortestOp == null) return null;
        return shortestOp.op;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Hit=").append(reuseHits).append(" ");
        str.append("Run=").append(numberOfOps).append(" ");
        str.append("Min=").append(getShortestTime()).append(" ms ");
        str.append("Max=").append(getLongestTime()).append(" ms ");
        str.append("Avg=").append(getAverageTime()).append(" ms");
        return str.toString();
    }
}
