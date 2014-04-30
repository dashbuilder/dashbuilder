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

import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.dashbuilder.model.displayer.XAxisChartBuilder;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A KPI builder for bar charts
 *
 * <pre>
 new LineChartKPIBuilder()
 .dataset("target-dataset-uuid")
 .group(CLOSING_DATE,12, ScalarFunctionType.MONTH)
 .sum(AMOUNT)
 .title("Sales opportunities evolution")
 .width(700).height(400)
 .column("Closing date")
 .column("Total amount")
 .build()
 </pre>
 */
@Portable
public class LineChartKPIBuilder extends AbstractChartKPIBuilder<LineChartKPIBuilder> {

    protected DataDisplayerBuilder createDisplayerBuilder() {
        return new XAxisChartBuilder();
    }

    public LineChartKPIBuilder() {
        super();
        displayerBuilder.type(DataDisplayerType.LINECHART);
    }
}
