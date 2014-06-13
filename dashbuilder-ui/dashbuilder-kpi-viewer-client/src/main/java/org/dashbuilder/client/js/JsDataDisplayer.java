/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.js;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * {
 *     "title": "Expenses amount per department",
 *     "type": "piechart",
 *     "renderer": "google",
 *     "xAxis": {"columnId:", "department", "displayName": "Department"}
 *     "yAxes": [{"columnId:", "amount", "displayName": "Total amount"}]
 * }
 */
public class JsDataDisplayer extends JavaScriptObject {

    // Overlay types always have protected, zero-arg constructors
    protected JsDataDisplayer() {}

    public static native JsDataDisplayer fromJson(String jsonString) /*-{
        return eval('(' + jsonString + ')');
    }-*/;

    public final native String getTitle() /*-{
        return this.title;
    }-*/;

    public final native String getType() /*-{
        return this.type;
    }-*/;

    public final native String getRenderer() /*-{
        return this.renderer;
    }-*/;

    public final native JsXAxis getJsXAxis() /*-{
        return this.xAxis;
    }-*/;

    public final native JsArray<JsYAxis> getJsYAxes() /*-{
        return this.yAxes;
    }-*/;
}
