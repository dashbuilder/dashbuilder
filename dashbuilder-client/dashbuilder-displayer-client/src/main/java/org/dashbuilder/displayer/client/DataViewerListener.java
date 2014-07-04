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
package org.dashbuilder.displayer.client;

import java.util.List;

import org.dashbuilder.dataset.group.DataSetGroup;

/**
 * Interface addressed to capture events coming from a DataViewer instance.
 */
public interface DataViewerListener {

    /**
     * Invoked when a group interval selection filter request is executed on a given DataViewer instance.
     *
     * @param viewer The DataViewer instance where the interval selection event comes from.
     * @param groupOp The group interval selection operation.
     */
    void onGroupIntervalsSelected(DataViewer viewer, DataSetGroup groupOp);

    /**
     * Invoked when a group interval reset request is executed on a given DataViewer instance.
     *
     * @param viewer The DataViewer instance where the interval selection event comes from.
     * @param groupOps The set of group interval selection operations reset.
     */
    void onGroupIntervalsReset(DataViewer viewer, List<DataSetGroup> groupOps);
}