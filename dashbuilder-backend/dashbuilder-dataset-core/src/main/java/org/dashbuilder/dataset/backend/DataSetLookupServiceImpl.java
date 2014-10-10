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
package org.dashbuilder.dataset.backend;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.dashbuilder.dataset.DataSetManager;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.DataSetLookupService;
import org.dashbuilder.dataset.backend.BackendDataSetManager;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;

/**
 * Data set lookup service implementation
 */
@ApplicationScoped
@Service
public class DataSetLookupServiceImpl implements DataSetLookupService {

    @Inject BackendDataSetManager dataSetManager;
    @Inject DataSetDefDeployer dataSetDefDeployer;

    @PostConstruct
    private void init() {
        ServletContext servletContext = RpcContext.getHttpSession().getServletContext();
        if (!dataSetDefDeployer.isRunning() && servletContext != null) {
            String dir = servletContext.getRealPath("datasets");
            if (dir != null) {
                dir = dir.replaceAll("\\\\", "/");
                dataSetDefDeployer.deploy(dir);
            }
        }
    }

    /**
     * Apply a sequence of operations (filter, sort, group, ...) on a remote and get the resulting data set.
     */
    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        return dataSetManager.lookupDataSet(lookup);
    }

    /**
     * Same as lookupDataSet but only retrieves the metadata of the resulting data set.
     * @return A DataSetMetadata instance containing general information about the data set.
     */
    public DataSetMetadata lookupDataSetMetadata(DataSetLookup lookup) throws Exception {
        return dataSetManager.lookupDataSetMetadata(lookup);
    }
}
