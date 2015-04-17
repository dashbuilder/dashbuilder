/**
 * Copyright (C) 2015 JBoss Inc
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
package org.dashbuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.backend.DataSetDefDeployer;
import org.dashbuilder.scheduler.Scheduler;
import org.slf4j.Logger;
import org.uberfire.commons.services.cdi.Startup;

/**
 *
 */
@ApplicationScoped
@Startup
public class Bootstrap {

    @Inject
    private Logger log;

    @Inject
    private Scheduler scheduler;

    @Inject
    private DataSetDefDeployer datasetDeployer;

    @PostConstruct
    private void init() {
        log.info("Dashbuilder initialized");
    }
}
