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
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.dashbuilder.dataset.*;
import org.dashbuilder.dataset.exception.DataSetLookupException;
import org.dashbuilder.dataset.backend.exception.ExceptionManager;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.service.DataSetLookupServices;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.slf4j.Logger;

/**
 * Data set backend services implementation
 */
@ApplicationScoped
@Service
public class DataSetLookupServicesImpl implements DataSetLookupServices {

    @Inject
    protected Logger log;

    @Inject
    protected ExceptionManager exceptionManager;
    
    @Inject
    protected BackendDataSetManager dataSetManager;

    @Inject
    protected BackendUUIDGenerator backendUUIDGenerator;

    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        DataSet _d = null;
        try {
            _d = dataSetManager.lookupDataSet(lookup);
        } catch (DataSetLookupException e) {
            throw exceptionManager.handleException(e);
        }
        
        return _d;
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        try {
            // Although if using a not registered definition, it must have an uuid set for performing lookups.
            if (def.getUUID() == null) {
                final String uuid = backendUUIDGenerator.newUuid();
                def.setUUID(uuid);
                lookup.setDataSetUUID(uuid);
            }
            return dataSetManager.resolveProvider(def)
                    .lookupDataSet(def, lookup);
        } catch (Exception e) {
            throw exceptionManager.handleException(e);
        }
    }

    public DataSetMetadata lookupDataSetMetadata(String uuid) throws Exception {
        DataSetMetadata _d = null;
        try {
            _d = dataSetManager.getDataSetMetadata(uuid);
        } catch (DataSetLookupException e) {
            throw exceptionManager.handleException(e);
        }

        return _d;
    }
}
