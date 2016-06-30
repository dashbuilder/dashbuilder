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
 * * Search response.
 */
public class SearchResponse {

    private final  long tookInMillis;
    private final  int responseStatus;
    private final  long totalHits;
    private final  float maxScore;
    private final  int totalShards;
    private final  int successfulShards;
    private final  int shardFailures;
    private final  SearchHitResponse[] hits;


    public SearchResponse( long tookInMillis, 
                           int responseStatus, 
                           long totalHits, 
                           float maxScore, 
                           int totalShards, 
                           int successfulShards, 
                           int shardFailures, 
                           SearchHitResponse[] hits ) {
        this.tookInMillis = tookInMillis;
        this.responseStatus = responseStatus;
        this.totalHits = totalHits;
        this.maxScore = maxScore;
        this.totalShards = totalShards;
        this.successfulShards = successfulShards;
        this.shardFailures = shardFailures;
        this.hits = hits;
    }

    public long getTookInMillis() {
        return tookInMillis;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public int getTotalShards() {
        return totalShards;
    }

    public int getSuccessfulShards() {
        return successfulShards;
    }

    public int getShardFailures() {
        return shardFailures;
    }

    public SearchHitResponse[] getHits() {
        return hits;
    }

}
