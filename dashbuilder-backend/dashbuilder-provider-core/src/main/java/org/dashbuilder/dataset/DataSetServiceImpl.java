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

import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.DataSetOperation;
import org.dashbuilder.service.DataSetLookupOptions;
import org.dashbuilder.service.DataSetService;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class DataSetServiceImpl implements DataSetService {

    public DataSetMetadata getDataSetMetadata(String uid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataSetMetadata refreshDataSet(String uid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataSetMetadata transformDataSet(String uid, DataSetOperation... ops) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataSet lookupDataSet(String uid, DataSetLookupOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
