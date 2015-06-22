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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.backend.exception.ExceptionManager;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.exception.DataSetLookupException;
import org.dashbuilder.dataset.service.DataSetDefServices;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;
import org.uberfire.backend.vfs.Path;
import org.uberfire.rpc.SessionInfo;

@ApplicationScoped
@Service
public class DataSetDefServicesImpl implements DataSetDefServices {

    @Inject
    protected Logger log;

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    protected BackendDataSetManager dataSetManager;

    @Inject
    protected BackendUUIDGenerator backendUUIDGenerator;

    @Inject
    protected ExceptionManager exceptionManager;

    @Inject
    protected User identity;

    @Inject
    protected DataSetDefDeployer dataSetDefDeployer;

    @PostConstruct
    protected void init() {
        // By default, enable the register of data set definitions stored into the deployment folder.
        ServletContext servletContext = RpcContext.getHttpSession().getServletContext();
        if (!dataSetDefDeployer.isRunning() && servletContext != null) {
            String dir = servletContext.getRealPath("WEB-INF/datasets");
            if (dir != null && new File(dir).exists()) {
                dir = dir.replaceAll("\\\\", "/");
                dataSetDefDeployer.deploy(dir);
            }
        }
    }

    @Override
    public List<DataSetDef> getPublicDataSetDefs() {
        return dataSetDefRegistry.getDataSetDefs(true);
    }

    @Override
    public DataSetDef createDataSetDef(DataSetProviderType type) {
        DataSetDef result = new DataSetDef();
        if (type != null) result = DataSetProviderType.createDataSetDef(type);
        result.setUUID(backendUUIDGenerator.newUuid());
        return result;
    }

    @Override
    public String registerDataSetDef(DataSetDef definition, String comment) {
        // Data sets registered from the UI does not contain a UUID.
        if (definition.getUUID() == null) {
            final String uuid = backendUUIDGenerator.newUuid();
            definition.setUUID(uuid);
        }
        dataSetDefRegistry.registerDataSetDef(definition,
                identity != null ? identity.getIdentifier() : "---",
                comment);
        return definition.getUUID();
    }

    @Override
    public void removeDataSetDef(final String uuid, String comment) {
        final DataSetDef def = dataSetDefRegistry.getDataSetDef(uuid);
        if (def != null) {
            dataSetDefRegistry.removeDataSetDef(uuid,
                identity != null ? identity.getIdentifier() : "---",
                comment);
        }
    }
}
