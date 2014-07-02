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
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;

/**
 *      {
 *          "id": "amount",
 *          "type": "label",
 *          "name": "Total expenses amount",
 *          "values": {"10300.45", "9000.00", "3022.44", "22223.56"}
 *      }
 */
public class JsDataColumn extends JavaScriptObject {

    // Overlay types always have protected, zero-arg constructors
    protected JsDataColumn() {}

    public final native String getId() /*-{
        return this.id;
    }-*/;

    public final native String getName() /*-{
        return this.name;
    }-*/;

    public final native String getType() /*-{
        return this.type;
    }-*/;

    public final native JsArrayString getJsStrings() /*-{
        return this.values;
    }-*/;

    public final native JsArrayNumber getJsNumbers() /*-{
        return this.values;
    }-*/;

}
