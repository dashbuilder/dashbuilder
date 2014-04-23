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

import java.util.List;

import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetOp;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.group.Domain;
import org.dashbuilder.model.dataset.group.DomainStrategy;
import org.dashbuilder.model.dataset.group.Range;
import org.dashbuilder.model.dataset.sort.DataSetSort;
import org.dashbuilder.model.displayer.impl.DataDisplayerImpl;
import org.dashbuilder.model.displayer.impl.XAxisImpl;
import org.dashbuilder.model.displayer.impl.YAxisImpl;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * It allows for the building of DataSetLookup instances in a friendly manner.
 *
 * <pre>
    DataDisplayer displayer = new DataDisplayerBuilder()
     .title("Opportunities by Pipeline")
     .type("piechart")
     .renderer("google")
     .domain("pipeline", "Pipeline")
     .range("count", "Number of opportunities")
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

    public DataDisplayerBuilder x(String columnId, String displayName) {
        dataDisplayer.setXAxis(new XAxisImpl(columnId, displayName));
        return this;
    }

    public DataDisplayerBuilder y(String columnId, String displayName) {
        dataDisplayer.getYAxes().add(new YAxisImpl(columnId, displayName));
        return this;
    }

    public DataDisplayer build() {
        if (dataDisplayer.getRenderer() == null) {
            dataDisplayer.setRenderer(DataDisplayerRenderer.DEFAULT);
        }
        return dataDisplayer;
    }
}
