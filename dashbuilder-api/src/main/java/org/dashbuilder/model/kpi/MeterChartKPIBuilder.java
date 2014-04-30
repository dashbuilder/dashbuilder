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

import org.dashbuilder.model.displayer.BarChartBuilder;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.displayer.DataDisplayerType;
import org.dashbuilder.model.displayer.MeterChartBuilder;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A KPI builder for meter charts
 *
 * <pre>
 new MeterChartKPIBuilder()
 .title("Expected amount per year")
 .dataset("target-dataset-uuid")
 .group(CREATION_DATE, YEAR)
 .sum(AMOUNT)
 .width(400).height(200)
 .meter(0, 1000000, 3000000, 5000000)
 .column("Year")
 .column("Amount")
 .build()
 </pre>
 */
@Portable
public class MeterChartKPIBuilder extends AbstractChartKPIBuilder<MeterChartKPIBuilder> {

    protected DataDisplayerBuilder createDisplayerBuilder() {
        return new MeterChartBuilder();
    }

    public MeterChartKPIBuilder() {
        super();
        displayerBuilder.type(DataDisplayerType.METERCHART);
    }

    public MeterChartKPIBuilder meter(long start, long warning, long critical, long end) {
        ((MeterChartBuilder) displayerBuilder).meter(start, warning, critical, end);
        return this;
    }
}
