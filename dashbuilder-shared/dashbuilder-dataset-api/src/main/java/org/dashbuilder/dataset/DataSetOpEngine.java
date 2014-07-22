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
package org.dashbuilder.dataset;

import java.util.List;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetOp;

/**
 * Provides several operations for data set manipulation.
 */
public interface DataSetOpEngine {

    /**
     * Apply a sequence of operations on the specified data set.
     *
     * @param dataSet The target data set.
     * @param opList The list of operations.
     * @return A brand new data set reflecting all the operations.
     */
    DataSet execute(DataSet dataSet, List<DataSetOp> opList);
    DataSet execute(DataSet dataSet, DataSetOp... opList);
}
