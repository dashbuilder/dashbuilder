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
package org.dashbuilder.client.google;

import java.util.List;

import com.google.gwt.visualization.client.AbstractDataTable;
import org.dashbuilder.model.displayer.DataDisplayerColumn;

public abstract class GoogleXAxisChartViewer extends GoogleChartViewer {

    public AbstractDataTable createTable() {
        List<DataDisplayerColumn> displayerColumns = dataDisplayer.getColumnList();
        if (displayerColumns.size() == 1) {
            throw new IllegalArgumentException("XAxis charts require to specify at least 2 columns. The X axis plus one ore more columns for the Y axis.");
        }
        return super.createTable();
    }
}
