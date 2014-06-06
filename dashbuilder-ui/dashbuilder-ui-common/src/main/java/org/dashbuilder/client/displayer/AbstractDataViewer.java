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
 * Base class for implementing custom viewers.
 * <p>Any derived class must implement:
 * <ul>
 *     <li>The draw() & redraw() methods.</li>
 *     <li>The capture of events coming from the DataViewerListener interface.</li>
 * </ul>
 */
public abstract class AbstractDataViewer<T extends DataDisplayer> extends Composite implements DataViewer<T> {

    protected DataSet dataSet;
    protected DataSetHandler dataSetHandler;
    protected T dataDisplayer;
    protected List<DataViewerListener> listenerList = new ArrayList<DataViewerListener>();

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

    public void addListener(DataViewerListener listener) {
        listenerList.add(listener);
    }

    // DATA SET HANDLING LOGIC

    /**
     * This method is only applicable for displayers based on grouped data sets.
     *
     * @param columnId The name of the pivot column.
     * @param intervalNames The name of the group intervals selected.
     */
    public void selectGroupIntervals(String columnId, List<String> intervalNames) {
/*
        DataSetGroup groupOp = dataSetHandler.getGroupOperation(columnId);
        if (groupOp != null && groupOp.getColumnGroup() != null) {
            DataSetGroup _groupSelect = groupOp.cloneInstance();
            _groupSelect.setSelectedIntervalNames(intervalNames);
            _groupSelect.getGroupFunctions().clear();

            // Also notify to those interested parties the intervals selection event.
            for (DataViewerListener listener : listenerList) {
                listener.onOperationEnabled(this, _groupSelect);
            }
        }
*/
    }
}