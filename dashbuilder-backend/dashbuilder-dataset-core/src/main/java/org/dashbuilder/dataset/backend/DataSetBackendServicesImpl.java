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

import java.io.File;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.dashbuilder.dataset.DataSetBackendServices;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;

/**
 * Data set backend services implementation
 */
@ApplicationScoped
@Service
public class DataSetBackendServicesImpl implements DataSetBackendServices {

    @Inject BackendDataSetManager dataSetManager;
    @Inject DataSetDefDeployer dataSetDefDeployer;
    @Inject DataSetDefRegistry dataSetDefRegistry;

    @PostConstruct
    private void init() {
        ServletContext servletContext = RpcContext.getHttpSession().getServletContext();
        if (!dataSetDefDeployer.isRunning() && servletContext != null) {
            String dir = servletContext.getRealPath("datasets");
            if (dir != null && new File(dir).exists()) {
                dir = dir.replaceAll("\\\\", "/");
                dataSetDefDeployer.deploy(dir);
            }
        }
    }

    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        return dataSetManager.lookupDataSet(lookup);
    }

    public DataSetMetadata lookupDataSetMetadata(DataSetLookup lookup) throws Exception {
        return dataSetManager.lookupDataSetMetadata(lookup);
    }

    public List<DataSetDef> getSharedDataSetDefs() {
        return dataSetDefRegistry.getSharedDataSetDefs();
    }
}
