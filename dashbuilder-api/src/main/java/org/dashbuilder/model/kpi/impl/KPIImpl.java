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
package org.dashbuilder.model.kpi.impl;

import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.kpi.KPI;

public class KPIImpl implements KPI {

    protected String UUID;
    protected DataDisplayer dataDisplayer;
    protected DataSetLookup dataSetLookup;

    public KPIImpl() {
    }

    public KPIImpl(String UUID) {
        this.UUID = UUID;
    }

    public String getUUID() {
        return UUID;
    }

    public KPIImpl setUUID(String UUID) {
        this.UUID = UUID;
        return this;
    }

    public DataSetLookup getDataSetLookup() {
        return dataSetLookup;
    }

    public KPIImpl setDataSetLookup(DataSetLookup dataSetLookup) {
        this.dataSetLookup = dataSetLookup;
        return this;
    }

    public DataDisplayer getDataDisplayer() {
        return dataDisplayer;
    }

    public KPIImpl setDataDisplayer(DataDisplayer dataDisplayer) {
        this.dataDisplayer = dataDisplayer;
        return this;
    }
}
