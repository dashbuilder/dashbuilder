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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dashbuilder.dataset.group.DataSetGroup;

/**
 * The coordinator class holds a list of Displayer instances and it makes sure that the data shared among
 * all of them is properly synced. This means every time a data display modification request comes from any
 * of the displayer components the rest are updated to reflect those changes.
 */
public class DataViewerCoordinator {

    protected List<Displayer> displayerList = new ArrayList<Displayer>();
    protected Map<RendererLibrary,List<Displayer>> rendererMap = new HashMap<RendererLibrary,List<Displayer>>();
    protected DisplayerListener displayerListener = new CoordinatorListener();

    public void addViewer(Displayer displayer) {
        displayerList.add(displayer);
        displayer.addListener(displayerListener);

        RendererLibrary renderer = RendererLibLocator.get().lookupRenderer(displayer.getDisplayerSettings());
        List<Displayer> rendererGroup = rendererMap.get(renderer);
        if (rendererGroup == null) rendererMap.put(renderer, rendererGroup = new ArrayList<Displayer>());
        rendererGroup.add(displayer);
    }

    public void drawAll() {
        for (RendererLibrary renderer : rendererMap.keySet()) {
            List<Displayer> rendererGroup = rendererMap.get(renderer);
            renderer.draw(rendererGroup);
        }
    }

    public void redrawAll() {
        for (RendererLibrary renderer : rendererMap.keySet()) {
            List<Displayer> rendererGroup = rendererMap.get(renderer);
            renderer.redraw(rendererGroup);
        }
    }

    /**
     * Internal class that listens to events raised by any of the Displayer instances handled by this coordinator.
     */
    private class CoordinatorListener implements DisplayerListener {

        public void onGroupIntervalsSelected(Displayer displayer, DataSetGroup groupOp) {
            for (Displayer other : displayerList) {
                if (other == displayer) continue;
                other.onGroupIntervalsSelected(displayer, groupOp);
            }
        }

        public void onGroupIntervalsReset(Displayer displayer, List<DataSetGroup> groupOps) {
            for (Displayer other : displayerList) {
                if (other == displayer) continue;
                other.onGroupIntervalsReset(displayer, groupOps);
            }
        }
    }
}