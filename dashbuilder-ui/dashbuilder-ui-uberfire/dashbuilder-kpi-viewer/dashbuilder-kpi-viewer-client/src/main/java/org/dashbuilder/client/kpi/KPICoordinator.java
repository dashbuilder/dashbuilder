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
package org.dashbuilder.client.kpi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.dashbuilder.client.displayer.DataViewerCoordinator;
import org.uberfire.client.workbench.events.PerspectiveChange;

/**
 * It holds the set of KPIViewer instances being displayed on the current perspective.
 * <p>It also makes sure those instances are properly synced to reflect the data set manipulation requests
 * issued by any KPIViewer on the dashboard.</p>
 */
@ApplicationScoped
public class KPICoordinator {

    /**
     * A DataViewer coordinator for all the KPIViewer instances placed on the same perspective.
     */
    private DataViewerCoordinator coordinator;

    @PostConstruct
    public void init() {
        coordinator = new DataViewerCoordinator();
    }

    /**
     * Add a KPIViewer instance to the dashboard context.
     */
    public void addKPIViewer(KPIViewer kpiViewer) {
        coordinator.addViewer(kpiViewer.getDataViewer());
    }

    /**
     * Reset the coordinator every time we switch from perspective.
     */
    private void onPerspectiveChanged(@Observes final PerspectiveChange event) {
        init();
    }
}
