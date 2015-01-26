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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.ValidationError;
import org.dashbuilder.dataset.client.date.DateUtils;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;

/**
 * Base class for implementing custom displayers.
 * <p>Any derived class must implement:
 * <ul>
 *     <li>The draw() & redraw() methods.</li>
 *     <li>The capture of events coming from the DisplayerListener interface.</li>
 * </ul>
 */
public abstract class AbstractDisplayer extends Composite implements Displayer {

    protected DataSetHandler dataSetHandler;
    protected DisplayerSettings displayerSettings;
    protected DisplayerConstraints displayerConstraints;
    protected List<DisplayerListener> listenerList = new ArrayList<DisplayerListener>();
    protected Map<String,List<Interval>> columnSelectionMap = new HashMap<String,List<Interval>>();

    public abstract DisplayerConstraints createDisplayerConstraints();

    public DisplayerConstraints getDisplayerConstraints() {
        if (displayerConstraints == null) {
            displayerConstraints = createDisplayerConstraints();
        }
        return displayerConstraints;
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public void setDisplayerSettings(DisplayerSettings displayerSettings) {
        checkDisplayerSettings(displayerSettings);
        this.displayerSettings = displayerSettings;
    }

    public void checkDisplayerSettings(DisplayerSettings displayerSettings) {
        DisplayerConstraints constraints = getDisplayerConstraints();
        if (displayerConstraints != null) {
            ValidationError error = constraints.check(displayerSettings);
            if (error != null) throw error;
        }
    }

    public DataSetHandler getDataSetHandler() {
        return dataSetHandler;
    }

    public void setDataSetHandler(DataSetHandler dataSetHandler) {
        this.dataSetHandler = dataSetHandler;
    }

    public void addListener(DisplayerListener listener) {
        listenerList.add(listener);
    }

    public String getDisplayerId() {
        String id = displayerSettings.getUUID();
        if (!StringUtils.isBlank(id)) {
            return id;
        }

        id = displayerSettings.getTitle();
        if (!StringUtils.isBlank(id)) {
            int hash = id.hashCode();
            return Integer.toString(hash < 0 ? hash*-1 : hash);
        }
        return null;
    }

    // CAPTURE EVENTS RECEIVED FROM OTHER DISPLAYERS

    public void onGroupIntervalsSelected(Displayer displayer, DataSetGroup groupOp) {
        if (displayerSettings.isFilterListeningEnabled()) {
            dataSetHandler.filter(groupOp);
            redraw();
        }
    }

    public void onGroupIntervalsReset(Displayer displayer, List<DataSetGroup> groupOps) {
        if (displayerSettings.isFilterListeningEnabled()) {
            for (DataSetGroup groupOp : groupOps) {
                dataSetHandler.unfilter(groupOp);
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
     * Get the current filter intervals for the given data set column.
     *
     * @param columnId The column identifier.
     * @return A list of intervals.
     */
    protected List<Interval> filterIntervals(String columnId) {
        List<Interval> selected = columnSelectionMap.get(columnId);
        if (selected == null) return new ArrayList<Interval>();;
        return selected;
    }

    /**
     * Get the current filter selected interval indexes for the given data set column.
     *
     * @param columnId The column identifier.
     * @return A list of interval indexes
     */
    protected List<Integer> filterIndexes(String columnId) {
        List<Integer> result = new ArrayList<Integer>();
        List<Interval> selected = columnSelectionMap.get(columnId);
        if (selected == null) return result;

        for (Interval interval : selected) {
            result.add(interval.getIndex());
        }
        return result;
    }

    /**
     * Updates the current filter values for the given data set column.
     *
     * @param columnId The column to filter for.
     * @param row The row selected.
     */
    protected void filterUpdate(String columnId, int row) {
        filterUpdate(columnId, row, null);
    }

    /**
     * Updates the current filter values for the given data set column.
     *
     * @param columnId The column to filter for.
     * @param row The row selected.
     * @param maxSelections The number of different selectable values available.
     */
    protected void filterUpdate(String columnId, int row, Integer maxSelections) {
        if (!displayerSettings.isFilterEnabled()) return;

        Interval intervalSelected = dataSetHandler.getInterval(columnId, row);
        if (intervalSelected == null) return;

        List<Interval> selectedIntervals = columnSelectionMap.get(columnId);
        if (selectedIntervals == null) {
            selectedIntervals = new ArrayList<Interval>();
            selectedIntervals.add(intervalSelected);
            columnSelectionMap.put(columnId, selectedIntervals);
            filterApply(columnId, selectedIntervals);
        }
        else if (selectedIntervals.contains(intervalSelected)) {
            selectedIntervals.remove(intervalSelected);
            if (!selectedIntervals.isEmpty()) {
                filterApply(columnId, selectedIntervals);
            } else {
                filterReset(columnId);
            }
        } else {
            if (displayerSettings.isFilterSelfApplyEnabled()) {
                columnSelectionMap.put(columnId, selectedIntervals = new ArrayList<Interval>());
            }
            selectedIntervals.add(intervalSelected);
            if (maxSelections != null && maxSelections > 0 && selectedIntervals.size() >= maxSelections) {
                filterReset(columnId);
            } else {
                filterApply(columnId, selectedIntervals);
            }
        }
    }

    /**
     * Filter the values of the given column.
     *
     * @param columnId The name of the column to filter.
     * @param intervalList A list of interval selections to filter for.
     */
    protected void filterApply(String columnId, List<Interval> intervalList) {
        if (!displayerSettings.isFilterEnabled()) return;

        // For string column filters, create and notify a group interval selection operation.
        DataSetGroup groupOp = dataSetHandler.getGroupOperation(columnId);
        DataSetGroup _groupSelect = null;
        if (groupOp != null && groupOp.getColumnGroup() != null) {
            _groupSelect = groupOp.cloneInstance();
            _groupSelect.setSelectedIntervalList(intervalList);

        } else {
            _groupSelect = new DataSetGroup();
            _groupSelect.setDataSetUUID(displayerSettings.getDataSetLookup().getDataSetUUID());
            _groupSelect.setSelectedIntervalList(intervalList);
            _groupSelect.setColumnGroup(new ColumnGroup(columnId, columnId, GroupStrategy.DYNAMIC));
        }
        // Notify to those interested parties the selection event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DisplayerListener listener : listenerList) {
                listener.onGroupIntervalsSelected(this, _groupSelect);
            }
        }
        // Drill-down support
        if (displayerSettings.isFilterSelfApplyEnabled()) {
            dataSetHandler.drillDown(_groupSelect);
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
            groupOp.setColumnGroup(new ColumnGroup(columnId, columnId, GroupStrategy.DYNAMIC));
        }
        // Notify to those interested parties the reset event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DisplayerListener listener : listenerList) {
                listener.onGroupIntervalsReset(this, Arrays.asList(groupOp));
            }
        }
        // Apply the selection to this displayer
        if (displayerSettings.isFilterSelfApplyEnabled()) {
            dataSetHandler.drillUp(groupOp);
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
                groupOp.setColumnGroup(new ColumnGroup(columnId, columnId, GroupStrategy.DYNAMIC));
            }
            groupOpList.add(groupOp);

        }
        columnSelectionMap.clear();

        // Notify to those interested parties the reset event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DisplayerListener listener : listenerList) {
                listener.onGroupIntervalsReset(this, groupOpList);
            }
        }
        // Apply the selection to this displayer
        if (displayerSettings.isFilterSelfApplyEnabled()) {
            for (DataSetGroup groupOp : groupOpList) {
                dataSetHandler.drillUp(groupOp);
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
        dataSetHandler.sort(sortOp);
    }

    // DATA FORMATTING

    protected String formatInterval(Interval interval, DataColumn column) {

        // Raw values
        if (column == null || column.getColumnGroup() == null) {
            return interval.getName();
        }
        // Date interval
        String type = interval.getType();
        if (StringUtils.isBlank(type)) type = column.getIntervalType();
        DateIntervalType intervalType = DateIntervalType.getByName(type);
        if (intervalType != null) {
            return DateUtils.formatDate(intervalType,
                    column.getColumnGroup().getStrategy(),
                    interval.getName());
        }
        // Label interval
        return interval.getName();
    }

    protected String formatValue(Object value, DataColumn column) {

        // TODO: support for displayer settings column format
        // For example: .format("amount", "{value} profit in $", "---", "#,###.##")

        if (column == null) {
            return value.toString();
        }
        // Aggregations and raw values
        ColumnGroup cg = column.getColumnGroup();
        if (cg == null) {
            return formatValue(value, column.getColumnType());
        }
        // Date group
        DateIntervalType intervalType = DateIntervalType.getByName(column.getIntervalType());
        if (intervalType != null) {
            return DateUtils.formatDate(intervalType, cg.getStrategy(), value.toString());
        }
        // Label group
        return value.toString();
    }

    protected String formatValue(Object value, ColumnType columnType) {

        if (ColumnType.DATE.equals(columnType)) {
            if (value == null) return "---";
            Date d = (Date) value;
            return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(d);
        }
        if (ColumnType.NUMBER.equals(columnType)) {
            Double d = (value == null ? 0d : ((Number) value).doubleValue());
            return NumberFormat.getDecimalFormat().format(d);
        }

        return (value == null ? "---" : value.toString());
    }
}