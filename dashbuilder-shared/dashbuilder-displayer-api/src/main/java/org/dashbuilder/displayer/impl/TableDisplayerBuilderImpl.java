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
package org.dashbuilder.displayer.impl;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.TableDisplayerSettings;
import org.dashbuilder.displayer.TableDisplayerBuilder;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TableDisplayerBuilderImpl extends AbstractDisplayerBuilder<TableDisplayerBuilderImpl> implements TableDisplayerBuilder<TableDisplayerBuilderImpl> {

    public DisplayerSettings createDisplayerSettings() {
        return new TableDisplayerSettings();
    }

    public TableDisplayerBuilderImpl tablePageSize(int pageSize) {
        TableDisplayerSettings d = (TableDisplayerSettings ) displayerSettings;
        d.setPageSize(pageSize);
        return this;
    }

    public TableDisplayerBuilderImpl tableOrderEnabled(boolean enabled) {
        TableDisplayerSettings d = (TableDisplayerSettings ) displayerSettings;
        d.setSortEnabled(enabled);
        return this;
    }

    public TableDisplayerBuilderImpl tableOrderDefault(String columnId, SortOrder order) {
        TableDisplayerSettings d = (TableDisplayerSettings ) displayerSettings;
        d.setDefaultSortColumnId(columnId);
        d.setDefaultSortOrder(order);
        return this;
    }

    public TableDisplayerBuilderImpl tableOrderDefault(String columnId, String order) {
        return tableOrderDefault(columnId, SortOrder.getByName(order));
    }
}
