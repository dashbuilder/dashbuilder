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

    protected String columnId;
    protected DomainStrategy domainStrategy;

    public Domain() {
    }

    public Domain(String columnId, DomainStrategy strategy) {
        this.columnId = columnId;
        this.domainStrategy = strategy;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public DomainStrategy getDomainStrategy() {
        return domainStrategy;
    }

    public void setDomainStrategy(DomainStrategy domainStrategy) {
        this.domainStrategy = domainStrategy;
    }
}
