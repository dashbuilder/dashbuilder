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
package org.dashbuilder.model.displayer.impl;

import org.dashbuilder.model.displayer.YAxis;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class YAxisImpl implements YAxis {

    protected String columnId;
    protected String displayName;

    public YAxisImpl() {
    }

    public YAxisImpl(String columnId, String displayName) {
        this.columnId = columnId;
        this.displayName = displayName;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
