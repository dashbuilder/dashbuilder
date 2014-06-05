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
package org.dashbuilder.model.displayer.impl;

import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.LineChartBuilder;
import org.dashbuilder.model.displayer.LineChartDisplayer;

public class LineChartBuilderImpl extends AbstractXAxisChartBuilder<LineChartBuilderImpl> implements LineChartBuilder {

    protected DataDisplayer createDisplayer() {
        return new LineChartDisplayer();
    }
}
