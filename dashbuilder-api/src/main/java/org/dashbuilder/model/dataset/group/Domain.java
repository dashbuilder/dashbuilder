/**
 * Copyright (C) 2012 JBoss Inc
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
package org.dashbuilder.model.dataset.group;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A data domain definition.
 */
@Portable
public class Domain {

    protected String columnId = null;
    protected DomainStrategy strategy = DomainStrategy.DYNAMIC;
    protected int maxIntervals = 15;
    protected String intervalSize = null;

    public Domain() {
    }

    public Domain(String columnId, DomainStrategy strategy) {
        this.columnId = columnId;
        this.strategy = strategy;
    }

    public Domain(String columnId, DomainStrategy strategy, int maxIntervals, String intervalSize) {
        this.columnId = columnId;
        this.strategy = strategy;
        this.maxIntervals = maxIntervals;
        this.intervalSize = intervalSize;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public DomainStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(DomainStrategy strategy) {
        this.strategy = strategy;
    }

    public int getMaxIntervals() {
        return maxIntervals;
    }

    public void setMaxIntervals(int maxIntervals) {
        this.maxIntervals = maxIntervals;
    }

    public String getIntervalSize() {
        return intervalSize;
    }

    public void setIntervalSize(String intervalSize) {
        this.intervalSize = intervalSize;
    }

    public boolean equals(Object obj) {
        try {
            Domain other = (Domain) obj;
            if (columnId != null && !columnId.equals(other.columnId)) return false;
            if (strategy != null && !strategy.equals(other.strategy)) return false;
            if (intervalSize != null && !intervalSize.equals(other.intervalSize)) return false;
            if (maxIntervals != other.maxIntervals) return false;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
