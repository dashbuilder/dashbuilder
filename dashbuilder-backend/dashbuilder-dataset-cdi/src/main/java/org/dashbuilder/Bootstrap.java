/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import org.dashbuilder.dataprovider.BeanDataSetProviderCDI;
import org.dashbuilder.dataprovider.DataSetProviderRegistryCDI;
import org.dashbuilder.dataprovider.StaticDataSetProviderCDI;
import org.dashbuilder.dataset.DataSetDefDeployerCDI;
import org.dashbuilder.dataset.DataSetDefRegistryCDI;
import org.dashbuilder.dataset.DataSetManagerCDI;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.scheduler.SchedulerCDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.services.cdi.Startup;

/**
 * Class in charge of bootstrapping the core subsystems using CDI
 */
@ApplicationScoped
@Startup
public class Bootstrap {

    protected static Logger log = LoggerFactory.getLogger(Bootstrap.class);

    @Inject
    protected SchedulerCDI scheduler;

    @Inject
    protected StaticDataSetProviderCDI staticDataSetProvider;

    @Inject
    protected BeanDataSetProviderCDI beanDataSetProvider;

    @Inject
    protected DataSetProviderRegistryCDI providerRegistry;

    @Inject
    protected DataSetDefRegistryCDI dataSetDefRegistry;

    @Inject
    protected DataSetDefDeployerCDI dataSetDefDeployer;

    @Inject
    protected DataSetManagerCDI dataSetManager;

    @PostConstruct
    protected void init() {
        DataSetCore dataSetCore = DataSetCore.get();
        dataSetCore.setScheduler(scheduler);
        dataSetCore.setStaticDataSetProvider(staticDataSetProvider);
        dataSetCore.setBeanDataSetProvider(beanDataSetProvider);
        dataSetCore.setDataSetDefRegistry(dataSetDefRegistry);
        dataSetCore.setDataSetDefDeployer(dataSetDefDeployer);
        dataSetCore.setDataSetProviderRegistry(providerRegistry);
        dataSetCore.setDataSetManager(dataSetManager);

        log.info("Core subsystems initialized");
    }
}
