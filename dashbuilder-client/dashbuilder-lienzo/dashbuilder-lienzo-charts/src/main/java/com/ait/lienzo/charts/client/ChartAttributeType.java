/*
   Copyright (c) 2014,2015 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.charts.client;

import com.ait.lienzo.charts.client.axis.AxisValidator;
import com.ait.lienzo.charts.client.xy.XYChartSerieValidator;
import com.ait.lienzo.charts.shared.core.types.*;
import com.ait.lienzo.client.core.AttributeType;
import com.ait.lienzo.client.core.shape.json.validators.ArrayValidator;
import com.ait.lienzo.client.core.shape.json.validators.EnumValidator;
import com.ait.lienzo.client.core.shape.json.validators.IAttributeTypeValidator;
import com.ait.lienzo.shared.core.types.ArrowType;

public class ChartAttributeType extends AttributeType
{
    public final static ChartAttributeType ALIGN = new ChartAttributeType(new EnumValidator<ChartAlign>("align", ChartAlign.values()));
    public final static ChartAttributeType DIRECTION  = new ChartAttributeType(new EnumValidator<ChartDirection>("direction", ChartDirection.values()));
    public final static ChartAttributeType ORIENTATION = new ChartAttributeType(new EnumValidator<ChartOrientation>("orientation", ChartOrientation.values()));
    public final static ChartAttributeType LEGEND_POSITION = new ChartAttributeType(new EnumValidator<LegendPosition>("LegendPosition", LegendPosition.values()));
    public final static ChartAttributeType LEGEND_ALIGN = new ChartAttributeType(new EnumValidator<LegendAlign>("LegendAlign", LegendAlign.values()));
    public final static ChartAttributeType AXIS_TYPE = new ChartAttributeType(AxisValidator.INSTANCE);
    public final static ChartAttributeType XY_CHART_DATA_TYPE = new ChartAttributeType(new ArrayValidator(XYChartSerieValidator.INSTANCE));
    public final static ChartAttributeType PIE_CHART_DATA_TYPE = new ChartAttributeType(new ArrayValidator(PieChartEntryValidator.INSTANCE));

    protected ChartAttributeType(IAttributeTypeValidator validator)
    {
        super(validator);
    }
}
