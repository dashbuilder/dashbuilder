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

package com.ait.lienzo.charts.shared.core.types;

import com.ait.lienzo.client.core.types.NFastStringMap;
import com.ait.lienzo.shared.core.types.EnumWithValue;

import java.util.List;

public enum LegendPosition implements EnumWithValue
{
    /**
     * To the right of the chart.
     */
    RIGHT("right"),
    /**
     * To the left of the chart.
     */
    LEFT("left"),
    /**
     * Above the chart.
     */
    TOP("top"),
    /**
     * Below the chart.
     */
    BOTTOM("bottom"),
    /**
     * Inside the chart.
     */
    INSIDE("inside"),
    /**
     * No legend is displayed.
     */
    NONE("none");

    private final String                            m_value;

    private static final NFastStringMap<LegendPosition> LOOKUP_MAP = Statics.build(LegendPosition.values());

    private LegendPosition(String value)
    {
        m_value = value;
    }

    @Override
    public final String getValue()
    {
        return m_value;
    }

    @Override
    public final String toString()
    {
        return m_value;
    }

    public static final LegendPosition lookup(String key)
    {
        return Statics.lookup(key, LOOKUP_MAP, TOP);
    }

    public static final List<String> getKeys()
    {
        return Statics.getKeys(LegendPosition.values());
    }

    public static final List<LegendPosition> getValues()
    {
        return Statics.getValues(LegendPosition.values());
    }
}
