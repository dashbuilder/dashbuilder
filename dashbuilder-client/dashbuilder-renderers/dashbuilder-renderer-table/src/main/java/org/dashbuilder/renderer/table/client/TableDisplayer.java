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
package org.dashbuilder.renderer.table.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
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
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.displayer.DisplayerSettingsColumn;
import org.dashbuilder.displayer.TableDisplayerSettings;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.renderer.table.client.resources.i18n.TableConstants;
import org.kie.uberfire.client.tables.PagedTable;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

public class TableDisplayer extends AbstractDisplayer<TableDisplayerSettings> {

    private Map< String, String > columnCaptionIds = new HashMap< String, String >(5);

    private Widget currentSelectionWidget = null;

    protected int numberOfRows = 0;
    protected String lastOrderedColumn = null;
    protected SortOrder lastSortOrder = null;

    protected boolean drawn = false;

    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    protected DataSet dataSet;

    protected PagedTable<Integer> table;
    protected TableProvider tableProvider = new TableProvider();

    public TableDisplayer() {
        initWidget( panel );
    }

    public void draw() {
        if ( !drawn ) {
            drawn = true;

            if ( displayerSettings == null ) {
                displayMessage( "ERROR: DisplayerSettings property not set" );
            } else if ( dataSetHandler == null ) {
                displayMessage( "ERROR: DataSetHandler property not set" );
            } else {
                try {
                    displayMessage( TableConstants.INSTANCE.tableDisplayer_initializing() + " '" + displayerSettings.getTitle() + "'..." );
                    lookupDataSet(0, new DataSetReadyCallback() {

                        public void callback( DataSet dataSet ) {
                            Widget w = createWidget();
                            panel.clear();
                            panel.add( w );
                        }
                        public void notFound() {
                            displayMessage( "ERROR: Data set not found." );
                        }
                    });
                } catch ( Exception e ) {
                    displayMessage( "ERROR: " + e.getMessage() );
                }
            }
        }
    }

    /**
     * Just reload the data set and make the current Displayer redraw.
     */
    public void redraw() {
        lookupDataSet(0, new DataSetReadyCallback() {

            public void callback( DataSet dataSet ) {
                tableProvider.gotoFirstPage();
                table.setRowCount(numberOfRows, true);
                table.redraw();
                redrawColumnSelectionWidget();
            }
            public void notFound() {
                displayMessage( "ERROR: Data set not found." );
            }
        } );
    }

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage( String msg ) {
        panel.clear();
        panel.add( label );
        label.setText( msg );
    }

    /**
     * Lookup the data
     */
    public void lookupDataSet(Integer offset, final DataSetReadyCallback callback) {
        try {

            // Get the sort settings
            if (lastOrderedColumn == null) {
                String defaultSortColumn = displayerSettings.getDefaultSortColumnId();
                if (defaultSortColumn != null && !"".equals( defaultSortColumn)) {
                    lastOrderedColumn = defaultSortColumn;
                    lastSortOrder = displayerSettings.getDefaultSortOrder();
                }
            }
            // Apply the sort order specified (if any)
            if (lastOrderedColumn != null) {
                sortApply(lastOrderedColumn, lastSortOrder);
            }
            // Lookup only the target rows
            dataSetHandler.limitDataSetRows(offset, displayerSettings.getPageSize());

            // Do the lookup
            dataSetHandler.lookupDataSet(
                    new DataSetReadyCallback() {

                        public void callback( DataSet dataSet ) {
                            TableDisplayer.this.dataSet = dataSet;
                            numberOfRows = dataSet.getRowCountNonTrimmed();
                            callback.callback( dataSet );
                        }
                        public void notFound() {
                            callback.notFound();
                        }
                    }
            );
        } catch ( Exception e ) {
            displayMessage("ERROR: " + e.getMessage());
        }
    }

    protected Widget createWidget() {
        table = createTable();

        tableProvider.addDataDisplay(table);

        HTML titleHtml = new HTML();
        if ( displayerSettings.isTitleVisible() ) {
            titleHtml.setText( displayerSettings.getTitle() );
        }

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add( titleHtml );
        verticalPanel.add(table);
        return verticalPanel;
    }

    protected PagedTable<Integer> createTable() {

        final PagedTable<Integer> pagedTable = new PagedTable<Integer>(displayerSettings.getPageSize());
        List<DisplayerSettingsColumn> displayerSettingsColumns = displayerSettings.getColumnList();

        int nColumns = 0;
        if ( !displayerSettingsColumns.isEmpty() ) {
            createTableColumnsFromDisplayerSettings( pagedTable, displayerSettingsColumns );
            nColumns = displayerSettingsColumns.size();
        } else {
            createTableColumnsFromDataSet( pagedTable, dataSet.getColumns() );
            nColumns = dataSet.getColumns().size();
        }

        pagedTable.setRowCount( numberOfRows, true );
        int height = 40 * displayerSettings.getPageSize() + 20;
        pagedTable.setHeight( ( height > ( Window.getClientHeight() - this.getAbsoluteTop() ) ? ( Window.getClientHeight() - this.getAbsoluteTop() ) : height ) + "px" );

        int tableWidth = displayerSettings.getTableWidth();
        pagedTable.setWidth( tableWidth == 0 ? nColumns * 100 + "px" : tableWidth + "px");

        pagedTable.setEmptyTableCaption( TableConstants.INSTANCE.tableDisplayer_noDataAvailable() );

        pagedTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler( pagedTable ) {

            public void onColumnSort( ColumnSortEvent event ) {
                // Get the column Id, in case the table is being drawn from a displayer configuration, the identifier will
                // have to be recovered from the columnCaptionIds correspondence Map
                String sortEventColumnName = event.getColumn().getDataStoreName();
                String _columnId = columnCaptionIds.get( sortEventColumnName );
                lastOrderedColumn = _columnId != null ? _columnId : sortEventColumnName;
                lastSortOrder = event.isSortAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING;

                redraw();
            }
        });
        return pagedTable;
    }

    private void createTableColumnsFromDataSet( PagedTable<Integer> table, List<DataColumn> dataColumns ) {
        for ( int i = 0; i < dataColumns.size(); i++ ) {
            DataColumn dataColumn = dataColumns.get( i );
            String columnId = dataColumn.getId();

            Column<Integer, ?> column = createColumn( dataColumn.getColumnType(), columnId, i );
            if ( column != null ) {
                column.setSortable( true );
                table.addColumn( column, columnId );
            }
        }
    }

    private void createTableColumnsFromDisplayerSettings( PagedTable<Integer> table, List<DisplayerSettingsColumn> displayerSettingsColumns ) {
        int columnIndex = 0;
        for ( int i = 0; i < displayerSettingsColumns.size(); i++ ) {
            DisplayerSettingsColumn displayerSettingsColumn = displayerSettingsColumns.get( i );
            DataColumn dataColumn;
            if (displayerSettingsColumn.getColumnId() != null) dataColumn = dataSet.getColumnById( displayerSettingsColumn.getColumnId() );
            else dataColumn = dataSet.getColumnByIndex( columnIndex++ );

            if (dataColumn == null) {
                String msg = "Displayer column not found in the data set: " + displayerSettingsColumn.getDisplayName();
                GWT.log( msg );
                throw new RuntimeException( msg );
            }

            String columnId = dataColumn.getId();
            String displayName = displayerSettingsColumn.getDisplayName();
            String caption = null;
            if ( displayName != null && !"".equals( displayName ) ) {
                caption = displayName;
                columnCaptionIds.put( displayName, columnId );
            } else {
                caption = columnId;
            }

            int colIndex = dataSet.getColumnIndex( dataColumn );
            Column<Integer, ?> column = createColumn( dataColumn.getColumnType(), columnId, colIndex );
            if ( column != null ) {
                column.setSortable( true );
                table.addColumn( column, caption );
            }
        }
    }

    private Column<Integer, ?> createColumn( ColumnType columnType, String columnId, final int columnNumber ) {

        switch ( columnType ) {
            case LABEL: return new Column<Integer, String>(
                            new SelectableTextCell( columnId ) ) {
                                public String getValue( Integer row ) {
                                    Object value = dataSet.getValueAt(row, columnNumber);
                                    return value.toString();
                                }
                            };

            case NUMBER: return new Column<Integer, Number>(
                            new NumberCell( NumberFormat.getFormat( "#.###" ) ) ) {
                                public Number getValue( Integer row ) {
                                    return (Number) dataSet.getValueAt(row, columnNumber);
                                }
                            };

            case DATE:return new Column<Integer, Date>(
                            new DateCell( DateTimeFormat.getFormat( DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM ) ) ) {
                                public Date getValue( Integer row ) {
                                    return (Date) dataSet.getValueAt(row, columnNumber);
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
            List<String> selectedValues = filterValues( columnId );
            for (String interval : selectedValues) {
                panel.add(new com.github.gwtbootstrap.client.ui.Label(interval));
            }
        }

        Anchor anchor = new Anchor("reset");
        panel.add(anchor);
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                filterReset();
                redrawColumnSelectionWidget();
            }
        });
        return panel;
    }

    private void redrawColumnSelectionWidget() {
        if ( currentSelectionWidget != null ) table.getLeftToolbar().remove( currentSelectionWidget );
        currentSelectionWidget = createCurrentSelectionWidget();
        if ( currentSelectionWidget != null ) table.getLeftToolbar().add( currentSelectionWidget );
    }

    private class SelectableTextCell extends TextCell {

        private String columnId;

        private SelectableTextCell( String columnId ) {
            this.columnId = columnId;
        }

        @Override
        public Set<String> getConsumedEvents() {
            Set<String> consumedEvents = new HashSet<String>();
            consumedEvents.add( CLICK );
            return consumedEvents;
        }

        @Override
        public void onBrowserEvent( Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater ) {

            if ( !displayerSettings.isFilterEnabled() ) return;

            filterUpdate( columnId, value );
            redrawColumnSelectionWidget();
        }
    }

    /**
     * Table data provider
     */
    protected class TableProvider extends AsyncDataProvider<Integer> {

        protected int lastOffset = -1;

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
                lastOffset = start;
                lookupDataSet(lastOffset,
                        new DataSetReadyCallback() {
                            public void callback(DataSet dataSet) {
                                updateRowData(lastOffset, rows);
                            }
                            public void notFound() {
                                displayMessage("ERROR: Data set not found.");
                            }
                        }
                );
            }
        }
    }
}
