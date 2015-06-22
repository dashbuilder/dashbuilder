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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetGenerator;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.events.DataSetDefRemovedEvent;
import org.dashbuilder.dataset.events.DataSetStaleEvent;
import org.slf4j.Logger;

@ApplicationScoped
@Named("bean")
public class BeanDataSetProvider implements DataSetProvider {

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    protected BeanManager beanManager;

    protected Map<String,DataSetGenerator> generatorMap = new HashMap<String, DataSetGenerator>();

    @Inject
    protected Logger log;

    @PostConstruct
    protected void init() {
        Set<Bean<?>> beans = beanManager.getBeans(DataSetGenerator.class);
        for (Bean<?> bean : beans) {
            CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
            DataSetGenerator generator = (DataSetGenerator) beanManager.getReference(bean, DataSetGenerator.class, ctx);
            generatorMap.put(bean.getBeanClass().getName(), generator);
        }
    }

    public DataSetProviderType getType() {
        return DataSetProviderType.BEAN;
    }

    public DataSetMetadata getDataSetMetadata(DataSetDef def) throws Exception {
        DataSet dataSet = lookupDataSet(def, null);
        if (dataSet == null) return null;
        return dataSet.getMetadata();
    }

    public DataSetGenerator lookupGenerator(DataSetDef def) {
        BeanDataSetDef beanDef = (BeanDataSetDef) def;
        String beanName = beanDef.getGeneratorClass();
        return generatorMap.get(beanName);
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        // Look first into the static data set provider since BEAN data sets are statically registered once loaded.
        DataSet dataSet = staticDataSetProvider.lookupDataSet(def.getUUID(), null);

        // If test mode or not exists then invoke the BEAN generator class
        if ((lookup != null && lookup.testMode()) || dataSet == null) {
            BeanDataSetDef beanDef = (BeanDataSetDef) def;
            DataSetGenerator dataSetGenerator = lookupGenerator(def);
            dataSet = dataSetGenerator.buildDataSet(beanDef.getParamaterMap());
            dataSet.setUUID(def.getUUID());
            dataSet.setDefinition(def);

            // Remove non declared columns
            if (!def.isAllColumnsEnabled()) {
                for (DataColumn column : dataSet.getColumns()) {
                    if (def.getColumnById(column.getId()) == null) {
                        dataSet.removeColumn(column.getId());
                    }
                }
            }
            // Register the data set before return
            staticDataSetProvider.registerDataSet(dataSet);
        }
        try {
            // Always do the lookup over the static data set registry.
            dataSet = staticDataSetProvider.lookupDataSet(def, lookup);
        } finally {
            if (lookup != null && lookup.testMode()) {
                // In test mode remove the data set from cache
                staticDataSetProvider.removeDataSet(def.getUUID());
            }
        }
        // Return the lookup results
        return dataSet;
    }

    public boolean isDataSetOutdated(DataSetDef def) {
        return false;
    }

    // Listen to changes on the data set definition registry

    protected  void onDataSetStaleEvent(@Observes DataSetStaleEvent event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.BEAN.equals(def.getProvider())) {
            remove(def.getUUID());
        }
    }

    protected  void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        DataSetDef def = event.getDataSetDef();
        if (DataSetProviderType.BEAN.equals(def.getProvider())) {
            remove(def.getUUID());
        }
    }
    
    private void remove(final String uuid) {
        staticDataSetProvider.removeDataSet(uuid);
    }
}
