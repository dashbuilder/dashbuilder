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

public enum LegendAlign implements EnumWithValue
{
    /**
     * Aligned to the start of the area allocated for the legend.
     */
    START("start"),
    /**
     * Centered in the area allocated for the legend.
     */
    CENTER("center"),
    /**
     * Aligned to the end of the area allocated for the legend.
     */
    END("end");

    private final String                            m_value;

    private static final NFastStringMap<LegendAlign> LOOKUP_MAP = Statics.build(LegendAlign.values());

    private LegendAlign(String value)
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

    public static final LegendAlign lookup(String key)
    {
        return Statics.lookup(key, LOOKUP_MAP, CENTER);
    }

    public static final List<String> getKeys()
    {
        return Statics.getKeys(LegendAlign.values());
    }

    public static final List<LegendAlign> getValues()
    {
        return Statics.getValues(LegendAlign.values());
    }
}
