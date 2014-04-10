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

import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.group.Domain;
import org.dashbuilder.model.dataset.group.DomainStrategy;
import org.dashbuilder.model.dataset.group.Range;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * It allows for the building of DataSetLookup instances in a friendly manner.
 *
 * <pre>
     DataSetLookup lookup = new DataSetLookupBuilder()
     .uuid("target-dataset-uuid")
     .domain("department", "Department")
     .range("id", "occurrences", "Number of expenses", "count")
     .range("amount", "totalAmount", "Total amount", "sum")
     .sort("total", "asc")
     .build();
 </pre>

 */
@Portable
public class DataSetLookupBuilder {

    private DataSetLookup dataSetLookup = new DataSetLookup();

    public DataSetLookupBuilder() {
    }

    private DataSetOp getCurrentOp() {
        List<DataSetOp> dataSetOps = dataSetLookup.getOperationList();
        if (dataSetOps.isEmpty()) return null;
        return dataSetOps.get(dataSetOps.size()-1);
    }

    public DataSetLookupBuilder uuid(String uuid) {
        dataSetLookup.dataSetUUID = uuid;
        return this;
    }

    public DataSetLookupBuilder domain(String columnId) {
        return domain(columnId, DomainStrategy.DYNAMIC);
    }

    public DataSetLookupBuilder domain(String columnId, String strategy) {
        return domain(columnId, DomainStrategy.getByName(strategy));
    }

    public DataSetLookupBuilder domain(String columnId, DomainStrategy strategy) {
        return domain(columnId, strategy, 15, null);
    }

    public DataSetLookupBuilder domain(String columnId, String strategy, int maxIntervals, String intervalSize) {
        return domain(columnId, DomainStrategy.getByName(strategy), maxIntervals, intervalSize);
    }

    public DataSetLookupBuilder domain(String columnId, DomainStrategy strategy, int maxIntervals, String intervalSize) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetGroup)) {
            dataSetLookup.addOperation(new DataSetGroup());
        }
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        gOp.addDomains(new Domain(columnId, strategy, maxIntervals, intervalSize));
        return this;
    }

    public DataSetLookupBuilder range(String fromId, String columnId, String columnName, String function) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetGroup)) {
            dataSetLookup.addOperation(new DataSetGroup());
        }
        DataSetGroup gOp = (DataSetGroup) getCurrentOp();
        gOp.addRanges(new Range(fromId, columnId, columnName, function));
        return this;
    }

    public DataSetLookupBuilder sort(String columnId, String order) {
        DataSetOp op = getCurrentOp();
        if (op == null || !(op instanceof DataSetSort)) {
            dataSetLookup.addOperation(new DataSetSort());
        }
        DataSetSort sOp = (DataSetSort) getCurrentOp();
        return this;
    }

    public DataSetLookup build() {
        return dataSetLookup;
    }
}
