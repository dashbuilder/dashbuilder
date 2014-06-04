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
package org.dashbuilder.model.displayer;

import org.dashbuilder.model.dataset.sort.SortOrder;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TableDisplayer extends AbstractDataDisplayer {

    protected int pageSize = 20;
    protected boolean sortEnabled = true;
    protected String defaultSortColumnId = null;
    protected SortOrder defaultSortOrder = SortOrder.ASCENDING;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isSortEnabled() {
        return sortEnabled;
    }

    public void setSortEnabled(boolean sortEnabled) {
        this.sortEnabled = sortEnabled;
    }

    public String getDefaultSortColumnId() {
        return defaultSortColumnId;
    }

    public void setDefaultSortColumnId(String defaultSortColumnId) {
        this.defaultSortColumnId = defaultSortColumnId;
    }

    public SortOrder getDefaultSortOrder() {
        return defaultSortOrder;
    }

    public void setDefaultSortOrder(SortOrder defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }
}
