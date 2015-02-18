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

import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.google.gwt.core.client.JavaScriptObject;

public final class XYChartSerie
{
    private XYChartSerieJSO m_jso;

    public XYChartSerie(String name, IColor color, String valuesAxisProperty) {
        this(XYChartSerieJSO.make(name, color.getColorString(), valuesAxisProperty));
    }

    public XYChartSerie(XYChartSerieJSO m_jso) {
        this.m_jso = m_jso;
    }

    public XYChartSerieJSO getJSO() {
        return m_jso;
    }

    public String getName() {
        return m_jso.getName();
    }

    public IColor getColor() {
        String colorStr = m_jso.getColor();
        return ColorName.lookup(colorStr);
    }

    public String getValuesAxisProperty() {
        return m_jso.getValuesAxisProperty();
    }
    
    public static final class XYChartSerieJSO extends JavaScriptObject {
        protected XYChartSerieJSO() {
        }

        public static final native XYChartSerieJSO make(String name, String color, String valuesAxisProperty)
        /*-{
            return {
                name : name,
                color : color,
                valuesAxisProperty: valuesAxisProperty
            };
        }-*/;
    
        public final native String getName()
        /*-{
            return this.name;
        }-*/;
    
        public final native String getColor()
        /*-{
            return this.color;
        }-*/;

        public final native String getValuesAxisProperty()
        /*-{
            return this.valuesAxisProperty;
        }-*/;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof XYChartSerie && getName().equals(((XYChartSerie) obj).getName());

    }
}