/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.renderer.client.table;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.client.Displayer;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.mvp.Command;

@Dependent
public class TableDisplayer extends AbstractDisplayer<TableDisplayer.View> {

    public interface View extends AbstractDisplayer.View<TableDisplayer> {

        String getGroupsTitle();

        String getColumnsTitle();

        void showTitle(String title);

        void createTable(int pageSize);

        void redrawTable();

        void setWidth(int width);

        void setSortEnabled(boolean enabled);

        void setTotalRows(int rows);

        void setPagerEnabled(boolean enabled);

        void addColumn(ColumnType columnType, String columnId, String columnName, int index, boolean selectEnabled, boolean sortEnabled);

        void clearFilterStatus();

        void addFilterValue(String value);

        void addFilterReset();

        void gotoFirstPage();

        int getLastOffset();
    }

    protected View view;
    protected int totalRows = 0;
    protected String lastOrderedColumn = null;
    protected SortOrder lastSortOrder = null;
    protected Command onCellSelectedCommand = new Command() {public void execute() {}};
    protected String selectedCellColumn = null;
    protected Integer selectedCellRow = null;

    public TableDisplayer() {
        this(new TableDisplayerView());
    }

    @Inject
    public TableDisplayer(View view) {
        this.view = view;
        this.view.init(this);
    }

    @Override
    public View getView() {
        return view;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public String getLastOrderedColumn() {
        return lastOrderedColumn;
    }

    public SortOrder getLastSortOrder() {
        return lastSortOrder;
    }

    public String getSelectedCellColumn() {
        return selectedCellColumn;
    }

    public Integer getSelectedCellRow() {
        return selectedCellRow;
    }

    public void setOnCellSelectedCommand(Command onCellSelectedCommand) {
        this.onCellSelectedCommand = onCellSelectedCommand;
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupAllowed(true)
                .setGroupRequired(false)
                .setMaxColumns(-1)
                .setMinColumns(1)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle(view.getGroupsTitle())
                .setColumnsTitle(view.getColumnsTitle());

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute( DisplayerAttributeDef.TYPE )
                .supportsAttribute( DisplayerAttributeDef.RENDERER )
                .supportsAttribute( DisplayerAttributeGroupDef.COLUMNS_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.FILTER_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.REFRESH_GROUP )
                .supportsAttribute( DisplayerAttributeGroupDef.GENERAL_GROUP)
                .supportsAttribute( DisplayerAttributeGroupDef.TABLE_GROUP );
    }

    @Override
    protected void beforeDataSetLookup() {
        // Get the sort settings
        if (lastOrderedColumn == null) {
            String defaultSortColumn = displayerSettings.getTableDefaultSortColumnId();
            if (!StringUtils.isBlank(defaultSortColumn)) {
                lastOrderedColumn = defaultSortColumn;
                lastSortOrder = displayerSettings.getTableDefaultSortOrder();
            }
        }
        // Apply the sort order specified (if any)
        if (lastOrderedColumn != null) {
            sortApply(lastOrderedColumn, lastSortOrder);
        }
        // Lookup only the target rows
        dataSetHandler.limitDataSetRows(view.getLastOffset(), displayerSettings.getTablePageSize());

    }

    @Override
    protected void afterDataSetLookup(DataSet dataSet) {
        totalRows = dataSet.getRowCountNonTrimmed();
    }

    @Override
    protected void createVisualization() {
        if (displayerSettings.isTitleVisible()) {
            view.showTitle(displayerSettings.getTitle());
        }

        List<DataColumn> dataColumns = dataSet.getColumns();
        int width = displayerSettings.getTableWidth();

        view.createTable(displayerSettings.getTablePageSize());
        view.setWidth(width == 0 ? dataColumns.size() * 100 + 40 : width);
        view.setSortEnabled(displayerSettings.isTableSortEnabled());
        view.setTotalRows(totalRows);
        view.setPagerEnabled(displayerSettings.getTablePageSize() < dataSet.getRowCountNonTrimmed());

        for ( int i = 0; i < dataColumns.size(); i++ ) {
            DataColumn dataColumn = dataColumns.get(i);
            ColumnSettings columnSettings = displayerSettings.getColumnSettings(dataColumn);
            String columnName = columnSettings.getColumnName();
            switch (dataColumn.getColumnType()) {

                case LABEL: {
                    // Only label columns cells are selectable
                    view.addColumn(dataColumn.getColumnType(), dataColumn.getId(), columnName, i, displayerSettings.isFilterEnabled(), true);
                    break;
                }
                default: {
                    view.addColumn(dataColumn.getColumnType(), dataColumn.getId(), columnName, i, false, true);
                    break;
                }
            }
        }
        view.gotoFirstPage();
    }

    @Override
    protected void updateVisualization() {
        view.setTotalRows(totalRows);
        view.setPagerEnabled(displayerSettings.getTablePageSize() < dataSet.getRowCountNonTrimmed());
        view.gotoFirstPage();
        view.redrawTable();
        updateFilterStatus();
    }

    protected void updateFilterStatus() {
        view.clearFilterStatus();
        Set<String> columnFilters = filterColumns();
        if (displayerSettings.isFilterEnabled() && !columnFilters.isEmpty()) {

            for (String columnId : columnFilters) {
                List<Interval> selectedValues = filterIntervals(columnId);
                DataColumn column = dataSet.getColumnById(columnId);
                for (Interval interval : selectedValues) {
                    String formattedValue = formatInterval(interval, column);
                    view.addFilterValue(formattedValue);
                }
            }
            view.addFilterReset();
        }
    }

    public void sortBy(String column, SortOrder order) {
        if (displayerSettings.isTableSortEnabled()) {
            lastOrderedColumn = column;
            lastSortOrder = order;
            super.redraw();
        }
    }

    public void selectCell(String columnId, int rowIndex) {
        if (displayerSettings.isFilterEnabled()) {
            selectedCellColumn = columnId;
            selectedCellRow = rowIndex;
            onCellSelectedCommand.execute();
            if (displayerSettings.isFilterSelfApplyEnabled()) {
                view.gotoFirstPage();
            }
            super.filterUpdate(columnId, rowIndex);
            updateFilterStatus();
        }
    }

    @Override
    public void filterReset(String columnId) {
        super.filterReset(columnId);
        if (selectedCellColumn != null && selectedCellColumn.equals(columnId)) {
            selectedCellColumn = null;
            selectedCellRow = null;
        }
    }

    @Override
    public void filterReset() {
        selectedCellColumn = null;
        selectedCellRow = null;
        view.clearFilterStatus();
        super.filterReset();
    }

    public void lookupCurrentPage(final Callback<Integer> callback) {
        try {
            beforeDataSetLookup();
            dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                public void callback(DataSet ds) {
                    try {
                        dataSet = ds;
                        afterDataSetLookup(dataSet);
                        callback.callback(dataSet.getRowCount());
                    }
                    catch (Exception e) {
                        showError(new ClientRuntimeError(e));
                    }
                }
                public void notFound() {
                    view.errorDataSetNotFound(displayerSettings.getDataSetLookup().getDataSetUUID());
                }
                public boolean onError(ClientRuntimeError error) {
                    showError(error);
                    return false;
                }
            });
        } catch (Exception e) {
            showError(new ClientRuntimeError(e));
        }
    }

    // Reset the current navigation status on filter requests from external displayers

    @Override
    public void onFilterEnabled(Displayer displayer, DataSetGroup groupOp) {
        view.gotoFirstPage();
        super.onFilterEnabled(displayer, groupOp);
    }

    @Override
    public void onFilterEnabled(Displayer displayer, DataSetFilter filter) {
        view.gotoFirstPage();
        super.onFilterEnabled(displayer, filter);
    }

    @Override
    public void onFilterReset(Displayer displayer, List<DataSetGroup> groupOps) {
        view.gotoFirstPage();
        super.onFilterReset(displayer, groupOps);
    }

    @Override
    public void onFilterReset(Displayer displayer, DataSetFilter filter) {
        view.gotoFirstPage();
        super.onFilterReset(displayer, filter);
    }
}
