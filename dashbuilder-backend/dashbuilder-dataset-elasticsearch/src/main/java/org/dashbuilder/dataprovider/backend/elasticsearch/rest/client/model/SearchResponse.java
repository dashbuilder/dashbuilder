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
package org.dashbuilder.dataprovider.backend.elasticsearch.rest.client.model;

import org.dashbuilder.dataset.DataColumn;

import java.util.List;

/**
 * * Search response.
 */
public class SearchResponse {

    protected long tookInMillis;
    protected int responseStatus;
    protected long totalHits;
    protected float maxScore;
    protected int totalShards;
    protected int successfulShards;
    protected int shardFailures;
    protected List<DataColumn> columns;
    protected SearchHitResponse[] hits;


    public SearchResponse(long tookInMillis, int responseStatus, long totalHits, float maxScore, int totalShards, int successfulShards, int shardFailures, List<DataColumn> columns, SearchHitResponse[] hits) {
        this.tookInMillis = tookInMillis;
        this.responseStatus = responseStatus;
        this.totalHits = totalHits;
        this.maxScore = maxScore;
        this.totalShards = totalShards;
        this.successfulShards = successfulShards;
        this.shardFailures = shardFailures;
        this.columns = columns;
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

    public List<DataColumn> getColumns() {
        return columns;
    }
}
