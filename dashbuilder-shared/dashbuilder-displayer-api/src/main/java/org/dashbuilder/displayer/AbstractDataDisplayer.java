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
package org.dashbuilder.displayer;

import java.util.List;
import java.util.ArrayList;

public abstract class AbstractDataDisplayer implements DataDisplayer {

    protected String title;
    protected boolean titleVisible = true;
    protected String renderer;
    protected List<DataDisplayerColumn> columnList = new ArrayList<DataDisplayerColumn>();

    protected boolean filterEnabled = false;
    protected boolean filterSelfApplyEnabled = false;
    protected boolean filterNotificationEnabled = false;
    protected boolean filterListeningEnabled = false;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isTitleVisible() {
        return titleVisible;
    }

    public void setTitleVisible(boolean titleVisible) {
        this.titleVisible = titleVisible;
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public List<DataDisplayerColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<DataDisplayerColumn> columnList) {
        this.columnList = columnList;
    }

    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    public void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    public boolean isFilterSelfApplyEnabled() {
        return filterSelfApplyEnabled;
    }

    public void setFilterSelfApplyEnabled(boolean filterSelfApplyEnabled) {
        this.filterSelfApplyEnabled = filterSelfApplyEnabled;
    }

    public boolean isFilterNotificationEnabled() {
        return filterNotificationEnabled;
    }

    public void setFilterNotificationEnabled(boolean filterNotificationEnabled) {
        this.filterNotificationEnabled = filterNotificationEnabled;
    }

    public boolean isFilterListeningEnabled() {
        return filterListeningEnabled;
    }

    public void setFilterListeningEnabled(boolean filterListeningEnabled) {
        this.filterListeningEnabled = filterListeningEnabled;
    }
}
