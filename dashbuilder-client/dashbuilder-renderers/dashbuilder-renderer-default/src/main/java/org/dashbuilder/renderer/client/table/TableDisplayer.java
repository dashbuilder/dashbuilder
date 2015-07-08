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
package org.dashbuilder.renderer.client.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.client.resources.i18n.DisplayerConstants;
import org.dashbuilder.renderer.client.resources.i18n.CommonConstants;
import org.dashbuilder.renderer.client.resources.i18n.TableConstants;

import org.uberfire.ext.widgets.common.client.tables.PagedTable;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

public class TableDisplayer extends AbstractDisplayer {

    protected Widget currentSelectionWidget = null;

    protected int numberOfRows = 0;
    protected String lastOrderedColumn = null;
    protected SortOrder lastSortOrder = null;

    protected PagedTable<Integer> table;
    protected TableProvider tableProvider = new TableProvider();

    @Override
    public DisplayerConstraints createDisplayerConstraints() {

        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupAllowed(true)
                .setGroupRequired(false)
                .setMaxColumns(-1)
                .setMinColumns(1)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle(TableConstants.INSTANCE.tableDisplayer_groupsTitle())
                .setColumnsTitle(TableConstants.INSTANCE.tableDisplayer_columnsTitle());

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
        dataSetHandler.limitDataSetRows(tableProvider.lastOffset, displayerSettings.getTablePageSize());

    }

    @Override
    protected void afterDataSetLookup(DataSet dataSet) {
        numberOfRows = dataSet.getRowCountNonTrimmed();
    }

    @Override
    protected Widget createVisualization() {
        table = createTable();
        tableProvider.addDataDisplay(table);
        updateVisualization();

        HTML titleHtml = new HTML();
        if ( displayerSettings.isTitleVisible() ) {
            titleHtml.setText( displayerSettings.getTitle() );
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(titleHtml);
        verticalPanel.add(table);
        return verticalPanel;
    }

    @Override
    protected void updateVisualization() {
        tableProvider.gotoFirstPage();
        table.setRowCount(numberOfRows, true);

        int height = 42 + 37 * (dataSet.getRowCount() == 0 ? 1 : dataSet.getRowCount());
        table.setHeight((height > (Window.getClientHeight() - this.getAbsoluteTop()) ? (Window.getClientHeight() - this.getAbsoluteTop()) : height) + "px");

        if (table.getPageSize() >= table.getRowCount()) table.pager.setVisible(false);
        else table.pager.setVisible(true);

        table.redraw();

        redrawColumnSelectionWidget();
    }


    protected PagedTable<Integer> createTable() {

        final PagedTable<Integer> pagedTable = new PagedTable<Integer>(displayerSettings.getTablePageSize());
        pagedTable.pageSizesSelector.setVisible(false);

        List<DataColumn> dataColumns = dataSet.getColumns();
        for ( int i = 0; i < dataColumns.size(); i++ ) {
            DataColumn dataColumn = dataColumns.get(i);
            ColumnSettings columnSettings = displayerSettings.getColumnSettings(dataColumn);
            String columnName = columnSettings.getColumnName();

            Column<Integer, ?> column = createColumn( dataColumn, i );
            if ( column != null ) {
                column.setSortable( true );
                pagedTable.addColumn( column, columnName );
            }
        }

        pagedTable.pager.setPageSize(displayerSettings.getTablePageSize());
        int tableWidth = displayerSettings.getTableWidth();
        pagedTable.setWidth( tableWidth == 0 ? dataColumns.size() * 100  + 40 + "px" : tableWidth + "px");
        pagedTable.setEmptyTableCaption( TableConstants.INSTANCE.tableDisplayer_noDataAvailable() );

        if (displayerSettings.isTableSortEnabled()) {
            pagedTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler( pagedTable ) {
                public void onColumnSort( ColumnSortEvent event ) {
                    lastOrderedColumn = ((DataColumnCell) event.getColumn().getCell()).columnId;
                    lastSortOrder = event.isSortAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING;
                    redraw();
                }
            });
        }
        return pagedTable;
    }

    protected Column<Integer, ?> createColumn( final DataColumn column, final int columnNumber ) {

        switch ( column.getColumnType() ) {
            case LABEL: return new Column<Integer, String>(
                            new DataColumnCell( column.getId(), true ) ) {
                                public String getValue( Integer row ) {
                                    Object value = dataSet.getValueAt(row, columnNumber);
                                    return formatValue(value, column);
                                }
                            };

            case NUMBER:
            case DATE:
            case TEXT: return new Column<Integer, String>(
                            new DataColumnCell( column.getId(), false ) ) {
                                public String getValue( Integer row ) {
                                    Object value = dataSet.getValueAt(row, columnNumber);
                                    return formatValue(value, column);
                                }
            };
        }
        return null;
    }

    protected Widget createCurrentSelectionWidget() {
        if (!displayerSettings.isFilterEnabled()) return null;

        Set<String> columnFilters = filterColumns();

        if ( columnFilters.isEmpty() ) return null;

        HorizontalPanel panel = new HorizontalPanel();
        panel.getElement().setAttribute("cellpadding", "2");

        for ( String columnId : columnFilters ) {
            List<Interval> selectedValues = filterIntervals(columnId);
            DataColumn column = dataSet.getColumnById(columnId);
            for (Interval interval : selectedValues) {
                String formattedValue = formatInterval(interval, column);
                panel.add(new org.gwtbootstrap3.client.ui.Label(formattedValue));
            }
        }

        Anchor anchor = new Anchor(TableConstants.INSTANCE.tableDisplayer_reset());
        panel.add(anchor);
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                filterReset();
                redrawColumnSelectionWidget();
            }
        });
        return panel;
    }

    protected void redrawColumnSelectionWidget() {
        if ( currentSelectionWidget != null ) table.getLeftToolbar().remove( currentSelectionWidget );
        currentSelectionWidget = createCurrentSelectionWidget();
        if ( currentSelectionWidget != null ) table.getLeftToolbar().add( currentSelectionWidget );
    }

    protected class DataColumnCell extends TextCell {

        private String columnId;
        private boolean selectable = false;

        DataColumnCell(String columnId, boolean selectable) {
            this.columnId = columnId;
            this.selectable = selectable;
        }

        @Override
        public Set<String> getConsumedEvents() {
            Set<String> consumedEvents = new HashSet<String>();
            consumedEvents.add( CLICK );
            return consumedEvents;
        }

        @Override
        public void onBrowserEvent( Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater ) {
            int rowIndexInPage = context.getIndex() - table.getPageStart();
            onCellSelected(columnId, selectable, rowIndexInPage);
        }
    }

    protected void onCellSelected(String columnId, boolean selectable, int rowIndex) {
        if ( !selectable || !displayerSettings.isFilterEnabled() ) return;

        tableProvider.lastOffset = 0;
        filterUpdate( columnId, rowIndex );
        redrawColumnSelectionWidget();
    }

    /**
     * Table data provider
     */
    protected class TableProvider extends AsyncDataProvider<Integer> {

        protected int lastOffset = 0;

        protected List<Integer> getCurrentPageRows(HasData<Integer> display) {
            final int start = ((PagedTable) display).getPageStart();
            int pageSize = ((PagedTable) display).getPageSize();
            int end = start + pageSize;
            if (end > numberOfRows) end = numberOfRows;

            final List<Integer> rows = new ArrayList<Integer>(end-start);
            for (int i = 0; i < end-start; i++) {
                rows.add(i);
            }
            return rows;
        }

        /**
         * Both filter & sort invoke this method from redraw()
         */
        public void gotoFirstPage() {
            // Avoid fetching the data set again
            lastOffset = 0;
            table.pager.setPage(0); // This calls internally to onRangeChanged() when the page changes

            int start = table.getPageStart();
            final List<Integer> rows = getCurrentPageRows(table);
            updateRowData(start, rows);
        }

        /**
         * Invoked from createWidget just after the data set has been fetched.
         */
        public void addDataDisplay(HasData<Integer> display) {
            // Avoid fetching the data set again
            lastOffset = 0;
            super.addDataDisplay(display); // This calls internally to onRangeChanged()
        }

        /**
         * This is invoked internally by the PagedTable on navigation actions.
         */
        protected void onRangeChanged(final HasData<Integer> display) {
            int start = ((PagedTable) display).getPageStart();
            final List<Integer> rows = getCurrentPageRows(display);

            if (lastOffset == start) {
                updateRowData(start, rows);
            }
            else {
                try {
                    lastOffset = start;
                    beforeDataSetLookup();
                    dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet ds) {
                            try {
                                dataSet = ds;
                                afterDataSetLookup(dataSet);
                                updateRowData(lastOffset, rows);
                                int height = 42 + 37 * (dataSet.getRowCount() == 0 ? 1 : dataSet.getRowCount());
                                table.setHeight(height + "px");
                            } catch (Exception e) {
                                displayMessage(CommonConstants.INSTANCE.error() + e.getMessage());
                                afterError(e);
                            }
                        }
                        public void notFound() {
                            displayMessage(CommonConstants.INSTANCE.error() + CommonConstants.INSTANCE.error_dataset_notfound());
                            afterError(new ClientRuntimeError(CommonConstants.INSTANCE.error_dataset_notfound()));
                        }
                        public boolean onError(ClientRuntimeError error) {
                            displayMessage(CommonConstants.INSTANCE.error() + error.getMessage());
                            afterError(error);
                            return false;
                        }
                    });
                } catch (Exception e) {
                    displayMessage(CommonConstants.INSTANCE.error() + e.getMessage());
                    afterError(e);
                }
            }
        }
    }
}
