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

import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.events.DataSetModifiedEvent;
import org.uberfire.client.workbench.events.PerspectiveChange;

import static org.uberfire.commons.validation.PortablePreconditions.*;

/**
 * It holds the set of Displayer instances being displayed on the current perspective.
 * <p>It also makes sure those instances are properly synced to reflect the data set manipulation requests
 * issued by any Displayer on the dashboard.</p>
 */
@ApplicationScoped
public class PerspectiveCoordinator {

    /**
     * The real coordinator.
     */
    private DisplayerCoordinator coordinator;

    @PostConstruct
    public void init() {
        coordinator = new DisplayerCoordinator();
    }

    /**
     * Adds a Displayer instance to the current perspective context.
     */
    public void addDisplayer(Displayer displayer) {
        coordinator.addDisplayer(displayer);
    }

    /**
     * Removes a Displayer instance from the current perspective context.
     */
    public boolean removeDisplayer(Displayer displayer) {
        return coordinator.removeDisplayer(displayer);
    }

    /**
     * Reset the coordinator every time the perspective is changed.
     */
    private void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        init();
    }

    /**
     * Listen to modifications on any of the data set being used in this perspective.
     */
    private void onDataSetModifiedEvent(@Observes DataSetModifiedEvent event) {
        checkNotNull("event", event);

        String targetUUID = event.getDataSetUUID();
        for (Displayer displayer : coordinator.getDisplayerList()) {

            String uuid = null;
            DataSet dataSet = displayer.getDisplayerSettings().getDataSet();
            if (dataSet != null) {
                uuid = dataSet.getUUID();
            }
            DataSetLookup dataSetLookup = displayer.getDisplayerSettings().getDataSetLookup();
            if (uuid == null && dataSetLookup != null) {
                uuid = dataSetLookup.getDataSetUUID();
            }
            if (uuid != null && targetUUID.equals(uuid)) {
                displayer.redraw();
            }
        }
    }
}
