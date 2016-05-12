/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.json.DataSetDefJSONMarshallerExt;
import org.dashbuilder.dataset.json.ELSDefJSONMarshaller;

/**
 * For accessing data sets that are the result of querying an elasticsearch server.
 */
public class ELSProviderType extends AbstractProviderType<ElasticSearchDataSetDef>  {

    @Override
    public String getName() {
        return "ELASTICSEARCH";
    }

    @Override
    public ElasticSearchDataSetDef createDataSetDef() {
        return new ElasticSearchDataSetDef();
    }

    @Override
    public DataSetDefJSONMarshallerExt<ElasticSearchDataSetDef> getJsonMarshaller() {
        return ELSDefJSONMarshaller.INSTANCE;
    }
}
