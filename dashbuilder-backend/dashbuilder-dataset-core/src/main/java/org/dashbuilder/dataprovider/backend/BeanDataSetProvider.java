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

import java.io.File;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetGenerator;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetDefModifiedEvent;
import org.slf4j.Logger;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@ApplicationScoped
@Named("bean")
public class BeanDataSetProvider implements DataSetProvider {

    @Inject
    protected StaticDataSetProvider staticDataSetProvider;

    @Inject
    protected Logger log;

    public DataSetProviderType getType() {
        return DataSetProviderType.BEAN;
    }

    public DataSetMetadata getDataSetMetadata(DataSetDef def) {
        DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);
        if (dataSet == null) return null;
        return dataSet.getMetadata();
    }

    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        // Look first into the static data set provider since BEAN data sets are statically registered once loaded.
        DataSet dataSet = staticDataSetProvider.lookupDataSet(def, null);
        if (dataSet == null) {
            // If not exists then invoke the BEAN generator class
            BeanDataSetDef beanDef = (BeanDataSetDef) def;
            Class generatorClass = Class.forName(beanDef.getGeneratorClass());
            DataSetGenerator dataSetGenerator = (DataSetGenerator) generatorClass.newInstance();
            dataSet = dataSetGenerator.buildDataSet(beanDef.getParamaterMap());
            dataSet.setUUID(def.getUUID());
            dataSet.setDefinition(def);

            // Register the data set before return
            staticDataSetProvider.registerDataSet(dataSet);

        }
        // Always do the lookup over the static data set registry.
        return staticDataSetProvider.lookupDataSet(def, lookup);
    }

    public boolean isDataSetOutdated(DataSetDef def) {
        return false;
    }

    // Listen to changes on the data set definition registry

    private void onDataSetDefModifiedEvent(@Observes DataSetDefModifiedEvent event) {
        checkNotNull("event", event);
        checkNotNull("event", event.getOldDataSetDef());

        DataSetDef oldDef = event.getOldDataSetDef();
        if (DataSetProviderType.BEAN.equals(oldDef.getProvider())) {
            String uuid = event.getOldDataSetDef().getUUID();
            staticDataSetProvider.removeDataSet(uuid);
        }
    }
}
