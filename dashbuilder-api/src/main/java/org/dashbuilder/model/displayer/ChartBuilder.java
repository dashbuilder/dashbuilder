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

/**
 * A displayer builder for the assembly of Chart based data displayer instances.
 */
public interface ChartBuilder<T extends ChartBuilder> extends DataDisplayerBuilder<T> {

    /**
     * Sets the width of the chart.
     * @param width The width of the chart.
     * @return The DataDisplayerBuilder instance that is being used to configure a Chart data displayer.
     */
    T width(int width);

    /**
     * Sets the height of the chart.
     * @param height The height of the chart.
     * @return The DataDisplayerBuilder instance that is being used to configure a Chart data displayer.
     */
    T height(int height);

    /**
     * Set the margins for this chart.
     * @param top The top margin.
     * @param bottom The bottom margin.
     * @param left The left margin.
     * @param right The right margin.
     * @return The DataDisplayerBuilder instance that is being used to configure a Chart data displayer.
     */
    T margins(int top, int bottom, int left, int right);
}
