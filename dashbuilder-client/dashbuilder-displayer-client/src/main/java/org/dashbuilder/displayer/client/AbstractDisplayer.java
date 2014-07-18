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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettings;

/**
 * Base class for implementing custom displayers.
 * <p>Any derived class must implement:
 * <ul>
 *     <li>The draw() & redraw() methods.</li>
 *     <li>The capture of events coming from the DataViewerListener interface.</li>
 * </ul>
 */
public abstract class AbstractDisplayer<T extends DisplayerSettings> extends Composite implements Displayer<T> {

    protected DataSetHandler dataSetHandler;
    protected T displayerSettings;
    protected List<DataViewerListener> listenerList = new ArrayList<DataViewerListener>();
    protected Map<String,List<String>> columnSelectionMap = new HashMap<String,List<String>>();

    public T getDisplayerSettings() {
        return displayerSettings;
    }

    public void setDisplayerSettings(T displayerSettings) {
        this.displayerSettings = displayerSettings;
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

    // CAPTURE EVENTS RECEIVED FROM OTHER VIEWERS

    public void onGroupIntervalsSelected(Displayer displayer, DataSetGroup groupOp) {
        if (displayerSettings.isFilterListeningEnabled()) {
            dataSetHandler.addGroupOperation(groupOp);
            redraw();
        }
    }

    public void onGroupIntervalsReset(Displayer displayer, List<DataSetGroup> groupOps) {
        if (displayerSettings.isFilterListeningEnabled()) {
            for (DataSetGroup groupOp : groupOps) {
                dataSetHandler.removeGroupOperation(groupOp);
            }
            redraw();
        }
    }

    // DATA COLUMN VALUES SELECTION, FILTER & NOTIFICATION

    /**
     * Get the set of columns being filtered.
     */
    protected Set<String> filterColumns() {
        return columnSelectionMap.keySet();
    }

    /**
     * Get the current filter values for the given data set column.
     *
     * @param columnId The column identifier-
     * @return A list of distinct values currently selected.
     */
    protected List<String> filterValues(String columnId) {
        return columnSelectionMap.get(columnId);
    }

    /**
     * Updates the current filter values for the given data set column.
     *
     * @param columnId The column to filter for.
     * @param valueSelected The value to add/remove from the current filter.
     */
    protected void filterUpdate(String columnId, String valueSelected) {
        filterUpdate(columnId, valueSelected, null);
    }

    /**
     * Updates the current filter values for the given data set column.
     *
     * @param columnId The column to filter for.
     * @param valueSelected The value to add/remove from the current filter.
     * @param maxSelections The number of different selectable values available.
     */
    protected void filterUpdate(String columnId, String valueSelected, Integer maxSelections) {
        if (!displayerSettings.isFilterEnabled()) return;

        List<String> selectedValues = columnSelectionMap.get(columnId);
        if (selectedValues == null) {
            selectedValues = new ArrayList<String>();
            selectedValues.add(valueSelected);
            columnSelectionMap.put(columnId, selectedValues);
            filterApply(columnId, selectedValues);
        }
        else if (selectedValues.contains(valueSelected)) {
            selectedValues.remove(valueSelected);
            if (!selectedValues.isEmpty()) {
                filterApply(columnId, selectedValues);
            } else {
                filterReset(columnId);
            }
        } else {
            selectedValues.add(valueSelected);
            if (maxSelections != null && maxSelections > 0 && selectedValues.size() >= maxSelections) {
                filterReset(columnId);
            } else {
                filterApply(columnId, selectedValues);
            }
        }
    }

    /**
     * Filter the values of the given column.
     *
     * @param columnId The name of the column to filter.
     * @param values A list of values to filter for.
     */
    protected void filterApply(String columnId, List<String> values) {
        if (!displayerSettings.isFilterEnabled()) return;

        // For string column filters, create and notify a group interval selection operation.
        DataSetGroup groupOp = dataSetHandler.getGroupOperation(columnId);
        DataSetGroup _groupSelect = null;
        if (groupOp != null && groupOp.getColumnGroup() != null) {
            _groupSelect = groupOp.cloneInstance();
            _groupSelect.setSelectedIntervalNames(values);
            _groupSelect.getGroupFunctions().clear();

        } else {
            _groupSelect = new DataSetGroup();
            _groupSelect.setSelectedIntervalNames(values);
            _groupSelect.setColumnGroup(new ColumnGroup(columnId, columnId, GroupStrategy.DYNAMIC));
        }
        // Notify to those interested parties the selection event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DataViewerListener listener : listenerList) {
                listener.onGroupIntervalsSelected(this, _groupSelect);
            }
        }
        // Apply the selection to this displayer
        if (displayerSettings.isFilterSelfApplyEnabled()) {
            dataSetHandler.addGroupOperation(_groupSelect);
            redraw();
        }
    }

    /**
     * Clear any filter on the given column.
     *
     * @param columnId The name of the column to reset.
     */
    protected void filterReset(String columnId) {
        if (!displayerSettings.isFilterEnabled()) return;

        columnSelectionMap.remove(columnId);
        DataSetGroup groupOp = dataSetHandler.getGroupOperation(columnId);
        if (groupOp == null || groupOp.getColumnGroup() == null) {
            groupOp = new DataSetGroup();
            groupOp .setColumnGroup(new ColumnGroup(columnId, columnId, GroupStrategy.DYNAMIC));
        }
        // Notify to those interested parties the reset event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DataViewerListener listener : listenerList) {
                listener.onGroupIntervalsReset(this, Arrays.asList(groupOp));
            }
        }
        // Apply the selection to this displayer
        if (displayerSettings.isFilterSelfApplyEnabled()) {
            dataSetHandler.removeGroupOperation(groupOp);
            redraw();
        }
    }

    /**
     * Clear any filter.
     */
    protected void filterReset() {
        if (!displayerSettings.isFilterEnabled()) return;

        List<DataSetGroup> groupOpList = new ArrayList<DataSetGroup>();
        for (String columnId : columnSelectionMap.keySet()) {
            DataSetGroup groupOp = dataSetHandler.getGroupOperation(columnId);
            if (groupOp == null || groupOp.getColumnGroup() == null) {
                groupOp = new DataSetGroup();
                groupOp .setColumnGroup(new ColumnGroup(columnId, columnId, GroupStrategy.DYNAMIC));
            }
            groupOpList.add(groupOp);

        }
        columnSelectionMap.clear();

        // Notify to those interested parties the reset event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DataViewerListener listener : listenerList) {
                listener.onGroupIntervalsReset(this, groupOpList);
            }
        }
        // Apply the selection to this displayer
        if (displayerSettings.isFilterSelfApplyEnabled()) {
            for (DataSetGroup groupOp : groupOpList) {
                dataSetHandler.removeGroupOperation(groupOp);
            }
            redraw();
        }
    }

    // DATA COLUMN SORT

    /**
     * Set the sort order operation to apply to the data set.
     *
     * @param columnId The name of the column to sort.
     * @param sortOrder The sort order.
     */
    protected void sortApply(String columnId, SortOrder sortOrder) {
        DataSetSort sortOp = new DataSetSort();
        sortOp.addSortColumn(new ColumnSort(columnId, sortOrder));
        dataSetHandler.setSortOperation(sortOp);
    }
}