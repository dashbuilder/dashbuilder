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

import java.util.List;
import java.util.ArrayList;

public abstract class AbstractDataDisplayer implements DataDisplayer {

    protected String title;
    protected boolean titleVisible = true;
    protected DataDisplayerType type;
    protected DataDisplayerRenderer renderer;
    protected List<DataDisplayerColumn> columnList = new ArrayList<DataDisplayerColumn>();

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

    public DataDisplayerType getType() {
        return type;
    }

    public void setType(DataDisplayerType type) {
        this.type = type;
    }

    public DataDisplayerRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(DataDisplayerRenderer renderer) {
        this.renderer = renderer;
    }

    public List<DataDisplayerColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<DataDisplayerColumn> columnList) {
        this.columnList = columnList;
    }
}
