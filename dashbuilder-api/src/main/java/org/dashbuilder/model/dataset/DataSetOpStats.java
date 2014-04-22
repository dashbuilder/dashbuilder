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

/**
 * Data set operation statistics.
 */
public interface DataSetOpStats {

    /**
     * Number of operations performed on the data set.
     */
    int getNumberOfOps();

    /**
     * Number of times the data set op has been reused.
     */
    int getReuseHits();

    /**
     * Average time spent by all the operations applied on the data set.
     */
    long getAverageTime();

    /**
     * Time spent by the longest operation.
     */
    long getLongestTime();

    /**
     * Time spent by the shortest operation.
     */
    long getShortestTime();

    /**
     * A reference to the longest operation.
     */
    DataSetOp getLongestOp();

    /**
     * A reference to the shortest operation.
     */
    DataSetOp getShortestOp();
}
