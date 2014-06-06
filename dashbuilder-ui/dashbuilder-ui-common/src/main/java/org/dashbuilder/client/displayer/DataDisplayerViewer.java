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
package org.dashbuilder.client.displayer;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.displayer.DataDisplayer;

/**
 * A DataDisplayer viewer takes care of drawing a DataDisplayer instance.
 */
public abstract class DataDisplayerViewer<T extends DataDisplayer> extends Composite implements DataDisplayerViewerListener {

    protected DataSet dataSet;
    protected DataSetHandler dataSetHandler;
    protected T dataDisplayer;
    protected List<DataDisplayerViewerListener> listenerList = new ArrayList<DataDisplayerViewerListener>();

    public T getDataDisplayer() {
        return dataDisplayer;
    }

    public void setDataDisplayer(T dataDisplayer) {
        this.dataDisplayer = dataDisplayer;
    }

    public DataSetHandler getDataSetHandler() {
        return dataSetHandler;
    }

    public void setDataSetHandler(DataSetHandler dataSetHandler) {
        this.dataSetHandler = dataSetHandler;
    }

    public void addListener(DataDisplayerViewerListener listener) {
        listenerList.add(listener);
    }

    /**
     * Draw the chart
     */
    public abstract void draw();

    /**
     * Same as draw but does not necessary implies to repaint everything again.
     * It's just a matter of update & display the latest data set changes.
     */
    public abstract void redraw();
}