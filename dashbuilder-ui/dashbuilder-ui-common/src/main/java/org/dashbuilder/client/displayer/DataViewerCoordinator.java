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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dashbuilder.model.dataset.sort.SortOrder;

/**
 * The coordinator class holds a list of DataViewer. The coordinator makes sure that the data shared among
 * all the viewers is properly synced. This means every time a data display modification request comes from any
 * of the viewer components the remain viewers are updated to reflect the changes.
 */
public class DataViewerCoordinator implements DataViewerListener {

    List<DataViewer> viewerList = new ArrayList<DataViewer>();

    public void addViewer(DataViewer viewer) {
        viewerList.add(viewer);
        viewer.addListener(this);

    }

    public void onIntervalsSelected(DataViewer viewer, String columnId, Collection<String> intervalNames) {
        for (DataViewer other : viewerList) {
            if (other == viewer) continue;
            viewer.onIntervalsSelected(viewer, columnId, intervalNames);
        }
    }

    public void onColumnSorted(DataViewer viewer, String columnId, SortOrder order) {
        for (DataViewer other : viewerList) {
            if (other == viewer) continue;
            viewer.onColumnSorted(viewer, columnId, order);
        }
    }

    public void onColumnFiltered(DataViewer viewer, String columnId, Collection<Comparable> allowedValues) {
        for (DataViewer other : viewerList) {
            if (other == viewer) continue;
            viewer.onColumnFiltered(viewer, columnId, allowedValues);
        }
    }

    public void onColumnFiltered(DataViewer viewer, String columnId, Comparable lowValue, Comparable highValue) {
        for (DataViewer other : viewerList) {
            if (other == viewer) continue;
            viewer.onColumnFiltered(viewer, columnId, lowValue, highValue);
        }
    }
}