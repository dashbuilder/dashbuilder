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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dashbuilder.model.dataset.group.DataSetGroup;

/**
 * The coordinator class holds a list of DataViewer instances and it makes sure that the data shared among
 * all of them is properly synced. This means every time a data display modification request comes from any
 * of the viewer components the rest are updated to reflect those changes.
 */
public class DataViewerCoordinator {

    protected List<DataViewer> viewerList = new ArrayList<DataViewer>();
    protected Map<RendererLibrary,List<DataViewer>> rendererMap = new HashMap<RendererLibrary,List<DataViewer>>();
    protected DataViewerListener viewerListener = new CoordinatorListener();

    public void addViewer(DataViewer viewer) {
        viewerList.add(viewer);
        viewer.addListener(viewerListener);

        RendererLibrary renderer = RendererLibLocator.get().lookupRenderer(viewer.getDataDisplayer());
        List<DataViewer> rendererGroup = rendererMap.get(renderer);
        if (rendererGroup == null) rendererMap.put(renderer, rendererGroup = new ArrayList<DataViewer>());
        rendererGroup.add(viewer);
    }

    public void drawAll() {
        for (RendererLibrary renderer : rendererMap.keySet()) {
            List<DataViewer> rendererGroup = rendererMap.get(renderer);
            renderer.draw(rendererGroup);
        }
    }

    public void redrawAll() {
        for (RendererLibrary renderer : rendererMap.keySet()) {
            List<DataViewer> rendererGroup = rendererMap.get(renderer);
            renderer.redraw(rendererGroup);
        }
    }

    /**
     * Internal class that listens to events raised by any of the DataViewer instances handled by this coordinator.
     */
    private class CoordinatorListener implements DataViewerListener {

        public void onGroupIntervalsSelected(DataViewer viewer, DataSetGroup groupOp) {
            for (DataViewer other : viewerList) {
                if (other == viewer) continue;
                other.onGroupIntervalsSelected(viewer, groupOp);
            }
        }

        public void onGroupIntervalsReset(DataViewer viewer, DataSetGroup groupOp) {
            for (DataViewer other : viewerList) {
                if (other == viewer) continue;
                other.onGroupIntervalsReset(viewer, groupOp);
            }
        }
    }
}