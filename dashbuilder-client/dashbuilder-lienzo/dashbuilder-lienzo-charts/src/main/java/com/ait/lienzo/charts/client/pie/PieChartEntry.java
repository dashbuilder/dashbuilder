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

import com.google.gwt.core.client.JavaScriptObject;

public final class PieChartEntry extends JavaScriptObject
{
    public static final native PieChartEntry make(String label, double value, String color)
    /*-{
		return {
			label : label,
			value : value,
			color : color
		};
    }-*/;

    protected PieChartEntry()
    {
    }

    public final native double getValue()
    /*-{
		return this.value;
    }-*/;

    public final native String getLabel()
    /*-{
		return this.label;
    }-*/;

    public final native String getColor()
    /*-{
		return this.color;
    }-*/;
}