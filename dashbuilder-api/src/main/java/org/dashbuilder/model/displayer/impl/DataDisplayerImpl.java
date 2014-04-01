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

import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.XAxis;
import org.dashbuilder.model.displayer.YAxis;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataDisplayerImpl implements DataDisplayer {

    protected String title;
    protected String type;
    protected String renderer;
    protected XAxis xAxis;
    protected List<YAxis> yAxisList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public XAxis getXAxis() {
        return xAxis;
    }

    public void setXAxis(XAxis xAxis) {
        this.xAxis = xAxis;
    }

    public List<YAxis> getYAxes() {
        return yAxisList;
    }

    public void setYAxes(List<YAxis> yAxisList) {
        this.yAxisList = yAxisList;
    }
}
