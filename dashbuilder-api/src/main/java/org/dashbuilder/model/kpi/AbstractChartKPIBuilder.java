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
package org.dashbuilder.model.kpi;

import org.dashbuilder.model.displayer.AbstractChartBuilder;

/**
 * Base KPI builder for charts.
 */
public abstract class AbstractChartKPIBuilder<T extends AbstractChartKPIBuilder<?>> extends KPIBuilder<T> {

    public T width(int width) {
        ((AbstractChartBuilder) displayerBuilder).width(width);
        return (T) this;
    }

    public T height(int height) {
        ((AbstractChartBuilder) displayerBuilder).height(height);
        return (T) this;
    }
}
