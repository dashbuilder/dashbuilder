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
package org.dashbuilder.model.displayer.impl;

import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.TableDisplayer;
import org.dashbuilder.model.displayer.TableDisplayerBuilder;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TableDisplayerBuilderImpl extends AbstractDisplayerBuilder implements TableDisplayerBuilder {

    public DataDisplayer createDisplayer() {
        return new TableDisplayer();
    }

    public TableDisplayerBuilder tablePageSize(int pageSize) {
        TableDisplayer d = (TableDisplayer) dataDisplayer;
        d.setPageSize(pageSize);
        return this;
    }

    public TableDisplayerBuilder tableOrderEnabled(boolean enabled) {
        TableDisplayer d = (TableDisplayer) dataDisplayer;
        d.setSortEnabled(enabled);
        return this;
    }

    public TableDisplayerBuilder tableOrderDefault(String columnId, SortOrder order) {
        TableDisplayer d = (TableDisplayer) dataDisplayer;
        d.setDefaultSortColumnId(columnId);
        d.setDefaultSortOrder(order);
        return this;
    }

    public TableDisplayerBuilder tableOrderDefault(String columnId, String order) {
        return tableOrderDefault(columnId, SortOrder.getByName(order));
    }
}
