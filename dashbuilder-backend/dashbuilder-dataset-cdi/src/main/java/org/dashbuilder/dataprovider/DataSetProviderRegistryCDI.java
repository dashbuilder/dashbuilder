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
package org.dashbuilder.dataprovider;

import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.dashbuilder.DataSetCore;
import org.dashbuilder.dataset.json.DataSetDefJSONMarshaller;

@ApplicationScoped
public class DataSetProviderRegistryCDI extends DataSetProviderRegistryImpl {

    @Inject
    private StaticDataSetProviderCDI staticDataSetProviderCDI;

    @Inject
    private BeanDataSetProviderCDI beanDataSetProviderCDI;

    @Inject
    private CSVDataSetProviderCDI csvDataSetProviderCDI;

    @Inject
    private SQLDataSetProviderCDI sqlDataSetProviderCDI;

    @Inject
    private ElasticSearchDataSetProviderCDI elasticSearchDataSetProviderCDI;

    @Inject
    private Instance<DataSetProvider> providerSet;

    protected DataSetDefJSONMarshaller dataSetDefJSONMarshaller = new DataSetDefJSONMarshaller(this);

    @PostConstruct
    public void init() {
        DataSetCore.get().setDataSetDefJSONMarshaller(dataSetDefJSONMarshaller);

        // Register all the providers available in classpath
        Iterator<DataSetProvider> it = providerSet.iterator();
        while (it.hasNext()) {
            DataSetProvider provider = it.next();
            super.registerDataProvider(provider);
        }

        // Register the core providers
        super.registerDataProvider(staticDataSetProviderCDI);
        super.registerDataProvider(beanDataSetProviderCDI);
        super.registerDataProvider(csvDataSetProviderCDI);
        super.registerDataProvider(sqlDataSetProviderCDI);
        super.registerDataProvider(elasticSearchDataSetProviderCDI);
    }

    public StaticDataSetProviderCDI getStaticDataSetProviderCDI() {
        return staticDataSetProviderCDI;
    }

    public BeanDataSetProviderCDI getBeanDataSetProviderCDI() {
        return beanDataSetProviderCDI;
    }

    public CSVDataSetProviderCDI getCsvDataSetProviderCDI() {
        return csvDataSetProviderCDI;
    }

    public SQLDataSetProviderCDI getSqlDataSetProviderCDI() {
        return sqlDataSetProviderCDI;
    }

    public ElasticSearchDataSetProviderCDI getElasticSearchDataSetProviderCDI() {
        return elasticSearchDataSetProviderCDI;
    }

    public DataSetDefJSONMarshaller getDataSetDefJSONMarshaller() {
        return dataSetDefJSONMarshaller;
    }
}
