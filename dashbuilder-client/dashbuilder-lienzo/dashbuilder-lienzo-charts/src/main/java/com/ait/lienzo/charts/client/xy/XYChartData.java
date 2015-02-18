/*
   Copyright (c) 2014 Ahome' Innovation Technologies. All rights reserved.

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

package com.ait.lienzo.charts.client.xy;

import com.ait.lienzo.charts.client.model.DataTable;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public final class XYChartData implements Iterable<XYChartSerie>
{
    private final XYChartDataJSO m_jso;

    public XYChartData(XYChartDataJSO jso)
    {
        if (null != jso)
        {
            m_jso = jso;
        }
        else
        {
            m_jso = XYChartDataJSO.make();
        }
    }

    public XYChartData(DataTable dataTable)
    {
        this(XYChartDataJSO.make());
        this.m_jso.setDataTable(dataTable);
    }

    public final XYChartData setCategoryAxisProperty(String categoryAxisProperty) {
        if (categoryAxisProperty != null || categoryAxisProperty.trim().length() != 0) {
            if (getDataTable().getColumn(categoryAxisProperty) != null) m_jso.setCategoryAxisProperty(categoryAxisProperty);
            else throw new RuntimeException("The data model property [" + categoryAxisProperty + "] does not exist in the model.");
        }
        
        return this;
    }
    
    public final XYChartData addSerie(XYChartSerie serie)
    {   if (serie == null) return null;
        
        if (getSerie(serie.getName()) != null) throw new RuntimeException("A serie with name [" + serie.getName() + "] already exist."); 
        m_jso.push(serie.getJSO());
        return this;
    }

    public final XYChartData removeSerie(XYChartSerie serie)
    {
        int pos = getSeriePosition(serie.getName());
        if (pos > -1) m_jso.removeSerie(pos);

        return this;
    }

    public final DataTable getDataTable() {
        return this.m_jso.getDataTable();
    }

    public final String getCategoryAxisProperty() {
        return m_jso.getCategoryAxisProperty();
    }
    
    public final int size()
    {
        return m_jso.length();
    }

    public final XYChartDataJSO getJSO()
    {
        return m_jso;
    }

    public final Collection<XYChartSerie> toCollection()
    {
        final int size = size();

        ArrayList<XYChartSerie> list = new ArrayList<XYChartSerie>(size);

        for (int i = 0; i < size; i++)
        {
            list.add(new XYChartSerie(m_jso.get(i)));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public final Iterator<XYChartSerie> iterator()
    {
        return toCollection().iterator();
    }

    public XYChartSerie[] getSeries()
    {
        final int size = size();

        XYChartSerie[] list = new XYChartSerie[size];

        for (int i = 0; i < size; i++)
        {
            list[i] = new XYChartSerie(m_jso.get(i));
        }
        return list;
    }

    public XYChartSerie getSerie(String name)
    {
        final int size = size();


        for (int i = 0; i < size; i++)
        {
            XYChartSerie serie = new XYChartSerie(m_jso.get(i));
            if (serie.getName().equals(name)) return serie;
        }
        return null;
    }

    public int getSeriePosition(String name)
    {
        final int size = size();


        for (int i = 0; i < size; i++)
        {
            XYChartSerie serie = new XYChartSerie(m_jso.get(i));
            if (serie.getName().equals(name)) return i;
        }
        return -1;
    }

    public static final class XYChartDataJSO extends JsArray<XYChartSerie.XYChartSerieJSO>
    {
        protected XYChartDataJSO()
        {
        }

        public static final XYChartDataJSO make()
        {
            return JsArray.createArray().cast();
        }

        public final native void setCategoryAxisProperty(String property) /*-{
            this.categoryAxisProperty = property;
        }-*/;

        public final native String getCategoryAxisProperty() /*-{
            return this.categoryAxisProperty;
        }-*/;

        public final native void setDataTable(DataTable dataTable) /*-{
            this.dataTable = dataTable;
        }-*/;

        public final native DataTable getDataTable() /*-{
            return this.dataTable;
        }-*/;

        public final native void removeSerie(int pos) /*-{
            this.splice(pos, 1);
        }-*/;
    }
}
