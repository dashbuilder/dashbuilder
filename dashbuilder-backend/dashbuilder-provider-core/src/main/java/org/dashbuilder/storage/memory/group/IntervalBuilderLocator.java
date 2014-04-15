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
import org.dashbuilder.model.dataset.group.Domain;
import org.dashbuilder.model.dataset.group.DomainStrategy;

@ApplicationScoped
public class IntervalBuilderLocator {

    @Inject IntervalBuilderDynamicLabel intervalBuilderDynamicLabel;
    @Inject IntervalBuilderDynamicDate intervalBuilderDynamicDate;
    @Inject IntervalBuilderFixedDate intervalBuilderFixedDate;

    public IntervalBuilder lookup(DataColumn column, Domain domain) {
        DomainStrategy strategy = domain.getStrategy();
        ColumnType columnType = column.getColumnType();
        if (ColumnType.LABEL.equals(columnType)) {
            if (DomainStrategy.FIXED.equals(strategy)) return intervalBuilderDynamicLabel;
            if (DomainStrategy.DYNAMIC.equals(strategy)) return intervalBuilderDynamicLabel;
            if (DomainStrategy.MULTIPLE.equals(strategy)) return intervalBuilderDynamicLabel;
            if (DomainStrategy.CUSTOM.equals(strategy)) return intervalBuilderDynamicLabel;
        }
        if (ColumnType.DATE.equals(columnType)) {
            if (DomainStrategy.FIXED.equals(strategy)) return intervalBuilderFixedDate;
            if (DomainStrategy.DYNAMIC.equals(strategy)) return intervalBuilderDynamicDate;
            if (DomainStrategy.MULTIPLE.equals(strategy)) return intervalBuilderFixedDate;
            if (DomainStrategy.CUSTOM.equals(strategy)) return intervalBuilderDynamicDate;
            return intervalBuilderDynamicDate;
        }
        if (ColumnType.NUMBER.equals(columnType)) {
            // TODO
        }
        return null;
    }

}
