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
package org.dashbuilder.model.dataset;

/**
 * Data set performance stats.
 */
public interface DataSetStats {

    /**
     * Time required to "build" (load, create, filter, ...) the data set.
     */
    long getBuildTime();

    /**
     * Number of times the data set has been reused.
     */
    int getReuseHits();

    /**
     * Operation summary stats.
     */
    DataSetOpStats getOpStats(DataSetOpType type);
}
