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

import org.dashbuilder.displayer.AbstractChartDisplayer;
import org.dashbuilder.displayer.ChartBuilder;

public abstract class AbstractChartBuilder<T extends ChartBuilder> extends AbstractDisplayerBuilder<T> implements ChartBuilder<T> {

    public T width(int width) {
        ((AbstractChartDisplayer) dataDisplayer).setWidth(width);
        return (T) this;
    }

    public T height(int height) {
        ((AbstractChartDisplayer) dataDisplayer).setHeight(height);
        return (T) this;
    }

    public T margins(int top, int bottom, int left, int right) {
        ((AbstractChartDisplayer) dataDisplayer).setMarginTop(top);
        ((AbstractChartDisplayer) dataDisplayer).setMarginBottom(bottom);
        ((AbstractChartDisplayer) dataDisplayer).setMarginLeft(left);
        ((AbstractChartDisplayer) dataDisplayer).setMarginRight(right);
        return (T) this;
    }
}
