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
package org.dashbuilder.storage.memory.group;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.group.DomainStrategy;
import org.dashbuilder.model.dataset.group.DomainType;

@ApplicationScoped
public class IntervalBuilderLocator {

    @Inject FixedLabelBuilder fixedLabelBuilder;

    public IntervalBuilder lookup(DataColumn column, DomainStrategy strategy) {
        ColumnType columnType = column.getColumnType();
        DomainType domainType = strategy.getDomainType();
        if (ColumnType.LABEL.equals(columnType)) {
            if (DomainType.FIXED.equals(domainType)) return fixedLabelBuilder;
            if (DomainType.ADAPTATIVE.equals(domainType)) return fixedLabelBuilder;
            if (DomainType.MULTIPLE.equals(domainType)) return fixedLabelBuilder;
            if (DomainType.CUSTOM.equals(domainType)) return fixedLabelBuilder;
        }
        if (ColumnType.DATE.equals(columnType)) {
            // TODO
        }
        if (ColumnType.NUMBER.equals(columnType)) {
            // TODO
        }
        return null;
    }

}
