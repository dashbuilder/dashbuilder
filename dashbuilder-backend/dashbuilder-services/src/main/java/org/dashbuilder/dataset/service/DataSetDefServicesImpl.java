/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataset.service;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.DataSetCore;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSetDefRegistryCDI;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.uuid.UUIDGenerator;
import org.dashbuilder.exception.ExceptionManager;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Service
public class DataSetDefServicesImpl implements DataSetDefServices {

    protected static Logger log = LoggerFactory.getLogger(DataSetDefServicesImpl.class);
    protected User identity;
    protected ExceptionManager exceptionManager;
    protected UUIDGenerator uuidGenerator;
    protected DataSetDefRegistryCDI dataSetDefRegistry;

    public DataSetDefServicesImpl() {
    }

    @Inject
    public DataSetDefServicesImpl(User identity,
                                  ExceptionManager exceptionManager,
                                  DataSetDefRegistryCDI dataSetDefRegistry) {
        this.identity = identity;
        this.uuidGenerator = DataSetCore.get().getUuidGenerator();
        this.dataSetDefRegistry = dataSetDefRegistry;
        this.exceptionManager = exceptionManager;
    }

    @Override
    public List<DataSetDef> getPublicDataSetDefs() {
        return dataSetDefRegistry.getDataSetDefs(true);
    }

    @Override
    public DataSetDef createDataSetDef(DataSetProviderType type) {
        DataSetDef result = new DataSetDef();
        if (type != null) result = DataSetProviderType.createDataSetDef(type);
        result.setUUID(uuidGenerator.newUuid());
        return result;
    }

    @Override
    public String registerDataSetDef(DataSetDef definition, String comment) {
        // Data sets registered from the UI does not contain a UUID.
        if (definition.getUUID() == null) {
            final String uuid = uuidGenerator.newUuid();
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
