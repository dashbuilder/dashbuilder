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
package org.dashbuilder.model.displayer;

import org.dashbuilder.model.displayer.impl.DataDisplayerColumnImpl;
import org.dashbuilder.model.displayer.impl.DataDisplayerImpl;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * It allows for the building of DataSetLookup instances in a friendly manner.
 *
 * <pre>
    DataDisplayer displayer = new DataDisplayerBuilder()
     .title("Opportunities by Pipeline")
     .type("piechart")
     .renderer("google")
     .group("pipeline", "Pipeline")
     .function("count", "Number of opportunities")
     .build();
 </pre>
 */
@Portable
public class DataDisplayerBuilder {

    private DataDisplayerImpl dataDisplayer = new DataDisplayerImpl();

    public DataDisplayerBuilder() {
    }

    public DataDisplayerBuilder title(String title) {
        dataDisplayer.setTitle(title);
        return this;
    }

    public DataDisplayerBuilder type(DataDisplayerType type) {
        dataDisplayer.setType(type);
        return this;
    }

    public DataDisplayerBuilder width(int width) {
        dataDisplayer.setWidth(width);
        return this;
    }

    public DataDisplayerBuilder height(int height) {
        dataDisplayer.setHeight(height);
        return this;
    }

    public DataDisplayerBuilder type(String type) {
        dataDisplayer.setType(DataDisplayerType.getByName(type));
        return this;
    }

    public DataDisplayerBuilder renderer(String renderer) {
        dataDisplayer.setRenderer(DataDisplayerRenderer.getByName(renderer));
        return this;
    }

    public DataDisplayerBuilder renderer(DataDisplayerRenderer renderer) {
        dataDisplayer.setRenderer(renderer);
        return this;
    }

    public DataDisplayerBuilder column(String displayName) {
        return column(null, displayName);
    }

    public DataDisplayerBuilder column(String columnId, String displayName) {
        dataDisplayer.getColumnList().add(new DataDisplayerColumnImpl(columnId, displayName));
        return this;
    }

    public DataDisplayer build() {
        if (dataDisplayer.getRenderer() == null) {
            dataDisplayer.setRenderer(DataDisplayerRenderer.DEFAULT);
        }
        return dataDisplayer;
    }
}
