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
package org.dashbuilder.storage.spi;

import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetOp;

/**
 *  Provides data set storage services.
 */
public interface DataSetStorage {

    void put(DataSet source) throws Exception;
    void remove(String uuid) throws Exception;
    DataSet get(String uuid) throws Exception;
    DataSet apply(String uuid, DataSetOp op) throws Exception;
}
