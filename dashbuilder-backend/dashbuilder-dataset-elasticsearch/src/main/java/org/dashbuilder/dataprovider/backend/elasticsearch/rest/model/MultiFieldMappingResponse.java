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

public class MultiFieldMappingResponse {

    private final  String name;
    private final  FieldMappingResponse.FieldType dataType;
    private final  FieldMappingResponse.IndexType indexType;

    public MultiFieldMappingResponse( String name, 
                                      FieldMappingResponse.FieldType fieldType, 
                                      FieldMappingResponse.IndexType indexType ) {
        this.name = name;
        this.dataType = fieldType;
        this.indexType = indexType;
    }
    
    public String getName() {
        return name;
    }

    public FieldMappingResponse.FieldType getDataType() {
        return dataType;
    }

    public FieldMappingResponse.IndexType getIndexType() {
        return indexType;
    }

}
