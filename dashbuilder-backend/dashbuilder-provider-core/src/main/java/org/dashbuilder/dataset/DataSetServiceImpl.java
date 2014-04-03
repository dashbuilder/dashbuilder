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
package org.dashbuilder.dataset;

import javax.inject.Inject;

import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataLookup;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.DataSetOperation;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.service.DataSetLookupOptions;
import org.dashbuilder.service.DataSetService;
import org.dashbuilder.service.UUIDGeneratorService;
import org.dashbuilder.uuid.UUIDGenerator;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class DataSetServiceImpl implements DataSetService {

    @Inject UUIDGenerator uuidGenerator;

    public DataSetMetadata getDataSetMetadata(String uid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataSetMetadata refreshDataSet(String uid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataSetMetadata transformDataSet(String uid, DataSetOperation... ops) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataSet lookupDataSet(DataLookup lookup) {
        DataSetImpl dataSet = new DataSetImpl();
        dataSet.setUUID(lookup.getDataSetUUID());
        dataSet.addColumn("department", ColumnType.LABEL);
        dataSet.addColumn("amount", ColumnType.NUMBER);
        dataSet.setValueAt(0, 0, "Engineering");
        dataSet.setValueAt(1, 0, "Services");
        dataSet.setValueAt(2, 0, "HR");
        dataSet.setValueAt(3, 0, "Administration");
        dataSet.setValueAt(0, 1, 13423);
        dataSet.setValueAt(1, 1, 8984.45);
        dataSet.setValueAt(2, 1, 3434.44);
        dataSet.setValueAt(3, 1, 10034.4);
        return dataSet;
    }
}
