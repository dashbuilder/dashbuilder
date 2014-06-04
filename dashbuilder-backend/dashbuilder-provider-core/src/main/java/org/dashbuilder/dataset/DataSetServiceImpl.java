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
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.impl.DataSetImpl;
import org.dashbuilder.service.DataSetService;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;

@Service
public class DataSetServiceImpl implements DataSetService {

    @Inject Logger log;
    @Inject DataSetManager dataSetManager;

    public DataSetMetadata fetchMetadata(String uid) {
        try {
            DataSet dataSet = dataSetManager.getDataSet(uid);
            return dataSet.getMetadata();
        } catch (Exception e) {
            log.error("Data set fetch failed. UID=" + uid, e);
            return null;
        }
    }

    public DataSet lookupDataSet(DataSetLookup lookup) {
        try {
            return dataSetManager.lookupDataSet(lookup);
        } catch (Exception e) {
            log.error("Data set lookup failed. UID= " + lookup.getDataSetUUID(), e);
            return null;
        }
    }
}
