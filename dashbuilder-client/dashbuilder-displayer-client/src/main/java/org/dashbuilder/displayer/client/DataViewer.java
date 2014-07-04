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
package org.dashbuilder.displayer.client;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.displayer.DataDisplayer;

/**
 * A DataViewer takes care of drawing a DataDisplayer instance.
 */
public interface DataViewer<T extends DataDisplayer> extends DataViewerListener, IsWidget {

    /**
     * The data displayer to draw.
     */
    void setDataDisplayer(T dataDisplayer);
    T getDataDisplayer();

    /**
     * The handler used to fetch and manipulate the data set.
     */
    void setDataSetHandler(DataSetHandler dataSetHandler);
    DataSetHandler getDataSetHandler();

    /**
     * Add a listener interested in receive events generated within this viewer component.
     */
    void addListener(DataViewerListener listener);

    /**
     * Draw the chart
     */
    void draw();

    /**
     * Same as draw but does not necessary implies to repaint everything again.
     * It's just a matter of update & display the latest data set changes.
     */
    void redraw();
}