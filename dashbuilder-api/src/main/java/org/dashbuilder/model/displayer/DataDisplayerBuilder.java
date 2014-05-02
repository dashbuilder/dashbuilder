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
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Base class for DataDisplayerBuilder implementations.
 */
public abstract class DataDisplayerBuilder<T extends DataDisplayerBuilder<?>> {

    protected DataDisplayer dataDisplayer = createDisplayer();

    protected abstract DataDisplayer createDisplayer();

    public T title(String title) {
        dataDisplayer.setTitle(title);
        return (T) this;
    }

    public T titleVisible(boolean visible) {
        dataDisplayer.setTitleVisible(visible);
        return (T) this;
    }

    public T type(DataDisplayerType type) {
        dataDisplayer.setType(type);
        return (T) this;
    }

    public T type(String type) {
        dataDisplayer.setType(DataDisplayerType.getByName(type));
        return (T) this;
    }

    public T renderer(String renderer) {
        dataDisplayer.setRenderer(DataDisplayerRenderer.getByName(renderer));
        return (T) this;
    }

    public T renderer(DataDisplayerRenderer renderer) {
        dataDisplayer.setRenderer(renderer);
        return (T) this;
    }

    public T column(String displayName) {
        return column(null, displayName);
    }

    public T column(String columnId, String displayName) {
        dataDisplayer.getColumnList().add(new DataDisplayerColumnImpl(columnId, displayName));
        return (T) this;
    }

    public DataDisplayer build() {
        if (dataDisplayer.getRenderer() == null) {
            dataDisplayer.setRenderer(DataDisplayerRenderer.DEFAULT);
        }
        return (DataDisplayer) dataDisplayer;
    }
}
