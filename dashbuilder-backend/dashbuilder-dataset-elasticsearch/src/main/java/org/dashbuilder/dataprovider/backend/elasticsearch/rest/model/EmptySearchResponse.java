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

/**
 * Search response with no hit results.
 */
public class EmptySearchResponse extends SearchResponse {

    public EmptySearchResponse(long tookInMillis, int responseStatus, long totalHits, float maxScore, int totalShards, int successfulShards, int shardFailures) {
        super(tookInMillis, responseStatus, totalHits, maxScore, totalShards, successfulShards, shardFailures, null);
    }
}
