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

import com.ait.lienzo.charts.client.model.DataTable;
import com.ait.lienzo.charts.client.model.DataTableColumn;
import com.google.gwt.core.client.JavaScriptObject;

/*
    - PieChartData
        - datatable
        - categories property -> only accepts string model property
        - values property -> only accepts numeric model property
        - colors
            - strategies
                - based on another model property -> property value match a color name
                - 1 color for each category prop value
                    - Set manually "value" -> color
                - Aleatory
                - color thresholds for values prop range
 */
public final class PieChartData
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

    public PieChartData(DataTable dataTable, String categoriesProperty, String valuesProperty)
    {
        this(PieChartDataJSO.make());
        this.m_jso.setDataTable(dataTable);
        DataTableColumn categoriesCol = getDataTable().getColumn(categoriesProperty);
        DataTableColumn valuesCol = getDataTable().getColumn(valuesProperty);
        if (categoriesCol == null || !categoriesCol.getType().equals(DataTableColumn.DataTableColumnType.STRING))
            throw new RuntimeException("PieChart only support STRING data types for categories property");
        if (valuesCol == null || !valuesCol.getType().equals(DataTableColumn.DataTableColumnType.NUMBER))
            throw new RuntimeException("PieChart only support NUMERIC data types for values property");
        this.m_jso.setCategoriesProperty(categoriesProperty);
        this.m_jso.setValuesProperty(valuesProperty);
    }

    public final PieChartDataJSO getJSO()
    {
        return m_jso;
    }

    public final DataTable getDataTable() {
        return this.m_jso.getDataTable();
    }

    public final String getCategoriesProperty() {
        return m_jso.getCategoriesProperty();
    }

    public final String getValuesProperty() {
        return m_jso.getValuesProperty();
    }

    public final int size()
    {
        return this.m_jso.getDataTable().size();
    }

    public static final class PieChartDataJSO extends JavaScriptObject
    {
        protected PieChartDataJSO()
        {
        }

        public static final PieChartDataJSO make()
        {
            return createObject().cast();
        }

        public final native void setCategoriesProperty(String property) /*-{
            this.categoriesProperty = property;
        }-*/;

        public final native String getCategoriesProperty() /*-{
            return this.categoriesProperty;
        }-*/;

        public final native String getValuesProperty() /*-{
            return this.valuesProperty;
        }-*/;

        public final native void setValuesProperty(String property) /*-{
            this.valuesProperty = property;
        }-*/;

        public final native void setDataTable(DataTable dataTable) /*-{
            this.dataTable = dataTable;
        }-*/;

        public final native DataTable getDataTable() /*-{
            return this.dataTable;
        }-*/;
    }
}
