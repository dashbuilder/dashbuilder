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
package org.dashbuilder.client.displayer;

import java.util.List;

import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.sort.SortOrder;

/**
 * Interface addressed to capture events coming from a DataViewer instance.
 */
public interface DataViewerListener {

    void onGroupIntervalsSelected(DataViewer viewer, DataSetGroup groupOp);
    void onGroupIntervalsReset(DataViewer viewer, List<DataSetGroup> groupOps);
}