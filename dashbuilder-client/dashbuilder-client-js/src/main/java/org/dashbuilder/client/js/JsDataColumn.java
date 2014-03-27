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

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import org.dashbuilder.client.dataset.ColumnType;
import org.dashbuilder.client.dataset.DataColumn;

/**
 *      {
 *          "id": "amount",
 *          "type": "label",
 *          "name": "Total expenses amount",
 *          "values": {"10300.45", "9000.00", "3022.44", "22223.56"}
 *      }
 */
public class JsDataColumn extends JavaScriptObject implements DataColumn {

    // Overlay types always have protected, zero-arg constructors
    protected JsDataColumn() {}

    public final native String getId() /*-{
        return this.id;
    }-*/;

    public final native String getName() /*-{
        return this.name;
    }-*/;

    private final native String getType() /*-{
        return this.type;
    }-*/;

    private final native JsArray getJSValues() /*-{
        return this.columns;
    }-*/;

    public final List getValues() {
        List  results = new ArrayList();
        JsArray array = getJSValues();
        for (int i = 0; i < array.length(); i++) {
            results.add(array.get(i));
        }
        return results;
    }

    public final ColumnType getColumnType() {
        return ColumnType.valueOf(getType());
    }
}
