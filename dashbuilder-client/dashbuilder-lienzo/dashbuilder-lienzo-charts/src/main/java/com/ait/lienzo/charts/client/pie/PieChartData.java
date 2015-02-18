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

package com.ait.lienzo.charts.client.pie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.IColor;
import com.google.gwt.core.client.JsArray;

public final class PieChartData implements Iterable<PieChartEntry>
{
    private final PieChartDataJSO m_jso;

    public PieChartData(PieChartDataJSO jso)
    {
        if (null != jso)
        {
            m_jso = jso;
        }
        else
        {
            m_jso = PieChartDataJSO.make();
        }
    }

    public PieChartData()
    {
        this(PieChartDataJSO.make());
    }

    public final PieChartData add(double value)
    {
        m_jso.push(PieChartEntry.make(null, value, Color.getRandomHexColor()));

        return this;
    }

    public final PieChartData add(double value, IColor color)
    {
        m_jso.push(PieChartEntry.make(null, value, color.getColorString()));

        return this;
    }

    public final PieChartData add(String label, double value)
    {
        m_jso.push(PieChartEntry.make(label, value, Color.getRandomHexColor()));

        return this;
    }

    public final PieChartData add(String label, double value, IColor color)
    {
        m_jso.push(PieChartEntry.make(label, value, color.getColorString()));

        return this;
    }

    public final int size()
    {
        return m_jso.length();
    }

    public final PieChartDataJSO getJSO()
    {
        return m_jso;
    }

    public final Collection<PieChartEntry> toCollection()
    {
        final int size = size();

        ArrayList<PieChartEntry> list = new ArrayList<PieChartEntry>(size);

        for (int i = 0; i < size; i++)
        {
            list.add(m_jso.get(i));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public final Iterator<PieChartEntry> iterator()
    {
        return toCollection().iterator();
    }

    public PieChartEntry[] getEntries()
    {
        final int size = size();

        PieChartEntry[] list = new PieChartEntry[size];

        for (int i = 0; i < size; i++)
        {
            list[i] = m_jso.get(i);
        }
        return list;
    }

    public static final class PieChartDataJSO extends JsArray<PieChartEntry>
    {
        protected PieChartDataJSO()
        {
        }

        public static final PieChartDataJSO make()
        {
            return JsArray.createArray().cast();
        }
    }
}
