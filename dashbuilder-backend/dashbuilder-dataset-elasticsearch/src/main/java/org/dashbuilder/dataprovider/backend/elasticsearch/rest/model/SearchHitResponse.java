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

import java.util.LinkedHashMap;

public class SearchHitResponse {

    protected float score;
    protected String index;
    protected String id;
    protected String type;
    protected long version;
    protected LinkedHashMap<String ,Object> fields;

    public SearchHitResponse(float score, String index, String id, String type, long version, LinkedHashMap<String ,Object> fields) {
        this.score = score;
        this.index = index;
        this.id = id;
        this.type = type;
        this.version = version;
        this.fields = fields;
    }

    public SearchHitResponse(LinkedHashMap<String ,Object> fields) {
        this.score = -1;
        this.index = null;
        this.id = null;
        this.type = null;
        this.version = -1;
        this.fields = fields;
    }

    public float getScore() {
        return score;
    }

    public String getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getVersion() {
        return version;
    }

    public LinkedHashMap<String, Object> getFields() {
        return fields;
    }
    public Object getFieldValue(String name) {
        if (name == null || name.trim().length() == 0 || fields == null || fields.isEmpty()) return null;
        return fields.get(name);
    }
}
