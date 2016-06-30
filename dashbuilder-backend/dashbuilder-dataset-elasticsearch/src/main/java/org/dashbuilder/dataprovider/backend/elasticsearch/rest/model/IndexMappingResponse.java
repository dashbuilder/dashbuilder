/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.model;

public class IndexMappingResponse {
    private final  String indexName;
    private final  TypeMappingResponse[] typeMappings;

    public IndexMappingResponse( String indexName, 
                                 TypeMappingResponse[] mappings ) {
        this.indexName = indexName;
        this.typeMappings = mappings;
    }

    public String getIndexName() {
        return indexName;
    }

    public TypeMappingResponse[] getTypeMappings() {
        return typeMappings;
    }

    public TypeMappingResponse getType(String name) {
        if (name == null || typeMappings == null || typeMappings.length == 0) {
            return null;
        }

        for (TypeMappingResponse typeMappingResponse : typeMappings) {
            if (name.equalsIgnoreCase(typeMappingResponse.getTypeName())) return typeMappingResponse;
        }

        return null;
    }
}
