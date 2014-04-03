/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.model.dataset;

import java.util.List;
import java.util.ArrayList;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A data set look up
 */
@Portable
public class DataLookup {

    protected String dataSetUUID = null;
    protected List<DataSetOperation> operationList = new ArrayList<DataSetOperation>();

    public DataLookup() {
    }

    public DataLookup(String dataSetUUID, DataSetOperation... ops) {
        this.dataSetUUID = dataSetUUID;
        for (DataSetOperation op : ops) {
            operationList.add(op);
        }
    }

    public String getDataSetUUID() {
        return dataSetUUID;
    }

    public List<DataSetOperation> getOperationList() {
        return operationList;
    }

    public DataLookup addOperation(DataSetOperation op) {
        operationList.add(op);
        return this;
    }
}
