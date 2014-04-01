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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.displayer.XAxis;

/**
 * {"columnId:", "department", "displayName": "Department"}
 */
public class JsXAxis extends JavaScriptObject {

    // Overlay types always have protected, zero-arg constructors
    protected JsXAxis() {}

    public final native String getColumnId() /*-{
        return this.columnId;
    }-*/;

    public final native String getDisplayName() /*-{
        return this.displayName;
    }-*/;
}
