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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.events.DataSetModifiedEvent;
import org.dashbuilder.displayer.DisplayerSettings;
import org.uberfire.client.workbench.events.PerspectiveChange;

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

    /**
     * Flag indicating if the perspective is on edit mode.
     */
    boolean editOn = false;

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
     * Turn on the edition of the perspective
     */
    public void editOn() {
        editOn = true;

        // Turns off the automatic refresh of all the displayers.
        for (Displayer displayer : coordinator.getDisplayerList()) {
            displayer.refreshOff();
        }
    }

    /**
     * Turn off the edition of the perspective
     */
    public void editOff() {
        editOn = false;

        // Turns on the automatic refresh of all the displayers.
        for (Displayer displayer : coordinator.getDisplayerList()) {
            displayer.refreshOn();
        }
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
        if (editOn) return;

        String targetUUID = event.getDataSetDef().getUUID();
        for (Displayer displayer : coordinator.getDisplayerList()) {

            // If a displayer is handling the refresh by itself then do nothing.
            if (displayer.isRefreshOn()) {
                continue;
            }

            String uuid = null;
            DisplayerSettings settings = displayer.getDisplayerSettings();
            DataSet dataSet = settings.getDataSet();
            if (dataSet != null) {
                uuid = dataSet.getUUID();
            }
            DataSetLookup dataSetLookup = settings.getDataSetLookup();
            if (uuid == null && dataSetLookup != null) {
                uuid = dataSetLookup.getDataSetUUID();
            }
            if (uuid != null && targetUUID.equals(uuid)) {
                displayer.redraw();
            }
        }
    }
}
