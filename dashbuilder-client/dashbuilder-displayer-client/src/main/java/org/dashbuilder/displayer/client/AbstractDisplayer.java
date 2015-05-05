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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.ValidationError;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.resources.i18n.DayOfWeekConstants;
import org.dashbuilder.dataset.client.resources.i18n.MonthConstants;
import org.dashbuilder.dataset.date.DayOfWeek;
import org.dashbuilder.dataset.date.Month;
import org.dashbuilder.dataset.group.DateIntervalPattern;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.dataset.group.ColumnGroup;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;

/**
 * Base class for implementing custom displayers.
 * <p>Any derived class must implement:
 * <ul>
 *     <li>The draw(), redraw() & close() methods.</li>
 *     <li>The capture of events coming from the DisplayerListener interface.</li>
 * </ul>
 */
public abstract class AbstractDisplayer extends Composite implements Displayer {

    protected DataSetHandler dataSetHandler;
    protected DisplayerSettings displayerSettings;
    protected DisplayerConstraints displayerConstraints;
    protected List<DisplayerListener> listenerList = new ArrayList<DisplayerListener>();
    protected Map<String,List<Interval>> columnSelectionMap = new HashMap<String,List<Interval>>();
    protected boolean refreshEnabled = true;
    protected boolean drawn = false;
    protected Timer refreshTimer = null;

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

    public boolean isDrawn() {
        return drawn;
    }

    // REFRESH TIMER

    public void setRefreshOn(boolean enabled) {
        boolean changed = enabled != refreshEnabled;
        refreshEnabled = enabled;
        if (changed) {
            updateRefreshTimer();
        }
    }

    public boolean isRefreshOn() {
        return refreshTimer != null;
    }


    protected void updateRefreshTimer() {
        int seconds = displayerSettings.getRefreshInterval();
        if (refreshEnabled && seconds > 0) {
            if (refreshTimer == null) {
                refreshTimer = new Timer() {
                    public void run() {
                        if (isDrawn()) {
                            redraw();
                        }
                    }
                };
            }
            refreshTimer.schedule(seconds * 1000);
        }
        else if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }

    // LIFECYCLE CALLBACKS

    protected void afterDraw() {
        drawn = true;
        updateRefreshTimer();
        for (DisplayerListener listener : listenerList) {
            listener.onDraw(this);
        }
    }

    protected void afterRedraw() {
        updateRefreshTimer();
        for (DisplayerListener listener : listenerList) {
            listener.onRedraw(this);
        }
    }

    protected void afterClose() {
        setRefreshOn(false);

        for (DisplayerListener listener : listenerList) {
            listener.onClose(this);
        }
    }

    protected void afterError(final Displayer displayer, final DataSetClientServiceError error) {
        for (DisplayerListener listener : listenerList) {
            listener.onError(displayer, error);
        }
    }

    // CAPTURE EVENTS RECEIVED FROM OTHER DISPLAYERS

    public void onDraw(Displayer displayer) {
        // Do nothing
    }

    public void onRedraw(Displayer displayer) {
        // Do nothing
    }

    public void onClose(Displayer displayer) {
        // Do nothing
    }

    @Override
    public void onError(final Displayer displayer, final DataSetClientServiceError error) {
        afterError(displayer, error);
    }

    public void onFilterEnabled(Displayer displayer, DataSetGroup groupOp) {
        if (displayerSettings.isFilterListeningEnabled()) {
            dataSetHandler.filter(groupOp);
            redraw();
        }
    }

    public void onFilterReset(Displayer displayer, List<DataSetGroup> groupOps) {
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
        if (selected == null) return new ArrayList<Interval>();
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

        // For string column filters, init the group interval selection operation.
        DataSetGroup groupOp = dataSetHandler.getGroupOperation(columnId);
        groupOp.setSelectedIntervalList(intervalList);

        // Notify to those interested parties the selection event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DisplayerListener listener : listenerList) {
                listener.onFilterEnabled(this, groupOp);
            }
        }
        // Drill-down support
        if (displayerSettings.isFilterSelfApplyEnabled()) {
            dataSetHandler.drillDown(groupOp);
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

        // Notify to those interested parties the reset event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DisplayerListener listener : listenerList) {
                listener.onFilterReset(this, Arrays.asList(groupOp));
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
            groupOpList.add(groupOp);

        }
        columnSelectionMap.clear();

        // Notify to those interested parties the reset event.
        if (displayerSettings.isFilterNotificationEnabled()) {
            for (DisplayerListener listener : listenerList) {
                listener.onFilterReset(this, groupOpList);
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
        dataSetHandler.sort(columnId, sortOrder);
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
        if (StringUtils.isBlank(type)) type = column.getColumnGroup().getIntervalSize();
        DateIntervalType intervalType = DateIntervalType.getByName(type);
        if (intervalType != null) {
            ColumnSettings columnSettings = displayerSettings.getColumnSettings(column.getId());
            String pattern = columnSettings != null ? columnSettings.getValuePattern() : ColumnSettings.getDatePattern(intervalType);
            String expression = columnSettings != null ? columnSettings.getValueExpression() : null;

            if (pattern == null) {
                pattern = ColumnSettings.getDatePattern(intervalType);
            }
            if (expression == null && column.getColumnGroup().getStrategy().equals(GroupStrategy.FIXED)) {
                expression = ColumnSettings.getFixedExpression(intervalType);
            }

            return formatDate(intervalType,
                    column.getColumnGroup().getStrategy(),
                    interval.getName(), pattern, expression);
        }
        // Label interval
        return interval.getName();
    }

    public String formatValue(Object value, DataColumn column) {

        ColumnSettings columnSettings = displayerSettings.getColumnSettings(column);
        String pattern = columnSettings.getValuePattern();
        String empty = columnSettings.getEmptyTemplate();
        String expression = columnSettings.getValueExpression();

        if (value == null) {
            return empty;
        }

        // Date grouped columns
        DateIntervalType intervalType = DateIntervalType.getByName(column.getIntervalType());
        if (intervalType != null) {
            ColumnGroup columnGroup = column.getColumnGroup();
            return formatDate(intervalType,
                    columnGroup.getStrategy(),
                    value.toString(), pattern, expression);
        }
        // Label grouped columns, aggregations & raw values
        else {
            ColumnType columnType = column.getColumnType();
            if (ColumnType.DATE.equals(columnType)) {
                Date d = (Date) value;
                return getDateFormat(pattern).format(d);
            }
            else if (ColumnType.NUMBER.equals(columnType)) {
                double d = ((Number) value).doubleValue();
                if (!StringUtils.isBlank(expression)) d = Double.parseDouble(applyExpression(value.toString(), expression));
                return getNumberFormat(pattern).format(d);
            }
            else {
                if (StringUtils.isBlank(expression)) return value.toString();
                return applyExpression(value.toString(), expression);
            }
        }
    }


    public static final String[] _jsMalicious = {"document.", "window.", "alert(", "eval(", ".innerHTML"};

    protected String applyExpression(String val, String expr) {
        if (StringUtils.isBlank(expr)) {
            return val;
        }
        for (String keyword : _jsMalicious) {
            if (expr.contains(keyword)) {
                throw new RuntimeException(CommonConstants.INSTANCE.displayer_keyword_not_allowed(expr));
            }
        }
        try {
            return evalExpression(val, expr);
        } catch (Exception e) {
            throw new RuntimeException(CommonConstants.INSTANCE.displayer_expr_invalid_syntax(expr));
        }
    }

    protected native String evalExpression(String val, String expr) /*-{
        value = val;
        return eval(expr) + '';
    }-*/;

    // NUMBER FORMATTING

    private static Map<String,NumberFormat> numberPatternMap = new HashMap<String, NumberFormat>();

    protected NumberFormat getNumberFormat(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return getNumberFormat(ColumnSettings.NUMBER_PATTERN);
        }
        NumberFormat format = numberPatternMap.get(pattern);
        if (format == null) {
            numberPatternMap.put(pattern, format = NumberFormat.getFormat(pattern));
        }
        return format;
    }

    // DATE FORMATTING

    protected String formatDate(DateIntervalType type, GroupStrategy strategy, String date, String pattern, String expression) {
        if (date == null) return null;

        String str = GroupStrategy.FIXED.equals(strategy) ? formatDateFixed(type, date) : formatDateDynamic(type, date, pattern);
        if (StringUtils.isBlank(expression)) return str;
        return applyExpression(str, expression);
    }

    protected String formatDateFixed(DateIntervalType type, String date) {
        if (date == null) return null;

        int index = Integer.parseInt(date);
        if (DateIntervalType.DAY_OF_WEEK.equals(type)) {
            DayOfWeek dayOfWeek = DayOfWeek.getByIndex(index);
            return DayOfWeekConstants.INSTANCE.getString(dayOfWeek.name());
        }
        if (DateIntervalType.MONTH.equals(type)) {
            Month month = Month.getByIndex(index);
            return MonthConstants.INSTANCE.getString(month.name());
        }
        return date;
    }

    protected String formatDateDynamic(DateIntervalType type, String date, String pattern) {
        if (date == null) return null;
        Date d = parseDynamicGroupDate(type, date);
        DateTimeFormat format = getDateFormat(pattern);

        /*if (DateIntervalType.QUARTER.equals(type)) {
            String result = format.format(d);
            int endMonth = d.getMonth() + 2;
            d.setMonth(endMonth % 12);
            if (endMonth > 11) d.setYear(d.getYear() + 1);
            return result + " - " + format.format(d);
        }*/
        return format.format(d);
    }

    protected Date parseDynamicGroupDate(DateIntervalType type, String date) {
        String pattern = DateIntervalPattern.getPattern(type);
        DateTimeFormat format = getDateFormat(pattern);
        return format.parse(date);
    }

    private static Map<String,DateTimeFormat> datePatternMap = new HashMap<String, DateTimeFormat>();

    protected DateTimeFormat getDateFormat(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return getDateFormat(ColumnSettings.DATE_PATTERN);
        }
        DateTimeFormat format = datePatternMap.get(pattern);
        if (format == null) {
            datePatternMap.put(pattern, format = DateTimeFormat.getFormat(pattern));
        }
        return format;
    }
}