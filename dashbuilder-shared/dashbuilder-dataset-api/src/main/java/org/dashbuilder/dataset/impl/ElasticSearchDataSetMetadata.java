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
package org.dashbuilder.dataset.impl;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Portable
public class ElasticSearchDataSetMetadata extends DataSetMetadataImpl {

    // The patterns for EL index fields (defined by querying index' mappings).
    private Map<String, String> fieldPatterns = new HashMap<String, String>();
    
    public ElasticSearchDataSetMetadata() {
    }

    public ElasticSearchDataSetMetadata(DataSetDef definition, String uuid, int numberOfRows, int numberOfColumns, List<String> columnIds, List<ColumnType> columnTypes, int estimatedSize) {
        super(definition, uuid, numberOfRows, numberOfColumns, columnIds, columnTypes, estimatedSize);
    }

    public ElasticSearchDataSetMetadata(DataSetImpl dataSet) {
        super(dataSet);
    }
    
    public ElasticSearchDataSetMetadata setFieldPattern(String field, String pattern) {
        fieldPatterns.put(field, pattern);
        return this;
    }

    public String getFieldPattern(String field) {
        return fieldPatterns.get(field);
    }
}