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
package org.dashbuilder.dataset.def;

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class BeanDataSetDef extends DataSetDef {

    protected String generatorClass;

    public BeanDataSetDef() {
        super.setProvider(DataSetProviderType.BEAN);
    }

    public String getGeneratorClass() {
        return generatorClass;
    }

    public void setGeneratorClass(String generatorClass) {
        this.generatorClass = generatorClass;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("UUID=").append(UUID).append("\n");
        out.append("Provider=").append(provider).append("\n");
        out.append("Shared=").append(shared).append("\n");
        out.append("Push enabled=").append(pushEnabled).append("\n");
        out.append("Max push size=").append(maxPushSize).append(" Kb\n");
        out.append("Generator=").append(generatorClass);
        return out.toString();
    }
}
