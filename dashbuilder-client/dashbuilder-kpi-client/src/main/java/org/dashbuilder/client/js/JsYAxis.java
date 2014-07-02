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

/**
 * {"columnId:", "amount", "displayName": "Total expenses amount"}
 */
public class JsYAxis extends JavaScriptObject {

    // Overlay types always have protected, zero-arg constructors
    protected JsYAxis() {}

    public final native String getColumnId() /*-{
        return this.columnId;
    }-*/;

    public final native String getDisplayName() /*-{
        return this.displayName;
    }-*/;
}
