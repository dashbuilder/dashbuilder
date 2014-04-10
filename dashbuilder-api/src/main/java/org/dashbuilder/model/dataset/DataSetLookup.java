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
 * A data set look up request.
 */
@Portable
public class DataSetLookup {

    /**
     * The UUID of the data set to retrieve.
     */
    protected String dataSetUUID = null;

    /**
     * The starting row offset of the target data set.
     */
    protected int rowOffset = 0;

    /**
     * The number of rows to get.
     */
    protected int numberOfRows = 0;

    /**
     * The list of operations to apply on the target data set as part of the lookup operation.
     */
    protected List<DataSetOp> operationList = new ArrayList<DataSetOp>();

    public DataSetLookup() {
    }

    public DataSetLookup(String dataSetUUID, DataSetOp... ops) {
        this.dataSetUUID = dataSetUUID;
        for (DataSetOp op : ops) {
            operationList.add(op);
        }
    }

    public String getDataSetUUID() {
        return dataSetUUID;
    }

    public List<DataSetOp> getOperationList() {
        return operationList;
    }

    public DataSetLookup addOperation(DataSetOp op) {
        operationList.add(op);
        return this;
    }
}
