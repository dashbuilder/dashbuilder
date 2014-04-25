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
package org.dashbuilder.model.displayer.impl;

import java.util.List;
import java.util.ArrayList;

import org.dashbuilder.model.displayer.Chart;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerColumn;
import org.dashbuilder.model.displayer.DataDisplayerRenderer;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataDisplayerImpl implements DataDisplayer, Chart {

    // DataDisplayer interface

    protected String title;
    protected DataDisplayerType type;
    protected DataDisplayerRenderer renderer;
    protected List<DataDisplayerColumn> columnList = new ArrayList<DataDisplayerColumn>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    // Chart interface

    protected int width;
    protected int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
