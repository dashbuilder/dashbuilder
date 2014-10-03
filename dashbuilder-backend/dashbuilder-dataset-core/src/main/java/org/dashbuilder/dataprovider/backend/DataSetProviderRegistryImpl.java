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
package org.dashbuilder.dataprovider.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * Data provider setting backend registry
 */
@ApplicationScoped
public class DataSetProviderRegistryImpl implements DataSetProviderRegistry {

    @Inject
    private Instance<DataSetProvider> dataSetProviders;

    private Map<DataSetProviderType,DataSetProvider> dataSetProviderMap = new HashMap<DataSetProviderType,DataSetProvider>();
    private List<DataSetProviderType> availableTypes = new ArrayList<DataSetProviderType>();

    @PostConstruct
    private void init() {
        for (DataSetProvider dataProvider : dataSetProviders) {
            DataSetProviderType type = dataProvider.getType();
            dataSetProviderMap.put(type, dataProvider);
            availableTypes.add(type);
        }
    }

    public DataSetProvider getDataSetProvider(DataSetProviderType type) {
        return dataSetProviderMap.get(type);
    }

    public List<DataSetProviderType> getAvailableTypes() {
        return availableTypes;
    }
}
