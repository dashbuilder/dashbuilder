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

import java.util.Collection;

import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.sort.SortOrder;
import org.dashbuilder.model.displayer.DataDisplayer;

public abstract class DataDisplayerViewer extends Composite {

    protected DataSet dataSet;
    protected DataSetHandler dataSetHandler;
    protected DataDisplayer dataDisplayer;

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
        this.dataSetHandler = new DataSetStaticHandler(dataSet);
    }

    public DataDisplayer getDataDisplayer() {
        return dataDisplayer;
    }

    public void setDataDisplayer(DataDisplayer dataDisplayer) {
        this.dataDisplayer = dataDisplayer;
    }

    public DataSetHandler getDataSetHandler() {
        return dataSetHandler;
    }

    public void setDataSetHandler(DataSetHandler dataSetHandler) {
        this.dataSetHandler = dataSetHandler;
    }

    public void dataSetSort(String columnId, SortOrder order) {
        dataSetHandler.sortDataSet(columnId, order);
        redraw();
    }

    public void dataSetSelectIntervals(String columnId, Collection<String> intervalNames) {
        dataSetHandler.selectIntervals(columnId, intervalNames);
        redraw();
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