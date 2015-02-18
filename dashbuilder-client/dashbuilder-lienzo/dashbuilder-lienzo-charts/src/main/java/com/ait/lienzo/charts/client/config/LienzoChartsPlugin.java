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

package com.ait.lienzo.charts.client.config;

import java.util.ArrayList;
import java.util.Collection;

import com.ait.lienzo.charts.client.pie.PieChart;
import com.ait.lienzo.client.core.config.ILienzoPlugin;
import com.ait.lienzo.client.core.shape.json.IFactory;

public class LienzoChartsPlugin implements ILienzoPlugin
{
    private final ArrayList<IFactory<?>> m_factories = new ArrayList<IFactory<?>>();

    public LienzoChartsPlugin()
    {
    }

    @Override
    public String getNameSpace()
    {
        return "LienzoCharts";
    }

    @Override
    public Collection<IFactory<?>> getFactories()
    {
        if (m_factories.isEmpty())
        {
            m_factories.add(new PieChart.PieChartFactory());
        }
        return m_factories;
    }
}
