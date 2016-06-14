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

public class TypeMappingResponse {

    private final String typeName;
    private final FieldMappingResponse[] fields;

    public TypeMappingResponse( String typeName, 
                                FieldMappingResponse[] fields ) {
        this.typeName = typeName;
        this.fields = fields;
    }

    public String getTypeName() {
        return typeName;
    }

    public FieldMappingResponse[] getFields() {
        return fields;
    }
    
    public FieldMappingResponse getField(String name) {
        if (name == null || fields == null || fields.length == 0) {
            return null;
        }
        
        for (FieldMappingResponse fieldMappingResponse : fields) {
            if (name.equalsIgnoreCase(fieldMappingResponse.getName())) return fieldMappingResponse;
        }
        
        return null;
    }
}
