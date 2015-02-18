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

import java.util.List;

import com.ait.lienzo.client.core.types.NFastStringMap;
import com.ait.lienzo.shared.core.types.EnumWithValue;

public enum ChartAlign implements EnumWithValue
{
    TOP("top"), BOTTOM("bottom"), LEFT("left"), RIGHT("right"), CENTER("center");

    private final String                            m_value;

    private static final NFastStringMap<ChartAlign> LOOKUP_MAP = Statics.build(ChartAlign.values());

    private ChartAlign(String value)
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

    public static final ChartAlign lookup(String key)
    {
        return Statics.lookup(key, LOOKUP_MAP, TOP);
    }

    public static final List<String> getKeys()
    {
        return Statics.getKeys(ChartAlign.values());
    }

    public static final List<ChartAlign> getValues()
    {
        return Statics.getValues(ChartAlign.values());
    }
}
